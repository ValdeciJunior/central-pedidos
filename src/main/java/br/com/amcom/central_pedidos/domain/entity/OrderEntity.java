package br.com.amcom.central_pedidos.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_orders", indexes = {
        // Usando o index do ID do produto externo para agligilar a busca por duplicidade. Evitando
        // engargalar o banco com um número alto de requisições.
        @Index(name = "idx_external_order_id", columnList = "external_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilizando o unique true como uma camada extra para não duplicar no banco caso threads
    //concorrentes tentei inserir um produto ao mesmo tempo
    @Column(name = "external_order_id", nullable = false, unique = true)
    private String externalOrderId;

    @Column(name = "total_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "status", nullable = false)
    private String status;

    // Usando o @Version do Lock Otimist para caso threads concorrentes tentem atualizar o mesmo registro.
    /*** pode lançar uma ObjectOptimisticLockingFailureException, criar um tratamento no futuro ******/
    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> items = new ArrayList<>();

    public void addItem(OrderItemEntity item) {
        this.items.add(item);
        item.setOrder(this);
    }
}