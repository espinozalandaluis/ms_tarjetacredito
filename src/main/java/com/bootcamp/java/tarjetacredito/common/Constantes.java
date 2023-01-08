package com.bootcamp.java.tarjetacredito.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@AllArgsConstructor
@Component
public class Constantes {
    public static final Integer ClientTypePersonal = 1;

    public static final Integer ClientTypePersonalVIP = 3;

    public static final Integer ProductTypePasivo = 1;

    public static final Integer ProductSubTypePasivo = 1;
    public static final Integer ProductTypeActivo = 2;

    public static final Integer ProductSubTypeActivoCreditoPersonal = 4;

    public static final Integer ProductSubTypeActivoCreditoEmpresarial = 5;

    public static final Integer ProductSubTypeActivoTarjetaCredito = 6;

    public static final Integer TransactionTypeDeposito =    1;

    public static final Double MaintenanceCost = 0.0;

    public static final Integer TransferenciasPropiaCuenta = 1;
    public static final Integer TipoTrxDeposito = 1;
    public static final Integer TipoTrxRetiro = 2;
    public static final Integer TipoTrxPago = 3;
    public static final Integer TipoTrxConsumo = 4;
    public static final Integer TipoTrxTransferenciaSalida = 5;
    public static final Integer TipoTrxTransferenciaEntrada = 6;
    public static final Integer ProductoPasivoAhorros = 1;

    public static final Integer ProductoPasivoCuentaCorriente = 2;

    public static final Integer ProductoPasivoPlazoFijo = 3;

    public static final Integer ProductoActivoPersonal = 4;

    public static final Integer ProductoActivoEmpresarial = 5;

    public static final Integer ProductoActivoTarjetaCredito = 6;

    public static final long TimeOutWebClients = 10_000;

    public static String webClientUriMSCliente;

    @Value("${SERVER_GATEWAY:localhost}")
    public void setwebClientUriMSCliente(String SERVER_GATEWAY) {
        Constantes.webClientUriMSCliente = "http://" + SERVER_GATEWAY + ":8080/v1/client";
    }
    public static String webClientUriMSProducto = "";

    @Value("${SERVER_GATEWAY:localhost}")
    public void setWebClientUriMSProducto(String SERVER_GATEWAY) {
        Constantes.webClientUriMSProducto = "http://" + SERVER_GATEWAY + ":8080/v1/product";
    }
    //Products Pasivos
    //Products Pasivos
    public static String webClientUriMSPasivoCuentaCorriente = "";
    public static String webClientUriMSPasivoCuentaCorrienteTrx = "";

    @Value("${SERVER_GATEWAY:localhost}")
    public void setWebClientUriMSPasivoCuentaCorriente(String SERVER_GATEWAY) {
        Constantes.webClientUriMSPasivoCuentaCorriente = "http://" + SERVER_GATEWAY + ":8080/v1/pasivocuentacorriente/getByAccountNumber";
    }

    @Value("${SERVER_GATEWAY:localhost}")
    public void setWebClientUriMSPasivoCuentaCorrienteTrx(String SERVER_GATEWAY) {
        Constantes.webClientUriMSPasivoCuentaCorrienteTrx = "http://" + SERVER_GATEWAY + ":8080/v1/pasivocuentacorriente/externalTransaction";
    }

}
