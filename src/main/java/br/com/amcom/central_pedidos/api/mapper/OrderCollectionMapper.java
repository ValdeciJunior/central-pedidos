package br.com.amcom.central_pedidos.api.mapper;

import br.com.amcom.central_pedidos.api.dto.OrderItemResponse;
import br.com.amcom.central_pedidos.api.dto.OrderRequest;
import br.com.amcom.central_pedidos.api.dto.OrderResponse;
import br.com.amcom.central_pedidos.domain.model.Order;
import br.com.amcom.central_pedidos.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCollectionMapper {

    public Order toDomain(OrderRequest request) {
        List<OrderItem> domainItems = request.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
        return new Order(request.externalOrderId(), domainItems);
    }

    public OrderResponse toResponse(Order domain) {
        List<OrderItemResponse> itemResponses = domain.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.calculateSubtotal()
                )).toList();

        return new OrderResponse(
                domain.getExternalOrderId(),
                domain.getTotalValue(),
                domain.getStatus().name(),
                itemResponses
        );
    }
}