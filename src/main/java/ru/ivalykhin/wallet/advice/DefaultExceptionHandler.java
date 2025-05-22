package ru.ivalykhin.wallet.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ivalykhin.wallet.dto.ErrorResponse;
import ru.ivalykhin.wallet.exception.InsufficientFundsExceptionWallet;
import ru.ivalykhin.wallet.exception.InvalidOperationExceptionWallet;
import ru.ivalykhin.wallet.exception.WalletBusinessException;
import ru.ivalykhin.wallet.exception.WalletNotFoundException;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler({InsufficientFundsExceptionWallet.class, InvalidOperationExceptionWallet.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(WalletBusinessException ex) {
        log.error(((RuntimeException) ex).getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getErrorCode(), ((RuntimeException) ex).getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleValidation(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("{}:{}", ex.getClass(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }

    @ExceptionHandler({
            jakarta.persistence.OptimisticLockException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class,
            org.hibernate.StaleObjectStateException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponse(
                                "CONCURRENT_MODIFICATION",
                                "Another request updated the wallet. Please retry."));
    }

    @ExceptionHandler(java.util.concurrent.ExecutionException.class)
    public ResponseEntity<ErrorResponse> handleExecutionException(ExecutionException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof WalletNotFoundException e) {
            return handleWalletNotFound(e);
        } else if (cause instanceof InsufficientFundsExceptionWallet e) {
            return handleBadRequest(e);
        } else if (cause instanceof InvalidOperationExceptionWallet e) {
            return handleBadRequest(e);
        } else if (cause instanceof HttpMessageNotReadableException e) {
            return handleValidation(e);
        } else if (cause instanceof MethodArgumentNotValidException e) {
            return handleValidation(e);
        } else if (cause instanceof jakarta.persistence.OptimisticLockException e) {
            return handleOptimisticLock(e);
        }

        return handleGeneric(ex);
    }
}
