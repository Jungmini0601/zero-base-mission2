package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @AccountLock
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ) throws Exception{
        try {
            Thread.sleep(5000L);
            TransactionDto transactionDto = transactionService
                    .useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount());
            log.debug("transactionDto: {}", transactionDto.getTransactionResultType());
            return UseBalance.Response.fromDto(transactionDto);
        }catch (AccountException e) {
            log.error("Failed to use balance.");
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @AccountLock
    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ) {
        try {
            TransactionDto transactionDto = transactionService
                    .cancelBalance(request.getTransactionId(), request.getAccountNumber(), request.getAmount());

            return CancelBalance.Response.fromDto(transactionDto);
        }catch (AccountException e) {
            log.error("Failed to use balance.");
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId
    ) {
        TransactionDto transactionDto = transactionService.queryTransaction(transactionId);
        return QueryTransactionResponse.from(transactionDto);
    }
}
