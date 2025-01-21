package com.example.repository.secondary;

import com.example.entity.secondary.UserSecondary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.secondary.hikari.jdbc-url=jdbc:postgresql://localhost:5434/db2",
        "spring.datasource.secondary.hikari.username=admin2",
        "spring.datasource.secondary.hikari.password=admin2"
})
public class UserSecondaryRepositoryTest {

    @Autowired
    private UserSecondaryRepository userSecondaryRepository;

    @Test
    public void testSaveUserInSecondaryDatabase() {
        // Arrange
        UserSecondary user = new UserSecondary();
        user.setName("Jane Doe");

        // Act
        UserSecondary savedUser = userSecondaryRepository.save(user);

        // Assert
        Optional<UserSecondary> retrievedUser = userSecondaryRepository.findById(savedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("Jane Doe");
    }
}
@SpringBootTest
@Testcontainers
public class UserPrimaryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserPrimaryRepository userPrimaryRepository;

    @Test
    public void testSaveUser() {
        // Arrange
        UserPrimary user = new UserPrimary();
        user.setName("Test User");

        // Act
        UserPrimary savedUser = userPrimaryRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
    }
}
