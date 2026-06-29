package com.pharmadesk.backend.sales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmadesk.backend.sales.dto.SaleItemDTO;
import com.pharmadesk.backend.sales.dto.SaleRequestDTO;
import com.pharmadesk.backend.model.Medicine;
import com.pharmadesk.backend.model.MedicineStock;
import com.pharmadesk.backend.sales.model.PharmacyBill;
import com.pharmadesk.backend.pharmacy.enums.PaymentMode;
import com.pharmadesk.backend.pharmacy.repository.MedicineRepository;
import com.pharmadesk.backend.pharmacy.repository.MedicineStockRepository;
import com.pharmadesk.backend.sales.repository.PharmacyBillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
public class SaleControllerIntegrationTest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("pharmadesk_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static org.testcontainers.containers.GenericContainer<?> redisContainer =
            new org.testcontainers.containers.GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        // Ensure Hibernate auto-ddl is validate or none, rely on Flyway
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicineStockRepository stockRepository;

    @Autowired
    private PharmacyBillRepository billRepository;

    private Long savedStockId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        billRepository.deleteAll();
        stockRepository.deleteAll();
        medicineRepository.deleteAll();

        Medicine medicine = new Medicine();
        medicine.setName("Integration Paracetamol");
        medicine.setCategory("TABLET");
        medicine.setUnit("STRIP");
        medicine = medicineRepository.save(medicine);

        MedicineStock stock = new MedicineStock();
        stock.setMedicine(medicine);
        stock.setBatchNumber("INTBATCH1");
        stock.setQuantityAvailable(100);
        stock.setSellingRate(BigDecimal.valueOf(15.5));
        stock.setPurchaseRate(BigDecimal.valueOf(10.0));
        stock.setExpiryDate(LocalDate.now().plusMonths(12));
        
        savedStockId = stockRepository.save(stock).getId();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_SYSTEM_ADMIN"})
    void postSale_shouldDeductStock() throws Exception {
        SaleItemDTO item = new SaleItemDTO();
        item.setStockId(savedStockId);
        item.setQuantity(10);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setPatientName("Test Patient");
        request.setItems(List.of(item));
        request.setPaymentMode(PaymentMode.CASH);
        request.setAmountPaid(BigDecimal.valueOf(155.0));
        request.setDiscountAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/pharmacy/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert stock deducted
        MedicineStock updatedStock = stockRepository.findById(savedStockId).orElseThrow();
        assertEquals(90, updatedStock.getQuantityAvailable());

        // Assert bill created
        assertEquals(1, billRepository.count());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_SYSTEM_ADMIN"})
    void postSale_withInsufficientStock_shouldReturn422() throws Exception {
        SaleItemDTO item = new SaleItemDTO();
        item.setStockId(savedStockId);
        item.setQuantity(200); // Only 100 available

        SaleRequestDTO request = new SaleRequestDTO();
        request.setPatientName("Test Patient");
        request.setItems(List.of(item));
        request.setPaymentMode(PaymentMode.CASH);
        request.setAmountPaid(BigDecimal.valueOf(3100.0));
        request.setDiscountAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/pharmacy/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

        // Assert stock NOT deducted
        MedicineStock updatedStock = stockRepository.findById(savedStockId).orElseThrow();
        assertEquals(100, updatedStock.getQuantityAvailable());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_SYSTEM_ADMIN"})
    void deleteSale_shouldRestoreStock() throws Exception {
        // First create a sale programmatically or via endpoint
        SaleItemDTO item = new SaleItemDTO();
        item.setStockId(savedStockId);
        item.setQuantity(25);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setPatientName("Delete Patient");
        request.setItems(List.of(item));
        request.setPaymentMode(PaymentMode.CASH);
        request.setAmountPaid(BigDecimal.valueOf(387.5));
        request.setDiscountAmount(BigDecimal.ZERO);

        String responseJson = mockMvc.perform(post("/api/pharmacy/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract ID
        Long billId = objectMapper.readTree(responseJson).get("data").get("id").asLong();

        // Verify stock is 75
        assertEquals(75, stockRepository.findById(savedStockId).orElseThrow().getQuantityAvailable());

        // Now Delete the sale
        mockMvc.perform(delete("/api/pharmacy/sales/" + billId))
                .andExpect(status().isOk());

        // Verify stock is restored to 100
        assertEquals(100, stockRepository.findById(savedStockId).orElseThrow().getQuantityAvailable());

        // Verify bill is marked deleted
        PharmacyBill bill = billRepository.findById(billId).orElseThrow();
        assertTrue(bill.isDeleted());
    }
}
