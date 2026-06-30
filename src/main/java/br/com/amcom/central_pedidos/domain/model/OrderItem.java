package br.com.amcom.central_pedidos.domain.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItem {
    private final String productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String productId, Integer quantity, BigDecimal unitPrice) {
        // Validações básicas de negócio
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("O ID do produto não pode ser nulo ou vazio.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser maior que zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço unitário deve ser maior que zero.");
        }

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal calculateSubtotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}