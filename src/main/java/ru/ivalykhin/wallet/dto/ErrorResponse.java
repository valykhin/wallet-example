package ru.ivalykhin.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    @JsonProperty("error_code")
    private final String errorCode;
    private final String message;
}
