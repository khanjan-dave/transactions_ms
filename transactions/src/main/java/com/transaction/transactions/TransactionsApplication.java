package com.transaction.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionsApplication {

	public static void main(String[] args) {
		System.out.println("Transaction Service is starting...");
		SpringApplication.run(TransactionsApplication.class, args);
	}

}
