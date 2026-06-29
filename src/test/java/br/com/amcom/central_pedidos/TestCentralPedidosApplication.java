package br.com.amcom.central_pedidos;

import org.springframework.boot.SpringApplication;

public class TestCentralPedidosApplication {

	public static void main(String[] args) {
		SpringApplication.from(CentralPedidosApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
