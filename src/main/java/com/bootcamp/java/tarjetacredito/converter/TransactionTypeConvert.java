package com.bootcamp.java.tarjetacredito.converter;

import com.bootcamp.java.tarjetacredito.dto.TransactionTypeDTO;
import com.bootcamp.java.tarjetacredito.entity.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class TransactionTypeConvert {
    public static TransactionTypeDTO EntityToDTO(TransactionType transactionType) {
        return TransactionTypeDTO.builder()
                .id(transactionType.getId())
                .idTransactionType(transactionType.getIdTransactionType())
                .description(transactionType.getDescription())
                .build();
    }
}
