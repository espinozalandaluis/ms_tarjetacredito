package com.bootcamp.java.tarjetacredito.service.transaction;

import com.bootcamp.java.tarjetacredito.common.Constantes;
import com.bootcamp.java.tarjetacredito.common.Funciones;
import com.bootcamp.java.tarjetacredito.common.exceptionHandler.FunctionalException;
import com.bootcamp.java.tarjetacredito.converter.ProductClientConvert;
import com.bootcamp.java.tarjetacredito.converter.TransactionConvert;
import com.bootcamp.java.tarjetacredito.dto.*;
import com.bootcamp.java.tarjetacredito.entity.ProductClient;
import com.bootcamp.java.tarjetacredito.entity.Transaction;
import com.bootcamp.java.tarjetacredito.repository.ProductClientRepository;
import com.bootcamp.java.tarjetacredito.repository.TransactionRepository;
import com.bootcamp.java.tarjetacredito.service.webClients.pasivoCuentaCorriente.WcPasivoCuentaCorrienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService{

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductClientRepository productClientRepository;

    @Autowired
    TransactionConvert transactionConverter;

    @Autowired
    ProductClientConvert productClientConvert;

    @Autowired
    WcPasivoCuentaCorrienteService wcPasivoCuentaCorrienteService;

    @Override
    public Mono<TransactionDTO> register(TransactionRequestDTO transactionRequestDTO) {
        return productClientRepository.findById(transactionRequestDTO.getIdProductClient())
                .flatMap(prodclient -> {
                    return transactionRepository.findTrxPerMoth(Funciones.GetFirstDayOfCurrentMonth()
                                    ,transactionRequestDTO.getIdProductClient()).collectList()
                            .flatMap(trxPerMonth -> {

                                if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaEntrada
                                        || transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxPago)
                                    return Mono.error(() -> new FunctionalException("Error, tipo de transaccion no admitida"));

                                if(transactionRequestDTO.getMont() <= 0.009)
                                    return Mono.error(() -> new FunctionalException("El monto debe ser mayor a 0.00"));

                                if (trxPerMonth.stream().count() >= prodclient.getMovementLimit()) {
                                    log.info("Cobro de comision por pasar limite de movimientos");
                                    transactionRequestDTO.setTransactionFee(prodclient.getTransactionFee());
                                }
                                else{
                                    log.info("NO Cobro de comision porque aun no pasa limite de movimientos");
                                    transactionRequestDTO.setTransactionFee(0.0);
                                }
                                transactionRequestDTO.setSourceAccountNumber(prodclient.getAccountNumber());

                                switch (transactionRequestDTO.getDestinationIdProduct()){
                                    case 1: {
                                        if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida){
                                            if(prodclient.getBalance() < (transactionRequestDTO.getMont() + transactionRequestDTO.getTransactionFee()))
                                                return Mono.error(() -> new FunctionalException("No tiene fondos suficientes para realizar la operacion"));
                                            return productClientRepository.findByAccountNumber(transactionRequestDTO.getDestinationAccountNumber())
                                                    .flatMap(xy -> {
                                                        if(xy.getAccountNumber().equals(prodclient.getAccountNumber()))
                                                            return Mono.error(() -> new FunctionalException("No puede realizar una transferencia a su misma cuenta de origen"));
                                                        if(xy.getDocumentNumber().equals(prodclient.getDocumentNumber())) {
                                                            transactionRequestDTO.setOwnAccountNumber(1); //La cuenta de destino le pertenece al mismo cliente
                                                        }else{
                                                            transactionRequestDTO.setOwnAccountNumber(0); //La cuenta de destino NO le pertenece al mismo cliente
                                                        }

                                                        Transaction trx = transactionConverter.DTOtoEntity(transactionRequestDTO);
                                                        return transactionRepository.save(trx)
                                                                .flatMap(t->{
                                                                    prodclient.setBalance(CalculateBalance(prodclient.getBalance(),
                                                                            trx.getMont(),
                                                                            trx.getIdTransactionType(),
                                                                            trx.getTransactionFee()));

                                                                    return productClientRepository.save(prodclient)
                                                                            .flatMap(x-> {
                                                                                log.info("Actualizado el balance");
                                                                                //AQUI AGREGAR LLAMADO AL API DE TRX
                                                                                return registerTrxEntradaDebt(xy,trx)
                                                                                        .flatMap(xyz -> {
                                                                                            return Mono.just(transactionConverter.EntityToDTO(t));
                                                                                        });
                                                                            });
                                                                });
                                                    })
                                                    .switchIfEmpty(Mono.error(() -> new FunctionalException("La cuenta de destino es existe")));
                                        }else {
                                            log.info("Trx Pasivo Ahorro Otro");
                                            return Mono.error(() -> new FunctionalException("IdTransactionType no identificado"));
                                        }
                                    }
                                    case 2: {
                                        return wcPasivoCuentaCorrienteService.findByAccountNumber(transactionRequestDTO.getDestinationAccountNumber())
                                                .flatMap(xy -> {
                                                    if(xy.getDocumentNumber().equals(prodclient.getDocumentNumber())) {
                                                        transactionRequestDTO.setOwnAccountNumber(1); //La cuenta de destino le pertenece al mismo cliente
                                                    }else{
                                                        transactionRequestDTO.setOwnAccountNumber(0); //La cuenta de destino NO le pertenece al mismo cliente
                                                    }

                                                    Transaction trx = transactionConverter.DTOtoEntity(transactionRequestDTO);
                                                    return transactionRepository.save(trx)
                                                            .flatMap(t->{
                                                                prodclient.setBalance(CalculateBalance(prodclient.getBalance(),
                                                                        trx.getMont(),
                                                                        trx.getIdTransactionType(),
                                                                        trx.getTransactionFee()));

                                                                return productClientRepository.save(prodclient)
                                                                        .flatMap(x-> {
                                                                            log.info("Actualizado el balance");
                                                                            //AQUI AGREGAR LLAMADO AL API DE TRX
                                                                            return wcPasivoCuentaCorrienteService.registerTrxEntradaExterna(transactionConverter.EntityToDTO(trx),
                                                                                    xy.getId())
                                                                                    .flatMap(z -> {
                                                                                        return Mono.just(transactionConverter.EntityToDTO(t));
                                                                                    })
                                                                                    .switchIfEmpty(Mono.error(() -> new FunctionalException("Error al registrar la trx en el API PasivoCuentaCorrienteService")));
                                                                        });
                                                            });
                                                })
                                                .switchIfEmpty(Mono.error(() -> new FunctionalException("La cuenta de destino es existe")));
                                    }
                                    case 4: {
                                        if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxDeposito ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxRetiro ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxConsumo ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxPago ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida){

                                            if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxRetiro ||
                                                    transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxConsumo ||
                                                    transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida)
                                                if((prodclient.getCreditLimit() - prodclient.getDebt()) < (transactionRequestDTO.getMont() + transactionRequestDTO.getTransactionFee()))
                                                    return Mono.error(() -> new FunctionalException("No tiene fondos suficientes para realizar la operacion"));

                                            return productClientRepository.findByAccountNumber(transactionRequestDTO.getDestinationAccountNumber())
                                                    .flatMap(xy -> {
                                                        if(xy.getAccountNumber().equals(prodclient.getAccountNumber()) && transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida)
                                                            return Mono.error(() -> new FunctionalException("No puede realizar una transferencia a su misma cuenta de origen"));
                                                        if(xy.getDocumentNumber().equals(prodclient.getDocumentNumber())) {
                                                            transactionRequestDTO.setOwnAccountNumber(1); //La cuenta de destino le pertenece al mismo cliente
                                                        }else{
                                                            transactionRequestDTO.setOwnAccountNumber(0); //La cuenta de destino NO le pertenece al mismo cliente
                                                        }

                                                        Transaction trx = transactionConverter.DTOtoEntity(transactionRequestDTO);
                                                        return transactionRepository.save(trx)
                                                                .flatMap(t->{
                                                                    prodclient.setDebt(CalculateDebt(prodclient.getDebt(),
                                                                            trx.getMont(),
                                                                            trx.getIdTransactionType(),
                                                                            trx.getTransactionFee()));

                                                                    return productClientRepository.save(prodclient)
                                                                            .flatMap(x-> {
                                                                                log.info("Actualizado el balance");
                                                                                //AQUI AGREGAR LLAMADO AL API DE TRX

                                                                                if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida){
                                                                                    return registerTrxEntradaDebt(xy,trx)
                                                                                            .flatMap(xyz -> {
                                                                                                return Mono.just(transactionConverter.EntityToDTO(t));
                                                                                            });
                                                                                }
                                                                                else
                                                                                    return Mono.just(transactionConverter.EntityToDTO(t));
                                                                            });
                                                                });
                                                    })
                                                    .switchIfEmpty(Mono.error(() -> new FunctionalException("La cuenta de destino es existe")));
                                        }else {
                                            log.info("Trx Activo Personal Otro");
                                            return Mono.error(() -> new FunctionalException("IdTransactionType no identificado"));
                                        }
                                    }
                                    case 6: {
                                        if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxDeposito ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxRetiro ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxConsumo ||
                                                transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxPago){

                                            if(transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxRetiro ||
                                                    transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxConsumo)
                                                if((prodclient.getCreditLimit() - prodclient.getDebt()) < (transactionRequestDTO.getMont() + transactionRequestDTO.getTransactionFee()))
                                                    return Mono.error(() -> new FunctionalException("No tiene fondos suficientes para realizar la operacion"));

                                            return productClientRepository.findByAccountNumber(transactionRequestDTO.getDestinationAccountNumber())
                                                    .flatMap(xy -> {
                                                        transactionRequestDTO.setOwnAccountNumber(0);

                                                        Transaction trx = transactionConverter.DTOtoEntity(transactionRequestDTO);
                                                        return transactionRepository.save(trx)
                                                                .flatMap(t->{
                                                                    prodclient.setDebt(CalculateDebt(prodclient.getDebt(),
                                                                            trx.getMont(),
                                                                            trx.getIdTransactionType(),
                                                                            trx.getTransactionFee()));

                                                                    return productClientRepository.save(prodclient)
                                                                            .flatMap(x-> {
                                                                                log.info("Actualizado el balance");
                                                                                return Mono.just(transactionConverter.EntityToDTO(t));
                                                                            });
                                                                });
                                                    })
                                                    .switchIfEmpty(Mono.error(() -> new FunctionalException("La cuenta de destino es existe")));
                                        }else {
                                            log.info("Trx Activo Personal Otro");
                                            return Mono.error(() -> new FunctionalException("IdTransactionType no identificado"));
                                        }
                                    }
                                    default: {
                                        return Mono.error(() -> new FunctionalException("El destinationIdProduct especificado no a sido implementado"));
                                    }
                                }

                            });
                })
                .switchIfEmpty(Mono.error(() -> new FunctionalException("No se encontro el producto")));
    }

    @Override
    public Mono<TransactionDTO> registerTrxEntradaExterna(TransactionDTO transactionDTO, String IdProductClient) {

        return transactionRepository.save(transactionConverter.DTOtoEntity(transactionDTO))
                .flatMap(trx -> {
                    return productClientRepository.findById(IdProductClient)
                            .flatMap(productClient -> {
                                productClient.setBalance(CalculateBalance(productClient.getBalance(),
                                        transactionDTO.getMont(),
                                        Constantes.TipoTrxTransferenciaEntrada,0.0));

                                return productClientRepository.save(productClient)
                                        .flatMap(prdcli -> {
                                            return Mono.just(transactionConverter.EntityToDTO(trx));
                                        });
                            })
                            .switchIfEmpty(Mono.error(() -> new FunctionalException("Error, No se encontro producto")));
                })
                .switchIfEmpty(Mono.error(() -> new FunctionalException("Error al registrar la trx de entrada")));
    }


    public Mono<TransactionDTO> registerTrxEntrada(ProductClient productClient, Transaction transactionOrigen){

        transactionOrigen.setId(null);
        transactionOrigen.setIdTransactionType(Constantes.TipoTrxTransferenciaEntrada);
        transactionOrigen.setTransactionFee(0.00);

        return transactionRepository.save(transactionOrigen)
                .flatMap(x -> {
                    productClient.setBalance(CalculateBalance(productClient.getBalance(),
                            transactionOrigen.getMont(),
                            transactionOrigen.getIdTransactionType(),
                            transactionOrigen.getTransactionFee()));
                    return productClientRepository.save(productClient)
                            .flatMap(pc -> {
                                return Mono.just(transactionConverter.EntityToDTO(x));
                            });
                });
    }

    public Mono<TransactionDTO> registerTrxEntradaDebt(ProductClient productClient, Transaction transactionOrigen){

        String newDestinationAccountNumber = transactionOrigen.getSourceAccountNumber();
        String newSourceAccountNumber = transactionOrigen.getDestinationAccountNumber();
        transactionOrigen.setId(null);
        transactionOrigen.setIdProductClient(productClient.getId());
        transactionOrigen.setIdTransactionType(Constantes.TipoTrxTransferenciaEntrada);
        transactionOrigen.setDestinationAccountNumber(newDestinationAccountNumber);
        transactionOrigen.setSourceAccountNumber(newSourceAccountNumber);
        transactionOrigen.setTransactionFee(0.00);
        return transactionRepository.save(transactionOrigen)
                .flatMap(x -> {
                    productClient.setDebt(CalculateDebt(productClient.getDebt(),
                            transactionOrigen.getMont(),
                            transactionOrigen.getIdTransactionType(),
                            transactionOrigen.getTransactionFee()));
                    return productClientRepository.save(productClient)
                            .flatMap(pc -> {
                                return Mono.just(transactionConverter.EntityToDTO(x));
                            });
                });
    }

    @Override
    public Flux<ProductClientTransactionDTO2> findByDocumentNumber(String documentNumber) {
        return productClientRepository.findByDocumentNumber(documentNumber)
                .flatMap(productocliente -> {
                    log.info("ProductClientTransactionDTO {}", productocliente);

                    return transactionRepository.findByIdProductClient(productocliente.getId())
                            .flatMap(trx -> {
                                return Flux.just(ProductClientTransactionDTO2.builder()
                                        .productClientDTO(productClientConvert.EntityToDTO(productocliente))
                                        .transactionDTO(transactionConverter.EntityToDTO(trx))
                                        .build());
                            });
                })
                .switchIfEmpty(Mono.error(() -> new FunctionalException("No se encontraron registros de productos afiliados")));
    }

    @Override
    public Flux<TransactionDTO> findAll() {
        log.debug("findAll executing");
        Flux<TransactionDTO> dataTransactionDTO = transactionRepository.findAll()
                .map(TransactionConvert::EntityToDTO);
        return dataTransactionDTO;
    }

    private Double CalculateBalance(Double ActualBalance, Double amountTrx, Integer transactionType, Double trxFee) {
        Double newBalance = 0.00;
        if(transactionType.equals(Constantes.TipoTrxRetiro)) //retiro
            newBalance = ActualBalance - amountTrx - trxFee;

        if(transactionType.equals(Constantes.TipoTrxDeposito)) //deposito
            newBalance = ActualBalance + amountTrx - trxFee;

        if(transactionType.equals(Constantes.TipoTrxConsumo)) //deposito
            newBalance = ActualBalance - amountTrx - trxFee;

        if(transactionType.equals(Constantes.TipoTrxTransferenciaSalida)) //Transferencia a cuenta externa
            newBalance = ActualBalance - amountTrx - trxFee;

        if(transactionType.equals(Constantes.TipoTrxTransferenciaEntrada)) //Transferencia a cuenta externa
            newBalance = ActualBalance + amountTrx - trxFee;

        BigDecimal bd = new BigDecimal(newBalance).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private Double CalculateDebt(Double ActualDebt, Double debtTrx, Integer transactionType, Double trxFee) {
        Double newDebt = 0.00;
        log.info("---->>>>");
        log.info(ActualDebt.toString());
        log.info(debtTrx.toString());
        log.info(transactionType.toString());
        log.info(trxFee.toString());

        if(transactionType.equals(Constantes.TipoTrxDeposito)) //deposito
            newDebt = ActualDebt - debtTrx + trxFee;

        if(transactionType.equals(Constantes.TipoTrxPago)) //pago
            newDebt = ActualDebt - debtTrx + trxFee;

        if(transactionType.equals(Constantes.TipoTrxRetiro)) //retiro
            newDebt = ActualDebt + debtTrx + trxFee;

        if(transactionType.equals(Constantes.TipoTrxConsumo)) //consumo
            newDebt = ActualDebt + debtTrx + trxFee;

        if(transactionType.equals(Constantes.TipoTrxTransferenciaSalida)) //Transferencia a cuenta externa
            newDebt = ActualDebt + debtTrx + trxFee;

        if(transactionType.equals(Constantes.TipoTrxTransferenciaEntrada)) //Transferencia a cuenta externa
            newDebt = ActualDebt - debtTrx + trxFee;

        BigDecimal bd = new BigDecimal(newDebt).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }



}
