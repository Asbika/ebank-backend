package com.example.ebankbackend.services;

import com.example.ebankbackend.dtos.CustomerDTO;
import com.example.ebankbackend.entities.AccountOperation;
import com.example.ebankbackend.entities.BankAccount;
import com.example.ebankbackend.entities.CurrentAccount;
import com.example.ebankbackend.entities.Customer;
import com.example.ebankbackend.enums.AccountStatus;
import com.example.ebankbackend.exceptions.CustomerNotFoundException;
import com.example.ebankbackend.mappers.BankAccountMapperImpl;
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

import static org.mockito.ArgumentMatchers.any;
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
    void saveSavingBankAccount() {
    }

    @Test
    void listCustomers() {
    }

    @Test
    void getBankAccount() {
    }

    @Test
    void debit() {
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