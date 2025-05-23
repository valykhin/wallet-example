package ru.ivalykhin.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class WalletOperationResponse {
    @JsonProperty("wallet_id")
    private UUID walletId;
    @JsonProperty("operation_id")
    private UUID operationId;
}
