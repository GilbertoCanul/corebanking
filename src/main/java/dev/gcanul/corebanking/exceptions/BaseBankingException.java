package dev.gcanul.corebanking.exceptions;

import org.springframework.http.HttpStatus;

public abstract class BaseBankingException extends RuntimeException {

    public BaseBankingException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}