package com.management.controllers;

import com.management.jpa.RoleEmploye;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.sessionbeans.RoleEmployeFacade;

import java.io.Serializable;
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

@ManagedBean(name="roleEmployeController")
@SessionScoped
public class RoleEmployeController implements Serializable {

    private RoleEmploye current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.RoleEmployeFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private Boolean bol = false;
    private String find;
    private DataModel<RoleEmploye> allRole;
    private Boolean updateForm = false;
    private String nouveauIntitule;
    private RoleEmploye toModify;

    public RoleEmployeController() {
    }

    public RoleEmploye getSelected() {
        if (current == null) {
            current = new RoleEmploye();
            selectedItemIndex = -1;
        }
        return current;
    }
    
    public void modifierCurrent(RoleEmploye c){
        current=c;
    }

    private RoleEmployeFacade getFacade() {
        return ejbFacade;
    }

    public void afForm() {
        bol = true;
    }

    public void annuler() {
        bol = false;
        current = null;
    }

    public Boolean getBol() {
        return bol;
    }

    public Boolean cacheButton() {
        return !(bol == true || updateForm == true);
    }

    public Boolean getUpdateForm() {
        return updateForm;
    }

    public void setUpdateForm(Boolean updateForm) {
        this.updateForm = updateForm;
    }

    public void closeDialogUpdate() {
        updateForm = false;
    }

    public String getFind() {
        return find;
    }

    public void setFind(String find) {
        this.find = find;
    }

    public void findRole() {
        System.out.println("Recherche= " + this.getFind());
    }

    public DataModel<RoleEmploye> getAllRole() {
        //String req = "select * from classe_produit order by nom ASC";
        //List<RoleEmploye> list = (List<RoleEmploye>) entityManager.createNativeQuery(req, RoleEmploye.class).getResultList();
        //System.out.println("Taille allRole: "+allRole.size());
        if(allRole==null){
        allRole = new ListDataModel<>();
        allRole.setWrappedData(getFacade().orderAllRole());
        }
        return allRole;
    }

    public void setAllRole(DataModel<RoleEmploye> allRole) {
        this.allRole = allRole;
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

    public void prepareView() {
        current = (RoleEmploye) getAllRole().getRowData();
    }

    /*public String prepareView() {
        current = (RoleEmploye) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }*/
    public RoleEmploye verifyUnicity(String nom) {
        RoleEmploye retour;
        try {
            retour = entityManager.createNamedQuery("RoleEmploye.findByRole", RoleEmploye.class).setParameter("role", nom).getSingleResult();
        } catch (NoResultException e) {
            retour = null;
        }
        return retour;
    }

    public void prepareCreate() {
        current = null;
        //selectedItemIndex = -1;
        updateForm = false;
        // return "Create";
    }

    //Insérer l'unicité dans le processus de création
    public void create() {
        if(getSelected().getRole().equals("")){
            JsfUtil.addErrorMessage("Aucun Intitulé de rôle saisie!! Veuillez saisir avant de valider");
            return;
        }
        try {
            RoleEmploye retour = this.verifyUnicity(this.getSelected().getRole());
            if (retour != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("RoleEmployeNotUnique"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE,"Rôle déjà existant!!");
            } else {
                getFacade().create(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RoleEmployeCreated"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Rôle crée avec succès!!");
                //JsfUtil.addSuccessMessage("C'est correct!");
                recreateModel();
                prepareCreate();
            }
            //return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(RoleEmployeController.class.getName()).log(Level.SEVERE,null,e);
            //return null;
        }
    }

    /* public void prepareUpdate(){
        current=null;
        updateForm=false;
        current = (RoleEmploye) allRole.getRowData();
        updateForm=true;
    }*/

    public String getNouveauIntitule() {
        return nouveauIntitule;
    }

    public void setNouveauIntitule(String nouveau) {
        this.nouveauIntitule = nouveau;
    }
    
    public void prepareEdit() {
        toModify =(RoleEmploye) allRole.getRowData();
        //current=(RoleEmploye) allRole.getRowData();
        System.out.println("Rôle selectionnée: "+getSelected().toString());
        nouveauIntitule=getSelected().getRole();
        updateForm = true;
        bol=false;
    }

    public RoleEmploye getToModify() {
        if(toModify==null){
            toModify=new RoleEmploye();
        }
        return toModify;
    }
    
    
    public void test(){
        System.out.println("Oui j'ai clické!!");
    }

    public void update() {
        System.out.println("Mise à jour du rôle: "+ toModify.toString() +" en cours!!!");
        if(getSelected().getRole().equals("")){
            JsfUtil.addErrorMessage("Aucun Intitulé de rôle saisie!! Veuillez saisir avant de valider");
            return;
        }
        try {
            RoleEmploye retour = this.verifyUnicity(nouveauIntitule);
            if (retour != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("RoleEmployeNotUnique"));
                Logger.getLogger(RoleEmployeController.class.getName()).log(Level.SEVERE,"Rôle déjà existant!!");
            } else {
                toModify.setRole(getNouveauIntitule());
                getFacade().edit(toModify);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RoleEmployeUpdated"));
                Logger.getLogger(RoleEmployeController.class.getName()).log(Level.INFO,"Rôle mis à jour avec succès!!");
                //return "View";
                updateForm = false;
                toModify=null;
                recreateModel();
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(RoleEmployeController.class.getName()).log(Level.SEVERE,null,e);
            //return null;
        }

    }

    public void destroy() {
        current = (RoleEmploye) allRole.getRowData();
        System.out.println("Nombre d'employé ayant ce " + current.getRole() + " " + current.getEmployeList().size());
        if (current.getEmployeList().isEmpty()) {
            performDestroy();
            recreatePagination();
            recreateModel();
            JsfUtil.addSuccessMessage("Rôle supprimé avec succès!!");
            Logger.getLogger(RoleEmployeController.class.getName()).log(Level.INFO,"Rôle supprimé avec succès!!");
        } else {
            JsfUtil.addErrorMessage("Rôle impossible à supprimer, car il existe encore des employés effectuant ce rôle, veuillez soit changer le rôle de ces employés, ou supprimer ces employés, ou bien changer l'intitulé du rôle!!");
            Logger.getLogger(RoleEmployeController.class.getName()).log(Level.INFO,"Rôle impossible à supprimer, car il existe encore des employés effectuant ce rôle, veuillez soit changer le rôle de ces employés, ou supprimer ces employés, ou bien changer l'intitulé du rôle!!");
        }
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("RoleEmployeDeleted"));
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
        allRole = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public void next() {
        getPagination().nextPage();
        recreateModel();
        //return "List";
    }

    public void previous() {
        getPagination().previousPage();
        recreateModel();
        // return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        //System.out.println("nombre total items available: "+JsfUtil.getSelectItems(ejbFacade.findAll(), true).length);
        return JsfUtil.getSelectItems(ejbFacade.orderAllRole(), true);
    }

    public RoleEmploye getRoleEmploye(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = RoleEmploye.class)
    public static class RoleEmployeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RoleEmployeController controller = (RoleEmployeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "roleEmployeController");
            return controller.getRoleEmploye(getKey(value));
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
            if (object instanceof RoleEmploye) {
                RoleEmploye o = (RoleEmploye) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + RoleEmploye.class.getName());
            }
        }

    }

}
