import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;

import java.io.File;
import java.util.*;

public class YamlEndpointSearcher {

    private final Map<String, String> operationIdToPathMap = new HashMap<>();

    public YamlEndpointSearcher() {
        initializeOperationIdMap();
    }

    private void initializeOperationIdMap() {
        try {
            File directory = new File(this.getClass().getClassLoader().getResource("crw").getFile());
            File[] yamlFiles = directory.listFiles((dir, name) -> name.endsWith(".yanl"));

            if (yamlFiles == null || yamlFiles.length == 0) {
                throw new IllegalStateException("No YAML files found in the directory.");
            }

            for (File yamlFile : yamlFiles) {
                OpenAPI openAPI = new OpenAPIV3Parser().read(yamlFile.getAbsolutePath());
                if (openAPI != null && openAPI.getPaths() != null) {
                    openAPI.getPaths().forEach((path, pathItem) -> {
                        if (pathItem != null) {
                            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                                if (operation.getOperationId() != null) {
                                    operationIdToPathMap.put(operation.getOperationId(), path);
                                }
                            });
                        }
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing YAML files", e);
        }
    }

    public String searchEndpointInAllYamlFiles(String operationId) {
        return operationIdToPathMap.getOrDefault(operationId, null);
    }
}

