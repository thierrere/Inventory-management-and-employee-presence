/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.controllers.util;

import com.management.jpa.Produit;

/**
 *
 * @author Thierry
 */
public class ProduitASortir {
    
    private Produit produit;
    private Integer quantite;
    
    public ProduitASortir(){
        
    }
    
    public ProduitASortir(Produit p, Integer q){
        this.produit=p;
        quantite=q;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public Produit getProduit() {
        return produit;
    }

    public Integer getQuantite() {
        return quantite;
    }
        
}
