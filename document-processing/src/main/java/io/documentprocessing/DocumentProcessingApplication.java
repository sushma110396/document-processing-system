package io.documentprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = "io.documentprocessing",  exclude = {org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class})
@EnableAsync
public class DocumentProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentProcessingApplication.class, args);
	}

}
