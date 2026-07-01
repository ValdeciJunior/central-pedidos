package br.com.amcom.central_pedidos.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CircuitBreakerCustomConfig {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerConfig.class);
    private final CircuitBreakerRegistry registry;

    @Bean
    public CircuitBreaker externalProductBCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                //Analisa as últimas 20 requisições para tomar decisões.
                .slidingWindowSize(20)

                //Se 50% ou mais das últimas 20 requisições falharem o circuito abre.
                .failureRateThreshold(50.0f)

                //Se 50% ou mais das chamadas demorarem mais que 2 segundos, o circuito abre.
                // Isso impede que lentidões travem as threads.
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))

                // Quando o circuito abrir, ele rejeita chamadas imediatamente por 30 segundos,
                // para o processamento se recuperar sem receber mais chamadas nesse período.
                .waitDurationInOpenState(Duration.ofSeconds(30))

                // Após os 30 segundos acima, o circuito vai para o status meio-aberto
                // e permite 5 chamadas de teste. Se todas passarem, o circuito fecha, voltando assim o fluxo normal.
                // Se alguma falhar, ele reabre.
                .permittedNumberOfCallsInHalfOpenState(5)

                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return registry.circuitBreaker("productBClient", config);
    }

    @PostConstruct
    public void registerCircuitBreakerLogs() {
        registry.circuitBreaker("productBClient").getEventPublisher()
                .onStateTransition(event -> log.warn("Circuit Breaker '{}' alterou o estado de {} para {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }
}
