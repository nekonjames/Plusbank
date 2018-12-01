/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekonjames.plusbank.controller;

import com.nekonjames.plusbank.ResponseSettings;
import com.nekonjames.plusbank.exception.ResourceNotFoundException;
import com.nekonjames.plusbank.model.Account;
import com.nekonjames.plusbank.model.Transaction;
import com.nekonjames.plusbank.repository.AccountRepository;
import com.nekonjames.plusbank.repository.TransactionRepository;
import java.util.HashMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Nekon
 */
@RestController
@RequestMapping("/v1/transaction")
public class TransactionController {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @GetMapping("/all-transactions")
    public HashMap<String,Object> listAllTransactions() {
        
        HashMap<String,Object> map = new HashMap<>();
        List<Transaction> transaction =  transactionRepository.loadAllTransactions();
        
        map.put("code",ResponseSettings.APPROVED);
        map.put("message",ResponseSettings.SUCCESS_MESSAGE);
        map.put("transactions",transaction);
        
        return map;
    }    
    
    @GetMapping("/account/{accountNumber}")
    public Transaction getAccountTransactions(@PathVariable(value = "accountNumber") Long accountNumber) {
        return transactionRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "accountNumber", accountNumber));
    }   
    
}
