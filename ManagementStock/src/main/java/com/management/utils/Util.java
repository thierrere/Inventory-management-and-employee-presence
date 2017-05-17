/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.utils;

/**
 *
 * @author thierry
 */
import com.management.jpa.Users;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
 

public class Util {
 
      public static HttpSession getSession() {
        return (HttpSession)
          FacesContext.
          getCurrentInstance().
          getExternalContext().
          getSession(false);
      }
       
      public static HttpServletRequest getRequest() {
       return (HttpServletRequest) FacesContext.
          getCurrentInstance().
          getExternalContext().getRequest();
      }
 
      public static String getUserName()
      {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         if ( session != null ) {
        return  session.getAttribute("username").toString();
         }
         else{
             return "/faces/auth/login";
         }
      }
       
      public static Integer getUserId()
      {
        HttpSession session = getSession();
        if ( session != null ) {
              return (Integer) session.getAttribute("userid");
          }
        else {
              return null;
          }
      }
       public static Users getUsers()
      {
        HttpSession session = getSession();
        if ( session != null ) {
              return (Users) session.getAttribute("user");
          }
        else {
              return null;
          }
      }
       
       public static Boolean ifAdmin()
       {
        if(getUsers().getRole().equals("Administrateur")){
            return true;
        }
        else{
            return false;
        }
       }
       
       public static Boolean ifSuperAdmin()
       {
        if(getUsers().getRole().equals("Super-Administrateur")){
            return true;
        }
        else{
            return false;
        }
       }

       public static Boolean ifMagasin()
       {
        if(getUsers().getRole().equals("Magasinier(e)")){
            return true;
        }
        else{
            return false;
        }
       }  
       
       public static String getDirectoryParent()
      {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         if ( session != null ) {
        return  session.getAttribute("directoryParent").toString();
         }
         else{
             return "";
         }
      }
       
       public static String getSeparateurSys()
      {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         if ( session != null ) {
        return  session.getAttribute("separateurSys").toString();
         }
         else{
             return "";
         }
      }
}