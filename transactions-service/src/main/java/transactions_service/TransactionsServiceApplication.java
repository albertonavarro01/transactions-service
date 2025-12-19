package transactions_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
@EnableReactiveMongoRepositories(basePackages = "transactions_service.domain.repository")
public class TransactionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionsServiceApplication.class, args);
	}

}