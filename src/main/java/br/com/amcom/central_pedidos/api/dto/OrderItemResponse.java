package br.com.amcom.central_pedidos.api.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}