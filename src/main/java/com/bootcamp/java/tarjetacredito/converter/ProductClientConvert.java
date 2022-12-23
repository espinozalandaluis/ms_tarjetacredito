package com.bootcamp.java.tarjetacredito.converter;

import com.bootcamp.java.tarjetacredito.common.Constantes;
import com.bootcamp.java.tarjetacredito.dto.ProductClientDTO;
import com.bootcamp.java.tarjetacredito.dto.ProductClientRequest;
import com.bootcamp.java.tarjetacredito.dto.webClientDTO.ClientResponseDTO;
import com.bootcamp.java.tarjetacredito.dto.webClientDTO.ProductResponseDTO;
import com.bootcamp.java.tarjetacredito.entity.ProductClient;
import org.springframework.stereotype.Component;

@Component
public class ProductClientConvert {
    public static ProductClientDTO EntityToDTO(ProductClient productClient) {
        return ProductClientDTO.builder()
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

    public static ProductClient DTOToEntity2(ProductClientRequest productClientRequest,
                                            ProductResponseDTO productResponseDTO,
                                            ClientResponseDTO clientResponseDTO) {
        return ProductClient.builder()
                .idProduct(productResponseDTO.getIdProduct())
                .productDescription(productResponseDTO.getDescription())
                .idProductType(productResponseDTO.getProductTypeDTO().getIdProductType())
                .productTypeDescription(productResponseDTO.getProductTypeDTO().getDescription())
                .idProductSubType(productResponseDTO.getProductSubTypeDTO().getIdProductSubType())
                .productSubTypeDescription(productResponseDTO.getProductSubTypeDTO().getDescription())
                .idClient(clientResponseDTO.getIdClient())
                .idClientType(clientResponseDTO.getClientTypeDTO().getIdClientType())
                .clientTypeDescription(clientResponseDTO.getClientTypeDTO().getDescription())
                .idClientDocumentType(clientResponseDTO.getClientDocumentTypeDTO().getIdClientDocumentType())
                .clientDocumentTypeDescription(clientResponseDTO.getClientDocumentTypeDTO().getDescription())
                .documentNumber(clientResponseDTO.getDocumentNumber())
                .fullName(clientResponseDTO.getFullName())
                //.authorizedSigners(productClientDTO.getAuthorizedSigners())
                .creditLimit(productClientRequest.getCreditLimit())
                //.balance(productClientRequest.getDepositAmount())
                .debt(0.0)
                .maintenanceCost(Constantes.MaintenanceCost)
                .movementLimit(productResponseDTO.getProductSubTypeDTO().getMovementLimit())
                //.credits(productClientDTO.getCredits())
                .accountNumber(productClientRequest.getAccountNumber())
                .transactionFee(productResponseDTO.getTransactionFee())
                .creditCardNumber(productClientRequest.getCreditCardNumber())
                .billingDay(productClientRequest.getBillingDay())
                //.billingDate()
                .invoiceDebt(0.0)
                .expiredDebt(0.0)
                .build();
    }

    public static ProductClient DTOToEntity(ProductClientDTO productClientDTO) {
        return ProductClient.builder()
                .idProduct(productClientDTO.getIdProduct())
                .productDescription(productClientDTO.getProductDescription())
                .idProductType(productClientDTO.getIdProductType())
                .productTypeDescription(productClientDTO.getProductTypeDescription())
                .idProductSubType(productClientDTO.getIdProductSubType())
                .productSubTypeDescription(productClientDTO.getProductSubTypeDescription())
                .idClient(productClientDTO.getIdClient())
                .idClientType(productClientDTO.getIdClientType())
                .clientTypeDescription(productClientDTO.getClientTypeDescription())
                .idClientDocumentType(productClientDTO.getIdClientDocumentType())
                .clientDocumentTypeDescription(productClientDTO.getClientDocumentTypeDescription())
                .documentNumber(productClientDTO.getDocumentNumber())
                .fullName(productClientDTO.getFullName())
                .authorizedSigners(productClientDTO.getAuthorizedSigners())
                .creditLimit(productClientDTO.getCreditLimit())
                .balance(productClientDTO.getBalance())
                .debt(productClientDTO.getDebt())
                .maintenanceCost(productClientDTO.getMaintenanceCost())
                .movementLimit(productClientDTO.getMovementLimit())
                .credits(productClientDTO.getCredits())
                .accountNumber(productClientDTO.getAccountNumber())
                .transactionFee(productClientDTO.getTransactionFee())
                .creditCardNumber(productClientDTO.getCreditCardNumber())
                .billingDay(productClientDTO.getBillingDay())
                .billingDate(productClientDTO.getBillingDate())
                .invoiceDebt(productClientDTO.getInvoiceDebt())
                .expiredDebt(productClientDTO.getExpiredDebt())
                .build();
    }


}
