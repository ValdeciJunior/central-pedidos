package br.com.amcom.central_pedidos.client;

import br.com.amcom.central_pedidos.domain.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternalProductBClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalProductBClient.class);

    // A anotação intercepta a chamada.
    // SE o circuito estiver aberto este método nem sequer é executado. O fluxo desvia a execução
    //  direto para o método de 'fallback'.
    @CircuitBreaker(name = "productBClient", fallbackMethod = "notifyProductBFallback")
    public void notifyOrderCalculated(Order order) {
        log.info("Enviando pedido calculado {} via HTTP para o Produto Externo B...", order.getExternalOrderId());

        // Simulando uma comunicação com o Produto externo em um cenário de falha para testar o
        // CircuitBreaker futuramente
        if (Math.random() > 0.8) { // Simulação de instabilidade de 20% na rede externa
            throw new RuntimeException("Erro de comunicação de rede ou Timeout com o Produto B.");
        }

        log.info("Produto Externo B notificou sucesso para o pedido {}.", order.getExternalOrderId());
    }

    // Aqui definimos a estratégia de como deve proceder se o client estiver fora do ar
    public void notifyProductBFallback(Order order, Throwable t) {
        log.error("CIRCUIT BREAKER ATIVADO / FALHA NA INTEGRAÇÃO. Não foi possível notificar o Produto B sobre o pedido {}. Motivo: {}",
                order.getExternalOrderId(), t.getMessage());

        // Aqui poderíamos salvar em uma fila em algum serviço de mensageria para auditoria e não perdemos
        // os dados que deram falha.

        log.error("Enviando para a fila de falha");
    }
}