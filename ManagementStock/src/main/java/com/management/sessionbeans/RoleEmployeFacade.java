/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.jpa.RoleEmploye;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Thierry
 */
@Stateless
public class RoleEmployeFacade extends AbstractFacade<RoleEmploye> {

    @PersistenceContext(unitName = "Edifice_StockPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RoleEmployeFacade() {
        super(RoleEmploye.class);
    }
    
    public List<RoleEmploye> orderAllRole(){
        String req = "select * from role_employe order by role ASC";
        List<RoleEmploye> list = (List<RoleEmploye>) em.createNativeQuery(req, RoleEmploye.class).getResultList();
        return list;
    }
    
}
