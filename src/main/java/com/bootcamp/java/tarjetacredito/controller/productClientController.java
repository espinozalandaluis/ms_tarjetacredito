package com.bootcamp.java.tarjetacredito.controller;

import com.bootcamp.java.tarjetacredito.dto.ProductClientDTO;
import com.bootcamp.java.tarjetacredito.dto.ProductClientReportDTO;
import com.bootcamp.java.tarjetacredito.dto.ProductClientRequest;
import com.bootcamp.java.tarjetacredito.dto.ProductClientTransactionDTO;
import com.bootcamp.java.tarjetacredito.service.productClient.ProductClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tarjetacredito")
public class productClientController {

    @Autowired
    private ProductClientService productClientService;

    @PostMapping
    public Mono<ResponseEntity<ProductClientTransactionDTO>> Afiliar(@Valid @RequestBody ProductClientRequest productClientRequest) {
        log.info("create executed {}", productClientRequest);
        return productClientService.create(productClientRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping()
    public Mono<ResponseEntity<Flux<ProductClientDTO>>> getAll() {
        log.info("getAll executed");
        return Mono.just(ResponseEntity.ok()
                .body(productClientService.findAll()));
    }

    @GetMapping("/{documentNumber}")
    public Mono<ResponseEntity<Flux<ProductClientReportDTO>>> getByDocumentNumber(@PathVariable String documentNumber) {
        log.info("getByDocumentNumber executed {}", documentNumber);
        return Mono.just(ResponseEntity.ok()
                .body(productClientService.findByDocumentNumber(documentNumber)));
    }
/*
    @GetMapping("/{accountNumber}")
    public Mono<ResponseEntity<ProductClientDTO>> findByAccountNumber(@PathVariable String accountNumber){
        log.info("findByAccountNumber executed {}", accountNumber);
        return productClientService.findByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

 */

}
