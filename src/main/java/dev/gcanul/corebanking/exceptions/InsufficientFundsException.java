package dev.gcanul.corebanking.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends BaseBankingException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}