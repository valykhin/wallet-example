package ru.ivalykhin.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.ivalykhin.wallet.dto.WalletOperationResponse;
import ru.ivalykhin.wallet.dto.WalletRequest;
import ru.ivalykhin.wallet.entity.OperationType;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.exception.InsufficientFundsExceptionWallet;
import ru.ivalykhin.wallet.exception.InvalidOperationExceptionWallet;
import ru.ivalykhin.wallet.exception.WalletBusinessException;
import ru.ivalykhin.wallet.exception.WalletNotFoundException;
import ru.ivalykhin.wallet.service.WalletService;

import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private WalletService walletService;

    private final String walletOperateEndpoint = "/api/v1/wallets";

    @Test
    public void operate_DepositOperationType_success() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.DEPOSIT);
        UUID operationId = UUID.randomUUID();
        WalletOperationResponse response =
                new WalletOperationResponse(
                        walletRequest.getWalletId(),
                        operationId);
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())).thenReturn(response);

        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content((objectMapper.writeValueAsString(walletRequest)))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                        "wallet_id": "%s",
                        "operation_id": %s
                        }
                        """.formatted(walletRequest.getWalletId(), operationId)));
        verify(walletService, times(1))
                .publishOperation(
                        walletRequest.getWalletId(),
                        walletRequest.getOperationType(),
                        walletRequest.getAmount());
    }

    @Test
    public void operate_WithdrawOperationType_success() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);
        UUID operationId = UUID.randomUUID();
        WalletOperationResponse response =
                new WalletOperationResponse(
                        walletRequest.getWalletId(),
                        operationId);
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())).thenReturn(response);

        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content((objectMapper.writeValueAsString(walletRequest)))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                        "wallet_id": "%s",
                        "operation_id": %s
                        }
                        """.formatted(walletRequest.getWalletId(), operationId)));
        verify(walletService, times(1))
                .publishOperation(
                        walletRequest.getWalletId(),
                        walletRequest.getOperationType(),
                        walletRequest.getAmount());
    }

    @Test
    public void operate_NotExistingWallet_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);
        WalletNotFoundException exception = new WalletNotFoundException(walletRequest.getWalletId());
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())
        ).thenThrow(exception);

        performErrorOperateRequest(walletRequest, status().isNotFound(), exception);
        verify(walletService, times(1))
                .publishOperation(
                        walletRequest.getWalletId(),
                        walletRequest.getOperationType(),
                        walletRequest.getAmount());
    }

    @Test
    public void operate_WalletWIthInsufficientFunds_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);
        InsufficientFundsExceptionWallet exception = new InsufficientFundsExceptionWallet();
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())
        ).thenThrow(exception);

        performErrorOperateRequest(walletRequest, status().isBadRequest(), exception);
        verify(walletService, times(1))
                .publishOperation(
                        walletRequest.getWalletId(),
                        walletRequest.getOperationType(),
                        walletRequest.getAmount());
    }

    @Test
    public void operate_NullOperationType_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);
        InvalidOperationExceptionWallet exception = new InvalidOperationExceptionWallet(null);
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())
        ).thenThrow(exception);

        performErrorOperateRequest(walletRequest, status().isBadRequest(), exception);
        verify(walletService, times(1))
                .publishOperation(
                        walletRequest.getWalletId(),
                        walletRequest.getOperationType(),
                        walletRequest.getAmount());
    }

    @Test
    public void operate_InvalidOperationType_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);

        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "wallet_id": "%s",
                                "operation_type": "%s",
                                "amount": %d
                                }
                                """.formatted(
                                walletRequest.getWalletId(),
                                "UNKNOWN",
                                walletRequest.getAmount()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        allOf(stringContainsInOrder(
                                "VALIDATION_ERROR",
                                "String \\\"UNKNOWN\\\": not one of the values accepted for Enum class"
                        )))
                );
    }

    @Test
    public void operate_OperationZeroAmount_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);

        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "wallet_id": "%s",
                                "operation_type": "%s",
                                "amount": %d
                                }
                                """.formatted(
                                walletRequest.getWalletId(),
                                OperationType.WITHDRAW,
                                0L))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        allOf(stringContainsInOrder(
                                "VALIDATION_ERROR",
                                "on field 'amount': rejected value [0]"
                        )))
                );
    }

    @Test
    public void operate_RuntimeExceptionHappens_returnErrorResponse() throws Exception {
        WalletRequest walletRequest = createWalletRequest(OperationType.WITHDRAW);
        when(walletService.publishOperation(
                walletRequest.getWalletId(),
                walletRequest.getOperationType(),
                walletRequest.getAmount())).thenThrow(new RuntimeException("test"));

        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "wallet_id": "%s",
                                "operation_type": "%s",
                                "amount": %d
                                }
                                """.formatted(
                                walletRequest.getWalletId(),
                                OperationType.WITHDRAW,
                                1000L))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(
                        allOf(stringContainsInOrder(
                                "INTERNAL_ERROR",
                                "test"
                        )))
                );
    }


    @Test
    public void getBalance_success() throws Exception {
        UUID walletId = UUID.randomUUID();
        Long balance = 1000L;
        when(walletService.getBalance(walletId)).thenReturn(balance);

        mockMvc.perform(get(walletOperateEndpoint + "/{id}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                        "wallet_id": "%s",
                        "balance": %d
                        }
                        """.formatted(walletId, balance)));
        verify(walletService, times(1))
                .getBalance(walletId);
    }

    @Test
    public void getBalance_WalletNotFound_returnErrorResponse() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletNotFoundException exception = new WalletNotFoundException(walletId);
        when(walletService.getBalance(walletId)).thenThrow(exception);

        mockMvc.perform(get(walletOperateEndpoint + "/{id}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().json(getErrorMessage(exception)));
        verify(walletService, times(1))
                .getBalance(walletId);
    }

    private WalletRequest createWalletRequest(OperationType operationType) {
        return WalletRequest.builder()
                .walletId(UUID.randomUUID())
                .operationType(operationType)
                .amount(1000L)
                .build();
    }

    private Wallet createWallet(WalletRequest walletRequest) {
        return Wallet.builder()
                .id(walletRequest.getWalletId())
                .balance(walletRequest.getAmount())
                .build();
    }

    private void performErrorOperateRequest(WalletRequest walletRequest,
                                            ResultMatcher result,
                                            WalletBusinessException exception) throws Exception {
        mockMvc.perform(post(walletOperateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content((objectMapper.writeValueAsString(walletRequest)))
                )
                .andExpect(result)
                .andExpect(content().json(getErrorMessage(exception)));
    }

    private String getErrorMessage(WalletBusinessException exception) {
        return """
                {
                "error_code": "%s",
                "message": "%s"
                }
                """.formatted(exception.getErrorCode(),
                ((RuntimeException) exception).getMessage());
    }
}
