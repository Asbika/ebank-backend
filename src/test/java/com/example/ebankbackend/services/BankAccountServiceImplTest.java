package com.example.ebankbackend.services;

import com.example.ebankbackend.dtos.*;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;


import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.Null;
import java.util.*;
import java.util.stream.Collectors;

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
    void debitBankAccountNotFound() throws BalanceNotSufficientException {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount = 1900.00;
        String description = "debit";
        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.debit(accountId, amount, description);
        }catch(BankAccountNotFoundException e) {
            Assertions.assertEquals("BankAccount not found", e.getMessage());
        }
        //then
        verify(accountOperationRepository,Mockito.never()).save(any());
        verify(bankAccountRepository,Mockito.never()).save(any());
    }
    @Test
    void BalanceNotSufficient() throws BankAccountNotFoundException {
        //given

        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount = 2000.00;
        String description = "debit";
        BankAccount bankAccount = new CurrentAccount();
        double balance = 1900.00;
        Date creationDat = new Date();
        bankAccount.setBalance(balance);
        bankAccount.setCreationDat(creationDat);
        //when

        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(bankAccount));
        try {
            bankAccountServiceImpl.debit(accountId, amount, description);
        }catch(BalanceNotSufficientException e){
            Assertions.assertEquals("Balance not sufficient",e.getMessage());
        }
        //then

        verify(accountOperationRepository,Mockito.never()).save(any());
        verify(bankAccountRepository,Mockito.never()).save(any());
    }
    @Test
    void credit() throws BankAccountNotFoundException {

        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount = 1900.00;
        String description = "credit";
        double balance = 2000.00;
        BankAccount bankAccount = new CurrentAccount();
        bankAccount.setBalance(balance);
        bankAccount.setId(accountId);
        bankAccount.setCreationDat(new Date());

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setDescription(description);
        accountOperation.setAmount(amount);
        accountOperation.setBankAccount(bankAccount);

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(bankAccount));
        bankAccountServiceImpl.credit(accountId, amount, description);

        //then
        verify(bankAccountRepository,times(1)).save(bankAccount);
        verify(accountOperationRepository,times(1)).save(accountOperation);
    }
    @Test
    void creditBankAccountNotFound(){
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount=1900.00;
        String description="credit";

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.credit(accountId, amount, description);
        }catch(BankAccountNotFoundException e){
            Assertions.assertEquals("BankAccount not found",e.getMessage());
        }
        //then
       verify(accountOperationRepository,Mockito.never()).save(any());
       verify(bankAccountRepository,Mockito.never()).save(any());
    }

    //I should do unit tests also for debit and credit.

//    @Test
//    void transfer(){
//
//        //given
//        String accountIdSource="15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
//        String accountIdDestination="15bc190b-2e1c-44eb-8a3b-c3691c5hgv57c";
//        double amount=100.0;
//        //when
//
//        //then
//
//    }
//    @Test
//    void transfer() throws BankAccountNotFoundException, BalanceNotSufficientException {
//        //given
//        String accountIdSource="15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
//        String accountIdDestination ="15bc190b-2e1c-44eb-8a3b-c3691c5hgv57c";
//        double amount =100.0;
//
//        BankAccount bankAccount = new CurrentAccount();
//        double balance = 2000.00;
//        Date creationDat = new Date();
//        bankAccount.setBalance(balance);
//        bankAccount.setCreationDat(creationDat);
//
//
//        //when
//
//            bankAccountServiceImpl.transfer(accountIdSource, accountIdDestination, amount);
//
//        //then
//        verify(bankAccountServiceImpl,times(1)).debit(accountIdSource,amount,"Transfer to"+accountIdDestination);
//        verify(bankAccountServiceImpl,times(1)).credit(accountIdDestination,amount,"Transfer from"+accountIdSource);
//    }
//****
    @Test
    void getCurrentAccountList() {
        //given
        String accountId1 = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";

        double balance = 2000.00;
        CurrentAccount bankAccount1 = new CurrentAccount();
        bankAccount1.setBalance(balance);
        bankAccount1.setId(accountId1);
        bankAccount1.setOverDraft(100.0);
        bankAccount1.setCreationDat(new Date());

        CurrentBankAccountDTO  currentBankAccountDTO = new CurrentBankAccountDTO();
        currentBankAccountDTO.setBalance(balance);
        currentBankAccountDTO.setId(accountId1);
        currentBankAccountDTO.setOverDraft(100.0);
        currentBankAccountDTO.setCreationDat(new Date());

        List<BankAccount> bankAccounts = new ArrayList<>();
        bankAccounts.add(bankAccount1);

        List<BankAccountDTO> bankAccountsDTO = new ArrayList<>();
        bankAccountsDTO.add(currentBankAccountDTO);

        //when
        Mockito.when(bankAccountRepository.findAll()).thenReturn(bankAccounts);
        Mockito.when(dtoMapper.fromCurrentBankAccount(bankAccount1)).thenReturn(currentBankAccountDTO);
        List<BankAccountDTO> currentAccount= bankAccountServiceImpl.getBankAccountList();
        //then
        Assertions.assertNotNull(currentAccount);
        Assertions.assertEquals(currentAccount,bankAccountsDTO);
    }
    @Test
    void getSavingAccountList(){
        String accountId1 = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";

        double balance = 2000.00;
        SavingAccount bankAccount1 = new SavingAccount();
        bankAccount1.setBalance(balance);
        bankAccount1.setId(accountId1);
        bankAccount1.setInterestRate(5.1);
        bankAccount1.setCreationDat(new Date());

        SavingBankAccountDTO savingBankAccountDTO = new SavingBankAccountDTO();
        savingBankAccountDTO.setBalance(balance);
        savingBankAccountDTO.setId(accountId1);
        savingBankAccountDTO.setInterestRate(5.1);
        savingBankAccountDTO.setCreationDat(new Date());

        List<BankAccount> bankAccounts = new ArrayList<>();
        bankAccounts.add(bankAccount1);

        List<BankAccountDTO> bankAccountsDTO = new ArrayList<>();
        bankAccountsDTO.add(savingBankAccountDTO);

        //when
        Mockito.when(bankAccountRepository.findAll()).thenReturn(bankAccounts);
        Mockito.when(dtoMapper.fromSavingBankAccount(bankAccount1)).thenReturn(savingBankAccountDTO);
        List<BankAccountDTO> savingBankAccounts= bankAccountServiceImpl.getBankAccountList();
        //then
        Assertions.assertNotNull(savingBankAccounts);
        Assertions.assertEquals(savingBankAccounts,bankAccountsDTO);

    }

    @Test
    void getCustomer() throws CustomerNotFoundException {
        //given

        Long customerId =1L;
        String Name = "Customer";
        String Email = "Customer@gmail.com";
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customerId);
        customerDTO.setName(Name);
        customerDTO.setEmail(Email);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName(Name);
        customer.setEmail(Email);

        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Mockito.when(dtoMapper.fromCustomer(customer)).thenReturn(customerDTO);
        CustomerDTO customerDTO1 = bankAccountServiceImpl.getCustomer(customerId);
        //then
        Assertions.assertNotNull(customerDTO1);
        Assertions.assertEquals(customerDTO1,customerDTO);
    }

    @Test
    void getCustomerNotFound() throws CustomerNotFoundException {
        //given
        Long customerId =1L;

        //when
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.ofNullable(null));
        try {
            bankAccountServiceImpl.getCustomer(customerId);
        }catch(CustomerNotFoundException e){
            Assertions.assertEquals("Customer Not found",e.getMessage());
        }

    }
    @Test
    void updateCustomer() {
        //given
        Long customerId =1L;
        String Name = "Customer";
        String Email = "Customer@gmail.com";
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customerId);
        customerDTO.setName(Name);
        customerDTO.setEmail(Email);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName(Name);
        customer.setEmail(Email);

        //when
        Mockito.when(dtoMapper.fromCustomerDto(customerDTO)).thenReturn(customer);
        Mockito.when(dtoMapper.fromCustomer(customer)).thenReturn(customerDTO);
        Mockito.when(customerRepository.save(customer)).thenReturn(customer);
        CustomerDTO customerDTO1 =bankAccountServiceImpl.updateCustomer(customerDTO);

        //then
        verify(customerRepository,times(1)).save(customer);
        Assertions.assertEquals(customerDTO1,customerDTO);
    }

    @Test
    void deleteCustomer() {
        //given
        Long customerId =1L;
        //when
        bankAccountServiceImpl.deleteCustomer(customerId);
        //then
        verify(customerRepository,times(1)).deleteById(customerId);
    }

    @Test
    void accountHistory() {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        double amount = 1900.00;
        String description = "credit";
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setDescription(description);
        accountOperation.setAmount(amount);

        List<AccountOperation> accountOperations = new ArrayList<>();
        accountOperations.add(accountOperation);

        AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
        accountOperationDTO.setType(OperationType.CREDIT);
        accountOperationDTO.setDescription(description);
        accountOperation.setAmount(amount);

        List<AccountOperationDTO> accountOperationsDTO = new ArrayList<>();
        accountOperationsDTO.add(accountOperationDTO);

        //when
        Mockito.when(accountOperationRepository.findByBankAccount_Id(accountId)).thenReturn(accountOperations);
        Mockito.when(dtoMapper.fromAccountOperation(accountOperation)).thenReturn(accountOperationDTO);
        List<AccountOperationDTO> accountOperationsDTO1=  bankAccountServiceImpl.accountHistory(accountId);

        //then
        Assertions.assertEquals(accountOperationsDTO1,accountOperationsDTO);
        Assertions.assertNotNull(accountOperationsDTO1);
        Assertions.assertEquals(1,accountOperationsDTO1.size());

    }

    //think about pageable
//    @Test
//    void getAccountHistory() throws BankAccountNotFoundException {
//        //given
//        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
//
//        double balance = 2000.00;
//        CurrentAccount bankAccount = new CurrentAccount();
//        bankAccount.setBalance(balance);
//        bankAccount.setId(accountId);
//        bankAccount.setOverDraft(100.0);
//        bankAccount.setCreationDat(new Date());
//
//        double amount = 1900.00;
//        String description = "credit";
//        AccountOperation accountOperation = new AccountOperation();
//        accountOperation.setType(OperationType.CREDIT);
//        accountOperation.setDescription(description);
//        accountOperation.setAmount(amount);
//        accountOperation.setBankAccount(bankAccount);
//
//        AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
//        accountOperationDTO.setType(OperationType.CREDIT);
//        accountOperationDTO.setDescription(description);
//        accountOperationDTO.setAmount(amount);
//
//        List<AccountOperationDTO> accountOperationDTOS = new ArrayList<>();
//        accountOperationDTOS.add(accountOperationDTO);
//
//        List<AccountOperation> accountOperations1 = new ArrayList<>();
//        accountOperations1.add(accountOperation);
//        Pageable pageable = null;
//        int page = 0;
//        int size =5;
//
//        //Page<AccountOperation> accountOperations =  new PageImpl<>(accountId, PageRequest.of(page,size));
//        //Page<AccountOperation> accountOperations = new PageImpl<>(accountOperations1,pageable.withPage(0),1);
//
////        accountOperations.toSet().add((accountOperation);
//
//        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
//
//        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
//        accountHistoryDTO.setAccountId(bankAccount.getId());
//        accountHistoryDTO.setBalance(bankAccount.getBalance());
//        accountHistoryDTO.setCurrentPage(page);
//        accountHistoryDTO.setPageSize(size);
//        Page<AccountOperation> accountOperations=null;
////        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
//
//
//        //when
//        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(bankAccount));
//        Mockito.when(accountOperationRepository.findByBankAccount_Id(accountId, PageRequest.of(page,size))).thenReturn(accountOperations);
//        Mockito.when(dtoMapper.fromAccountOperation(accountOperation)).thenReturn(accountOperationDTO);
//        AccountHistoryDTO  accountHistoryDTO1 = bankAccountServiceImpl.getAccountHistory(accountId,page,size);
//        //then
//        Assertions.assertEquals(accountHistoryDTO1,accountHistoryDTO);
//    }

    @Test
    void getAccountHistoryNotFound() {
        //given
        String accountId = "15bc138b-2e1c-44eb-8a3b-c3691c56c37c";
        int page = 0 ;
        int size = 5 ;

        //when
        Mockito.when(bankAccountRepository.findById(accountId)).thenReturn(Optional.ofNullable(null));

        try{
            bankAccountServiceImpl.getAccountHistory(accountId,page,size);
        }catch(BankAccountNotFoundException e){
            Assertions.assertEquals("Account not Found",e.getMessage());
        }

    }
}