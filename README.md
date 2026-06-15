```
@Component
@RequiredArgsConstructor
public class AutoErrorCodesCustomizer implements OperationCustomizer {

    private final ExceptionCodeAnalyzer analyzer;

    // cache: controller method → detected exception codes
    private final Map<Method, List<ExceptionCode>> cache = new ConcurrentHashMap<>();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        
        // 1. check manual annotation first — takes priority
        List<ExceptionCode> codes = resolveManualCodes(handlerMethod);
        
        // 2. if no manual annotation — auto-detect from bytecode
        if (codes.isEmpty()) {
            codes = cache.computeIfAbsent(
                handlerMethod.getMethod(),
                m -> analyzer.detectCodes(handlerMethod)
            );
        }

        if (codes.isEmpty()) return operation;

        // ... same grouping + swagger enrichment as before
        return operation;
    }
}
```

```
@Component
public class ExceptionCodeAnalyzer {

    public Set<String> analyzeMethodForThrownCodes(Class<?> serviceClass, String methodName) 
            throws IOException {
        
        Set<String> foundCodes = new HashSet<>();
        
        ClassReader reader = new ClassReader(serviceClass.getName());
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, 
                    String descriptor, String signature, String[] exceptions) {
                if (name.equals(methodName)) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner, 
                                String fieldName, String fieldDescriptor) {
                            // detect: TamExceptionCode.AUTHORIZATION_NOT_FOUND
                            if (owner.contains("ExceptionCode")) {
                                foundCodes.add(owner + "." + fieldName);
                            }
                        }
                    };
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        
        return foundCodes;
    }
}
```


```
@Component
@RequiredArgsConstructor
public class ExceptionCodeAnalyzer implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext context;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // scan all @RestController beans
        context.getBeansWithAnnotation(RestController.class)
            .values()
            .forEach(this::analyzeController);
    }

    private void analyzeController(Object controller) {
        Arrays.stream(controller.getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(RequestMapping.class)
                      || m.isAnnotationPresent(GetMapping.class)
                      || m.isAnnotationPresent(PostMapping.class)
                      || m.isAnnotationPresent(PatchMapping.class))
            .forEach(method -> {
                Set<Class<? extends ExceptionCode>> thrown = findThrownExceptionCodes(method);
                if (!thrown.isEmpty()) {
                    log.info("Method {} can throw: {}", method.getName(), thrown);
                }
            });
    }
}

```


```

<dependency>
    <groupId>org.ow2.asm</groupId>
    <artifactId>asm</artifactId>
    <version>9.6</version>
</dependency>
```
