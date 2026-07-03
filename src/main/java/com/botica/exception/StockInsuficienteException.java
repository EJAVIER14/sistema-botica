package com.botica.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String nombreProducto, int disponible, int solicitado) {
        super("Stock insuficiente para " + nombreProducto + ". Disponible: "
                + disponible + " unidades, solicitado: " + solicitado + " unidades");
    }
}