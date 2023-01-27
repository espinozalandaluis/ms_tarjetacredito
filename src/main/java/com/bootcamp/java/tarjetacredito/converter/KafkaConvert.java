package com.bootcamp.java.tarjetacredito.converter;

import com.bootcamp.java.tarjetacredito.entity.ProductClient;
import com.bootcamp.java.tarjetacredito.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConvert {

    public static com.bootcamp.java.kafka.ProductClientDTO ProductClientEntityToDTOKafka(ProductClient productClient) {

        log.info("ProductClientEntityToDTOKafka ProductClient: {}", productClient.toString());

        return com.bootcamp.java.kafka.ProductClientDTO.builder()
                .id(productClient.getId())
                .idProduct(productClient.getIdProduct())
                .productDescription(productClient.getProductDescription())
                .idProductType(productClient.getIdProductType())
                .productTypeDescription(productClient.getProductTypeDescription())
                .idProductSubType(productClient.getIdProductSubType())
                .productSubTypeDescription(productClient.getProductSubTypeDescription())
                .idClient(productClient.getIdClient())
                .idClientType(productClient.getIdClientType())
                .clientTypeDescription(productClient.getClientTypeDescription())
                .idClientDocumentType(productClient.getIdClientDocumentType())
                .clientDocumentTypeDescription(productClient.getClientDocumentTypeDescription())
                .documentNumber(productClient.getDocumentNumber())
                .fullName(productClient.getFullName())
                .authorizedSigners(productClient.getAuthorizedSigners())
                .creditLimit(productClient.getCreditLimit())
                .balance(productClient.getBalance())
                .debt(productClient.getDebt())
                .maintenanceCost(productClient.getMaintenanceCost())
                .movementLimit(productClient.getMovementLimit())
                .credits(productClient.getCredits())
                .accountNumber(productClient.getAccountNumber())
                .transactionFee(productClient.getTransactionFee())
                .creditCardNumber(productClient.getCreditCardNumber())
                .billingDay(productClient.getBillingDay())
                .billingDate(productClient.getBillingDate())
                .invoiceDebt(productClient.getInvoiceDebt())
                .expiredDebt(productClient.getExpiredDebt())
                .build();
    }

    public static com.bootcamp.java.kafka.TransactionDTO TransactionEntityToDTOKafka(Transaction transaction) {
        return com.bootcamp.java.kafka.TransactionDTO.builder()
                .id(transaction.getId())
                .idProductClient(transaction.getIdProductClient())
                .idTransactionType(transaction.getIdTransactionType())
                .mont(transaction.getMont())
                .registrationDate(transaction.getRegistrationDate())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .ownAccountNumber(transaction.getOwnAccountNumber())
                .transactionFee(transaction.getTransactionFee())
                .destinationIdProduct(transaction.getDestinationIdProduct())
                .build();
    }

}
