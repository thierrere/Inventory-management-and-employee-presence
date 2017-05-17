/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.jpa.Users;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author Thierry
 */
@Stateless
public class UsersFacade extends AbstractFacade<Users> {

    @PersistenceContext(unitName = "Edifice_StockPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }
    
    public  Users login(String login, String password) {
          //TypedQuery
        
        TypedQuery<Users> query;
        query = em.createNamedQuery("Users.findByLoginStatus", Users.class);
        query.setParameter("login", login); query.setParameter("status", 'A');
        List<Users> results = query.getResultList();
        if(results.isEmpty() || results.size()>1) {
             return null;
         }
        
        for(Users user:results)
        {
            if(user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;      
         
   }
    
}
