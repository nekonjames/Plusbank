/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekonjames.plusbank;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nekon
 */
public class CurrencyPojo {
    private List<RatesPojo> rates; 

    public CurrencyPojo() {
        rates = new  ArrayList<>();
    }

    public List<RatesPojo> getRates() {
        return rates;
    }

    public void setRates(List<RatesPojo> rates) {
        this.rates = rates;
    }
    
    
}
