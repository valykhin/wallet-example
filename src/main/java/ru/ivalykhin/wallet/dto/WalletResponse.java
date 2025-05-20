package ru.ivalykhin.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
    @JsonProperty("wallet_id")
    private UUID walletId;
    private Long balance;
}
