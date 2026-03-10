package com.proyspring.java.reservation_backend.controller.advice;

import com.proyspring.java.reservation_backend.exception.ReglaNegocioException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> handleReglaNegocio(ReglaNegocioException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String mensaje = ex.getMessage();

        if (mensaje != null && mensaje.startsWith("No existe una reserva con id:")) {
            status = HttpStatus.NOT_FOUND;
        } else if (mensaje != null && (mensaje.contains("Ya existe una reserva")
                || mensaje.contains("ya se encuentra cancelada"))) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status)
                .body(Map.of("error", mensaje));
    }
}
