```
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionCodeAnalyzer implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("=== Starting Exception Code Analysis ===");

        // get all @RestController beans
        applicationContext.getBeansWithAnnotation(RestController.class)
            .forEach((beanName, controllerBean) -> {
                log.info("--- Analyzing controller: {}", controllerBean.getClass().getSimpleName());
                analyzeController(controllerBean);
            });

        log.info("=== Exception Code Analysis Complete ===");
    }

    private void analyzeController(Object controller) {
        Class<?> controllerClass = AopUtils.getTargetClass(controller); // unwrap proxies

        // find all service fields injected into this controller
        List<Class<?>> serviceClasses = findInjectedServices(controllerClass);

        // for each controller method
        Arrays.stream(controllerClass.getDeclaredMethods())
            .filter(this::isApiMethod)
            .forEach(method -> {
                log.info("  Method: {}", method.getName());

                Set<String> allCodes = new HashSet<>();

                // scan each injected service for codes thrown in methods
                // that match the controller method name pattern
                serviceClasses.forEach(serviceClass -> {
                    try {
                        Set<String> codes = scanServiceClass(
                            serviceClass, 
                            method.getName()
                        );
                        allCodes.addAll(codes);
                    } catch (IOException e) {
                        log.warn("Could not analyze {}: {}", 
                            serviceClass.getSimpleName(), e.getMessage());
                    }
                });

                if (allCodes.isEmpty()) {
                    log.info("    → No exception codes detected");
                } else {
                    log.info("    → Detected exception codes: {}", allCodes);
                }
            });
    }

    private List<Class<?>> findInjectedServices(Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getDeclaredFields())
            .map(field -> {
                // resolve actual impl from Spring context
                try {
                    Object bean = applicationContext.getBean(field.getType());
                    return AopUtils.getTargetClass(bean); // unwrap proxy → get impl
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Set<String> scanServiceClass(Class<?> serviceClass, String controllerMethodName) 
            throws IOException {
        
        Set<String> foundCodes = new HashSet<>();

        ClassReader reader = new ClassReader(serviceClass.getName());
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name,
                    String descriptor, String signature, String[] exceptions) {

                // match service method by name similarity to controller method
                // e.g. controller: "initiateTransactionAuthorization"
                //      service:    "initiateTransactionAuthorization" (same name)
                if (isRelatedMethod(name, controllerMethodName)) {
                    log.debug("    Scanning service method: {}", name);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner,
                                String fieldName, String fieldDescriptor) {
                            // GETSTATIC = how enum constants are loaded in bytecode
                            if (opcode == Opcodes.GETSTATIC 
                                    && owner.contains("ExceptionCode")) {
                                log.debug("      Found: {}.{}", 
                                    owner.substring(owner.lastIndexOf('/') + 1), 
                                    fieldName);
                                foundCodes.add(
                                    owner.substring(owner.lastIndexOf('/') + 1) 
                                    + "." + fieldName
                                );
                            }
                        }
                    };
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);

        return foundCodes;
    }

    private boolean isApiMethod(Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
            || method.isAnnotationPresent(GetMapping.class)
            || method.isAnnotationPresent(PostMapping.class)
            || method.isAnnotationPresent(PatchMapping.class)
            || method.isAnnotationPresent(PutMapping.class)
            || method.isAnnotationPresent(DeleteMapping.class)
            // also check interface methods for controllers implementing Api interfaces
            || Arrays.stream(method.getDeclaringClass().getInterfaces())
                .flatMap(i -> Arrays.stream(i.getDeclaredMethods()))
                .filter(m -> m.getName().equals(method.getName()))
                .anyMatch(m -> m.isAnnotationPresent(RequestMapping.class)
                            || m.isAnnotationPresent(GetMapping.class)
                            || m.isAnnotationPresent(PostMapping.class)
                            || m.isAnnotationPresent(PatchMapping.class));
    }

    private boolean isRelatedMethod(String serviceMethodName, String controllerMethodName) {
        // exact match first
        if (serviceMethodName.equals(controllerMethodName)) return true;
        // partial match — service might have slightly different name
        String lower = controllerMethodName.toLowerCase();
        return serviceMethodName.toLowerCase().contains(lower)
            || lower.contains(serviceMethodName.toLowerCase());
    }
}
```

```
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionCodeAnalyzer implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        applicationContext.getBeansWithAnnotation(RestController.class)
            .forEach((name, controller) -> analyzeController(controller));
    }

    private void analyzeController(Object controller) {
        Class<?> controllerClass = AopUtils.getTargetClass(controller);

        Arrays.stream(controllerClass.getDeclaredFields())
            .forEach(field -> {
                try {
                    // get the actual service impl class
                    Class<?> serviceImpl = AopUtils.getTargetClass(
                        applicationContext.getBean(field.getType())
                    );

                    // scan ALL methods in that service for exception codes
                    Set<String> codes = scanForExceptionCodes(serviceImpl);

                    if (!codes.isEmpty()) {
                        log.info("[{}] → possible exception codes: {}",
                            controllerClass.getSimpleName(), codes);
                    }

                } catch (Exception e) {
                    // field is not a Spring bean — skip
                }
            });
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
