```
private Map<String, Map<String, Set<String>>> buildControllerReport() {
    Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter((metadataReader, factory) -> true); // scan ALL

    Set<BeanDefinition> candidates = scanner
        .findCandidateComponents(BASE_PACKAGE);

    log.info("Controller scan found {} candidates", candidates.size());

    candidates.forEach(bd -> {
        try {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            // check if it's a controller — either directly or via interface
            boolean isController = clazz.isAnnotationPresent(RestController.class)
                || Arrays.stream(clazz.getInterfaces())
                    .anyMatch(i -> i.isAnnotationPresent(RestController.class));

            if (!isController) return;

            log.info("Found controller: {}", clazz.getSimpleName());

            Map<String, Set<String>> methodReport = new LinkedHashMap<>();

            // get methods from controller AND its interfaces
            Set<Method> apiMethods = new HashSet<>();
            apiMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            Arrays.stream(clazz.getInterfaces())
                .flatMap(i -> Arrays.stream(i.getDeclaredMethods()))
                .forEach(apiMethods::add);

            apiMethods.stream()
                .filter(this::isApiMethod)
                .forEach(method -> {
                    String methodKey = clazz.getName() + "#" + method.getName();
                    log.info("  Scanning method: {}", method.getName());

                    Set<String> visited = new HashSet<>();
                    Set<String> allCodes = collectCodesTransitively(
                        methodKey, visited
                    );

                    log.info("  {} → {} codes found", method.getName(), allCodes.size());
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
```


@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";
    private static final String BASE_PACKAGE = "de.consorsbank.core.trauthsc";

    // cache: className+methodName → exception codes found in bytecode
    private final Map<String, Set<String>> methodCodesCache = new HashMap<>();
    // cache: className+methodName → methods it calls (className+methodName)
    private final Map<String, Set<String>> callGraphCache = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            log.info("=== Starting Exception Code Analysis ===");

            // 1. scan ALL classes in base package — build full call graph + codes
            buildFullCallGraph();

            // 2. scan controllers and trace through call graph
            Map<String, Map<String, Set<String>>> report = buildControllerReport();

            // 3. log
            logReport(report);

            // 4. write to file
            writeToFile(report);

            log.info("=== Exception Code Analysis Complete ===");

        } catch (Exception e) {
            log.warn("ExceptionCodeAnalyzer failed: {}", e.getMessage());
        }
    }

    // ── Step 1: scan ALL classes, build call graph ────────────────

    private void buildFullCallGraph() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        // scan everything — services, components, helpers
        scanner.addIncludeFilter(new RegexPatternTypeFilter(
            Pattern.compile(".*")
        ));

        scanner.findCandidateComponents(BASE_PACKAGE).forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                scanClass(clazz);
            } catch (Exception e) {
                // skip unloadable classes
            }
        });
    }

    private void scanClass(Class<?> clazz) throws IOException {
        new ClassReader(clazz.getName()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName,
                    String descriptor, String signature, String[] exceptions) {

                String methodKey = clazz.getName() + "#" + methodName;

                return new MethodVisitor(Opcodes.ASM9) {

                    @Override
                    public void visitFieldInsn(int opcode, String owner,
                            String fieldName, String fieldDescriptor) {
                        // collect exception codes thrown in this method
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
                            String name, String descriptor, boolean isInterface) {
                        // collect calls to other methods in our package
                        if (owner.replace('/', '.')
                                .startsWith(BASE_PACKAGE)) {
                            String calledKey = owner.replace('/', '.') + "#" + name;
                            callGraphCache
                                .computeIfAbsent(methodKey, k -> new HashSet<>())
                                .add(calledKey);
                        }
                    }
                };
            }
        }, 0);
    }

    // ── Step 2: build report per controller method ────────────────

    private Map<String, Map<String, Set<String>>> buildControllerReport() {
        Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        scanner.findCandidateComponents(BASE_PACKAGE).forEach(bd -> {
            try {
                Class<?> controllerClass = Class.forName(bd.getBeanClassName());
                Map<String, Set<String>> methodReport = new LinkedHashMap<>();

                Arrays.stream(controllerClass.getDeclaredMethods())
                    .filter(this::isApiMethod)
                    .forEach(method -> {
                        String methodKey = controllerClass.getName()
                            + "#" + method.getName();

                        // recursively collect all codes reachable from this method
                        Set<String> visited = new HashSet<>();
                        Set<String> allCodes = collectCodesTransitively(
                            methodKey, visited
                        );

                        methodReport.put(method.getName(), allCodes);
                    });

                if (!methodReport.isEmpty()) {
                    report.put(controllerClass.getSimpleName(), methodReport);
                }

            } catch (Exception e) {
                log.warn("Skip controller {}: {}", bd.getBeanClassName(), e.getMessage());
            }
        });

        return report;
    }

    // ── recursive call graph traversal ────────────────────────────

    private Set<String> collectCodesTransitively(String methodKey, Set<String> visited) {
        // prevent infinite loops in circular calls
        if (!visited.add(methodKey)) return Collections.emptySet();

        Set<String> codes = new HashSet<>();

        // add codes directly thrown in this method
        Set<String> directCodes = methodCodesCache.get(methodKey);
        if (directCodes != null) codes.addAll(directCodes);

        // recurse into all called methods
        Set<String> calledMethods = callGraphCache.get(methodKey);
        if (calledMethods != null) {
            calledMethods.forEach(calledKey ->
                codes.addAll(collectCodesTransitively(calledKey, visited))
            );
        }

        return codes;
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

    // ── Step 4: write JSON ────────────────────────────────────────

    private void writeToFile(Map<String, Map<String, Set<String>>> report) {
        try {
            StringBuilder json = new StringBuilder("{\n");

            Iterator<Map.Entry<String, Map<String, Set<String>>>> cIt =
                report.entrySet().iterator();

            while (cIt.hasNext()) {
                Map.Entry<String, Map<String, Set<String>>> cEntry = cIt.next();
                json.append("  \"").append(cEntry.getKey()).append("\": {\n");

                Iterator<Map.Entry<String, Set<String>>> mIt =
                    cEntry.getValue().entrySet().iterator();

                while (mIt.hasNext()) {
                    Map.Entry<String, Set<String>> mEntry = mIt.next();
                    json.append("    \"").append(mEntry.getKey()).append("\": [\n");

                    Iterator<String> codeIt = mEntry.getValue().iterator();
                    while (codeIt.hasNext()) {
                        json.append("      \"").append(codeIt.next()).append("\"");
                        if (codeIt.hasNext()) json.append(",");
                        json.append("\n");
                    }

                    json.append("    ]");
                    if (mIt.hasNext()) json.append(",");
                    json.append("\n");
                }

                json.append("  }");
                if (cIt.hasNext()) json.append(",");
                json.append("\n");
            }

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
            || method.isAnnotationPresent(DeleteMapping.class);
    }
}


@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));

            Set<BeanDefinition> candidates = scanner
                .findCandidateComponents("de.consorsbank.core.trauthsc");

            // ── change: collect into map instead of just logging ──
            Map<String, Set<String>> report = new LinkedHashMap<>();

            candidates.forEach(bd -> {
                try {
                    Class<?> serviceClass = Class.forName(bd.getBeanClassName());
                    Set<String> codes = scanForExceptionCodes(serviceClass);

                    if (!codes.isEmpty()) {
                        log.info("[{}] → possible exception codes: {}",
                            serviceClass.getSimpleName(), codes);

                        // ── add to report ──
                        report.put(serviceClass.getSimpleName(), codes);
                    }
                } catch (Exception e) {
                    log.warn("Skip {}: {}", bd.getBeanClassName(), e.getMessage());
                }
            });

            // ── write to file after scanning all ──
            writeToFile(report);

        } catch (Exception e) {
            log.warn("ExceptionCodeAnalyzer failed: {}", e.getMessage());
        }
    }

    private void writeToFile(Map<String, Set<String>> report) {
        try {
            StringBuilder json = new StringBuilder("{\n");

            Iterator<Map.Entry<String, Set<String>>> it = report.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Set<String>> entry = it.next();
                json.append("  \"").append(entry.getKey()).append("\": [\n");

                Iterator<String> codeIt = entry.getValue().iterator();
                while (codeIt.hasNext()) {
                    json.append("    \"").append(codeIt.next()).append("\"");
                    if (codeIt.hasNext()) json.append(",");
                    json.append("\n");
                }

                json.append("  ]");
                if (it.hasNext()) json.append(",");
                json.append("\n");
            }

            json.append("}");

            Path outputPath = Path.of(OUTPUT_FILE);
            Files.writeString(outputPath, json.toString());
            log.info("Exception codes report written to: {}", 
                outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.warn("Could not write report: {}", e.getMessage());
        }
    }

    private Set<String> scanForExceptionCodes(Class<?> serviceClass) throws IOException {
        Set<String> foundCodes = new HashSet<>();

        new ClassReader(serviceClass.getName()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner,
                            String fieldName, String fieldDescriptor) {
                        if (opcode == Opcodes.GETSTATIC
                                && owner.contains("ExceptionCode")) {
                            foundCodes.add(
                                owner.substring(owner.lastIndexOf('/') + 1)
                                + "." + fieldName
                            );
                        }
                    }
                };
            }
        }, 0);

        return foundCodes;
    }
}



```
@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            // 1. scan all @Service classes for exception codes
            Map<String, Set<String>> serviceCodesMap = scanAllServices();

            // 2. scan all @RestController classes and map to service codes
            Map<String, Map<String, Set<String>>> report = buildReport(serviceCodesMap);

            // 3. log it
            logReport(report);

            // 4. write to file
            writeToFile(report);

        } catch (Exception e) {
            log.warn("ExceptionCodeAnalyzer failed: {}", e.getMessage());
        }
    }

    // ── Step 1: scan all services ─────────────────────────────────

    private Map<String, Set<String>> scanAllServices() {
        Map<String, Set<String>> result = new HashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));

        scanner.findCandidateComponents("de.consorsbank.core.trauthsc")
            .forEach(bd -> {
                try {
                    Class<?> serviceClass = Class.forName(bd.getBeanClassName());
                    Set<String> codes = scanForExceptionCodes(serviceClass);
                    if (!codes.isEmpty()) {
                        result.put(serviceClass.getSimpleName(), codes);
                    }
                } catch (Exception e) {
                    log.warn("Skip service {}: {}", bd.getBeanClassName(), e.getMessage());
                }
            });

        return result;
    }

    // ── Step 2: map controller methods to service codes ───────────

    private Map<String, Map<String, Set<String>>> buildReport(
            Map<String, Set<String>> serviceCodesMap) {

        // result: ControllerName → { methodName → [ExceptionCodes] }
        Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        scanner.findCandidateComponents("de.consorsbank.core.trauthsc")
            .forEach(bd -> {
                try {
                    Class<?> controllerClass = Class.forName(bd.getBeanClassName());
                    Map<String, Set<String>> methodCodes = new LinkedHashMap<>();

                    // get injected service names from controller fields
                    Set<String> injectedServiceNames = Arrays
                        .stream(controllerClass.getDeclaredFields())
                        .map(f -> f.getType().getSimpleName())
                        .collect(Collectors.toSet());

                    // collect all codes from injected services
                    Set<String> controllerCodes = serviceCodesMap.entrySet().stream()
                        .filter(e -> injectedServiceNames.stream()
                            .anyMatch(s -> e.getKey().contains(
                                s.replace("Service", "").replace("Impl", ""))))
                        .flatMap(e -> e.getValue().stream())
                        .collect(Collectors.toSet());

                    // map per controller method
                    Arrays.stream(controllerClass.getDeclaredMethods())
                        .filter(this::isApiMethod)
                        .forEach(method -> {
                            // try to match method name to service codes
                            Set<String> matched = controllerCodes.stream()
                                .filter(code -> isCodeRelevantToMethod(
                                    code, method.getName()))
                                .collect(Collectors.toSet());

                            // if no specific match — show all controller codes
                            methodCodes.put(
                                method.getName(),
                                matched.isEmpty() ? controllerCodes : matched
                            );
                        });

                    if (!methodCodes.isEmpty()) {
                        report.put(controllerClass.getSimpleName(), methodCodes);
                    }

                } catch (Exception e) {
                    log.warn("Skip controller {}: {}", bd.getBeanClassName(), e.getMessage());
                }
            });

        return report;
    }

    // ── Step 3: log ───────────────────────────────────────────────

    private void logReport(Map<String, Map<String, Set<String>>> report) {
        log.info("=== Exception Code Analysis Report ===");
        report.forEach((controller, methods) -> {
            log.info("Controller: {}", controller);
            methods.forEach((method, codes) ->
                log.info("  {}: {}", method, codes));
        });
        log.info("======================================");
    }

    // ── Step 4: write to file ─────────────────────────────────────

    private void writeToFile(Map<String, Map<String, Set<String>>> report) {
        try {
            // build JSON manually — no Jackson dependency needed
            StringBuilder json = new StringBuilder("{\n");

            report.forEach((controller, methods) -> {
                json.append("  \"").append(controller).append("\": {\n");
                methods.forEach((method, codes) -> {
                    json.append("    \"").append(method).append("\": [\n");
                    codes.forEach(code ->
                        json.append("      \"").append(code).append("\",\n"));
                    // remove last comma
                    if (!codes.isEmpty()) {
                        json.deleteCharAt(json.lastIndexOf(","));
                    }
                    json.append("    ],\n");
                });
                if (!methods.isEmpty()) {
                    json.deleteCharAt(json.lastIndexOf(","));
                }
                json.append("  },\n");
            });
            if (!report.isEmpty()) {
                json.deleteCharAt(json.lastIndexOf(","));
            }
            json.append("}");

            Path outputPath = Path.of(OUTPUT_FILE);
            Files.writeString(outputPath, json.toString());
            log.info("Report written to: {}", outputPath.toAbsolutePath());

        } catch (IOException e) {
            log.warn("Could not write report file: {}", e.getMessage());
        }
    }

    // ── ASM bytecode scan ─────────────────────────────────────────

    private Set<String> scanForExceptionCodes(Class<?> serviceClass) throws IOException {
        Set<String> foundCodes = new HashSet<>();

        new ClassReader(serviceClass.getName()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner,
                            String fieldName, String fieldDescriptor) {
                        if (opcode == Opcodes.GETSTATIC
                                && owner.contains("ExceptionCode")) {
                            foundCodes.add(
                                owner.substring(owner.lastIndexOf('/') + 1)
                                + "." + fieldName
                            );
                        }
                    }
                };
            }
        }, 0);

        return foundCodes;
    }

    // ── helpers ───────────────────────────────────────────────────

    private boolean isApiMethod(Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
            || method.isAnnotationPresent(GetMapping.class)
            || method.isAnnotationPresent(PostMapping.class)
            || method.isAnnotationPresent(PatchMapping.class)
            || method.isAnnotationPresent(PutMapping.class)
            || method.isAnnotationPresent(DeleteMapping.class);
    }

    private boolean isCodeRelevantToMethod(String code, String methodName) {
        // e.g. methodName = "initiateTransactionAuthorization"
        // code = "TamExceptionCode.AUTHORIZATION_NOT_FOUND"
        // rough match on keywords
        String lowerCode = code.toLowerCase();
        String lowerMethod = methodName.toLowerCase();
        return Arrays.stream(lowerMethod.split("(?=[A-Z])"))
            .filter(part -> part.length() > 3)
            .anyMatch(lowerCode::contains);
    }
}

```
