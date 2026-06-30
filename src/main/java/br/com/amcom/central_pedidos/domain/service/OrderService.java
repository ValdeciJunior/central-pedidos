package br.com.amcom.central_pedidos.domain.service;

import br.com.amcom.central_pedidos.domain.model.Order;
import br.com.amcom.central_pedidos.domain.repository.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService implements ProcessOrderUseCase, FindOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepositoryPort orderRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    // O uso do @Async prepara o método para receber uma grande quantidade de requisições em pouco tempo,
    //  trabalahndo com mult-threads.
    // O Spring desvia a execução deste bloco para um Pool de Threads, executando o cálculo
    // e persistência em segundo plano sem travar a requisição.
    @Override
    @Async("orderExecutor")
    public void execute(Order order) {
        log.info("Iniciando processamento assíncrono do pedido ID Externo: {}", order.getExternalOrderId());

        try {
            // VErificando duplicidades
            if (orderRepositoryPort.existsByExternalOrderId(order.getExternalOrderId())) {
                log.warn("Pedido ID Externo {} já processado ou em processamento.", order.getExternalOrderId());
                return;
            }

            // Executa os cálculos
            order.calculateTotal();

            // Salva no banco
            orderRepositoryPort.save(order);

            log.info("Pedido ID Externo {} calculado e salvo com sucesso. Valor total: {}",
                    order.getExternalOrderId(), order.getTotalValue());

            /***** Implementar depois o Circuit Breaker *****/

        } catch (Exception e) {
            log.error("Erro fatal ao processar o pedido ID Externo: {}", order.getExternalOrderId(), e);
            order.markAsFailed();
            /***** Implementar depois o salvamento do estado falho para auditoria *****/
        }
    }

    @Override
    public Optional<Order> execute(String externalOrderId) {
        // Consutla do Produto Externo B
        return orderRepositoryPort.findByExternalOrderId(externalOrderId);
    }
}