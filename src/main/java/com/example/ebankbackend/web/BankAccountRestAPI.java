package com.example.ebankbackend.web;

import com.example.ebankbackend.services.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BankAccountRestAPI {

    @Autowired
    private BankAccountService bankAccountService;


}
