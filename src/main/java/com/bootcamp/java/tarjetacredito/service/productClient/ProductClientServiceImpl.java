package com.bootcamp.java.tarjetacredito.service.productClient;

import com.bootcamp.java.tarjetacredito.common.Constantes;
import com.bootcamp.java.tarjetacredito.common.exceptionHandler.FunctionalException;
import com.bootcamp.java.tarjetacredito.converter.ProductClientConvert;
import com.bootcamp.java.tarjetacredito.converter.TransactionConvert;
import com.bootcamp.java.tarjetacredito.dto.ProductClientDTO;
import com.bootcamp.java.tarjetacredito.dto.ProductClientReportDTO;
import com.bootcamp.java.tarjetacredito.dto.ProductClientRequest;
import com.bootcamp.java.tarjetacredito.dto.ProductClientTransactionDTO;
import com.bootcamp.java.tarjetacredito.entity.ProductClient;
import com.bootcamp.java.tarjetacredito.repository.ProductClientRepository;
import com.bootcamp.java.tarjetacredito.repository.TransactionRepository;
import com.bootcamp.java.tarjetacredito.service.transaction.TransactionService;
import com.bootcamp.java.tarjetacredito.service.webClients.Clients.WcClientsService;
import com.bootcamp.java.tarjetacredito.service.webClients.Products.WcProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductClientServiceImpl implements ProductClientService {

    @Autowired
    private ProductClientRepository productClientRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    TransactionService transactionService;

    @Autowired
    TransactionConvert transactionConvert;

    @Autowired
    ProductClientConvert productClientConvert;

    @Autowired
    WcClientsService wcClientsService;

    @Autowired
    WcProductsService wcProductsService;

    @Override
    public Flux<ProductClientDTO> findAll() {
        log.debug("findAll executing");
        Flux<ProductClientDTO> dataProductClientDTO = productClientRepository.findAll()
                .map(ProductClientConvert::EntityToDTO);
        return dataProductClientDTO;
    }

    @Override
    public Flux<ProductClientReportDTO> findByDocumentNumber(String DocumentNumber) {
        /*
        List<ProductClientReportDTO> productClientReports = new ArrayList<>();
        var productClients = productClientRepository.findByDocumentNumber(DocumentNumber)
                .flatMap(prodCli -> {
                    return Flux.just(prodCli);
                }).collectList();
        productClients.forEach((prodCli) -> {
            var transactions = transactionRepository.findByIdProductClient(prodCli.getId())
                    .flatMap(transaction -> {
                        return Flux.just(transaction);
                    }).collectList().block();
            var data = ProductClientReportDTO.builder()
                    .productClient(prodCli)
                    .transactionList(transactions)
                    .build();
            productClientReports.add(data);
        });
        return productClientReports;
         */
        return productClientRepository.findByDocumentNumber(DocumentNumber)
                .flatMap(prodCli -> {
                    var data = transactionRepository.findByIdProductClient(prodCli.getId())
                            .collectList()
                            .map(transactions -> ProductClientReportDTO.from(prodCli, transactions));
                    return data;
                });
    }

    @Override
    public Mono<ProductClientDTO> findByAccountNumber(String AccountNumber) {
        return productClientRepository.findByAccountNumber(AccountNumber)
                .map(ProductClientConvert::EntityToDTO)
                .switchIfEmpty(Mono.error(() -> new FunctionalException("No se encontraron registros")));
    }

    @Override
    public Mono<ProductClientTransactionDTO> create(ProductClientRequest productClientRequest) {
        log.info("Procedimiento para crear una nueva afiliacion");
        log.info("======================>>>>>>>>>>>>>>>>>>>>>>>");
        log.info(productClientRequest.toString());

        if (productClientRequest.getIdProduct() != Constantes.ProductoActivoTarjetaCredito)
            return Mono.error(new FunctionalException("El producto no es Tarjeta de credito"));

        return productClientRepository.findByDocumentNumber(productClientRequest.getDocumentNumber()).collectList()
                .flatMap(valProdCli -> {
                    return wcClientsService.findByDocumentNumber(productClientRequest.getDocumentNumber())
                            .flatMap(cliente -> {
                                log.info("Resultado de llamar al servicio de Clients: {}", cliente.toString());
                                if (!cliente.getClientTypeDTO().getIdClientType().equals(Constantes.ClientTypePersonal))
                                    return Mono.error(new FunctionalException("El cliente no es tipo Personal"));

                                return wcProductsService.findById(productClientRequest.getIdProduct())
                                        .flatMap(producto -> {
                                            log.info("Resultado de llamar al servicio de Products: {}", producto.toString());

                                            if (!producto.getProductTypeDTO().getIdProductType().equals(Constantes.ProductTypeActivo))
                                                return Mono.error(new FunctionalException("El producto no es Tipo Activo"));
                                            if (!producto.getProductSubTypeDTO().getIdProductSubType().equals(Constantes.ProductSubTypeActivoTarjetaCredito))
                                                return Mono.error(new FunctionalException("El producto no es SubTipo Tarjeta Credito"));

                                            return productClientRepository.findByAccountNumber(productClientRequest.getAccountNumber()).flux().collectList()
                                                    .flatMap(y -> {
                                                        if (y.stream().count() > 0)
                                                            return Mono.error(new FunctionalException("Existe un AccountNumber"));

                                                        return productClientRepository.findByCreditCardNumber(productClientRequest.getCreditCardNumber()).flux().collectList()
                                                                .flatMap(z -> {
                                                                    if (z.stream().count() > 0)
                                                                        return Mono.error(new FunctionalException("Existe un CreditCardNumber"));

                                                                    ProductClient prdCli = productClientConvert.DTOToEntity2(productClientRequest,
                                                                            producto, cliente);

                                                                    return productClientRepository.save(prdCli)
                                                                            .flatMap(productocliente -> {
                                                                                log.info("Resultado de guardar ProductClient: {}", productocliente.toString());
                                                                                return Mono.just(ProductClientTransactionDTO.builder()
                                                                                        .productClientDTO(productClientConvert.EntityToDTO(productocliente))
                                                                                        .build());
                                                                            })
                                                                            .switchIfEmpty(Mono.error(() -> new FunctionalException("Ocurrio un error al guardar el ProductClient")));
                                                                });
                                                    });
                                        })
                                        .switchIfEmpty(Mono.error(() -> new FunctionalException("Ocurrio un error al consultar el servicio de producto")));
                            })
                            .switchIfEmpty(Mono.error(() -> new FunctionalException("Ocurrio un error al consultar el servicio de cliente")));
                });
    }
}
