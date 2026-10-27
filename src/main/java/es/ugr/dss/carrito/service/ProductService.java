package es.ugr.dss.carrito.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.ugr.dss.carrito.model.Product;
import es.ugr.dss.carrito.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product saveProduct(Long id, String name, BigDecimal price) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }

        Product product = id == null ? new Product(normalizedName, price) : getProduct(id);
        product.setName(normalizedName);
        product.setPrice(price);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.delete(getProduct(id));
    }
}
