package br.com.mobin.wallit.api.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    Mono<String> exceptionHandler(ValidationException e) {

        log.warn( e.getMessage(), e.getCause() );
        return Mono.just( e.getMessage() );
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    Mono<String> exceptionHandler(ConstraintViolationException e) {

        log.warn( e.getMessage(), e.getCause() );
        return Mono.just( e.getLocalizedMessage() );
    }
}
