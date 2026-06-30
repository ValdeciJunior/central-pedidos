package br.com.amcom.central_pedidos.domain.service;

import br.com.amcom.central_pedidos.domain.model.Order;

public interface ProcessOrderUseCase {
    void execute(Order order);
}