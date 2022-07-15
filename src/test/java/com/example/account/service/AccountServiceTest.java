package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount() throws Exception{
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("정민")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(
                        Account.builder()
                                .accountNumber("1000000000")
                                .build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000001")
                        .build());
        // when
        // TODO captor가 이해가 잘 안가니까 나중에 코드 다시보기!
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        AccountDto account = accountService.createAccount(1L, 500L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, account.getUserId());
        assertEquals("1000000001", captor.getValue().getAccountNumber());
    }

    @Test
    void createFirstAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("정민")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000001")
                        .build());
        // when
        // TODO captor가 이해가 잘 안가니까 나중에 코드 다시보기!
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        AccountDto account = accountService.createAccount(1L, 500L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, account.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccountUserNotFound() {
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 500L));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("최대 계좌 생성 개수 초과 - 계좌 생성 실패")
    void createAccountMaxAccountPerUser10() {
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("정민")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countAccountByAccountUser(user))
                .willReturn(10);

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 500L));

        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    void deleteAccount() throws Exception{
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("정민")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(user)
                                .accountNumber("1000000000")
                                .balance(0L)
                                .build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        AccountDto account = accountService.deleteAccount(12L, "1000000000");


        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, account.getUserId());
        assertEquals("1000000000", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    void deleteAccount_userNotFound() throws Exception{
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1000000000"));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteAccount_accountAlreadyUnRegistered() throws Exception{
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("정민")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1000000000"));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    void deleteAccount_balanceNotEmpty() throws Exception{
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("정민")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .id(1L)
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .balance(1000L)
                        .accountStatus(AccountStatus.IN_USE)
                        .build()));

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1000000000"));

        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    void getAccountByUserId() {
        // given
        AccountUser user = AccountUser.builder()
                .id(1L)
                .name("정민")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        List<Account> accounts = List.of(
                Account.builder()
                        .accountNumber("1234567890")
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .id(1L)
                        .balance(500L)
                        .build(),
                Account.builder()
                        .accountNumber("0987654321")
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .id(2L)
                        .balance(500L)
                        .build(),
                Account.builder()
                        .accountNumber("1122334455")
                        .accountUser(user)
                        .accountStatus(AccountStatus.IN_USE)
                        .id(3L)
                        .balance(500L)
                        .build()
        );
        given(accountRepository.findAccountsByAccountUser(user))
                .willReturn(accounts);
        // when

        List<AccountDto> accountDtos = accountService.getAccountsByUserId(anyLong());
        // then
        assertEquals(3, accountDtos.size());
        assertEquals("1234567890", accountDtos.get(0).getAccountNumber());
        assertEquals("0987654321", accountDtos.get(1).getAccountNumber());
        assertEquals("1122334455", accountDtos.get(2).getAccountNumber());
    }

    @Test
    void getAccountByUserId_fail() {
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}