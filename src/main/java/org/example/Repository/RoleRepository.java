package org.example.Repository;

import org.example.Domain.Entities.ERole;
import org.example.Domain.Entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(ERole name);
    Optional<Role> findById(UUID id);
}
