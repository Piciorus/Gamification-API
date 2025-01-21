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
