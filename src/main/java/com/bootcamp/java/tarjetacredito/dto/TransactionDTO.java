package com.bootcamp.java.tarjetacredito.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private String id;
    private String idProductClient;
    private Integer idTransactionType;
    private Double mont;
    private Date registrationDate;
    private Double transactionFee;
    private String destinationAccountNumber;
    private String sourceAccountNumber;
    private Integer ownAccountNumber;
    private Integer destinationIdProduct;
}
