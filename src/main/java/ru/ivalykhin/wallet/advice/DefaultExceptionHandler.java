package ru.ivalykhin.wallet.advice;

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

@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler({InsufficientFundsExceptionWallet.class, InvalidOperationExceptionWallet.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(WalletBusinessException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getErrorCode(), ((RuntimeException) ex).getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleValidation(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }
}
