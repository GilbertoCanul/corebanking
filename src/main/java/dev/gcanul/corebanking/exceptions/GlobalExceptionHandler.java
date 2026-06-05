package dev.gcanul.corebanking.exceptions;

import dev.gcanul.corebanking.dtos.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseBankingException.class)
    public ResponseEntity<ErrorResponse> handleBankingException(BaseBankingException ex) {
        var error = new ErrorResponse(
                ex.getHttpStatus().value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }
}