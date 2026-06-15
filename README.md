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
