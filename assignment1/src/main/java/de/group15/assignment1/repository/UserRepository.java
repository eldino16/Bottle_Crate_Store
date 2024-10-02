package de.group15.assignment1.repository;

import de.group15.assignment1.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @EntityGraph(value = "User.User")
    Optional<User> getUserWithEntitiesByUsername(String id);

    boolean existsByUsername(String username);
}
