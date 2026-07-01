package br.com.amcom.central_pedidos.domain.repository;

import br.com.amcom.central_pedidos.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByExternalOrderId(String externalOrderId);
    Optional<OrderEntity> findByExternalOrderId(String externalOrderId);
}