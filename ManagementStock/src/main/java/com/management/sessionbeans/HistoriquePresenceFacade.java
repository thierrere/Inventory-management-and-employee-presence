/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.jpa.HistoriquePresence;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Thierry
 */
@Stateless
public class HistoriquePresenceFacade extends AbstractFacade<HistoriquePresence> {

    @PersistenceContext(unitName = "Edifice_StockPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistoriquePresenceFacade() {
        super(HistoriquePresence.class);
    }
    
}
