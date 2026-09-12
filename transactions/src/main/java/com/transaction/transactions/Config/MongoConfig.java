package com.transaction.transactions.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableMongoRepositories(basePackages = "com.transaction.transactions.Repository.Mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Override
    protected String getDatabaseName() {
        return "transaction_logs";
    }
    
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
    
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}