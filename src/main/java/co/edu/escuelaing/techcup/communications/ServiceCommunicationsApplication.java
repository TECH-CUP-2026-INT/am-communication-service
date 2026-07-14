package co.edu.escuelaing.techcup.communications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
public class ServiceCommunicationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceCommunicationsApplication.class, args);
	}

}
