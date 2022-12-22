package com.bootcamp.java.tarjetacredito.dto;

import com.bootcamp.java.tarjetacredito.entity.Transaction;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class ProductClientTransactionDTO2 {
    private ProductClientDTO productClientDTO;
    private TransactionDTO transactionDTO;
}
