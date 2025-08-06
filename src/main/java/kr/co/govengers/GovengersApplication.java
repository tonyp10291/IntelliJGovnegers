package kr.co.govengers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "kr.co.govengers")
@EnableScheduling
public class GovengersApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovengersApplication.class, args);
	}

}