package io.documentprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.documentprocessing")
public class DocumentProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentProcessingApplication.class, args);
	}

}
