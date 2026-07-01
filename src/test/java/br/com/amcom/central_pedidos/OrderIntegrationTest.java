package br.com.amcom.central_pedidos;

import br.com.amcom.central_pedidos.domain.model.Order;
import br.com.amcom.central_pedidos.domain.model.OrderItem;
import br.com.amcom.central_pedidos.domain.repository.OrderRepositoryPort;
import br.com.amcom.central_pedidos.domain.service.ProcessOrderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderIntegrationTest {

    // Vamos subir um container real do PostgreSQL separado para o teste.
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProcessOrderUseCase processOrderUseCase;;

    @Autowired
    private OrderRepositoryPort orderRepositoryPort;

    @Test
    @DisplayName("Deve calcular o total do pedido e persistir com sucesso no banco")
    void shouldCalculateTotalAndPersistOrderSuccessfully() {
        String externalId = "TEST-ID-001";
        OrderItem item1 = new OrderItem("PROD-01", 2, new BigDecimal("50.00")); // 100.00
        OrderItem item2 = new OrderItem("PROD-02", 1, new BigDecimal("35.50")); // 35.50
        Order order = new Order(externalId, List.of(item1, item2));

        processOrderUseCase.execute(order);

        // Aguarda um breve momento devido ao processamento ser assíncrono
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Assert
        Optional<Order> savedOrderOpt = orderRepositoryPort.findByExternalOrderId(externalId);
        assertThat(savedOrderOpt).isPresent();

        Order savedOrder = savedOrderOpt.get();
        assertThat(savedOrder.getTotalValue()).isEqualByComparingTo(new BigDecimal("135.50"));
    }

    @Test
    @DisplayName("Sob estresse de concorrência, não deve permitir a inserção de pedidos duplicados (Garantia de Idempotência)")
    void shouldPreventDuplicateOrdersUnderConcurrentStress() throws InterruptedException {
        // Arrange
        String duplicateExternalId = "CONCURRENT-ID-999";
        int numberOfThreads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);

        // Prepara 10 execuções simultâneas com exatamente o mesmo ID Externo
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Todas as threads travam aqui até o sinal de largada

                    OrderItem item = new OrderItem("PROD-XYZ", 1, new BigDecimal("10.00"));
                    Order order = new Order(duplicateExternalId, List.of(item));

                    processOrderUseCase.execute(order);
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    failureCounter.incrementAndGet();
                }
            });
        }

        // Act
        latch.countDown(); // DIspara todas as threads ao mesmo tempo
        executor.shutdown();
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Optional<Order> savedOrderOpt = orderRepositoryPort.findByExternalOrderId(duplicateExternalId);
        assertThat(savedOrderOpt).isPresent();

        // Valida se o banco manteve o estado consistente contendo exatamente um único registro
        long databaseCount = orderRepositoryPort.existsByExternalOrderId(duplicateExternalId) ? 1 : 0;
        assertThat(databaseCount).isEqualTo(1);

        System.out.println("Threads executadas: " + numberOfThreads);
        System.out.println("Pedidos salvos com sucesso no banco: " + databaseCount);
    }
}