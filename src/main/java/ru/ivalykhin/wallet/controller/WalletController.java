package ru.ivalykhin.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.ivalykhin.wallet.dto.WalletRequest;
import ru.ivalykhin.wallet.dto.WalletResponse;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.service.WalletService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WalletResponse operate(@RequestBody @Valid WalletRequest request) {
        Wallet wallet = walletService.processOperation(
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount());
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WalletResponse getBalance(@PathVariable UUID id) {
        Long balance = walletService.getBalance(id);
        return new WalletResponse(id, balance);
    }
}
