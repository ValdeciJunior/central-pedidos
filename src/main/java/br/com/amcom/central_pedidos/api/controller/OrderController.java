package br.com.amcom.central_pedidos.api.controller;

import br.com.amcom.central_pedidos.api.dto.OrderRequest;
import br.com.amcom.central_pedidos.api.dto.OrderResponse;
import br.com.amcom.central_pedidos.api.mapper.OrderCollectionMapper;
import br.com.amcom.central_pedidos.domain.model.Order;
import br.com.amcom.central_pedidos.domain.service.FindOrderUseCase;
import br.com.amcom.central_pedidos.domain.service.ProcessOrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderCollectionMapper mapper;
    private final ProcessOrderUseCase processOrderUseCase;
    private final FindOrderUseCase findOrderUseCase;

    @PostMapping
    public ResponseEntity<Void> receiveOrder(@Valid @RequestBody OrderRequest request) {
        log.info("API recebendo requisição de novo pedido. ID Externo: {}", request.externalOrderId());

        // Converte o DTO de request para a classe
        Order order = mapper.toDomain(request);

        // Com @Async anotado no OrderService, o Spring intercepta esta chamada, envia a execução
        // para um Pool de threads em segundo palno.
        processOrderUseCase.execute(order);

        // Suportar inúmeras chamadas em um curto período de tempo. A thread é liberada logo
        // para receber a próxima requisição do Produto Externo A.
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{externalOrderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String externalOrderId) {
        log.info("API recebendo consulta de pedido calculado. ID Externo: {}", externalOrderId);

        // Mapeia o domínio de volta para a DTO de response.
        return findOrderUseCase.execute(externalOrderId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Pedido ID Externo {} não encontrado para consulta.", externalOrderId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }
}