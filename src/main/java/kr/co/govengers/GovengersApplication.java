package kr.co.govengers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "kr.co.govengers")
public class GovengersApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovengersApplication.class, args);
	}

}