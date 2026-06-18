package dev.gcanul.corebanking.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseBankingException {

    public UserNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}