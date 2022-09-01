package com.example.ebankbackend.services;

import com.example.ebankbackend.dtos.CustomerDTO;
import com.example.ebankbackend.entities.*;
import com.example.ebankbackend.enums.AccountStatus;
import com.example.ebankbackend.enums.OperationType;
import com.example.ebankbackend.exceptions.BalanceNotSufficientException;
import com.example.ebankbackend.exceptions.BankAccountNotFoundException;
import com.example.ebankbackend.exceptions.CustomerNotFoundException;
import com.example.ebankbackend.mappers.BankAccountMapperImpl;
import com.example.ebankbackend.repositories.AccountOperationRepository;
import com.example.ebankbackend.repositories.BankAccountRepository;
import com.example.ebankbackend.repositories.CustomerRepository;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)

class BankAccountServiceImplTest {

    @InjectMocks
    private BankAccountServiceImpl bankAccountServiceImpl;
    @Mock
    private BankAccountMapperImpl dtoMapper;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    AccountOperationRepository accountOperationRepository;
    @Mock
    private CustomerRepository customerRepository;

    @Test
    void saveCustomer() {

        //given
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Customer");
        customerDTO.setEmail("Customer@gmail.com");
        Customer customer = new Customer();
        customer.setId(customerDTO.getId());
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());

        //when
        Mockito.when(dtoMapper.fromCustomerDto(customerDTO)).thenReturn(customer);
        bankAccountServiceImpl.saveCustomer(customerDTO);

        //then
        verify(customerRepository,times(1)).save(customer);
    }

    @Test
    void saveCurrentBankAccount() throws CustomerNotFoundException {
        //given
          Long customerId=1L;
          Customer customer = new Customer();
          CurrentAccount currentAccount  =  new CurrentAccount();
          customer.setId(customerId);
          customer.setName("Customer");
          customer.setEmail("Customer@gmail.com");
          double initialBalance = 1000L;
          double overDraft=900;
          currentAccount.setId(UUID.randomUUID().toString());
          currentAccount.setCreationDat(new Date());
          currentAccount.setBalance(initialBalance);
          currentAccount.setOverDraft(overDraft);
          currentAccount.setCustomer(customer);

        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        bankAccountServiceImpl.saveCurrentBankAccount(initialBalance,overDraft,customerId);

        //then
        verify(bankAccountRepository,times(1)).save(currentAccount);
    }

    @Test
    void saveCurrentBankAccountNotFound() {
        //given
        Long customerId = 1L;
        double initialBalance = 1000L;
        double overDraft=900;

        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.saveCurrentBankAccount(initialBalance, overDraft, customerId);
        }catch (CustomerNotFoundException e) {
            Assertions.assertEquals("Customer not found",e.getMessage());
        }

        //then
        verify(bankAccountRepository,Mockito.never()).save(any());
    }

    @Test
    void saveSavingBankAccount() throws CustomerNotFoundException {
        //given
        Customer customer = new Customer();
        Long customerId=1L;
        String name="customer";
        String email="customer@gmail.com";
        customer.setId(customerId);
        customer.setName(name);
        customer.setEmail(email);
        double initialBalance=1900.00;
        double interestRate=5.1;
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreationDat(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        bankAccountServiceImpl.saveSavingBankAccount(initialBalance,interestRate,customerId);
        //then
        verify(bankAccountRepository,times(1)).save(savingAccount);
    }
    @Test
    void saveSavingBankAccountNotFound(){
        //given
        Long customerId=1L;
        double initialBalance=1900.00;
        double interestRate=5.1;
        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.saveSavingBankAccount(initialBalance, interestRate, customerId);
        }catch(CustomerNotFoundException e){
            Assertions.assertEquals("Customer not found",e.getMessage());
        }
        //then
        verify(bankAccountRepository,Mockito.never()).save(any());
    }
    @Test
    void listCustomers() {
        //given
        Long customerId=1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("Customer");
        customer.setEmail("Customer@gmail.com");
        //when
        Mockito.when(customerRepository.findAll()).thenReturn(Collections.singletonList(customer));
        bankAccountServiceImpl.listCustomers();
        //then
        verify(customerRepository,times(1)).findAll();
    }

    @Test
    void getBankAccountAsSavingAccount() throws BankAccountNotFoundException {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double initialBalance=1900.00;
        double interestRate=5.1;
        SavingAccount savingAccount=new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreationDat(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(savingAccount));
        bankAccountServiceImpl.getBankAccount(accountId);

        //then
        Assertions.assertNotNull(savingAccount);
        Assertions.assertTrue(savingAccount instanceof SavingAccount );
    }

    @Test
    void getBankAccountAsCurrentAccount() throws BankAccountNotFoundException {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double initialBalance=1900.00;
        double overDraft=900;
        CurrentAccount currentAccount=new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreationDat(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(currentAccount));
        bankAccountServiceImpl.getBankAccount(accountId);

        //then
        Assertions.assertNotNull(currentAccount);
        Assertions.assertTrue(currentAccount instanceof CurrentAccount );
    }

    @Test
    void getBankAccountNotFound(){
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.getBankAccount(accountId);
        }catch(BankAccountNotFoundException e){
            Assertions.assertEquals("BankAccount not found",e.getMessage());
        }
    }
    @Test
    void debit() throws BankAccountNotFoundException, BalanceNotSufficientException {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount = 1900.00;
        String description = "debit";
        BankAccount bankAccount = new CurrentAccount();
        double balance = 2000.00;
        Date creationDat = new Date();
        bankAccount.setBalance(balance);
        bankAccount.setCreationDat(creationDat);

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setDescription(description);
        accountOperation.setAmount(amount);
        accountOperation.setBankAccount(bankAccount);

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(bankAccount));
        bankAccountServiceImpl.debit(accountId,amount,description);

        //then
        verify(accountOperationRepository,times(1)).save(accountOperation);
        verify(bankAccountRepository,times(1)).save(bankAccount);
    }

    @Test
    void credit() {
    }

    @Test
    void transfer() {
    }

    @Test
    void getBankAccountList() {
    }

    @Test
    void getCustomer() {
    }

    @Test
    void updateCustomer() {
    }

    @Test
    void deleteCustomer() {
    }

    @Test
    void accountHistory() {
    }

    @Test
    void getAccountHistory() {
    }
}