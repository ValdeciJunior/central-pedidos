package br.com.amcom.central_pedidos.domain.repository.adapter;

import br.com.amcom.central_pedidos.domain.entity.OrderEntity;
import br.com.amcom.central_pedidos.domain.entity.OrderItemEntity;
import br.com.amcom.central_pedidos.domain.model.Order;
import br.com.amcom.central_pedidos.domain.model.OrderItem;
import br.com.amcom.central_pedidos.domain.model.OrderStatus;
import br.com.amcom.central_pedidos.domain.repository.OrderRepository;
import br.com.amcom.central_pedidos.domain.repository.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsByExternalOrderId(String externalOrderId) {
        return orderRepository.existsByExternalOrderId(externalOrderId);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setExternalOrderId(order.getExternalOrderId());
        entity.setTotalValue(order.getTotalValue());
        entity.setStatus(order.getStatus().name());

        order.getItems().forEach(item -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setProductId(item.getProductId());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            entity.addItem(itemEntity);
        });

        OrderEntity savedEntity = orderRepository.save(entity);

        return reconstructDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByExternalOrderId(String externalOrderId) {
        return orderRepository.findByExternalOrderId(externalOrderId)
                .map(this::reconstructDomain);
    }

    private Order reconstructDomain(OrderEntity entity) {
        List<OrderItem> domainItems = entity.getItems().stream()
                .map(itemEntity -> new OrderItem(
                        itemEntity.getProductId(),
                        itemEntity.getQuantity(),
                        itemEntity.getUnitPrice()
                )).toList();

        Order order = new Order(entity.getExternalOrderId(), domainItems);

        // Como o registro nasce como RECEIVED, atualizamos seu estado com o histórico do banco
        if (OrderStatus.CALCULATED.name().equals(entity.getStatus())) {
            order.calculateTotal();
        } else if (OrderStatus.PROCESSED.name().equals(entity.getStatus())) {
            order.calculateTotal();
            order.markAsProcessed();
        } else if (OrderStatus.FAILED.name().equals(entity.getStatus())) {
            order.markAsFailed();
        }
        return order;
    }
}