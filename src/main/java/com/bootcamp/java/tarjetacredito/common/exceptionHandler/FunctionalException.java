package com.bootcamp.java.tarjetacredito.common.exceptionHandler;

public class FunctionalException extends RuntimeException{
    public FunctionalException(String messageError){
        super(messageError);
    }
}
