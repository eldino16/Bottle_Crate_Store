package de.group15.assignment1.repository;

import de.group15.assignment1.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(value = "Order.Order")
    List<Order> findAll();

    @Override
    @EntityGraph(value = "Order.Order")
    Optional<Order> findById(Long id);
}
