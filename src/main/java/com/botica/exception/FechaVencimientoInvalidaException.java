package com.botica.exception;

import java.time.LocalDate;

public class FechaVencimientoInvalidaException extends RuntimeException {
  public FechaVencimientoInvalidaException(LocalDate fecha) {
    super("La fecha de vencimiento no puede ser anterior a hoy. Fecha recibida: " + fecha);
  }
}