package newblogproject.example.newproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NewprojectApplication {

	public static void main(String[] args) {

		SpringApplication.run(NewprojectApplication.class, args);
	}

}
