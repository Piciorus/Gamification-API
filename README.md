```
@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    @EventListener(ApplicationReadyEvent.class)
    public void analyze(ApplicationReadyEvent event) {
        try {
            // scan classpath directly — zero Spring bean interaction
            ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));

            // scan your base package
            Set<BeanDefinition> candidates = scanner
                .findCandidateComponents("de.consorsbank.core.trauthsc");

            candidates.forEach(bd -> {
                try {
                    Class<?> serviceClass = Class.forName(bd.getBeanClassName());
                    Set<String> codes = scanForExceptionCodes(serviceClass);

                    if (!codes.isEmpty()) {
                        log.info("[{}] → possible exception codes: {}",
                            serviceClass.getSimpleName(), codes);
                    }
                } catch (Exception e) {
                    log.warn("Skip {}: {}", bd.getBeanClassName(), e.getMessage());
                }
            });

        } catch (Exception e) {
            log.warn("ExceptionCodeAnalyzer failed: {}", e.getMessage());
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
