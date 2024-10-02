package de.group15.assignment1.repository;

import de.group15.assignment1.model.Bottle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BottleRepository extends JpaRepository<Bottle, Long> {

}
