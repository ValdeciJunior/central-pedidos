package br.com.amcom.central_pedidos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "O ID externo do pedido é obrigatório.")
        String externalOrderId,

        @NotEmpty(message = "O pedido deve conter pelo menos um item.")
        @Valid
        List<OrderItemRequest> items
) {}