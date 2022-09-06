package com.example.ebankbackend.repositories;

import com.example.ebankbackend.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation,Long> {
     List<AccountOperation> findByBankAccount_Id(String accountId);

     Page<AccountOperation> findByBankAccount_Id(String accountId, Pageable pageable);//Page and Pageable of Spring Data
}
