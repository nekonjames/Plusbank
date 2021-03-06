/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekonjames.plusbank.repository;

import com.nekonjames.plusbank.model.Transaction;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Nekon
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByAccountNumber(@Param("accountNumber") Long accountNumber);
    
    List<Transaction> findAllByTransactionDateBetween(Date startDate, Date endDate);
    
    @Query("FROM Transaction t ORDER BY t.transactionDate DESC")
    List<Transaction> loadAllTransactions();
    
    @Query("FROM Transaction t WHERE t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> loadAllTransactionByDate(@Param("startDate") Date startDate,@Param("endDate") Date endDate);
    
    @Query("FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> loadAccountTransactionByDate(@Param("accountNumber") Long accountNumber,@Param("startDate") Date startDate,@Param("endDate") Date endDate);
    
}
