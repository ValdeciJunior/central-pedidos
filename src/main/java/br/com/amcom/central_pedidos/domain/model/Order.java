package br.com.amcom.central_pedidos.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;


@Getter
public class Order {
    private final String externalOrderId; // ID vindo do Produto Externo A para controle de duplicidade
    private final List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalValue;

    public Order(String externalOrderId, List<OrderItem> items) {
        if (externalOrderId == null || externalOrderId.isBlank()) {
            throw new IllegalArgumentException("O ID externo do pedido é obrigatório.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Um pedido precisa ter pelo menos um item.");
        }

        this.externalOrderId = externalOrderId;
        this.items = List.copyOf(items); // Protege a lista contra mutações externas involuntárias
        this.status = OrderStatus.RECEIVED;
        this.totalValue = BigDecimal.ZERO;
    }

    public void calculateTotal() {
        if (this.status != OrderStatus.RECEIVED) {
            throw new IllegalStateException("O pedido só pode ser calculado se estiver no status RECEIVED.");
        }

        this.totalValue = this.items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.status = OrderStatus.CALCULATED;
    }

    public void markAsProcessed() {
        this.status = OrderStatus.PROCESSED;
    }

    public void markAsFailed() {
        this.status = OrderStatus.FAILED;
    }
}