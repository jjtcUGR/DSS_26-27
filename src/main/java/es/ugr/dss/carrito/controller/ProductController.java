package es.ugr.dss.carrito.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import es.ugr.dss.carrito.model.Product;
import es.ugr.dss.carrito.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    @GetMapping("/products/add")
    public String addForm(Model model) {
        model.addAttribute("product", new Product("", BigDecimal.ZERO));
        model.addAttribute("formTitle", "Añadir producto");
        model.addAttribute("formAction", "/products/add");
        return "product_form";
    }

    @PostMapping("/products/add")
    public String add(@RequestParam String name, @RequestParam BigDecimal price, RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(null, name, price);
            redirectAttributes.addFlashAttribute("success", "Producto añadido.");
            return "redirect:/products";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            redirectAttributes.addFlashAttribute("productName", name);
            redirectAttributes.addFlashAttribute("productPrice", price);
            return "redirect:/products/add";
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProduct(id));
        model.addAttribute("formTitle", "Editar producto");
        model.addAttribute("formAction", "/products/edit/" + id);
        return "product_form";
    }

    @PostMapping("/products/edit/{id}")
    public String edit(@PathVariable Long id, @RequestParam String name, @RequestParam BigDecimal price,
            RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(id, name, price);
            redirectAttributes.addFlashAttribute("success", "Producto actualizado.");
            return "redirect:/products";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/products/edit/" + id;
        }
    }

    @PostMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado.");
        return "redirect:/products";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin";
    }
}
