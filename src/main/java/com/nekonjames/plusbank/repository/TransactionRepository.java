/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekonjames.plusbank.repository;

import com.nekonjames.plusbank.model.Transaction;
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
    
    @Query("FROM Transaction t where t.accountNumber = :accountNumber order by t.transactionDate DESC")
    List<Transaction> findAllByAccountNumber(@Param("accountNumber") Long accountNumber);
    
    @Query("FROM Transaction t order by t.transactionDate DESC")
    List<Transaction> loadAllTransactions();
    
}
