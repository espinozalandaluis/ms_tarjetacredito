package com.bootcamp.java.tarjetacredito.repository;

import com.bootcamp.java.tarjetacredito.entity.ProductClient;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductClientRepository extends ReactiveMongoRepository<ProductClient,String> {
    Mono<ProductClient> findById(String IdProductClient);

    Flux<ProductClient> findByDocumentNumber(String DocumentNumber);

    Mono<ProductClient> findByAccountNumber(String AccountNumber);

    Mono<ProductClient> findByCreditCardNumber(String CreditCardNumber);

}
