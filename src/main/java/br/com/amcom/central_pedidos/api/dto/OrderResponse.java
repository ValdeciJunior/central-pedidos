package br.com.amcom.central_pedidos.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String externalOrderId,
        BigDecimal totalValue,
        String status,
        List<OrderItemResponse> items
) {}