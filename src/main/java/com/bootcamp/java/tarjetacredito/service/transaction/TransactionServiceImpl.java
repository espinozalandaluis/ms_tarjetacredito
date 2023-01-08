package com.bootcamp.java.tarjetacredito.service.transaction;

import com.bootcamp.java.tarjetacredito.common.Constantes;
import com.bootcamp.java.tarjetacredito.common.Funciones;
import com.bootcamp.java.tarjetacredito.common.exceptionHandler.FunctionalException;
import com.bootcamp.java.tarjetacredito.converter.ProductClientConvert;
import com.bootcamp.java.tarjetacredito.converter.TransactionConvert;
import com.bootcamp.java.tarjetacredito.dto.ProductClientReportDTO;
import com.bootcamp.java.tarjetacredito.dto.TransactionDTO;
import com.bootcamp.java.tarjetacredito.dto.TransactionRequestDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

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
                    log.info("-------->>>>>>>>>");
                    log.info("Resultado de prodclient");
                    log.info(prodclient.toString());
                    return transactionRepository.findTrxPerMoth(Funciones.GetFirstDayOfCurrentMonth()
                                    , transactionRequestDTO.getIdProductClient()).collectList()
                            .flatMap(trxPerMonth -> {
                                log.info("-------->>>>>>>>>");
                                log.info("Resultado de trxPerMonth");
                                log.info(trxPerMonth.toString());
                                if (transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaEntrada
                                        || transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxTransferenciaSalida
                                        || transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxRetiro
                                        || transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxDeposito)
                                    return Mono.error(() -> new FunctionalException("Error, tipo de transaccion no admitida"));

                                if (transactionRequestDTO.getMont() <= 0.009)
                                    return Mono.error(() -> new FunctionalException("El monto debe ser mayor a 0.00"));

                                if (trxPerMonth.stream().count() >= prodclient.getMovementLimit()) {
                                    log.info("Cobro de comision por pasar limite de movimientos");
                                    transactionRequestDTO.setTransactionFee(prodclient.getTransactionFee());
                                } else {
                                    log.info("NO Cobro de comision porque aun no pasa limite de movimientos");
                                    transactionRequestDTO.setTransactionFee(0.0);
                                }
                                transactionRequestDTO.setSourceAccountNumber(prodclient.getAccountNumber());

                                if (transactionRequestDTO.getIdTransactionType() == Constantes.TipoTrxConsumo &&
                                        ((prodclient.getMovementLimit() - prodclient.getDebt()) >= (transactionRequestDTO.getMont() + transactionRequestDTO.getTransactionFee()))) {
                                    log.info("No tiene fondos suficientes para realizar la operacion");
                                    return Mono.error(() -> new FunctionalException("No tiene fondos suficientes para realizar la operacion"));
                                }
                                log.info("Trx Tarjeta de Credito Deposito o Consumo");
                                transactionRequestDTO.setOwnAccountNumber(1); //A mi misma cuenta
                                /*Nuevas lineas */
                                transactionRequestDTO.setDestinationAccountNumber(null);
                                transactionRequestDTO.setDestinationIdProduct(Constantes.ProductoActivoTarjetaCredito);

                                Transaction trx = transactionConverter.DTOtoEntity(transactionRequestDTO);
                                return transactionRepository.save(trx)
                                        .flatMap(t -> {
                                            prodclient.setDebt(CalculateDebt(prodclient.getDebt(),
                                                    trx.getMont(),
                                                    trx.getIdTransactionType(),
                                                    trx.getTransactionFee()));

                                            return productClientRepository.save(prodclient)
                                                    .flatMap(x -> {
                                                        log.info("Actualizado el Debt");
                                                        return transactionRepository.findById(t.getId())
                                                                .map(TransactionConvert::EntityToDTO);
                                                    });
                                        });
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
                                        Constantes.TipoTrxTransferenciaEntrada, 0.0));

                                return productClientRepository.save(productClient)
                                        .flatMap(prdcli -> {
                                            return Mono.just(transactionConverter.EntityToDTO(trx));
                                        });
                            })
                            .switchIfEmpty(Mono.error(() -> new FunctionalException("Error, No se encontro producto")));
                })
                .switchIfEmpty(Mono.error(() -> new FunctionalException("Error al registrar la trx de entrada")));
    }


    public Mono<TransactionDTO> registerTrxEntrada(ProductClient productClient, Transaction transactionOrigen) {

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

    public Mono<TransactionDTO> registerTrxEntradaDebt(ProductClient productClient, Transaction transactionOrigen) {

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
    public Flux<ProductClientReportDTO> findByDocumentNumber(String documentNumber) {
        return productClientRepository.findByDocumentNumber(documentNumber)
                .flatMap(prodCli -> {
                    var data = transactionRepository.findByIdProductClient(prodCli.getId())
                            .collectList()
                            .map(transactions -> ProductClientReportDTO.from(prodCli, transactions));
                    return data;
                }).switchIfEmpty(Mono.error(() -> new FunctionalException("No se encontraron registros de productos afiliados")));
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
        if (transactionType.equals(Constantes.TipoTrxRetiro)) //retiro
            newBalance = ActualBalance - amountTrx - trxFee;

        if (transactionType.equals(Constantes.TipoTrxDeposito)) //deposito
            newBalance = ActualBalance + amountTrx - trxFee;

        if (transactionType.equals(Constantes.TipoTrxConsumo)) //deposito
            newBalance = ActualBalance - amountTrx - trxFee;

        if (transactionType.equals(Constantes.TipoTrxTransferenciaSalida)) //Transferencia a cuenta externa
            newBalance = ActualBalance - amountTrx - trxFee;

        if (transactionType.equals(Constantes.TipoTrxTransferenciaEntrada)) //Transferencia a cuenta externa
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

        if (transactionType.equals(Constantes.TipoTrxDeposito)) //deposito
            newDebt = ActualDebt - debtTrx + trxFee;

        if (transactionType.equals(Constantes.TipoTrxPago)) //pago
            newDebt = ActualDebt - debtTrx + trxFee;

        if (transactionType.equals(Constantes.TipoTrxRetiro)) //retiro
            newDebt = ActualDebt + debtTrx + trxFee;

        if (transactionType.equals(Constantes.TipoTrxConsumo)) //consumo
            newDebt = ActualDebt + debtTrx + trxFee;

        if (transactionType.equals(Constantes.TipoTrxTransferenciaSalida)) //Transferencia a cuenta externa
            newDebt = ActualDebt + debtTrx + trxFee;

        if (transactionType.equals(Constantes.TipoTrxTransferenciaEntrada)) //Transferencia a cuenta externa
            newDebt = ActualDebt - debtTrx + trxFee;

        BigDecimal bd = new BigDecimal(newDebt).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
