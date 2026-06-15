```
@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";
    private static final String BASE_PACKAGE = "de.consorsbank.core.trauthsc";

    private final Map<String, Set<String>> methodCodesCache = new HashMap<>();
    private final Map<String, Set<String>> callGraphCache = new HashMap<>();
    // NEW: track which classes each class depends on
    private final Map<String, Set<String>> classDependencyCache = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            log.info("=== Starting Exception Code Analysis ===");

            buildFullCallGraph();

            log.info("Call Graph size: {}", callGraphCache.size());
            log.info("Method codes size: {}", methodCodesCache.size());
            log.info("Class dependencies size: {}", classDependencyCache.size());

            Map<String, Map<String, Set<String>>> report = buildControllerReport();
            log.info("Report size: {}", report.size());

            logReport(report);
            writeToFile(report);

            log.info("=== Exception Code Analysis Complete ===");

        } catch (Exception e) {
            log.error("ExceptionCodeAnalyzer failed", e);
        }
    }

    // ── Step 1: scan ALL classes ──────────────────────────────────

    private void buildFullCallGraph() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        Set<BeanDefinition> candidates = scanner
            .findCandidateComponents(BASE_PACKAGE);

        log.info("Found {} classes to scan", candidates.size());

        candidates.forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                scanClass(clazz);
            } catch (Exception e) {
                log.debug("Could not scan {}: {}", bd.getBeanClassName(), e.getMessage());
            }
        });
    }

    private void scanClass(Class<?> clazz) {
        try {
            String className = clazz.getName();

            new ClassReader(className).accept(new ClassVisitor(Opcodes.ASM9) {

                @Override
                public void visit(int version, int access, String name,
                        String signature, String superName, String[] interfaces) {
                    // track class-level dependencies (interfaces implemented)
                    if (interfaces != null) {
                        Arrays.stream(interfaces)
                            .map(i -> i.replace('/', '.'))
                            .filter(i -> i.startsWith(BASE_PACKAGE))
                            .forEach(i -> classDependencyCache
                                .computeIfAbsent(className, k -> new HashSet<>())
                                .add(i));
                    }
                }

                @Override
                public FieldVisitor visitField(int access, String name,
                        String descriptor, String signature, Object value) {
                    // track field-level dependencies (injected services)
                    String fieldType = descriptor
                        .replace('/', '.')
                        .replaceAll("^L|;$", "");
                    if (fieldType.startsWith(BASE_PACKAGE)) {
                        classDependencyCache
                            .computeIfAbsent(className, k -> new HashSet<>())
                            .add(fieldType);
                    }
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String methodName,
                        String descriptor, String signature, String[] exceptions) {

                    String methodKey = className + "#" + methodName;

                    return new MethodVisitor(Opcodes.ASM9) {

                        @Override
                        public void visitFieldInsn(int opcode, String owner,
                                String fieldName, String fieldDescriptor) {
                            // collect exception codes
                            if (opcode == Opcodes.GETSTATIC
                                    && owner.contains("ExceptionCode")) {
                                methodCodesCache
                                    .computeIfAbsent(methodKey, k -> new HashSet<>())
                                    .add(owner.substring(owner.lastIndexOf('/') + 1)
                                        + "." + fieldName);
                            }
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner,
                                String name, String descriptor,
                                boolean isInterface) {
                            // collect method calls within our package
                            if (owner.replace('/', '.')
                                    .startsWith(BASE_PACKAGE)) {
                                String calledKey = owner.replace('/', '.')
                                    + "#" + name;
                                callGraphCache
                                    .computeIfAbsent(methodKey, k -> new HashSet<>())
                                    .add(calledKey);
                            }
                        }
                    };
                }
            }, 0);

        } catch (IOException e) {
            log.debug("Could not read bytecode for {}: {}",
                clazz.getSimpleName(), e.getMessage());
        }
    }

    // ── Step 2: build report per controller method ────────────────

    private Map<String, Map<String, Set<String>>> buildControllerReport() {
        Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        Set<BeanDefinition> candidates = scanner
            .findCandidateComponents(BASE_PACKAGE);

        log.info("Controller scan found {} candidates", candidates.size());

        candidates.forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());

                boolean isController =
                    clazz.isAnnotationPresent(RestController.class)
                    || Arrays.stream(clazz.getInterfaces())
                        .anyMatch(i -> i.isAnnotationPresent(RestController.class));

                if (!isController) return;

                log.info("Found controller: {}", clazz.getSimpleName());

                Map<String, Set<String>> methodReport = new LinkedHashMap<>();

                // collect methods from class + interfaces
                Set<Method> apiMethods = new HashSet<>();
                Arrays.stream(clazz.getDeclaredMethods())
                    .forEach(apiMethods::add);
                Arrays.stream(clazz.getInterfaces())
                    .flatMap(i -> Arrays.stream(i.getDeclaredMethods()))
                    .forEach(apiMethods::add);

                apiMethods.stream()
                    .filter(this::isApiMethod)
                    .forEach(method -> {
                        String methodKey = clazz.getName() + "#" + method.getName();

                        Set<String> visited = new HashSet<>();
                        Set<String> allCodes = collectCodesTransitively(
                            methodKey, visited, 0
                        );

                        log.info("  {}() → {} codes, visited {} methods",
                            method.getName(), allCodes.size(), visited.size());

                        methodReport.put(method.getName(), allCodes);
                    });

                if (!methodReport.isEmpty()) {
                    report.put(clazz.getSimpleName(), methodReport);
                }

            } catch (Exception e) {
                log.debug("Skip {}: {}", bd.getBeanClassName(), e.getMessage());
            }
        });

        return report;
    }

    // ── recursive transitive traversal with depth tracking ────────

    private Set<String> collectCodesTransitively(
            String methodKey, Set<String> visited, int depth) {

        if (!visited.add(methodKey)) return Collections.emptySet();
        if (depth > 20) return Collections.emptySet(); // safety limit

        Set<String> codes = new HashSet<>();

        // resolve interface → impl
        Set<String> resolvedKeys = resolveToImpl(methodKey);

        resolvedKeys.forEach(resolvedKey -> {
            visited.add(resolvedKey);

            // direct codes
            Set<String> directCodes = methodCodesCache.get(resolvedKey);
            if (directCodes != null) codes.addAll(directCodes);

            // recurse into called methods
            Set<String> calledMethods = callGraphCache.get(resolvedKey);
            if (calledMethods != null) {
                calledMethods.forEach(calledKey ->
                    codes.addAll(
                        collectCodesTransitively(calledKey, visited, depth + 1)
                    )
                );
            }

            // ── NEW: also scan class dependencies ─────────────────
            // e.g. ServiceImpl depends on RepositoryImpl, HelperService
            String implClassName = resolvedKey.substring(0, resolvedKey.indexOf('#'));
            String implMethodName = resolvedKey.substring(resolvedKey.indexOf('#'));

            Set<String> classDeps = classDependencyCache.get(implClassName);
            if (classDeps != null) {
                classDeps.forEach(depClass -> {
                    String depMethodKey = depClass + implMethodName;
                    if (!visited.contains(depMethodKey)) {
                        codes.addAll(
                            collectCodesTransitively(depMethodKey, visited, depth + 1)
                        );
                    }
                });
            }
        });

        return codes;
    }

    // ── resolve interface key → impl key ─────────────────────────

    private Set<String> resolveToImpl(String methodKey) {
        if (methodCodesCache.containsKey(methodKey)
                || callGraphCache.containsKey(methodKey)) {
            return Set.of(methodKey);
        }

        String methodName = methodKey.substring(methodKey.indexOf('#'));
        String interfaceClassName = methodKey.substring(0, methodKey.indexOf('#'));

        Set<String> implKeys = new HashSet<>();

        Stream.concat(
            methodCodesCache.keySet().stream(),
            callGraphCache.keySet().stream()
        )
        .filter(k -> k.endsWith(methodName))
        .filter(k -> {
            String implClassName = k.substring(0, k.indexOf('#'));
            try {
                Class<?> iface = Class.forName(interfaceClassName);
                Class<?> impl = Class.forName(implClassName);
                return iface.isAssignableFrom(impl);
            } catch (ClassNotFoundException e) {
                return false;
            }
        })
        .forEach(implKeys::add);

        return implKeys.isEmpty() ? Set.of(methodKey) : implKeys;
    }

    // ── Step 3: log ───────────────────────────────────────────────

    private void logReport(Map<String, Map<String, Set<String>>> report) {
        report.forEach((controller, methods) -> {
            log.info("Controller: {}", controller);
            methods.forEach((method, codes) -> {
                if (codes.isEmpty()) {
                    log.info("  {}() → no exception codes detected", method);
                } else {
                    log.info("  {}() → {}", method, codes);
                }
            });
        });
    }

    // ── Step 4: write JSON with dependency tree ───────────────────

    private void writeToFile(Map<String, Map<String, Set<String>>> report) {
        try {
            StringBuilder json = new StringBuilder("{\n");

            // ── section 1: controller → method → codes ────────────
            json.append("  \"controllers\": {\n");
            Iterator<Map.Entry<String, Map<String, Set<String>>>> cIt =
                report.entrySet().iterator();

            while (cIt.hasNext()) {
                Map.Entry<String, Map<String, Set<String>>> cEntry = cIt.next();
                json.append("    \"").append(cEntry.getKey()).append("\": {\n");

                Iterator<Map.Entry<String, Set<String>>> mIt =
                    cEntry.getValue().entrySet().iterator();

                while (mIt.hasNext()) {
                    Map.Entry<String, Set<String>> mEntry = mIt.next();
                    json.append("      \"").append(mEntry.getKey()).append("\": [\n");

                    Iterator<String> codeIt = mEntry.getValue().iterator();
                    while (codeIt.hasNext()) {
                        json.append("        \"").append(codeIt.next()).append("\"");
                        if (codeIt.hasNext()) json.append(",");
                        json.append("\n");
                    }

                    json.append("      ]");
                    if (mIt.hasNext()) json.append(",");
                    json.append("\n");
                }

                json.append("    }");
                if (cIt.hasNext()) json.append(",");
                json.append("\n");
            }
            json.append("  },\n");

            // ── section 2: class dependency tree ──────────────────
            json.append("  \"dependencies\": {\n");
            Iterator<Map.Entry<String, Set<String>>> dIt =
                classDependencyCache.entrySet().iterator();

            while (dIt.hasNext()) {
                Map.Entry<String, Set<String>> dEntry = dIt.next();
                // only include service/component classes
                if (!dEntry.getKey().contains("Service")
                        && !dEntry.getKey().contains("Controller")
                        && !dEntry.getKey().contains("Component")) {
                    if (dIt.hasNext()) continue;
                    else break;
                }

                String simpleKey = dEntry.getKey()
                    .substring(dEntry.getKey().lastIndexOf('.') + 1);

                json.append("    \"").append(simpleKey).append("\": [\n");

                Iterator<String> depIt = dEntry.getValue().iterator();
                while (depIt.hasNext()) {
                    String dep = depIt.next();
                    String simpleDep = dep.substring(dep.lastIndexOf('.') + 1);
                    json.append("      \"").append(simpleDep).append("\"");
                    if (depIt.hasNext()) json.append(",");
                    json.append("\n");
                }

                json.append("    ]");
                if (dIt.hasNext()) json.append(",");
                json.append("\n");
            }
            json.append("  }\n");
            json.append("}");

            Path outputPath = Path.of(OUTPUT_FILE);
            Files.writeString(outputPath, json.toString());
            log.info("Report written to: {}", outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.warn("Could not write report: {}", e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────

    private boolean isApiMethod(Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
            || method.isAnnotationPresent(GetMapping.class)
            || method.isAnnotationPresent(PostMapping.class)
            || method.isAnnotationPresent(PatchMapping.class)
            || method.isAnnotationPresent(PutMapping.class)
            || method.isAnnotationPresent(DeleteMapping.class)
            || method.isAnnotationPresent(Operation.class);
    }
}
```
