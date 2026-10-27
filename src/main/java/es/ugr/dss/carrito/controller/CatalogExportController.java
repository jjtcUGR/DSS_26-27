package es.ugr.dss.carrito.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import es.ugr.dss.carrito.model.Product;
import es.ugr.dss.carrito.service.ProductService;

@Controller
public class CatalogExportController {

    private final ProductService productService;

    public CatalogExportController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/admin/export", "/admin/download-db-sql"})
    public ResponseEntity<byte[]> export() {
        List<Product> products = productService.getAllProducts();
        StringBuilder sql = new StringBuilder("-- Exportación del catálogo de productos\n");
        if (products.isEmpty()) {
            sql.append("-- No hay productos en el catálogo.\n");
        }
        for (Product product : products) {
            String name = product.getName().replace("'", "''");
            sql.append("INSERT INTO products (id, name, price) VALUES (")
                    .append(product.getId()).append(", '").append(name).append("', ")
                    .append(product.getPrice().toPlainString()).append(");\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog.sql\"")
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(sql.toString().getBytes(StandardCharsets.UTF_8));
    }
}
