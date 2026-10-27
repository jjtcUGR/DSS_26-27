package es.ugr.dss.carrito;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import es.ugr.dss.carrito.model.Product;
import es.ugr.dss.carrito.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "carrito.admin.password=integration-test-only")
@AutoConfigureMockMvc
class CarritoApplicationTests {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private MockMvc mockMvc;

	@AfterEach
	void cleanProducts() {
		productRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void savesAndLoadsProduct() {
		Product product = productRepository.save(new Product("Teclado", new BigDecimal("24.90")));

		assertThat(product.getId()).isNotNull();
		assertThat(productRepository.findById(product.getId()))
				.hasValueSatisfying(savedProduct -> {
					assertThat(savedProduct.getName()).isEqualTo("Teclado");
					assertThat(savedProduct.getPrice()).isEqualByComparingTo("24.90");
				});

		productRepository.deleteById(product.getId());
	}

	@Test
	void homeAndCatalogArePublicButAdminRequiresRole() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Ver catálogo")));
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Catálogo de productos")));
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		mockMvc.perform(get("/products/add"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanCreateProductAndCustomersCanAddItToCart() throws Exception {
		mockMvc.perform(post("/products/add")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "Cuaderno")
						.param("price", "3.50"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/products"));

		Product product = productRepository.findAll().get(0);
		MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/cart/add/{id}", product.getId()).with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"))
				.andReturn().getRequest().getSession(false);
		mockMvc.perform(get("/cart").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Cuaderno")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanUpdateDeleteAndExportProduct() throws Exception {
		Product product = productRepository.save(new Product("Bolígrafo", new BigDecimal("1.20")));

		mockMvc.perform(post("/products/edit/{id}", product.getId())
						.with(csrf())
						.param("name", "Bolígrafo azul")
						.param("price", "1.50"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/admin/export"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Bolígrafo azul")));

		mockMvc.perform(post("/products/delete/{id}", product.getId()).with(csrf()))
				.andExpect(status().is3xxRedirection());
		assertThat(productRepository.findById(product.getId())).isEmpty();
	}

	@Test
	void configuredAdminCanLogIn() throws Exception {
		MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "admin")
						.param("password", "integration-test-only"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin"))
				.andReturn().getRequest().getSession(false);

		mockMvc.perform(get("/admin").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Administración de productos")));
	}

	@Test
	void cartSupportsAddingRemovingAndContinuingShopping() throws Exception {
		Product first = productRepository.save(new Product("Taza", new BigDecimal("4.25")));
		Product second = productRepository.save(new Product("Plato", new BigDecimal("6.00")));

		MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/cart/add/{id}", first.getId()).with(csrf()))
				.andReturn().getRequest().getSession(false);
		mockMvc.perform(post("/cart/add/{id}", second.getId()).session(session).with(csrf()))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/cart").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Taza")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Plato")));

		mockMvc.perform(post("/cart/remove/{id}", first.getId()).session(session).with(csrf()))
				.andExpect(status().is3xxRedirection());
		mockMvc.perform(get("/cart").session(session))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Taza"))))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Plato")));
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk());
	}
}
