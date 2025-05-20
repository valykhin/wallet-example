package ru.ivalykhin.wallet.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.ivalykhin.wallet.entity.OperationType;

import java.util.UUID;

@Data
@Builder
public class WalletRequest {
    @NotNull
    @JsonProperty("wallet_id")
    private UUID walletId;

    @NotNull
    @JsonProperty("operation_type")
    private OperationType operationType;

    @NotNull
    @Min(1)
    private Long amount;
}
