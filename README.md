```
@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";
    private static final String BASE_PACKAGE = "de.consorsbank.core.trauthsc";

    // className → set of exception codes thrown directly in methods
    private final Map<String, Set<String>> methodCodesCache = new HashMap<>();
    // callerClassName#method → set of calledClassName#method
    private final Map<String, Set<String>> callGraphCache = new HashMap<>();
    // className → set of injected dependency classNames (fields)
    private final Map<String, Set<String>> fieldDependencyCache = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            log.info("=== Starting Exception Code Analysis ===");

            buildFullCallGraph();

            log.info("Call Graph size: {}", callGraphCache.size());
            log.info("Method codes size: {}", methodCodesCache.size());
            log.info("Field dependencies size: {}", fieldDependencyCache.size());

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
                scanClass(bd.getBeanClassName());
            } catch (Exception e) {
                log.debug("Could not scan {}: {}", bd.getBeanClassName(), e.getMessage());
            }
        });
    }

    private void scanClass(String className) throws IOException {
        new ClassReader(className).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public FieldVisitor visitField(int access, String name,
                    String descriptor, String signature, Object value) {

                // collect injected field types — these are the dependencies
                // descriptor format: "Lde/consorsbank/core/trauthsc/service/SomeService;"
                if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                    String fieldType = descriptor
                        .substring(1, descriptor.length() - 1)
                        .replace('/', '.');

                    if (fieldType.startsWith(BASE_PACKAGE)) {
                        fieldDependencyCache
                            .computeIfAbsent(className, k -> new HashSet<>())
                            .add(fieldType);
                    }
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
                        // GETSTATIC on *ExceptionCode = exception code reference
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
                        // track calls to methods within our package
                        String ownerClass = owner.replace('/', '.');
                        if (ownerClass.startsWith(BASE_PACKAGE)) {
                            callGraphCache
                                .computeIfAbsent(methodKey, k -> new HashSet<>())
                                .add(ownerClass + "#" + name);
                        }
                    }
                };
            }
        }, 0);
    }

    // ── Step 2: build controller report ──────────────────────────

    private Map<String, Map<String, Set<String>>> buildControllerReport() {
        Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        scanner.findCandidateComponents(BASE_PACKAGE).forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());

                boolean isController =
                    clazz.isAnnotationPresent(RestController.class)
                    || Arrays.stream(clazz.getInterfaces())
                        .anyMatch(i -> i.isAnnotationPresent(RestController.class));

                if (!isController) return;

                log.info("Found controller: {}", clazz.getSimpleName());

                Map<String, Set<String>> methodReport = new LinkedHashMap<>();

                Set<Method> apiMethods = new HashSet<>();
                Arrays.stream(clazz.getDeclaredMethods()).forEach(apiMethods::add);
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

                        log.info("  {}() → {} codes found, {} methods visited",
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

    // ── core: recursive transitive traversal ──────────────────────

    private Set<String> collectCodesTransitively(
            String methodKey, Set<String> visited, int depth) {

        if (!visited.add(methodKey)) return Collections.emptySet();
        if (depth > 30) return Collections.emptySet(); // safety

        Set<String> codes = new HashSet<>();

        // resolve interface → all impls
        Set<String> resolvedKeys = resolveToImpl(methodKey);

        resolvedKeys.forEach(resolvedKey -> {
            visited.add(resolvedKey);

            // 1. direct exception codes in this method
            Set<String> directCodes = methodCodesCache.get(resolvedKey);
            if (directCodes != null) {
                codes.addAll(directCodes);
            }

            // 2. follow explicit method calls (callGraph)
            Set<String> calledMethods = callGraphCache.get(resolvedKey);
            if (calledMethods != null) {
                calledMethods.forEach(calledKey ->
                    codes.addAll(
                        collectCodesTransitively(calledKey, visited, depth + 1)
                    )
                );
            }

            // 3. ── KEY FIX: scan ALL methods of injected dependencies ──
            // e.g. ServiceImpl has field: EncryptionService encryptionService
            // → scan ALL methods of EncryptionServiceImpl too
            String implClassName = resolvedKey.substring(0, resolvedKey.indexOf('#'));
            String implMethodName = resolvedKey.substring(resolvedKey.indexOf('#') + 1);

            Set<String> injectedDeps = fieldDependencyCache.get(implClassName);
            if (injectedDeps != null) {
                injectedDeps.forEach(depInterfaceName -> {
                    // resolve dep interface → dep impl
                    String depMethodKey = depInterfaceName + "#" + implMethodName;
                    Set<String> depImplKeys = resolveToImpl(depMethodKey);

                    depImplKeys.forEach(depImplKey -> {
                        if (!visited.contains(depImplKey)) {
                            // scan this specific method on the dep
                            codes.addAll(
                                collectCodesTransitively(depImplKey, visited, depth + 1)
                            );

                            // also scan ALL methods of dep impl
                            // (dep might throw in helper methods called internally)
                            String depImplClass = depImplKey.substring(
                                0, depImplKey.indexOf('#'));
                            scanAllMethodsOfClass(depImplClass, visited, depth, codes);
                        }
                    });
                });
            }
        });

        return codes;
    }

    // scan every method of a class transitively
    private void scanAllMethodsOfClass(
            String className, Set<String> visited, int depth, Set<String> codes) {

        // find all method keys for this class in both caches
        Set<String> allMethodKeys = new HashSet<>();

        methodCodesCache.keySet().stream()
            .filter(k -> k.startsWith(className + "#"))
            .forEach(allMethodKeys::add);

        callGraphCache.keySet().stream()
            .filter(k -> k.startsWith(className + "#"))
            .forEach(allMethodKeys::add);

        allMethodKeys.forEach(mk -> {
            if (!visited.contains(mk)) {
                codes.addAll(collectCodesTransitively(mk, visited, depth + 1));
            }
        });
    }

    // ── resolve interface → impl ──────────────────────────────────

    private Set<String> resolveToImpl(String methodKey) {
        // already concrete — exists in cache
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
        .filter(k -> k.contains("#"))
        .filter(k -> k.substring(k.indexOf('#')).equals(methodName))
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

    // ── log ───────────────────────────────────────────────────────

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

    // ── write JSON ────────────────────────────────────────────────

    private void writeToFile(Map<String, Map<String, Set<String>>> report) {
        try {
            StringBuilder json = new StringBuilder("{\n");
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
                    json.append("      \"")
                        .append(mEntry.getKey()).append("\": [\n");

                    // sort codes for consistent output
                    List<String> sortedCodes = new ArrayList<>(mEntry.getValue());
                    Collections.sort(sortedCodes);

                    Iterator<String> codeIt = sortedCodes.iterator();
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

            // dependency map section
            json.append("  \"serviceDependencies\": {\n");

            Iterator<Map.Entry<String, Set<String>>> dIt =
                fieldDependencyCache.entrySet().iterator();

            // only show service/component impls
            Map<String, Set<String>> filteredDeps = fieldDependencyCache
                .entrySet().stream()
                .filter(e -> e.getKey().contains("ServiceImpl")
                    || e.getKey().contains("ComponentImpl")
                    || e.getKey().contains("ManagerImpl"))
                .collect(Collectors.toMap(
                    e -> e.getKey().substring(e.getKey().lastIndexOf('.') + 1),
                    e -> e.getValue().stream()
                        .map(d -> d.substring(d.lastIndexOf('.') + 1))
                        .collect(Collectors.toSet()),
                    (a, b) -> a,
                    LinkedHashMap::new
                ));

            Iterator<Map.Entry<String, Set<String>>> fdIt =
                filteredDeps.entrySet().iterator();

            while (fdIt.hasNext()) {
                Map.Entry<String, Set<String>> entry = fdIt.next();
                json.append("    \"").append(entry.getKey()).append("\": [\n");

                Iterator<String> depIt = entry.getValue().iterator();
                while (depIt.hasNext()) {
                    json.append("      \"").append(depIt.next()).append("\"");
                    if (depIt.hasNext()) json.append(",");
                    json.append("\n");
                }

                json.append("    ]");
                if (fdIt.hasNext()) json.append(",");
                json.append("\n");
            }

            json.append("  }\n}");

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
