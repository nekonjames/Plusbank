/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekonjames.plusbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nekonjames.plusbank.CurrencyPojo;
import com.nekonjames.plusbank.RatesPojo;
import com.nekonjames.plusbank.ResponseSettings;
import com.nekonjames.plusbank.exception.ResourceNotFoundException;
import com.nekonjames.plusbank.model.Account;
import com.nekonjames.plusbank.model.Transaction;
import com.nekonjames.plusbank.repository.AccountRepository;
import com.nekonjames.plusbank.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Nekon
 */
@RestController
@RequestMapping("/v1/account")
public class AccountController {
        
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @GetMapping("/hello")
    public HashMap<String,Object> hello() {
        HashMap<String,Object> map = new HashMap<>();
        
        RestTemplate restTemplate = new RestTemplate();
        String currency = restTemplate.getForObject("http://data.fixer.io/api/latest?access_key=f3f2d7a94aeaefc42e16a1d80f80668c&symbols=USD", String.class);
            
//        CurrencyPojo currency = restTemplate.getForObject("http://data.fixer.io/api/latest?access_key=f3f2d7a94aeaefc42e16a1d80f80668c&symbols=USD", CurrencyPojo.class);
//        List<RatesPojo> rate= currency.getRates();
        
        map.put("code", ResponseSettings.APPROVED);
        map.put("message", ResponseSettings.SUCCESS_MESSAGE);
        map.put("currency", currency.substring(currency.lastIndexOf("USD")+5,currency.lastIndexOf("USD")+10));        
        
        return map;
    }
    
    @GetMapping("/all-account")
    public HashMap<String,Object> listAllAccounts() {
        List<Account> account = accountRepository.findAll();
        HashMap<String,Object> map = new HashMap<>();
        map.put("code", ResponseSettings.APPROVED);
        map.put("message", ResponseSettings.SUCCESS_MESSAGE);
        map.put("account", account);        
        
        return map;
    }
    
    @PostMapping("/create-account")
    public HashMap<String,Object> createAccount(@Valid @RequestBody Account account) {
        
        HashMap<String,Object> map = new HashMap<>();
        
        Account response = accountRepository.save(account);
        if(response.getAccountNumber() > 1){
            map.put("code", ResponseSettings.APPROVED);
            map.put("message", ResponseSettings.SUCCESS_MESSAGE);
            map.put("account", response);
        }else{
            map.put("code", ResponseSettings.FAILED);
            map.put("message", ResponseSettings.FAILED_MESSAGE);
        }                     
        
        return map;
    }
    
    @GetMapping("/{accountNumber}")
    public HashMap<String,Object> getCustomerAccountDetail(@PathVariable(value = "accountNumber") Long accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        List<Transaction> transaction = transactionRepository.findAllByAccountNumber(accountNumber);
        
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", ResponseSettings.APPROVED);
        map.put("message", ResponseSettings.SUCCESS_MESSAGE);
        map.put("account", account);
        map.put("transactions", transaction);
        
        return map;
    }
    
    @PutMapping("/update-account/{id}")
    public Account updateAccountDetails(@PathVariable(value = "accountNumber") Long accountNumber, @Valid @RequestBody Account accountDetail) {
        Account customerAccount = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        customerAccount.setAccountName(accountDetail.getAccountName());
        
        return accountRepository.save(customerAccount);
    }
    
    @PutMapping("/account-deposit/{accountNumber}")
    public HashMap<String,Object> creditAccount(@PathVariable(value = "accountNumber") Long accountNumber, @RequestBody Map<String, Double> body) {
        
        HashMap<String,Object> map = new HashMap<>();
        
        double amount = body.get("amount");
        Account customerAccount = accountRepository.findById(accountNumber).orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        customerAccount.setAccountBalance(customerAccount.getAccountBalance() + amount);
        
        //Add to transaction hisotry
        String transactionDescription = "Direct Account deposit for customer";
        String transactionType = "CREDIT";
        processTransaction(accountNumber, amount, transactionDescription, transactionType);
        
        Account account =  accountRepository.save(customerAccount);
        if(account.getAccountNumber()> 0){
            map.put("code",ResponseSettings.APPROVED);
            map.put("message",ResponseSettings.SUCCESS_MESSAGE);
        }else{
            map.put("code",ResponseSettings.FAILED);
            map.put("message",ResponseSettings.FAILED_MESSAGE);
        }
            
        return map;
    }
    
    @PutMapping("/transfer/{sourceAccount}")
    public HashMap<String,Object> transfer(@PathVariable(value = "sourceAccount") Long sourceAccountNumber,
            @RequestBody Map<String, String> args) { 
        
        HashMap<String,Object> map = new HashMap<>();
                
        Double amount = Double.parseDouble(args.get("amount"));
        Long destinationAccountNumber = Long.parseLong(args.get("destinationAccount"));
        
        //Very source and destination account exist or throw resource not found exception
        Account sourceAccount = accountRepository.findById(sourceAccountNumber).orElseThrow(() -> new ResourceNotFoundException("Account", "Source accountNumber", sourceAccountNumber));
        Account destinationAccount = accountRepository.findById(destinationAccountNumber).orElseThrow(() -> new ResourceNotFoundException("Account", "Destination accountNumber", destinationAccountNumber));
        
        //Check if Source account have sufficient fund
        if(sourceAccount.getAccountBalance() >= amount){
            sourceAccount.setAccountBalance(sourceAccount.getAccountBalance() - amount);
            
            String sourceCurrency = sourceAccount.getAccountCurrency();
            String destinationCurrency = destinationAccount.getAccountCurrency();
            
            //Call third party currency exchange rate                     
            double EUR = 1;
            double USD = getUSDRate();
            
            //Check if Source and Destination Currency are same or need currency conversion
            if(sourceCurrency.equals("USD") && destinationCurrency.equals("EUR")){
                
                amount = amount / USD;
                destinationAccount.setAccountBalance(destinationAccount.getAccountBalance() + amount);
                
                //Add to beneficiary transaction hisotry
                String transactionDescription = "USD Transfer between customers. From "+sourceAccount.getAccountName();
                String transactionType = "CREDIT";
                processTransaction(destinationAccountNumber, amount, transactionDescription, transactionType);
            }
            else if(sourceCurrency.equals("EUR") && destinationCurrency.equals("USD")){
                
                amount = amount * USD;
                destinationAccount.setAccountBalance(destinationAccount.getAccountBalance() + amount);
                
                //Add to beneficiary transaction hisotry
                String transactionDescription = "EUR Transfer between customers. From "+sourceAccount.getAccountName();
                String transactionType = "CREDIT";
                processTransaction(destinationAccountNumber, amount, transactionDescription, transactionType);
            }
            else{
                destinationAccount.setAccountBalance(destinationAccount.getAccountBalance() + amount);
                
                //Add to beneficiary transaction hisotry
                String transactionDescription = "Transfer between customers. From "+sourceAccount.getAccountName();
                String transactionType = "CREDIT";
                processTransaction(destinationAccountNumber, amount, transactionDescription, transactionType);
            }
            
            //Add to source account transaction hisotry
            String transactionDescription = "Transfer between customers. To "+destinationAccount.getAccountName();
            String transactionType = "DEBIT";
            processTransaction(sourceAccountNumber, Double.parseDouble(args.get("amount")), transactionDescription, transactionType);
            
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            
            map.put("code",ResponseSettings.APPROVED);
            map.put("message",ResponseSettings.SUCCESS_MESSAGE);
            map.put("USD_to_EUR_rate",getUSDRate());
        }else{
            map.put("code",ResponseSettings.INSUFFICIENT_FUND);
            map.put("message",ResponseSettings.INSUFFICIENT_FUND_MESSAGE);
        }
        
        return map;
    }
            
    @DeleteMapping("/delete-account/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable(value = "accountNumber") Long accountNumber) {   
        
        Account customerAccount = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        
        accountRepository.delete(customerAccount);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * This method save transaction history in the system
     * 
     * @param sourceAccount
     * @param amount
     * @param description
     * @param transactionType 
     */
    private void processTransaction(Long sourceAccount,Double amount,String description,String transactionType){
        
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(sourceAccount);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionType(transactionType);        
        transactionRepository.save(transaction);
        
    }
    
    /**
     * This method connects to fixer.io exchange rate API and return the rate of USD against EUR. 
     * 
     * Base Currency is EUR and return value is USD
     */
    private Double getUSDRate(){
        RestTemplate restTemplate = new RestTemplate();
        String currency = restTemplate.getForObject("http://data.fixer.io/api/latest?access_key=f3f2d7a94aeaefc42e16a1d80f80668c&symbols=USD", String.class);            
        String sanitizedUSD = currency.substring(currency.lastIndexOf("USD")+5,currency.lastIndexOf("USD")+10);
        return Double.parseDouble(sanitizedUSD);
    }
    
    
}
