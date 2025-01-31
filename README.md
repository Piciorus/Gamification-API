import java.util.zip.ZipFile

task extractCommonErrorYaml {
    doLast {
        def jarFile = configurations.runtimeClasspath.find { it.name.contains("commons-lib") }
        if (!jarFile) {
            throw new FileNotFoundException("commons-lib.jar not found in dependencies!")
        }

        def yamlFileName = "common-error-handling.yaml"
        def outputDir = file("$buildDir/extracted-yaml")
        outputDir.mkdirs()

        def outputFile = new File(outputDir, yamlFileName)

        new ZipFile(jarFile).withCloseable { zip ->
            def entry = zip.getEntry(yamlFileName)
            if (entry == null) {
                throw new FileNotFoundException("$yamlFileName not found inside $jarFile")
            }
            zip.getInputStream(entry).withCloseable { input ->
                outputFile.bytes = input.bytes
            }
        }

        println "Extracted $yamlFileName to: ${outputFile.absolutePath}"
    }
}import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.TransactionSystemException;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class CraRestExampleServiceTest {

    @Autowired
    private CraRestExampleService craRestExampleService;

    @Autowired
    private LutSegmenteRepository1 repository1;

    @Autowired
    private LutSegmenteRepository2 repository2;

    @Test
    public void testDistributedTransactionRollback() {
        // Ensure repositories are empty before the test
        assertEquals(0, repository1.count());
        assertEquals(0, repository2.count());

        try {
            craRestExampleService.saveEntitiesWithTransaction();
        } catch (RuntimeException e) {
            // Expected exception
        }

        // Both repositories should still be empty due to rollback
        assertEquals(0, repository1.count(), "Repository1 should be empty after rollback");
        assertEquals(0, repository2.count(), "Repository2 should be empty after rollback");
    }
}
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributedTransactionIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LutSegmenteRepository1 repository1;

    @Autowired
    private LutSegmenteRepository2 repository2;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/test/distributed-transaction";
    }

    @Test
    @Order(1)
    public void testSuccessfulDistributedTransaction() {
        // Ensure both repositories are empty before test
        repository1.deleteAll();
        repository2.deleteAll();

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "?fail=false", null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, repository1.count(), "Repository1 should contain one record");
        assertEquals(1, repository2.count(), "Repository2 should contain one record");
    }

    @Test
    @Order(2)
    public void testFailedDistributedTransactionRollback() {
        // Ensure both repositories are empty before test
        repository1.deleteAll();
        repository2.deleteAll();

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "?fail=true", null, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(0, repository1.count(), "Repository1 should be empty after rollback");
        assertEquals(0, repository2.count(), "Repository2 should be empty after rollback");
 
    
    }
}

package com.example.demo.integration;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUserAndRetrieveIt() throws Exception {
        // Create a user
        String userJson = "{ \"name\": \"John Doe\", \"email\": \"john@example.com\" }";

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk());

        // Verify user retrieval
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("john@example.com"));
    }
}


