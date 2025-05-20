package ru.ivalykhin.wallet.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.ivalykhin.wallet.entity.OperationType;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.exception.InsufficientFundsExceptionWallet;
import ru.ivalykhin.wallet.exception.InvalidOperationExceptionWallet;
import ru.ivalykhin.wallet.exception.WalletNotFoundException;
import ru.ivalykhin.wallet.repository.WalletRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    public void setUp() {
        wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(1000L)
                .build();
    }

    @Test
    public void processOperation_DepositOperationType_success() {
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService
                .processOperation(wallet.getId(), OperationType.DEPOSIT, wallet.getBalance());
        Assertions.assertNotNull(result);
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    public void processOperation_WithdrawOperationType_success() {
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService
                .processOperation(wallet.getId(), OperationType.WITHDRAW, wallet.getBalance());
        Assertions.assertNotNull(result);
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    public void processOperation_NotExistingWallet_throwException() {
        WalletNotFoundException expectedException =
                new WalletNotFoundException(wallet.getId());
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.empty());

        WalletNotFoundException actualException =
                assertThrows(WalletNotFoundException.class,
                        () -> walletService
                                .processOperation(
                                        wallet.getId(),
                                        OperationType.WITHDRAW,
                                        wallet.getBalance()));

        Assertions.assertEquals(actualException.getMessage(), expectedException.getMessage());
        Assertions.assertEquals(actualException.getErrorCode(), expectedException.getErrorCode());
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(0)).save(wallet);
    }

    @Test
    public void processOperation_InsufficientFunds_throwException() {
        InsufficientFundsExceptionWallet expectedException =
                new InsufficientFundsExceptionWallet();
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        InsufficientFundsExceptionWallet actualException =
                assertThrows(InsufficientFundsExceptionWallet.class,
                        () -> walletService
                                .processOperation(
                                        wallet.getId(),
                                        OperationType.WITHDRAW,
                                        wallet.getBalance() * 2));

        Assertions.assertEquals(actualException.getMessage(), expectedException.getMessage());
        Assertions.assertEquals(actualException.getErrorCode(), expectedException.getErrorCode());
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(0)).save(wallet);
    }

    @Test
    public void processOperationInvalidOperationType_throwException() {
        InvalidOperationExceptionWallet expectedException =
                new InvalidOperationExceptionWallet(null);
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        InvalidOperationExceptionWallet actualException =
                assertThrows(InvalidOperationExceptionWallet.class,
                        () -> walletService
                                .processOperation(
                                        wallet.getId(),
                                        null,
                                        wallet.getBalance()));

        Assertions.assertEquals(actualException.getMessage(), expectedException.getMessage());
        Assertions.assertEquals(actualException.getErrorCode(), expectedException.getErrorCode());
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(0)).save(wallet);
    }

    @Test
    public void getBalance_existingWallet_success() {
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        long result = walletService.getBalance(wallet.getId());
        Assertions.assertEquals(result, 1000L);
        verify(walletRepository, times(1)).findById(wallet.getId());
    }

    @Test
    public void getBalance_NotExistingWallet_throwException() {
        WalletNotFoundException exception = new WalletNotFoundException(wallet.getId());
        when(walletRepository.findById(wallet.getId())).thenThrow(exception);

        WalletNotFoundException actualException = assertThrows(WalletNotFoundException.class,
                () -> walletService
                        .processOperation(wallet.getId(), OperationType.WITHDRAW, wallet.getBalance()));

        Assertions.assertEquals(actualException.getMessage(), exception.getMessage());
        Assertions.assertEquals(actualException.getErrorCode(), exception.getErrorCode());
        verify(walletRepository, times(1)).findById(wallet.getId());
        verify(walletRepository, times(0)).save(wallet);
    }
}
