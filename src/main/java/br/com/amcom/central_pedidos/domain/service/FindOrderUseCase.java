package br.com.amcom.central_pedidos.domain.service;

import br.com.amcom.central_pedidos.domain.model.Order;

import java.util.Optional;

public interface FindOrderUseCase {
    Optional<Order> execute(String externalOrderId);
}