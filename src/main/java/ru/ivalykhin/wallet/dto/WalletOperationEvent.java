package ru.ivalykhin.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ivalykhin.wallet.entity.OperationType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationEvent {
    private UUID walletId;
    private OperationType operationType;
    private Long amount;
}
