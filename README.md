```
package de.consorsbank.core.trauthsc.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

@Slf4j
@Component
public class ExceptionCodeAnalyzer {

    private static final String OUTPUT_FILE = "exception-codes-report.json";
    private static final String BASE_PACKAGE = "de.consorsbank.core.trauthsc";

    // className#methodName → set of ExceptionCode constants found
    private final Map<String, Set<String>> methodCodesCache = new HashMap<>();
    // className#methodName → set of called className#methodName
    private final Map<String, Set<String>> callGraphCache = new HashMap<>();
    // className → set of injected field types (dependencies)
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

    // ── Step 1: scan ALL classes in base package ──────────────────

    private void buildFullCallGraph() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);

        // scan ALL classes — not just @Service
        // this ensures ErrorDecoders, helpers, utils are all included
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
                // collect injected field types — these are class dependencies
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
                        // GETSTATIC catches ALL enum constant usages:
                        // - throw new CommonException(TrauthExceptionCode.X)
                        // - if (x == TrauthExceptionCode.X)
                        // - return TrauthExceptionCode.X
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
                        // track all method calls within our base package
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

    // ── Step 2: find all controllers and map method → codes ───────

    private Map<String, Map<String, Set<String>>> buildControllerReport() {
        Map<String, Map<String, Set<String>>> report = new LinkedHashMap<>();

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        scanner.findCandidateComponents(BASE_PACKAGE).forEach(bd -> {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());

                // detect controller — either direct annotation or via interface
                boolean isController =
                    clazz.isAnnotationPresent(RestController.class)
                    || Arrays.stream(clazz.getInterfaces())
                        .anyMatch(i -> i.isAnnotationPresent(RestController.class));

                if (!isController) return;

                log.info("Found controller: {}", clazz.getSimpleName());

                Map<String, Set<String>> methodReport = new LinkedHashMap<>();

                // collect api methods from class + all interfaces
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

                        log.info("  {}() → {} codes, {} methods visited",
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

    // ── Step 3: recursive transitive code collection ──────────────

    private Set<String> collectCodesTransitively(
            String methodKey, Set<String> visited, int depth) {

        if (!visited.add(methodKey)) return Collections.emptySet();
        if (depth > 30) return Collections.emptySet(); // prevent infinite loops

        Set<String> codes = new HashSet<>();

        // resolve interface method → impl method(s)
        Set<String> resolvedKeys = resolveToImpl(methodKey);

        resolvedKeys.forEach(resolvedKey -> {
            visited.add(resolvedKey);

            // 1. direct exception codes in this method
            Set<String> directCodes = methodCodesCache.get(resolvedKey);
            if (directCodes != null) {
                codes.addAll(directCodes);
            }

            // 2. follow explicit method calls in call graph
            Set<String> calledMethods = callGraphCache.get(resolvedKey);
            if (calledMethods != null) {
                calledMethods.forEach(calledKey ->
                    codes.addAll(
                        collectCodesTransitively(calledKey, visited, depth + 1)
                    )
                );
            }

            // 3. scan all methods of injected dependencies
            // e.g. ServiceImpl has field EncryptionService → scan EncryptionServiceImpl
            String implClassName = resolvedKey.substring(0, resolvedKey.indexOf('#'));
            String implMethodName = resolvedKey.substring(resolvedKey.indexOf('#') + 1);

            Set<String> injectedDeps = fieldDependencyCache.get(implClassName);
            if (injectedDeps != null) {
                injectedDeps.forEach(depInterfaceName -> {

                    // try same method name on dependency first
                    String depMethodKey = depInterfaceName + "#" + implMethodName;
                    Set<String> depImplKeys = resolveToImpl(depMethodKey);

                    depImplKeys.forEach(depImplKey -> {
                        if (!visited.contains(depImplKey)) {
                            codes.addAll(
                                collectCodesTransitively(depImplKey, visited, depth + 1)
                            );
                        }

                        // also scan ALL methods of this dependency impl
                        // because it may throw in private helper methods
                        String depImplClass = depImplKey.substring(
                            0, depImplKey.indexOf('#'));
                        scanAllMethodsOfClass(depImplClass, visited, depth, codes);
                    });
                });
            }
        });

        return codes;
    }

    // scan every known method of a class transitively
    private void scanAllMethodsOfClass(
            String className, Set<String> visited, int depth, Set<String> codes) {

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

    // ── resolve interface → impl using isAssignableFrom ───────────

    private Set<String> resolveToImpl(String methodKey) {
        // already exists in cache as concrete key
        if (methodCodesCache.containsKey(methodKey)
                || callGraphCache.containsKey(methodKey)) {
            return Set.of(methodKey);
        }

        if (!methodKey.contains("#")) return Set.of(methodKey);

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

    // ── log report ────────────────────────────────────────────────

    private void logReport(Map<String, Map<String, Set<String>>> report) {
        log.info("=== Exception Code Report ===");
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
        log.info("=============================");
    }

    // ── write JSON ────────────────────────────────────────────────

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

                    // sort for consistent output
                    List<String> sortedCodes = new ArrayList<>(mEntry.getValue());
                    Collections.sort(sortedCodes);

                    Iterator<String> codeIt = sortedCodes.iterator();
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
            || method.isAnnotationPresent(DeleteMapping.class)
            || method.isAnnotationPresent(Operation.class);
    }
}
```
