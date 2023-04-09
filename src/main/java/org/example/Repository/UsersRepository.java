package org.example.Repository;

import org.example.Domain.Entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<User,Integer> {
    User save(User user);

    void deleteById(final Integer id);

    User getById(final Integer id);

    Iterable<User> findAll();

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
