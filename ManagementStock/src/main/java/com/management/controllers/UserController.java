package com.management.controllers;

import com.management.jpa.Users;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.sessionbeans.UsersFacade;
import com.management.utils.Util;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
//import javax.inject.Named;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@ManagedBean(name="userController")
@SessionScoped
public class UserController implements Serializable {

    private Users current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.UsersFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private DataModel<Users> allUsers;
    Boolean updateForm;
    String ancienLogin;
    String nouveauLogin;
    String ancienPassword;
    String myLastPassword;

    public UserController() {
    }

    public Users getSelected() {
        if (current == null) {
            current = new Users();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UsersFacade getFacade() {
        return ejbFacade;
    }

    public String getMyLastPassword() {
        return myLastPassword;
    }

    public void setMyLastPassword(String myLastPassword) {
        this.myLastPassword = myLastPassword;
    }

    public DataModel<Users> getAllUsers() {
        if (allUsers == null) {
            String req;
            if (Util.ifSuperAdmin()) {
                req = "select * from users";
            } else {
                req = "select * from users where role <> 'Super-Administrateur'";
            }
            List<Users> list = (List<Users>) entityManager.createNativeQuery(req, Users.class).getResultList();
            allUsers = new ListDataModel<>();
            allUsers.setWrappedData(list);
        }
        return allUsers;
    }

    public void setAllUsers(DataModel<Users> AllU) {
        this.allUsers = AllU;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Users) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public void prepareCreate() {
        current = new Users();
        updateForm = false;
        //selectedItemIndex = -1;
        //return "Create";
    }

    public void create() {
        if (getSelected().getLastname().equals("")) {
            JsfUtil.addErrorMessage("Aucun nom saisie!! Veuillez saisir le nom avant de valider");
            return;
        }
        if (getSelected().getLogin().equals("")) {
            JsfUtil.addErrorMessage("Aucun Login saisie!! Veuillez saisir le login avant de valider");
            return;
        }
        if (getSelected().getLogin().length() < 4) {
            JsfUtil.addErrorMessage("Le Login saisie doit contenir au moins 5 caractères!! Veuillez saisir un login valide avant de valider");
            return;
        }
        if (getSelected().getRole().equals("")) {
            JsfUtil.addErrorMessage("Aucun rôle selectionné!! Veuillez choisir le rôle avant de valider");
            return;
        }
        if (getSelected().getPassword().equals("")) {
            JsfUtil.addErrorMessage("Veuillez entrer votre mot de passe !!");
            return;
        }
        if (getSelected().getTelephone().equals("")) {
            JsfUtil.addErrorMessage("Veuillez entrer votre numéro de téléphone !!");
            return;
        }
        if (getSelected().getPassword().length() < 5) {
            JsfUtil.addErrorMessage("Votre mot de passe doit contenir au moins 5 caractères (exemple: azert, az124, ser74)!!");
            return;
        }
        
        try {
            if (current.getLogin().equals(current.getPassword())) {
                JsfUtil.addErrorMessage("Le login et le mot de passe ne doivent pas être identiques!!");
                Logger.getLogger(UserController.class.getName()).log(Level.WARNING, "Le login et le mot de passe ne doivent pas être identique !!");
                return;
            }
            TypedQuery<Users> query1 = entityManager.createNamedQuery("Users.findByLogin", Users.class).setParameter("login", current.getLogin());
            if (query1.getFirstResult() == 0) {
                //JsfUtil.addErrorMessage("Number: " + query1.getFirstResult());
                try {
                    JsfUtil.addErrorMessage("Login non disponible!! Login utilisé: " + query1.getSingleResult().getLogin());
                    Logger.getLogger(UserController.class.getName()).log(Level.WARNING, "Login non disponible!!");
                    return;
                } catch (NoResultException e) {
                    current.setStatus('D');
                    JsfUtil.addSuccessMessage("Login disponible!");
                    getFacade().create(current);
                    JsfUtil.addSuccessMessage("Utilisateur crée avec succès!!");
                    Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur crée avec succès!!");
                    recreateModel();
                    prepareCreate();
                }
            }
            prepareCreate();

        } catch (Exception e) {
            JsfUtil.addErrorMessage("Erreur lors de la création de l'utilisateur!!");
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, e);
            //return;
        }
    }

    public Boolean getUpdateForm() {
        return updateForm;
    }

    public void setUpdateForm(Boolean updateForm) {
        this.updateForm = updateForm;
    }

    public void prepareEdit() {
        current = (Users) getAllUsers().getRowData();
        ancienLogin = current.getLogin();
        ancienPassword = current.getPassword();
        updateForm = true;
    }

    public void update() {
        if (getSelected().getLastname().equals("")) {
            JsfUtil.addErrorMessage("Aucune nom saisie!! Veuillez saisir le nom avant de valider");
            return;
        }
        if (getSelected().getLogin().equals("")) {
            JsfUtil.addErrorMessage("Aucune Login saisie!! Veuillez saisir le login avant de valider");
            return;
        }
        if (getSelected().getLogin().length() < 4) {
            JsfUtil.addErrorMessage("Le Login saisie doit contenir au moins 5 caractères!! Veuillez saisir un login valide avant de valider");
            return;
        }
        if (getSelected().getRole().equals("")) {
            JsfUtil.addErrorMessage("Aucun rôle selectionné!! Veuillez choisir le rôle avant de valider");
            return;
        }
        if (getSelected().getTelephone().equals("")) {
            JsfUtil.addErrorMessage("Entrer votre numéro de téléphone!!");
            return;
        }
        nouveauLogin = current.getLogin();
        try {
            if (current.getLogin().equals(current.getPassword())) {
                JsfUtil.addErrorMessage("Le login et le mot de passe ne doivent pas être identiques!!");
                Logger.getLogger(UserController.class.getName()).log(Level.WARNING, "Update Users: Le login et le mot de passe ne doivent pas être identique!!");
                return;
            }
            TypedQuery<Users> query1 = entityManager.createNamedQuery("Users.findByLogin", Users.class).setParameter("login", current.getLogin());
            if (query1.getFirstResult() == 0) {
                //JsfUtil.addErrorMessage("Number: " + query1.getFirstResult());
                if (ancienLogin.equals(nouveauLogin)) {
                    if (current.getPassword().equals("")) {
                        current.setPassword(ancienPassword);
                    } else if (getSelected().getPassword().length() < 5) {
                        JsfUtil.addErrorMessage("Votre mot de passe doit contenir au moins 5 caractères (exemple: azert, az124, ser74) - Si vous ne mettez rien comme mot de passe c'est l'ancien qui sera considéré!!");
                        return;
                    }
                    getFacade().edit(current);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
                    recreateModel();
                    prepareCreate();
                    return;
                }
                try {
                    JsfUtil.addErrorMessage("Login non disponible!! number: " + query1.getSingleResult().getLogin());
                    Logger.getLogger(UserController.class.getName()).log(Level.WARNING, "Login non disponible!!");
                    return;
                } catch (NoResultException e) {
                    JsfUtil.addSuccessMessage("Login disponible!");
                    if (current.getPassword().equals("")) {
                        current.setPassword(ancienPassword);
                    } else if (getSelected().getPassword().length() < 4) {
                        JsfUtil.addErrorMessage("Votre mot de passe doit contenir au moins 5 caractères (exemple: azert, az124, ser74) - Si vous ne mettez rien comme mot de passe c'est l'ancien qui sera considéré!!");
                        return;
                    }
                    getFacade().edit(current);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
                    Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur mis à jour avec succès!!");
                    recreateModel();
                    prepareCreate();
                    return;
                }
            }
            prepareCreate();

        } catch (Exception e) {
            JsfUtil.addErrorMessage("Erreur lors de la création de l'utilisateur!!");
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, null, e);
            return;
        }
    }

    public String prepareEditPassword() {
        current = Util.getUsers();
        return "/faces/user/ModifyPassword";
    }

    public void updatePassword() {
        try {
            if (current.getLogin().equals(current.getPassword())) {
                JsfUtil.addErrorMessage("Le login et le password ne doivent pas être identiques!!");
                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Famille supprimée avec succès!!");
                return;
            }
            Users query1 = (Users) entityManager.createNamedQuery("Users.findByLogin", Users.class).setParameter("login", current.getLogin()).getSingleResult();
            //JsfUtil.addErrorMessage("Number: " + query1.getFirstResult());
            if (current.getPassword().equals("")) {
                current.setPassword(query1.getPassword());
                getFacade().edit(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur mis à jour avec succès -Mot de passe resté inchangé!!");
                recreateModel();
                prepareCreate();
            } else if (myLastPassword.equals(query1.getPassword())) {
                if (getSelected().getPassword().length() < 4) {
                    JsfUtil.addErrorMessage("Votre mot de passe doit contenir au moins 5 caractères (exemple: azert, az124, ser74) - Si vous ne mettez rien comme mot de passe c'est l'ancien qui sera considéré!!");
                    return;
                }
                getFacade().edit(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur mis à jour avec succès - Mot de passe modifié!!");
                recreateModel();
                prepareCreate();
            } else {
                JsfUtil.addErrorMessage("Ancien mot de passe incorrect!!!");
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, "Ancien mot de passe incorrect!!");
            }

        } catch (Exception e) {
            JsfUtil.addErrorMessage("Erreur lors de la modification de l'utilisateur!!");
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void desactiver() {
        current = (Users) getAllUsers().getRowData();
        if (Util.getUsers().getLogin().equals(current.getLogin())) {
            JsfUtil.addErrorMessage("Vous ne pouvez pas modifier votre propre status!!!!");
            Logger.getLogger(UserController.class.getName()).log(Level.WARNING, "Vous ne pouvez pas modifier votre propre status!!");
            return;
        }
        try {
            current.setStatus('D');
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur mis à jour avec succès!!");
            prepareCreate();
            //return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Une erreur de persistance!!");
            prepareCreate();
            //return null;
        }
    }

    public void activer() {
        current = (Users) getAllUsers().getRowData();
        if (Util.getUsers().getLogin().equals(current.getLogin())) {
            JsfUtil.addErrorMessage("Vous ne pouvez pas modifier votre propre status!!!!");
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Vous ne pouvez pas modifier votre propre status!!");
            return;
        }
        try {
            current.setStatus('A');
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserUpdated"));
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Utilisateur mis à jour avec succès!!");
            prepareCreate();
            //return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Une erreur de persistence!!");
            prepareCreate();
            //return null;
        }
    }

    public String destroy() {
        current = (Users) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UserDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Users getUser(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Users.class)
    public static class UserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserController controller = (UserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userController");
            return controller.getUser(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Users) {
                Users o = (Users) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Users.class.getName());
            }
        }

    }

}
