package org.example.Repository;

import org.example.Domain.Entities.ERole;
import org.example.Domain.Entities.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    Role findByName(ERole name);
}
