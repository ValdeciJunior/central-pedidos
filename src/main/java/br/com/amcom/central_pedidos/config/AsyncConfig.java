package br.com.amcom.central_pedidos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "orderExecutor")
    public Executor orderExecutor() {
        log.info("Inicializando Pool de Threads o para processamento de Pedidos.");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configurando o Pool baseando-se em 150k-200k pedidos por dia
        // 200.000 pedidos / 86.400 segundos (1 dia) = mais ou menos 3 pedidos por segundo.

        // COfigura a quantidade mínima de threads ativas ao mesmo tempo.
        executor.setCorePoolSize(10);

        // Qauntidade máxima de threads caso a fila de espera fique cheia.
        executor.setMaxPoolSize(25);

        // Capacidade da fila de espera. Se as 10 threads estiverem ocupadas,
        // as novas requisições aguardam nesta fila antes de disparar a criação de novas threads até o limite máximo.
        executor.setQueueCapacity(500);

        // Prefixo para identificação nos logs
        executor.setThreadNamePrefix("OrderAsyncThread-");

        // Configurando a política quando as threads chegarem no limite.
        // Em vez de derrubar a requisição lançando uma Exception, é ativada a CallerRunsPolicy.
        // Ela faz com que a thread que chamou execute a tarefa de forma síncrona.
        // Com isso esas política desacelerando a entrada da API temporariamente até que
        // as threads assíncronas fiquem livres novamente
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Configuração para, quando precisar derrubar ou reiniciar a aplicação, para um deploy ou qualquer
        // outro motivo, vai interromper novas requisições na aplicação, segura aplicação em pé até
        // as requisições que já estão no pool de threads em processamento finalizem. Evitando que
        // processos se percam quando a aplicação cair para um deploy ou manutenção.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}