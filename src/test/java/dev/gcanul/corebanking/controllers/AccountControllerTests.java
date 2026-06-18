package dev.gcanul.corebanking.controllers;

import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.security.JwtService;
import dev.gcanul.corebanking.services.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("Should create account successfully when data is valid")
    void shouldCreateAccountSuccessfully() throws Exception {
        // 1. Arrange
        String jsonRequest = """
        {
            "amount": 100.00,
            "userId": 1
        }
        """;

        // Instancia correcta del record con sus 4 campos
        var responseDto = new AccountResponse(1L, "ACC-12345", new BigDecimal("100.00"), 1L);

        // Mockeamos la respuesta del servicio
        when(accountService.createAccount(any())).thenReturn(responseDto);

        // 2. Act & Assert
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-12345"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should return 400 when userId is null")
    void shouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
        // Arrange
        String jsonRequest = """
            {
                "initialBalance": 100.00,
                "userId": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Verificamos que falla la validación
    }

    @Test
    @DisplayName("Should return 400 when initial balance is negative")
    void shouldReturnBadRequest_WhenInitialBalanceIsNegative() throws Exception {
        // 1. Arrange: Un balance negativo
        String jsonRequest = """
        {
            "initialBalance": -100.00,
            "userId": 1
        }
        """;

        // 2. Act & Assert: Esperamos un Bad Request (400)
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()); // <--- ¡Aquí está la magia!
    }
}
