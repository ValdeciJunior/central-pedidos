package br.com.amcom.central_pedidos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "O código do produto é obrigatório.")
        String productId,

        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 1, message = "A quantidade mínima permitida é 1.")
        Integer quantity,

        @NotNull(message = "O preço unitário é obrigatório.")
        @Positive(message = "O preço unitário deve ser maior que zero.")
        BigDecimal unitPrice
) {}