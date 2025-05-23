package ru.ivalykhin.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.ivalykhin.wallet.dto.WalletOperationResponse;
import ru.ivalykhin.wallet.dto.WalletRequest;
import ru.ivalykhin.wallet.dto.WalletResponse;
import ru.ivalykhin.wallet.service.WalletService;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WalletOperationResponse operate(@RequestBody @Valid WalletRequest request) throws ExecutionException {
        return walletService.publishOperation(
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WalletResponse getBalance(@PathVariable UUID id) {
        Long balance = walletService.getBalance(id);
        return new WalletResponse(id, balance);
    }
}
