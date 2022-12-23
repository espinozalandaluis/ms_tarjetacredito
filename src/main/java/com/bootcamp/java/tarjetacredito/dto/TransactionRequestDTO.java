package com.bootcamp.java.tarjetacredito.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {
    @JsonIgnore
    private String id;

    private String idProductClient;
    private Integer idTransactionType;
    private Double mont;

    @JsonIgnore
    private Date registrationDate;

    @JsonIgnore
    private String sourceAccountNumber;

    @JsonIgnore
    private Integer OwnAccountNumber;

    @JsonIgnore
    private String destinationAccountNumber;

    @JsonIgnore
    private Integer destinationIdProduct;

    @JsonIgnore
    private Double transactionFee;
}
