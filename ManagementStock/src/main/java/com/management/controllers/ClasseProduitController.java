package com.management.controllers;

import com.management.jpa.ClasseProduit;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.sessionbeans.ClasseProduitFacade;

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

@ManagedBean(name="classeProduitController")
@SessionScoped
public class ClasseProduitController implements Serializable {

    private ClasseProduit current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.ClasseProduitFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private Boolean bol = false;
    private String find;
    private DataModel<ClasseProduit> allClasse;
    private Boolean updateForm = false;
    private String nouveauNom;
    private ClasseProduit toModify;

    public ClasseProduitController() {
    }

    public ClasseProduit getSelected() {
        if (current == null) {
            current = new ClasseProduit();
            selectedItemIndex = -1;
        }
        return current;
    }
    
    public void modifierCurrent(ClasseProduit c){
        current=c;
    }

    private ClasseProduitFacade getFacade() {
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

    public void findClasse() {
        System.out.println("Recherche= " + this.getFind());
    }

    public DataModel<ClasseProduit> getAllClasse() {
        //String req = "select * from classe_produit order by nom ASC";
        //List<ClasseProduit> list = (List<ClasseProduit>) entityManager.createNativeQuery(req, ClasseProduit.class).getResultList();
        //System.out.println("Taille allClasse: "+allClasse.size());
        if(allClasse==null){
        allClasse = new ListDataModel<>();
        allClasse.setWrappedData(getFacade().orderAllClasse());
        }
        return allClasse;
    }

    public void setAllClasse(DataModel<ClasseProduit> allClasse) {
        this.allClasse = allClasse;
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
        current = (ClasseProduit) getAllClasse().getRowData();
    }

    /*public String prepareView() {
        current = (ClasseProduit) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }*/
    public ClasseProduit verifyUnicity(String nom) {
        ClasseProduit retour;
        try {
            retour = entityManager.createNamedQuery("ClasseProduit.findByNom", ClasseProduit.class).setParameter("nom", nom).getSingleResult();
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
        if(getSelected().getNom().equals("")){
            JsfUtil.addErrorMessage("Aucun nom de famille saisie!! Veuillez saisir avant de valider");
            return;
        }
        try {
            ClasseProduit retour = this.verifyUnicity(this.getSelected().getNom());
            if (retour != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ClasseProduitNotUnique"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE,"Famille de produit déjà existante!!");
            } else {
                getFacade().create(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClasseProduitCreated"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Famille crée avec succès!!");
                //JsfUtil.addSuccessMessage("C'est correct!");
                recreateModel();
                prepareCreate();
            }
            //return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE,null,e);
            //return null;
        }
    }

    /* public void prepareUpdate(){
        current=null;
        updateForm=false;
        current = (ClasseProduit) allClasse.getRowData();
        updateForm=true;
    }*/

    public String getNouveauNom() {
        return nouveauNom;
    }

    public void setNouveauNom(String nouveauNom) {
        this.nouveauNom = nouveauNom;
    }
    
    public void prepareEdit() {
        toModify =(ClasseProduit) allClasse.getRowData();
        //current=(ClasseProduit) allClasse.getRowData();
        System.out.println("Famille selectionnée: "+getSelected().toString());
        nouveauNom=toModify.getNom();
        updateForm = true;
        bol=false;
    }
    
    public ClasseProduit getToModify() {
        if(toModify==null){
            toModify=new ClasseProduit();
            toModify.setNom("");
        }
        return toModify;
    }
    
    public void test(){
        System.out.println("Oui j'ai clické!!");
    }

    public void update() {
        System.out.println("Mise à jour de la famille: "+ toModify.toString() +" en cours!!!");
        if(toModify.getNom().equals("")){
            JsfUtil.addErrorMessage("Aucun nom de famille saisie!! Veuillez saisir avant de valider");
            return;
        }
        try {
            ClasseProduit retour = this.verifyUnicity(nouveauNom);
            if (retour != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ClasseProduitNotUnique"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE,"Famille de produit déjà existante!!");
            } else {
                toModify.setNom(getNouveauNom());
                getFacade().edit(toModify);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClasseProduitUpdated"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Famille de produit mis à jour avec succès!!");
                //return "View";
                updateForm = false;
                toModify=null;
                recreateModel();
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,null,e);
            //return null;
        }

    }

    public void destroy() {
        current = (ClasseProduit) allClasse.getRowData();
        System.out.println("Nombre de produits dans la famille " + current.getNom() + " " + current.getProduitList().size());
        if (current.getProduitList().isEmpty()) {
            performDestroy();
            recreatePagination();
            recreateModel();
            JsfUtil.addSuccessMessage("Famille supprimée avec succès!!");
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Famille supprimée avec succès!!");
        } else {
            JsfUtil.addErrorMessage("Famille de produit impossible à supprimer, car elle contient des produits, veuillez soit changer la famille de ces produits, ou supprimer ces produits, ou bien changer le nom de la famille");
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Famille de produit impossible à supprimer, car elle contient des produits, veuillez d'abord supprimer ces produits, ou bien changer le nom de la famille!!");
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ClasseProduitDeleted"));
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
        allClasse = null;
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
        return JsfUtil.getSelectItems(ejbFacade.orderAllClasse(), true);
    }

    public ClasseProduit getClasseProduit(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = ClasseProduit.class)
    public static class ClasseProduitControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ClasseProduitController controller = (ClasseProduitController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "classeProduitController");
            return controller.getClasseProduit(getKey(value));
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
            if (object instanceof ClasseProduit) {
                ClasseProduit o = (ClasseProduit) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ClasseProduit.class.getName());
            }
        }

    }

}
