package com.example.ebankbackend.dtos;


import com.example.ebankbackend.enums.AccountStatus;
import lombok.Data;

import java.util.Date;

@Data
public class CurrentBankAccountDTO extends BankAccountDTO{
    private String id;
    private double balance;
    private Date creationDat;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double overDraft;
}
