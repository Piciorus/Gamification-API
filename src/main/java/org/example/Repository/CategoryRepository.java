package org.example.Repository;

import org.example.Domain.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT c FROM Category c " +
            "WHERE c.name = :name ")
    Category findByName(@Param("name") String name);
}
