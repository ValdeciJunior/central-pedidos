package br.com.amcom.central_pedidos.api.controller;

import br.com.amcom.central_pedidos.api.dto.OrderRequest;
import br.com.amcom.central_pedidos.api.dto.OrderResponse;
import br.com.amcom.central_pedidos.api.mapper.OrderCollectionMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderCollectionMapper mapper;

    public OrderController(OrderCollectionMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Void> receiveOrder(@Valid @RequestBody OrderRequest request) {
        /******* Fazer a implementação depois ******/

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{externalOrderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String externalOrderId) {
        // Endpoint para consulta solicitada para o Produto Externo B.
        // Irá buscar a informação calculada e retornar o estado atual do pedido.

        /***** FAzer a implementação depois *******/

        return ResponseEntity.ok().build();
    }
}