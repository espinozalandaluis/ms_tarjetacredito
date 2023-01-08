package com.bootcamp.java.tarjetacredito.service.transaction;

import com.bootcamp.java.tarjetacredito.dto.ProductClientReportDTO;
import com.bootcamp.java.tarjetacredito.dto.TransactionDTO;
import com.bootcamp.java.tarjetacredito.dto.TransactionRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    //public Mono<TransactionDTO> registerTrx(TransactionDTO transactionDTO);

    public Mono<TransactionDTO> register(TransactionRequestDTO transactionRequestDTO);

    public Mono<TransactionDTO> registerTrxEntradaExterna(TransactionDTO transactionDTO,
                                                          String IdProductClient);

    public Flux<ProductClientReportDTO> findByDocumentNumber(String documentNumber);

    public Flux<TransactionDTO> findAll();

}
