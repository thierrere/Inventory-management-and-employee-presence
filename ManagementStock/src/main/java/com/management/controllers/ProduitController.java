package com.management.controllers;

import com.management.jpa.Produit;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.controllers.util.ProduitASortir;
import com.management.jpa.ClasseProduit;
import com.management.sessionbeans.ClasseProduitFacade;
import com.management.sessionbeans.HistoriqueProduitFacade;
import com.management.sessionbeans.ProduitFacade;
import com.management.utils.Util;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
//import java.io.IOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

@ManagedBean(name="produitController")
@SessionScoped
public class ProduitController implements Serializable {

    private Produit current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.ProduitFacade ejbFacade;
    @EJB
    private com.management.sessionbeans.HistoriqueProduitFacade ejbHistoProduitFacade;
    @EJB
    private com.management.sessionbeans.ClasseProduitFacade ejbClasseProduitFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private DataModel<Produit> allProduit;
    //private DataModel<Produit> allProduitOrder;
    private Boolean bol = false;
    private Boolean bol1 = false;
    private Boolean bol2 = false;
    private Part myFile;
    private Part myFileRestore;
    static final int TAILLE_TAMPON = 10240; // 10 ko
    byte[] tampon = new byte[TAILLE_TAMPON];
    private ProduitASortir produitASortir;
    private Produit completeProd;
    private Integer quantite;
    private String destinataire;
    //Liste des produits en sortie
    private List<ProduitASortir> listProduitASortir;
    String pathFileDestockage;
    String pathFileStockage;
    String pathFileInventaire;
    String pathFileSaveDB;
    String pathFileRestoreDB;
    private Date heureStockageEnCours;
    private Date heureDestockageEnCours;
    private final String destockageDirectory = "Destockage";
    private final String stockageDirectory = "Stockage";
    private final String inventaireDirectory = "Inventaire";
    private final String saveDBDirectory = "SauvegardeDB";
    private final String restoreDBDirectory = "RestaurationDB";

    private Boolean updateQuantityForm = false;
    private int updateQuantity;
    private Boolean modifyForm = false;

    //la définition des polices
    Font police_entete = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, BaseColor.BLUE);
    Font police_entete_1 = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    Font police_tableau = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    Font police_services = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    Font police_premiere_ligne_tableau = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLUE);

    Font police_entete_f = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    Font police_entete_1_f = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);
    Font police_tableau_f = new Font(Font.FontFamily.COURIER, 14, Font.NORMAL);
    Font police_services_f = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    Font police_premiere_ligne_tableau_f = new Font(Font.FontFamily.COURIER, 14, Font.BOLD);

    Font police_ligne_recap_famille = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, BaseColor.LIGHT_GRAY);
    Font police_ligne_recap_total = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, BaseColor.ORANGE);

    //Les varaibles textes pour l'entête des documents
    String nom_entreprise = "EDIFICE CONSTRUCTION";
    String devise = "La qualité notre engagement";
    String titreDocument = "Gestion de Stock - Destockage de produit";
    String titreDocumentInventaire = "Inventaire du Stock";
    String acteurPrincipal = "Destockage des produits par : ";
    String acteurAutre = "Destockage des produits à destination de : ";
    String acteurPrincipalInventaire = "Inventaire des produits généré par : ";

    SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' HH:mm:ss");
    SimpleDateFormat formaterDateFile = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

    public ProduitController() {
    }

    public Produit getSelected() {
        if (current == null) {
            current = new Produit();
            current.setQuantite(0);
            current.setNombreExemplaire(0);
            selectedItemIndex = -1;
        }
        return current;
    }

    private ProduitFacade getFacade() {
        return ejbFacade;
    }

    public HistoriqueProduitFacade getEjbHistoProduitFacade() {
        return ejbHistoProduitFacade;
    }

    public ClasseProduitFacade getEjbClasseProduitFacade() {
        return ejbClasseProduitFacade;
    }

    public List<Produit> completeProduit(String query) {
        String req = "select * from produit";
        List<Produit> list = (List<Produit>) entityManager.createNativeQuery(req, Produit.class).getResultList();
        List<Produit> filtered = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Produit motif = list.get(i);
            if (motif.getDesignation().toLowerCase().contains(query)) {
                filtered.add(motif);
            }
        }

        return filtered;
    }

    public DataModel<Produit> getAllProduit() {
        if (allProduit == null) {
            String req = "select * from produit order by designation ASC";
            List<Produit> list = (List<Produit>) entityManager.createNativeQuery(req, Produit.class).getResultList();
            allProduit = new ListDataModel<>();
            allProduit.setWrappedData(list);
        }
        return allProduit;
    }

    public void setAllProduit(DataModel<Produit> AllProduit) {
        this.allProduit = AllProduit;
    }

    public ProduitASortir getProduitASortir() {
        if (produitASortir == null) {
            produitASortir = new ProduitASortir();
        }
        return produitASortir;
    }

    public void setProduitASortir(ProduitASortir produitASortir) {
        this.produitASortir = produitASortir;
    }

    public Produit getCompleteProd() {
        if (completeProd == null) {
            completeProd = new Produit();
            completeProd.setId(0);
            completeProd.setDesignation("");
            completeProd.setNombreExemplaire(1);
        }
        return completeProd;
    }

    public void setCompleteProd(Produit completeProd) {
        this.completeProd = completeProd;
    }

    public int quantiteProduit() {
        int retour = 0;
        try {
            if (completeProd == null) {
            } else {
                retour = (entityManager.createNamedQuery("Produit.findByDesignation", Produit.class).setParameter("designation", getCompleteProd().getDesignation()).getSingleResult()).getQuantite();
                int pos = this.existInListProduitASortir(completeProd);
                if (pos != -1) {
                    retour = retour - this.getListProduitASortir().get(pos).getQuantite();
                }
            }
        } catch (NoResultException e) {
            return retour;
        }
        return retour;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public List<ProduitASortir> getListProduitASortir() {
        if (listProduitASortir == null) {
            listProduitASortir = new ArrayList<>();
        }
        return listProduitASortir;
    }

    public void setListProduitASortir(List<ProduitASortir> listProduitASortir) {
        this.listProduitASortir = listProduitASortir;
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

    //Une partie du Code pour le destockage
    public Boolean getBol() {
        return bol;
    }

    public void afFormStock() {
        if (getDestinataire().equals("")) {
            JsfUtil.addErrorMessage("Veuillez saisir le nom du destinataire!!!");
        } else {
            bol = true;
        }
    }

    public void annuler() {
        try {
            bol = false;
            completeProd = null;
            produitASortir = null;
            quantite = 0;
        } catch (Exception e) {

        }
    }

    public int existInListProduitASortir(ProduitASortir pas) {
        int retour = -1;
        ProduitASortir p;
        for (int i = 0; i < this.getListProduitASortir().size(); i++) {
            p = this.getListProduitASortir().get(i);

            if (p.getProduit().toString().equals(pas.getProduit().toString())) {
                retour = i;
            }
        }
        return retour;
    }

    public int existInListProduitASortir(Produit ps) {
        int retour = -1;
        ProduitASortir p;
        for (int i = 0; i < this.getListProduitASortir().size(); i++) {
            p = this.getListProduitASortir().get(i);
            if (p.getProduit().toString().equals(ps.toString())) {
                retour = i;
            }
        }
        return retour;
    }

    public void addProduit() {
        try {
            if (getCompleteProd() == null) {
                JsfUtil.addErrorMessage("Veuillez choisir un produit à sortir!!!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Veuillez choisir un produit à sortir!!!");
                return;
            }
            if (getDestinataire().equals("")) {
                //completeProd = null;
                //produitASortir = null;
                quantite = 0;
                bol = false;
                return;
            }

            if (completeProd.getDesignation().equals("")) {
                JsfUtil.addErrorMessage("Veuillez saisir une désignation pour le produit à sortir!!!");
            }
            if (getQuantite() == 0) {
                JsfUtil.addErrorMessage("Veuillez entrer une quantité pour le produit à sortir!!!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Veuillez entrer une quantité pour le produit à sortir!!");
                return;
            }
            if (getQuantite() > quantiteProduit()) {
                if (completeProd == null) {
                    JsfUtil.addErrorMessage("Impossible! Produit: Veuillez d'abord selectionnez un produit!!");
                    Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Impossible Produit: Veuillez d'abord selectionnez un produit!!");
                } else {
                    JsfUtil.addErrorMessage("Impossible! Produit:" + getCompleteProd().toString() + " quantité demandée " + getQuantite() + " supérieur à celle disponible " + quantiteProduit() + " ");
                    Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Impossible! Produit: quantité demandée  supérieur à celle disponible ");
                    return;
                }
            }
            //stockASortir=new Stock();
            produitASortir = new ProduitASortir(getCompleteProd(), getQuantite());
            //getProduitASortir().setProduit(produit);
            //getProduitASortir().setQuantite(getQuantite());
            int position = this.existInListProduitASortir(produitASortir);
            if (position != -1) {
                int q = getListProduitASortir().get(position).getQuantite();
                getListProduitASortir().get(position).setQuantite(q + getQuantite());
                System.out.println("Quantité à sortir du produit: " + produitASortir.getProduit().toString() + " ajoutée de :" + getQuantite());
                JsfUtil.addSuccessMessage("Quantité à sortir du produit: " + produitASortir.getProduit().toString() + " ajoutée de :" + getQuantite());
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Quantité à sortir du produit:  ajoutée ");
            } else {
                getListProduitASortir().add(getProduitASortir());
                System.out.println("Quantité à sortir du produit " + produitASortir.getProduit().toString() + " : " + getQuantite());
                JsfUtil.addSuccessMessage("Quantité à sortir du produit " + produitASortir.getProduit().toString() + " : " + getQuantite());
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Quantité à sortir du produit ");
            }
            completeProd = null;
            produitASortir = null;
            quantite = 0;
        } catch (Exception e) {

        }
    }

    public void removePAS(ProduitASortir p) {
        listProduitASortir.remove(p);
    }

    //Fonction de destockage
    public void savePAS() {
        // ArrayList<Stock> lastListe = new ArrayList<Stock>();
        // HistoriqueProduit histoProd = new HistoriqueProduit();
        if (getDestinataire().equals("")) {
            //completeProd = null;
            //produitASortir = null;
            //quantite = 0;
            //listProduitASortir = new ArrayList();
            this.annuler();
            return;
        }

        try {
            Calendar c = Calendar.getInstance();
            heureDestockageEnCours = c.getTime();
            int retour = getFacade().savePAS(getListProduitASortir(), getDestinataire(), getHeureDestockageEnCours());
            if (retour == 0) {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DestockageSuccess"));
                JsfUtil.addSuccessMessage("Stock mis à jour!");
                JsfUtil.addSuccessMessage("Historique sauvegardé!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Destockage Réussie!!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Stock mis à jour!!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Historique sauvegardé!!");
                this.downloadDestockageFile(getDestinataire(), getHeureDestockageEnCours());
                this.saveBD();
                recreateModel();
                listProduitASortir = new ArrayList();
                destinataire = "";
                this.annuler();
            } else {
                JsfUtil.addErrorMessage("Le destockage n'a pas été réalisé!!");
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Le destockage n'a pas été réalisé!!");
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Le destockage n'a pas été réalisé!!");
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void annulerSortiePAS() {
        listProduitASortir = new ArrayList<>();
    }

    public Date getHeureDestockageEnCours() {
        return heureDestockageEnCours;
    }

    //Une partie du code pour le stockage
    public Boolean getBol1() {
        return bol1;
    }

    public Boolean getBol2() {
        return bol2;
    }

    public void afFormStock1() {
        bol1 = true;
        bol2 = false;
        updateQuantityForm = false;
        modifyForm=false;
        current=null;
        this.getSelected();
    }

    public void afFormStock2() {
        bol2 = true;
        bol1 = false;
        updateQuantityForm = false;
        modifyForm = false;
        
    }

    public void annuler12() {
        bol1 = false;
        bol2 = false;
        current=null;
        this.getSelected();
    }

    public Boolean cacheButton() {
        return !((bol1 == true) || (bol2 == true) || (updateQuantityForm == true));
    }

    public Part getMyFile() {
        if (myFile == null) {

        }
        return myFile;
    }

    public void setMyFile(Part myFile) {
        this.myFile = myFile;
    }

    public Part getMyFileRestore() {
        if (myFileRestore == null) {

        }
        return myFileRestore;
    }

    public void setMyFileRestore(Part myFile) {
        this.myFileRestore = myFile;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Produit) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public void prepareCreate() {
        current=null;
        this.getSelected();
        selectedItemIndex = -1;
        //return "Create";
    }

    public void create() {
        if (getSelected().getDesignation().equals("")) {
            JsfUtil.addErrorMessage("Aucune désignation de produit saisie!! Veuillez saisir avant de valider");
            return;
        }
        if (getSelected().getClasse() == null) {
            JsfUtil.addErrorMessage("Aucune famille selectionnée !! Veuillez choisir la famille du produit avant de valider");
            return;
        }
        if (getSelected().getQuantite() == 0) {
            JsfUtil.addSuccessMessage("La quantité du produit sera de zéro(0) - Vous pouvez ajouter la quantité plus tard");
            //return;
        }
        if (getSelected().getNombreExemplaire() == 0) {
            JsfUtil.addErrorMessage("Veuillez entrez le nombre d'exemplaire du produit à stocker (Nombre d'exemplaire >1)!!");
            return;
        }
        try {
            Produit p = this.getFacade().verifyDesignationProduit(this.getSelected().getDesignation());
            if (p != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitNotUnique"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Produit déjà existant!!");
                /*int q=p.getQuantite()+getSelected().getQuantite();
                p.setQuantite(q);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitUpdated"));*/
            } else {
                Calendar c = Calendar.getInstance();
                Date date = c.getTime();
                int retour = getFacade().createNew(current, date);
                if (retour == 0) {
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitCreated"));
                    Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Produit crée avec succès!!");
                    recreateModel();
                    prepareCreate();
                } else {
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                    Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Une erreur de persistance!!");
                }
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
            //return null;
        }
    }

    public Boolean getUpdateQuantityForm() {
        return updateQuantityForm;
    }

    public void setUpdateQuantityForm(Boolean updateQuantityForm) {
        this.updateQuantityForm = updateQuantityForm;
    }

    public int getUpdateQuantity() {
        return updateQuantity;
    }

    public void setUpdateQuantity(int updateQuantity) {
        this.updateQuantity = updateQuantity;
    }

    public void annulerUpdateQuantity() {
        updateQuantityForm = false;
    }

    public void prepareUpdateQuantity() {
        current = (Produit) getAllProduit().getRowData();
        //updateQuantityProd = (Produit) getAllProduit().getRowData();
        System.out.println("Produit selectionné pour ajouter la quantité: " + current.getDesignation());
        updateQuantityForm = true;
        bol1 = false;
        bol2 = false;
        modifyForm=false;
    }

    public void updateProduitQuantity() {
        try {
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            int retour = getFacade().updateProduitQuantity(updateQuantity, current, date);
            if (retour == 0) {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitUpdated"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Produit mis à jour - Quantité ajoutée!!");
                this.prepareCreate();
                updateQuantityForm = false;
                updateQuantity = 0;
            } else {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, "Une erreur de persistence!!");
            }

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public Boolean getModifyForm() {
        return modifyForm;
    }

    public void setModifyForm(Boolean modifyForm) {
        this.modifyForm = modifyForm;
    }

    public void annulerModify() {
        modifyForm = false;
    }

    public void prepareEdit() {
        current = (Produit) getAllProduit().getRowData();
        modifyForm = true;
        updateQuantityForm = false;
        bol1 = false;
        bol2 = false;
        //modifyForm=false;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        //return "Edit";
    }

    public void update() {
        if (getSelected().getDesignation().equals("")) {
            JsfUtil.addErrorMessage("Aucune désignation de produit saisie!! Veuillez saisir avant de valider");
            return;
        }
        if (getSelected().getClasse().getNom().equals("")) {
            JsfUtil.addErrorMessage("Aucune famille selectionnée !! Veuillez choisir la famille du produit avant de valider");
            return;
        }
        if (getSelected().getNombreExemplaire() == 0) {
            JsfUtil.addErrorMessage("Veuillez entrez le nombre d'exemplaire du produit à stocker (Nombre d'exemplaire >1)!!");
            return;
        }
        try {
            getFacade().update(current);
            modifyForm = false;
            this.prepareCreate();
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitUpdated"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Produit mis à jour avec succès!!");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void destroy() {
        current = (Produit) getAllProduit().getRowData();
        System.out.println("Quantité en présente en stock " + current.getQuantite() + " du produit à supprimer " + current.getDesignation());
        if (current.getQuantite() == 0) {
            performDestroy();
            recreateModel();
            JsfUtil.addSuccessMessage("Produit supprimé avec succès!!");
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Produit supprimé avec succès!!");
            
        } else {
            JsfUtil.addErrorMessage("Suppression produit impossible, veuillez d'abord effectuer un destockage complet du produit!!");
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Suppression produit impossible, veuillez d'abord effectuer un destockage complet du produit!!");
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ProduitDeleted"));
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
        this.prepareCreate();
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
        allProduit = null;
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

    public String saveBD() {

        Date hh = Calendar.getInstance().getTime();
        File f ;
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + saveDBDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileSaveDB = f.getAbsolutePath();

        f = new File(pathFileSaveDB + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileSaveDB = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileSaveDB + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileSaveDB + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileSaveDB = f.getAbsolutePath();

        String nomFichier = "saveAllDB" + formaterDateFile.format(hh) + ".backup";

        try {
            String cmd = "pg_dump.exe -h 127.0.0.1 -p 5432 -U postgres -w -f \"" + pathFileSaveDB + sepa + nomFichier + "\" edifice_stock";
            System.out.println("Sauvegarder la Base de données!");
            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            java.lang.Process p = rt.exec(cmd);
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Sauvegarde Base de données reussie!!");
        } catch (Exception ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nomFichier;
    }

    public void downloadBackupFile() {
        String chemin = saveBD();
        File file = new File(pathFileSaveDB + Util.getSeparateurSys() + chemin);
        //HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setHeader("Content-Disposition", "attachment;filename=" + chemin);
        response.setContentLength((int) file.length());
        FileInputStream input = null;
        try {
            int i = 0;
            input = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while ((i = input.read(buffer)) != -1) {
                response.getOutputStream().write(buffer);
                response.getOutputStream().flush();
            }
            facesContext.responseComplete();
            facesContext.renderResponse();
        } catch (IOException e) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public Produit getProduit(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    public String getPathFileDestockage() {
        return pathFileDestockage;
    }

    public void setPathFileDestockage(String pathFileDestockage) {
        this.pathFileDestockage = pathFileDestockage;
    }

    /* String convertInt(int n) {
        if (n < 10) {
            return "0" + n;
        }
        return n + "";
    }*/
    //Fonction permettant de créer le fichier du destockage en cours
    public List<String> createDestockageFile(String destinataireS, Date hh) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        //Flux d'écriture
        BufferedOutputStream sortie;
        File f = new File(".");
        pathFileDestockage = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + destockageDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDestockage = f.getAbsolutePath();

        f = new File(pathFileDestockage + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDestockage = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileDestockage + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileDestockage + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDestockage = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "Destockage" + formaterDateFile.format(hh) + ".pdf";

        //String nomFichier = "Destockage" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileDestockage + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileDestockage + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph(formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12 = new Paragraph(titreDocument, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_LEFT);
            Paragraph entete5 = new Paragraph(acteurAutre + destinataireS, police_entete_1_f);
            entete5.setAlignment(Element.ALIGN_LEFT);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete4);
            document.add(entete5);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Nombre d'exemplaire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Compter le nombre de produit destocké
            int destocker = 0;

            //Création des différentes lignes du tableau
            for (ProduitASortir h : getListProduitASortir()) {

                Phrase ph3 = new Phrase(h.getProduit().getDesignation(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);

                Phrase ph4 = new Phrase(h.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                destocker += h.getQuantite();

                Phrase ph5 = new Phrase(h.getProduit().getClasse() + "", police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);

                Phrase ph6 = new Phrase(h.getProduit().getNombreExemplaire() + "", police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
            }
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);
            Phrase nombre_destocke = new Phrase("Nombre total de produit destocké: " + destocker, police_entete);
            document.add(nombre_destocke);
            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            /*Header footer = new Header("Copyright Séssandè corporation aôut 2013","");
						//Footer footer1;
						//footer.setBorder(Rectangle.NO_BORDER);
						document.addHeader("Copyright Séssandè corporation aôut 2013","");//.setFooter(footer);
             */
            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileDestockage + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    //Fonction pour le téléchargement du fichier du destockage en cours
    public void downloadDestockageFile(String destinataireS, Date h) {

        List<String> liste = createDestockageFile(destinataireS, h);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(getPathFileDestockage() + Util.getSeparateurSys() + chemin, getPathFileDestockage() + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(getPathFileDestockage() + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(getPathFileDestockage() + Util.getSeparateurSys() + nomfichier);

        //String chemin = createDestockageFile(destinataireS, h);
        //File file = new File(getPathFileDestockage() + Util.getSeparateurSys() + chemin);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setHeader("Content-Disposition", "attachment;filename=" + nomfichier);
        response.setContentLength((int) file.length());
        FileInputStream input = null;
        try {
            int i = 0;
            input = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while ((i = input.read(buffer)) != -1) {
                response.getOutputStream().write(buffer);
                response.getOutputStream().flush();
            }
            facesContext.responseComplete();
            facesContext.renderResponse();
        } catch (IOException e) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
               Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public String getPathFileStockage() {
        return pathFileStockage;
    }

    public void setPathFileStockage(String pathFileStockage) {
        this.pathFileStockage = pathFileStockage;
    }

    //Ecrire le Validator plus tard pour le chargement des fichiers du type souhaité
    public void stockerFile() {
        String chemin = "";
        try {
            pathFileStockage = myFile.getName();
            chemin = myFile.getSubmittedFileName();

            System.out.println("File Stock type: " + myFile.getContentType() + " extension: " + chemin.contains(".csv"));
        } catch (NullPointerException e) {
            JsfUtil.addErrorMessage("Veuillez choisir un fichier avant de cliquer sur Stocker");
        }
        if (!chemin.contains(".csv")) {
            JsfUtil.addErrorMessage("Le fichier choisi doit être du type CSV");
            return;
        }

        //JsfUtil.addSuccessMessage("PathFile: "+getPathFile()+" Chemin: "+chemin);
        System.out.println("PathFile: " + getPathFileStockage() + " Chemin: " + chemin);
        try {
            File f = new File(".");
            pathFileStockage = f.getAbsolutePath();
            System.out.println("PathFile: " + getPathFileStockage());
            //Calendar
            Calendar c = Calendar.getInstance();
            heureStockageEnCours = c.getTime();
            Date hh = c.getTime();
            int mois = hh.getMonth() + 1;
            //int jour = hh.getDate();
            int year = hh.getYear() + 1900;
            //Partie qui déterminera le type de séparateur
            String sepa = Util.getSeparateurSys();
            int in = pathFileStockage.lastIndexOf(".");
            pathFileStockage = pathFileStockage.substring(0, in);
            System.out.println("PathFile: " + getPathFileStockage());
            System.out.println("Sepa: " + sepa);
            //Ensuite il faut sauvegarder le fichier qui a été chargé pour la comptabilité et vérification plus tard
            //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
            f = new File(Util.getDirectoryParent() + sepa + stockageDirectory + sepa);
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileStockage = f.getAbsolutePath();

            f = new File(pathFileStockage + sepa + "Annee " + year + sepa);
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileStockage = f.getAbsolutePath();

            if (mois == 1) {
                f = new File(pathFileStockage + sepa + "1er_mois" + sepa);
            } else {
                f = new File(pathFileStockage + sepa + mois + "e_mois" + sepa);
            }
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileStockage = f.getAbsolutePath();

            String nomFichier = "Stockage" + formaterDateFile.format(hh) + ".csv";
            //pathFileStockage = sepa + "Stockage" + sepa;
            //myFile.write(sepa+"Stockage"+sepa+"stockage"+jour+mois+year+".csv");
            //Enregistrement du fichier uploadé sur le disque
            writePartFile(myFile, nomFichier, pathFileStockage); // variable chemin coe second paramètre après
            //Lecture du fichier CSV pour stockage des produits
            readCSVFile(pathFileStockage + sepa + nomFichier, heureStockageEnCours);
            //Les produits ont été stockés
            recreateModel();
            //System.out.println("Stockage du fichier effectué avec succés!!!");
            //JsfUtil.addSuccessMessage("Stockage du fichier effectué avec Succés!!!");
        } catch (Exception ioe) {
            System.out.println("Erreur IO: " + ioe);

        }
        /*catch (Exception e) {
            System.out.println("Erreur: " + e);
        }*/
    }

    //Fonction pour écrire le fichier de type part sur le disque en ".csv"
    private void writePartFile(Part part, String nomFichier, String chemin) throws Exception {
        /* Prépare les flux. */
        BufferedInputStream entree = null;
        BufferedOutputStream sortie = null;
        try {
            /* Ouvre les flux. */
            entree = new BufferedInputStream(part.getInputStream(), TAILLE_TAMPON);
            sortie = new BufferedOutputStream(new FileOutputStream(new File(chemin + Util.getSeparateurSys() + nomFichier)),
                    TAILLE_TAMPON);

            /*
             * Lit le fichier reçu et écrit son contenu dans un fichier sur le
             * disque.
             */
            int longueur;
            while ((longueur = entree.read(tampon)) > 0) {
                sortie.write(tampon, 0, longueur);
            }
        } finally {
            try {
                if (sortie != null) {
                    sortie.close();
                }
            } catch (Exception e1) {
            }
            try {
                if (entree != null) {
                    entree.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    //Après avoir enregistré le fichier en ".cvs" il faut le lire et stocker les produits
    public int readCSVFile(String pathcsvf, Date date) {
        int retour = -1;
        try {
            retour = this.getFacade().readCVSFile(pathcsvf, date);
            if (retour == 0) {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("StockageCSVSuccessful"));
            } else {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("StockageCSVError"));
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("StockageCSVError"));
        }
        return retour;
    }

    //Fonction permettant de créer le fichier des inventaires
    public List<String> createInventaireFile(Date hh) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        //Flux d'écriture
        BufferedOutputStream sortie;
        File f = new File(".");
        pathFileInventaire = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + inventaireDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileInventaire = f.getAbsolutePath();

        f = new File(pathFileInventaire + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileInventaire = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileInventaire + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileInventaire + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileInventaire = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "Inventaire" + formaterDateFile.format(hh) + ".pdf";

        //String nomFichier = "Inventaire" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileInventaire + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileInventaire + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            document.setPageCount(1);
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph(formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12 = new Paragraph(titreDocumentInventaire, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph(acteurPrincipalInventaire + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_LEFT);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            List<ClasseProduit> listeFamille = getEjbClasseProduitFacade().orderAllClasse();
            for (ClasseProduit c : listeFamille) {

                Paragraph nom_famille = new Paragraph(c.getNom(), police_entete);
                document.add(nom_famille);

                Paragraph sep = new Paragraph("                                                       ", police_entete);
                document.add(sep);

                //Création du tableau pour afficher le rapport
                //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(98);//c'etait 95

                PdfPCell c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(c1);

                c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(c1);

                /*c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);*/
                c1 = new PdfPCell(new Phrase("Nombre d'exemplaire", police_premiere_ligne_tableau));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(c1);

                //le nombre de lignes constituant la ligne des titres
                table.setHeaderRows(1);

                //Création des différentes lignes du tableau
                getFacade().orderProduitClasse(c.getId()).stream().map((p) -> {
                    Phrase ph3 = new Phrase(p.getDesignation(), police_tableau);
                    PdfPCell pcell3 = new PdfPCell(ph3);
                    pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell3);
                    Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                    PdfPCell pcell4 = new PdfPCell(ph4);
                    pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell4);
                    Phrase ph6 = new Phrase(p.getNombreExemplaire() + "", police_tableau);
                    return ph6;
                }).map((ph6) -> new PdfPCell(ph6)).map((pcell6) -> {
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    return pcell6;
                }).forEach((pcell6) -> {
                    table.addCell(pcell6);
                });

                document.add(table);
            }
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());

            Phrase espace = new Phrase("      ");
            document.add(espace);
            Phrase nombre_destocke = new Phrase("Fin de l'inventaire du stock ", police_entete);
            document.add(nombre_destocke);
            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            //Header footer = new Header("Inventaire du stock - EDIFICE","Inventaire du stock - EDIFICE");
            //Footer footer1;
            //footer.setBorder(Rectangle.NO_BORDER);
            document.addHeader("Inventaire du stock - EDIFICE", "Inventaire du stock - EDIFICE");
            //.setFooter(footer);*

            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileInventaire + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    //Fonction pour le téléchargement du fichier du destockage en cours
    public void downloadInventaireFile() {
        Calendar c = Calendar.getInstance();
        Date h = c.getTime();

        List<String> liste = createInventaireFile(h);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileInventaire + Util.getSeparateurSys() + chemin, this.pathFileInventaire + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileInventaire + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileInventaire + Util.getSeparateurSys() + nomfichier);

        //String chemin = createInventaireFile(h);
        //File file = new File(this.pathFileInventaire + Util.getSeparateurSys() + chemin);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setHeader("Content-Disposition", "attachment;filename=" + nomfichier);
        response.setContentLength((int) file.length());
        FileInputStream input = null;
        try {
            int i = 0;
            input = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while ((i = input.read(buffer)) != -1) {
                response.getOutputStream().write(buffer);
                response.getOutputStream().flush();
            }
            facesContext.responseComplete();
            facesContext.renderResponse();
        } catch (IOException e) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Ecrire le Validator plus tard pour le chargement des fichiers du type souhaité
    public void restoreDBFile() {
        String chemin;
        try {
            pathFileRestoreDB = myFileRestore.getName();
            chemin = myFileRestore.getSubmittedFileName();
        } catch (NullPointerException e) {
            JsfUtil.addErrorMessage("Veuillez choisir le fichier de type BACKUP");
            return;
        }

        System.out.println("File Stock type: " + myFileRestore.getContentType());

        if (!chemin.contains(".backup")) {
            JsfUtil.addErrorMessage("Le fichier choisi doit être du type BACKUP");
            return;
        }

        //JsfUtil.addSuccessMessage("PathFile: "+getPathFile()+" Chemin: "+chemin);
        System.out.println("PathFile: " + pathFileRestoreDB + " Chemin: " + chemin);
        try {
            File f = new File(".");
            pathFileRestoreDB = f.getAbsolutePath();
            System.out.println("PathFile: " + pathFileRestoreDB);
            //Calendar
            Calendar c = Calendar.getInstance();
            Date hh = c.getTime();
            int mois = hh.getMonth() + 1;
            //int jour = hh.getDate();
            int year = hh.getYear() + 1900;
            //Partie qui déterminera le type de séparateur
            String sepa = Util.getSeparateurSys();
            int in = pathFileRestoreDB.lastIndexOf(".");
            pathFileRestoreDB = pathFileRestoreDB.substring(0, in);
            System.out.println("PathFile: " + pathFileRestoreDB);
            System.out.println("Sepa: " + sepa);
            //Ensuite il faut sauvegarder le fichier qui a été chargé pour la comptabilité et vérification plus tard
            //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
            f = new File(Util.getDirectoryParent() + sepa + restoreDBDirectory + sepa);
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileRestoreDB = f.getAbsolutePath();

            f = new File(pathFileRestoreDB + sepa + "Annee " + year + sepa);
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileRestoreDB = f.getAbsolutePath();

            if (mois == 1) {
                f = new File(pathFileRestoreDB + sepa + "1er_mois" + sepa);
            } else {
                f = new File(pathFileRestoreDB + sepa + mois + "e_mois" + sepa);
            }
            if (!f.exists()) {
                f.mkdir();
            }
            System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
            pathFileRestoreDB = f.getAbsolutePath();

            String nomFichier = "RestaurationDB" + formaterDateFile.format(hh) + ".backup";
            //pathFileStockage = sepa + "Stockage" + sepa;
            //myFile.write(sepa+"Stockage"+sepa+"stockage"+jour+mois+year+".csv");
            //Enregistrement du fichier uploadé sur le disque
            writePartFileDB(myFileRestore, nomFichier, pathFileRestoreDB); // variable chemin coe second paramètre après
            //Lecture du fichier CSV pour stockage des produits
            readRestoreFile(pathFileRestoreDB + sepa + nomFichier);
            //Les produits ont été stockés
            recreateModel();

            System.out.println("Restauration Base de donnée effectuée avec Succés!!!");
            JsfUtil.addSuccessMessage("Restauration Base de donnée effectuée avec Succés!!!");
        } catch (Exception ioe) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ioe);

        }
        /*catch (Exception e) {
            System.out.println("Erreur: " + e);
        }*/
    }

    //Fonction pour écrire le fichier de type part sur le disque en ".csv"
    private void writePartFileDB(Part part, String nomFichier, String chemin) throws Exception {
        System.out.println("Chemin du fichier de restauration: " + chemin + " nom du fichier: " + nomFichier);
        /* Prépare les flux. */
        BufferedInputStream entree = null;
        BufferedOutputStream sortie = null;
        try {
            /* Ouvre les flux. */
            entree = new BufferedInputStream(part.getInputStream(), TAILLE_TAMPON);
            sortie = new BufferedOutputStream(new FileOutputStream(new File(chemin + Util.getSeparateurSys() + nomFichier)),
                    TAILLE_TAMPON);

            /*
             * Lit le fichier reçu et écrit son contenu dans un fichier sur le
             * disque.
             */
            int longueur;
            while ((longueur = entree.read(tampon)) > 0) {
                sortie.write(tampon, 0, longueur);
            }
        } finally {
            try {
                if (sortie != null) {
                    sortie.close();
                }
            } catch (Exception e1) {
            }
            try {
                if (entree != null) {
                    entree.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    //Après avoir enregistré le fichier en ".cvs" il faut le lire et stocker les produits
    public void readRestoreFile(String pathcsvf) {
        try {
            String cmd = "psql -h 127.0.0.1 -p 5432 -U postgres -w -f \"" + pathcsvf + "\" edifice_stock";
            System.out.println("Restauration Base de données!");
            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            java.lang.Process p = rt.exec(cmd);
        } catch (Exception ex) {
            Logger.getLogger(ProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return retour;
    }

    private String logout() {
        HttpSession session = Util.getSession();
        session.invalidate();
        Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Resatauration DataBase!!!");
        return "/faces/auth/login";
    }

    public void enCours() {
        JsfUtil.addErrorMessage("En cours de développement - pas encore terminée!");
    }

    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfContentByte pagecontent;
        int bottomHorizontalOffset = 425;
        int bottomVerticalOffset = 25;
        for (int i = 0; i < n;) {
            pagecontent = stamper.getUnderContent(++i);
            //pagecontent.beginText();
            //ColumnText.showTextAligned(pagecontent, Element.ALIGN_RIGHT,
            // new Phrase(String.format("page %s of %s", i, n)), 559, 806, 0);

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            pagecontent.beginText();
            pagecontent.setFontAndSize(bf, 10);
            pagecontent.setTextMatrix(bottomHorizontalOffset, bottomVerticalOffset);
            pagecontent.showText(String.format("page %s sur %s", i, n));
            pagecontent.endText();
        }
        stamper.close();
        reader.close();
    }

    @FacesConverter(forClass = Produit.class)
    public static class ProduitControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ProduitController controller = (ProduitController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "produitController");
            return controller.getProduit(getKey(value));
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
            if (object instanceof Produit) {
                Produit o = (Produit) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Produit.class.getName());
            }
        }

    }

}
