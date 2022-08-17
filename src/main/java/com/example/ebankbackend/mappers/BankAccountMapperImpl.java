package com.example.ebankbackend.mappers;

import com.example.ebankbackend.dtos.CustomerDTO;
import com.example.ebankbackend.entities.Customer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
//MapStruct : frameWork do mapping between classes.
@Service
public class BankAccountMapperImpl {
    public CustomerDTO fromCustomer(Customer customer){
        CustomerDTO customerDto = new CustomerDTO();
        customerDto.setId(customer.getId());
        customerDto.setName(customer.getName());
        customerDto.setEmail(customer.getEmail());
        return customerDto;
    }

    public Customer fromCustomerDto(CustomerDTO customerDto){
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDto,customer);

        return customer;
    }
}
