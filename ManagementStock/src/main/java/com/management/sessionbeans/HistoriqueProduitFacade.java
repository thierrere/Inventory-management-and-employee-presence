/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.jpa.HistoriqueProduit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Thierry
 */
@javax.ejb.Stateless
public class HistoriqueProduitFacade extends AbstractFacade<HistoriqueProduit> {

    @PersistenceContext(unitName = "com.management.stock_ManagementStock_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistoriqueProduitFacade() {
        super(HistoriqueProduit.class);
    }
    
}
