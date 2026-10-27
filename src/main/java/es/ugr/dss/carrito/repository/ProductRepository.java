package es.ugr.dss.carrito.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ugr.dss.carrito.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
