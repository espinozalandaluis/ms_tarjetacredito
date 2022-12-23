package com.bootcamp.java.tarjetacredito.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ProductClientRequest {
    private Integer idProduct;
    private String documentNumber;
    //private Integer movementLimit;
    //private Double depositAmount;
    private Double creditLimit;
    private String accountNumber ;
    private String creditCardNumber ;
    private Integer billingDay;
}
