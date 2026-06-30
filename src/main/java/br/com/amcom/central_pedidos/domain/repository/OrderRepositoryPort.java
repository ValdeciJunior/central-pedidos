package br.com.amcom.central_pedidos.domain.repository;

import br.com.amcom.central_pedidos.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {
    // COMENTÁRIO SÊNIOR: Método crucial para o requisito de verificação de duplicidade.
    boolean existsByExternalOrderId(String externalOrderId);

    Order save(Order order);

    Optional<Order> findByExternalOrderId(String externalOrderId);
}