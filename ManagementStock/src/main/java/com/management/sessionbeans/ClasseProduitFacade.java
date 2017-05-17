/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.jpa.ClasseProduit;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Thierry
 */
@Stateless
public class ClasseProduitFacade extends AbstractFacade<ClasseProduit> {

    @PersistenceContext(unitName = "Edifice_StockPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ClasseProduitFacade() {
        super(ClasseProduit.class);
    }
    
    public List<ClasseProduit> orderAllClasse(){
        String req = "select * from classe_produit order by nom ASC";
        List<ClasseProduit> list = (List<ClasseProduit>) em.createNativeQuery(req, ClasseProduit.class).getResultList();
        return list;
    }
    
}
