package es.ugr.dss.carrito.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import es.ugr.dss.carrito.model.Product;

@Component
@SessionScope
public class CartService {

    private final ProductService productService;
    private final List<Long> productIds = new ArrayList<>();

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public void add(Long productId) {
        productService.getProduct(productId);
        productIds.add(productId);
    }

    public void remove(Long productId) {
        productIds.removeIf(productId::equals);
    }

    public List<Product> getItems() {
        List<Product> items = new ArrayList<>();
        List<Long> staleIds = new ArrayList<>();
        for (Long productId : productIds) {
            try {
                items.add(productService.getProduct(productId));
            } catch (org.springframework.web.server.ResponseStatusException exception) {
                if (exception.getStatusCode() != org.springframework.http.HttpStatus.NOT_FOUND) {
                    throw exception;
                }
                staleIds.add(productId);
            }
        }
        productIds.removeAll(staleIds);
        return items;
    }
}
