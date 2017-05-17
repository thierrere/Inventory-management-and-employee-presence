package com.management.controllers;

import com.management.jpa.HistoriqueProduit;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.jpa.Produit;
import com.management.sessionbeans.HistoriqueProduitFacade;
import com.management.utils.Util;
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.Exception;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;

@ManagedBean(name="historiqueProduitController")
@SessionScoped
public class HistoriqueProduitController implements Serializable {

    private HistoriqueProduit current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.HistoriqueProduitFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    //private List<HistoriqueProduit> allHistoryAffich;
    //private List<HistoriqueProduit> allHistory, dayThisHistory, weekThisHistory, monthThisHistory, yearThisHistory;
    //private List<HistoriqueProduit> periodHistory, dayHistory, weekHistory, monthHistory, yearHistory;
    //private List<HistoriqueProduit> allHistoryUser, dayHistoryUser, weekHistoryUser, monthHistoryUser;
    //private List<HistoriqueProduit> allHistoryUserStockage, dayHistoryUserStockage, weekHistoryUserStockage, monthHistoryUserStockage;
    // private List<HistoriqueProduit> allHistoryUserDeStockage, dayHistoryUserDeStockage, weekHistoryUserDeStockage, monthHistoryUserDeStockage;
    //private List<HistoriqueProduit> allHistoryDest, dayHistoryDest, weekHistoryDest, monthHistoryDest;
    private final String historiqueDirectory = "Historique_Produit";
    String pathFileHistoriqueThisDay, pathFileHistoriqueThisWeek, pathFileHistoriqueThisMonth, pathFileHistoriqueThisYear;
    String pathFileHistoriqueDay, pathFileHistoriqueWeek, pathFileHistoriqueMonth;
    String pathFileHistoriquePeriod, pathFileHistoriqueYear, pathFileAllHistory, pathFileHistoriquePeriodProduit;
    static final int TAILLE_TAMPON = 10240; // 10 ko
    byte[] tampon = new byte[TAILLE_TAMPON];
    private Boolean bolJDlg = false, bolPDlg = false, bolWDlg = false, bolMDlg = false, bolYDlg = false, bolPPDlg = false;
    private Date dateY, dateM, dateW, dateP1, dateP2, dateJ, datePP1, datePP2;
    Produit produit;
    private Boolean bolJDlgS = false, bolPDlgS = false, bolWDlgS = false, bolMDlgS = false, bolYDlgS = false, bolPPDlgS = false;
    //private Date dateYS, dateMS, dateWS, dateP1S, dateP2S, datePP1S, datePP2S;
    //Produit produitS;
    private Boolean bolJDlgDs = false, bolPDlgDs = false, bolWDlgDs = false, bolMDlgDs = false, bolYDlgDs = false, bolPPDlgDs = false;
    //private Date dateYDs, dateMDs, dateWDs, dateP1Ds, dateP2Ds, dateJDs, datePP1Ds, datePP2Ds;
    //Produit produitDs;
    //Flux d'écriture
    BufferedOutputStream sortie;

    //La partie pour les formulaire caché servant à la gnération des historiques généraux
    public Date getDateJ() {
        return dateJ;
    }

    public void setDateJ(Date date1) {
        this.dateJ = date1;
    }

    public Boolean getBolJDlg() {
        return bolJDlg;
    }

    public void setBolJDlg(Boolean bolDlg) {
        this.bolJDlg = bolDlg;
    }

    public void annulerJDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                //dateJ = null;
                break;
            case "Destockage":
                bolJDlgDs = false;
                //dateJDs = null;
                break;
            default:
                bolJDlg = false;
                
                break;
        }
        dateJ = null;

        //return bolDlg;
    }

    public void ouvrirJDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = true;
                bolPDlgS = false;
                bolPPDlgS = false;
                bolWDlgS = false;
                bolMDlgS = false;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = true;
                bolPDlgDs = false;
                bolPPDlgDs = false;
                bolWDlgDs = false;
                bolMDlgDs = false;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = true;
                bolPDlg = false;
                bolPPDlg = false;
                bolWDlg = false;
                bolMDlg = false;
                bolYDlg = false;
                break;
        }

        //return bolDlg;
    }

    public void takeDateHist(String action) {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateJ != null) {
            downloadHistoryDayFile(dateJ, action);
            dateJ = null;
            bolJDlg = false;
            bolJDlgS = false;
            bolJDlgDs = false;
        } else {
            JsfUtil.addErrorMessage("Veuillez choisir une date");
        }

    }

    public Date getDateW() {
        return dateW;
    }

    public void setDateW(Date date1) {
        this.dateW = date1;
    }

    public Boolean getBolWDlg() {
        return bolWDlg;
    }

    public void setBolWDlg(Boolean bolDlg) {
        this.bolWDlg = bolDlg;
    }

    public void annulerWDlg(String action) {
        switch (action) {
            case "Stockage":
                bolWDlgS = false;
                //dateWS = null;
                break;
            case "Destockage":
                bolWDlgDs = false;
                //dateWDs = null;
                break;
            default:
                bolWDlg = false;
                
                break;
        }
        dateW = null;

        //return bolDlg;
    }

    public void ouvrirWDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = false;
                bolPPDlgS = false;
                bolWDlgS = true;
                bolMDlgS = false;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = false;
                bolPPDlgDs = false;
                bolWDlgDs = true;
                bolMDlgDs = false;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = false;
                bolPDlg = false;
                bolPPDlg = false;
                bolWDlg = true;
                bolMDlg = false;
                bolYDlg = false;
                break;
        }

    }

    public void takeWeekHist(String action) {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateW != null) {
            downloadHistoryWeekFile(dateW, action);
            dateW = null;
            bolWDlg = false;
            bolWDlgS = false;
            bolWDlgDs = false;
        } else {
            JsfUtil.addErrorMessage("Veuillez choisir une date");
        }
    }

    public Date getDateM() {
        return dateM;
    }

    public void setDateM(Date date1) {
        this.dateM = date1;
    }

    public Boolean getBolMDlg() {
        return bolMDlg;
    }

    public void setBolMDlg(Boolean bolDlg) {
        this.bolMDlg = bolDlg;
    }

    public void annulerMDlg(String action) {
        switch (action) {
            case "Stockage":
                bolMDlgS = false;
                //dateMS = null;
                break;
            case "Destockage":
                bolMDlgDs = false;
                //dateMDs = null;
                break;
            default:
                bolMDlg = false;
                
                break;
        }
        dateM = null;

        //return bolDlg;
    }

    public void ouvrirMDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = false;
                bolPPDlgS = false;
                bolWDlgS = false;
                bolMDlgS = true;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = false;
                bolPPDlgDs = false;
                bolWDlgDs = false;
                bolMDlgDs = true;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = false;
                bolPDlg = false;
                bolPPDlg = false;
                bolWDlg = false;
                bolMDlg = true;
                bolYDlg = false;
                break;
        }

    }

    public void takeMonthHist(String action) {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateM != null) {
            downloadHistoryMonthFile(dateM, action);
            dateM = null;
            bolMDlg = false;
            bolMDlgS = false;
            bolMDlgDs = false;
        } else {
            JsfUtil.addErrorMessage("Veuillez choisir une date");
        }
    }

    public Date getDateP1() {
        return dateP1;
    }

    public void setDateP1(Date date1) {
        this.dateP1 = date1;
    }

    public Date getDateP2() {
        return dateP2;
    }

    public void setDateP2(Date date1) {
        this.dateP2 = date1;
    }

    public Boolean getBolPDlg() {
        return bolPDlg;
    }

    public void setBolPDlg(Boolean bolDlg) {
        this.bolPDlg = bolDlg;
    }

    public void annulerPDlg(String action) {
        switch (action) {
            case "Stockage":
                bolPDlgS = false;
                //dateP1S = null;
                //dateP2S = null;
                break;
            case "Destockage":
                bolPDlgDs = false;
                //dateP1Ds = null;
                //dateP2Ds = null;
                break;
            default:
                bolPDlg = false;
                
                break;
        }
        
        dateP1 = null;
        dateP2 = null;

        //return bolDlg;
    }

    public void ouvrirPDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = true;
                bolPPDlgS = false;
                bolWDlgS = false;
                bolMDlgS = false;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = true;
                bolPPDlgDs = false;
                bolWDlgDs = false;
                bolMDlgDs = false;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = false;
                bolPDlg = true;
                bolPPDlg = false;
                bolWDlg = false;
                bolMDlg = false;
                bolYDlg = false;
                break;
        }

    }

    public void takePeriodHist(String action) {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if ((dateP1 == null) || (dateP2 == null)) {
            JsfUtil.addErrorMessage("Veuillez bien délimiter la période grâce aux dates!!");
        } else {
            downloadHistoryPeriodFile(dateP1, dateP2, action);
            dateP1 = null;
            dateP2 = null;
            bolPDlg = false;
            bolPDlgS = false;
            bolPDlgDs = false;
        }
    }

    public Date getDatePP1() {
        return datePP1;
    }

    public void setDatePP1(Date date1) {
        this.datePP1 = date1;
    }

    public Date getDatePP2() {
        return datePP2;
    }

    public void setDatePP2(Date date1) {
        this.datePP2 = date1;
    }

    public Boolean getBolPPDlg() {
        return bolPPDlg;
    }

    public void setBolPPDlg(Boolean bolDlg) {
        this.bolPPDlg = bolDlg;
    }

    public void annulerPPDlg(String action) {
        switch (action) {
            case "Stockage":
                bolPPDlgS = false;
                //datePP1S = null;
                //datePP2S = null;
                break;
            case "Destockage":
                bolPPDlgDs = false;
                //datePP1Ds = null;
                //datePP2Ds = null;
                break;
            default:
                bolPPDlg = false;
                
                break;
        }

        datePP1 = null;
        datePP2 = null;
        //return bolDlg;
    }

    public void ouvrirPPDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = false;
                bolPPDlgS = true;
                bolWDlgS = false;
                bolMDlgS = false;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = false;
                bolPPDlgDs = true;
                bolWDlgDs = false;
                bolMDlgDs = false;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = false;
                bolPDlg = false;
                bolPPDlg = true;
                bolWDlg = false;
                bolMDlg = false;
                bolYDlg = false;
                break;
        }

    }

    public void takePeriodPHist(String action) {

        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (produit == null || datePP1 == null || datePP2 == null) {
            JsfUtil.addErrorMessage("Veuillez saisir le produit et délimiter la période");
        } else {
            downloadHistoryPeriodProduitFile(produit, datePP1, datePP2, action);
            datePP1 = null;
            datePP2 = null;
            bolPPDlg = false;
            bolPPDlgS = false;
            bolPPDlgDs = false;
            produit = null;
        }
    }

    public Date getDateY() {
        return dateY;
    }

    public void setDateY(Date date1) {
        this.dateY = date1;
    }

    public Boolean getBolYDlg() {
        return bolYDlg;
    }

    public void setBolYDlg(Boolean bolDlg) {
        this.bolYDlg = bolDlg;
    }

    public void annulerYDlg(String action) {
        switch (action) {
            case "Stockage":
                bolYDlgS = false;
                //dateYS = null;
                break;
            case "Destockage":
                bolYDlgDs = false;
                //dateYDs = null;
                break;
            default:
                bolYDlg = false;
                
                break;
        }
        dateY = null;
        //return bolDlg;
    }

    public void ouvrirYDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = false;
                bolPPDlgS = false;
                bolWDlgS = false;
                bolMDlgS = false;
                bolYDlgS = true;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = false;
                bolPPDlgDs = false;
                bolWDlgDs = false;
                bolMDlgDs = false;
                bolYDlgDs = true;
                break;
            default:
                bolJDlg = false;
                bolPDlg = false;
                bolPPDlg = false;
                bolWDlg = false;
                bolMDlg = false;
                bolYDlg = true;
                break;
        }

    }

    public void takeYearHist(String action) {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateY == null) {
            JsfUtil.addErrorMessage("Veuillez choisir une date dans l'année souhaitée");
        } else {
            downloadHistoryYearFile(dateY, action);
            dateY = null;
            bolYDlg = false;
            bolYDlgS = false;
            bolYDlgDs = false;
        }
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

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

    //Les variables textes pour l'entête des documents
    String nom_entreprise = "EDIFICE CONSTRUCTION";
    String devise = "La qualité notre engagement";
    String titreDocumentDay = "Historique Journalier des produits";
    String titreDocumentWeek = "Historique Hebdomadaire des produits";
    String titreDocumentMonth = "Historique Mensuel des produits";
    String titreDocumentYear = "Historique annuel des produits";
    String titreDocumentPeriod = "Historique sur une période";
    String titreDocumentPeriodProduit = "Historique sur une période du Produit: ";
    String titreDocumentAll = "Toute l'Historique";
    String titreDocumentDayS = "Stockage Journalier des produits";
    String titreDocumentWeekS = "Stockage Hebdomadaire des produits";
    String titreDocumentMonthS = "Stockage Mensuel des produits";
    String titreDocumentYearS = "Stockage annuel des produits";
    String titreDocumentPeriodS = "Stockage sur une période";
    String titreDocumentPeriodProduitS = "Stockage sur une période du Produit: ";
    String titreDocumentAllS = "Tous les stockages";
    String titreDocumentDayDs = "Destockage Journalier des produits";
    String titreDocumentWeekDs = "Destockage Hebdomadaire des produits";
    String titreDocumentMonthDs = "Destockage Mensuel des produits";
    String titreDocumentYearDs = "Destockage annuel des produits";
    String titreDocumentPeriodDs = "Destockage sur une période";
    String titreDocumentPeriodProduitDs = "Destockage sur une période du Produit: ";
    String titreDocumentAllDs = "Tous les destockages";
    String acteurPrincipal = "Historique généré par : ";

    SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' HH:mm:ss");
    SimpleDateFormat formaterDateHistory = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat formaterTimestamp = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat formaterDateOnly = new SimpleDateFormat("dd MMMMM yyyy");
    SimpleDateFormat formaterMonthYearOnly = new SimpleDateFormat("MMMM yyyy");
    SimpleDateFormat formaterYearOnly = new SimpleDateFormat("yyyy");
    SimpleDateFormat formaterDateFile = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
    SimpleDateFormat formaterDateFile2 = new SimpleDateFormat("dd_MM_yyyy");
    SimpleDateFormat formaterMonthFile = new SimpleDateFormat("MM_yyyy");

    public HistoriqueProduitController() {
    }

    public HistoriqueProduit getSelected() {
        if (current == null) {
            current = new HistoriqueProduit();
            selectedItemIndex = -1;
        }
        return current;
    }

    private HistoriqueProduitFacade getFacade() {
        return ejbFacade;
    }

    /*String convertInt(int n) {
        if (n < 10) {
            return "0" + n;
        }
        return n + "";
    }
    
    String convertDate(Date d){
        int mois = d.getMonth() + 1;
        int jour = d.getDate();
        int year = d.getYear() + 1900;
        String h_m_s = convertInt(d.getHours()) + "h" + convertInt(d.getMinutes()) + "m" + convertInt(d.getSeconds()) + "s";
        
        return convertInt(jour) + "/" + convertInt(mois) + "/" + year + " " + h_m_s;
    }
    
    String convertDateSansHeure(Date d){
        int mois = d.getMonth() + 1;
        int jour = d.getDate();
        int year = d.getYear() + 1900;
        
        return convertInt(jour) + "/" + convertInt(mois) + "/" + year;
    }*/
    public void annulerDlg(String action) {
        switch (action) {
            case "Stockage":
                bolJDlgS = false;
                bolPDlgS = false;
                bolWDlgS = false;
                bolMDlgS = false;
                bolYDlgS = false;
                break;
            case "Destockage":
                bolJDlgDs = false;
                bolPDlgDs = false;
                bolWDlgDs = false;
                bolMDlgDs = false;
                bolYDlgDs = false;
                break;
            default:
                bolJDlg = false;
                bolPDlg = false;
                bolWDlg = false;
                bolMDlg = false;
                bolYDlg = false;
                break;
        }

        //return bolDlg;
    }
    //h.action like '%Destockage%' and
    
    public List<HistoriqueProduit> getAllHistoryAffich() {
        //allHistory = this.getFacade().findAll();
        String req;
        req = "select * from historique_produit h order by date desc";
        List<HistoriqueProduit> all = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return all;
    }

    public List<HistoriqueProduit> getAllHistory(String action) {
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' order by date desc";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' order by date desc";
                break;
            default:
                req = "select * from historique_produit h order by date desc";
                break;
        }
        //allHistory = this.getFacade().findAll();
        List<HistoriqueProduit> all = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return all;
    }

    public List<HistoriqueProduit> getYearThisHistory(String action) {
        Calendar c = Calendar.getInstance();
        List<HistoriqueProduit> liste = getYearHistory(c.getTime(), action);
        return liste;
    }

    //h.action like '%Destockage%' and
    public List<HistoriqueProduit> getYearHistory(Date d, String action) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and extract(year from h.date)=extract (year from TIMESTAMP'" + t + "')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and extract(year from h.date)=extract (year from TIMESTAMP'" + t + "')";
                break;
            default:
                req = "select * from historique_produit h where extract(year from h.date)=extract (year from TIMESTAMP'" + t + "')";
                break;
        }
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<String> getUserDayDestinataire() {
        String req = "select distinct h.destinataire from historique_produit h where h.action like '%Destockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd') and h.users='" + Util.getUserId() + "'";
        List<String> list = (List<String>) entityManager.createNativeQuery(req).getResultList();
        return list;
    }

    public List<String> getDayDestinataire() {
        String req = "select distinct h.destinataire from historique_produit h where h.action like '%Destockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        List<String> list = (List<String>) entityManager.createNativeQuery(req).getResultList();
        return list;
    }

    public List<HistoriqueProduit> getDayThisHistory(String action) {
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
                break;
            //String req = "select h from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
                break;
            default:
                req = "select * from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
                break;
        }

        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        // System.out.println("Taille de l'historique d'aujourd'hui: " + dayThisHistory.size());
        return liste;
    }

    public List<HistoriqueProduit> getDayHistory(Date d, String action) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp('" + t + "'::text, 'yyyy-mm-dd')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp('" + t + "'::text, 'yyyy-mm-dd')";
                break;
            default:
                req = "select * from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp('" + t + "'::text, 'yyyy-mm-dd')";
                break;
        }
        //Datetime t=
        //String req = "select h from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        //System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + dayHistory.size());
        return liste;
    }

    public List<HistoriqueProduit> getPeriodHistory(Date d1, Date d2, String action) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t1;
        t1 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d1)));
        java.sql.Date t2;
        t2 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d2)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where  h.action like '%Stockage%' and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where  h.action like '%Destockage%' and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
            default:
                req = "select * from historique_produit h where to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
        }
        //Datetime t=
        //String req = "select h from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        //System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + periodHistory.size());
        return liste;
    }

    public List<HistoriqueProduit> getPeriodProduitHistory(Produit p, Date d1, Date d2, String action) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t1;
        t1 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d1)));
        java.sql.Date t2;
        t2 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d2)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and h.produit like '%" + p.getDesignation() + "%'  and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and h.produit like '%" + p.getDesignation() + "%'  and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
            default:
                req = "select * from historique_produit h where h.produit like '%" + p.getDesignation() + "%'  and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.date::text, 'yyyy-mm-dd') and to_timestamp(h.date::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd')";
                break;
        }
        //Datetime t=
        //String req = "select h from historique_produit h where to_timestamp(h.date::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriqueProduit> list = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + list.size());
        return list;
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryDayFile(Date heureGen, Date hh, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);

        File f = new File(".");
        pathFileHistoriqueDay = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueDay = f.getAbsolutePath();

        f = new File(pathFileHistoriqueDay + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueDay = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueDay + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueDay + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueDay = f.getAbsolutePath();
        f = new File(pathFileHistoriqueDay + sepa + "Jours" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueDay = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête          
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentDayS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageJour" + formaterDateFile2.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentDayDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageJour" + formaterDateFile2.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentDay, police_entete_f);
                    stamper = "HistoriqueProduitJour" + formaterDateFile2.format(hh) + ".pdf";
                    //
                    break;
            }
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(date de l'historique:    " + formaterDateOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getDayHistory(hh, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            // PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueDay + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryDayFile(Date h, String action) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryDayFile(hg, h, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueDay + Util.getSeparateurSys() + chemin, pathFileHistoriqueDay + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueDay + Util.getSeparateurSys() + chemin);
        file.delete();

        file = new File(pathFileHistoriqueDay + Util.getSeparateurSys() + nomfichier);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier courant
    public List<String> createHistoryDayFile(String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueThisDay = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        Calendar c = Calendar.getInstance();
        Date hh = c.getTime();

        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisDay = f.getAbsolutePath();

        f = new File(pathFileHistoriqueThisDay + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisDay = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueThisDay + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueThisDay + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisDay = f.getAbsolutePath();
        f = new File(pathFileHistoriqueThisDay + sepa + "Jours" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisDay = f.getAbsolutePath();

        //String nomFichier = "Historique_Produit" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";
        //stamper = "HistoriqueProduitJour" + formaterDateFile.format(hh) + ".pdf";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueThisDay + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueThisDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Récupération du logo de l'entreprise.
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentDayS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageJour" + formaterDateFile.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentDayDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageJour" + formaterDateFile.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentDay, police_entete_f);
                    stamper = "HistoriqueProduitJour" + formaterDateFile.format(hh) + ".pdf";
                    //
                    break;
            }
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(date de l'historique:    " + formaterDateOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);
            //;

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getDayThisHistory(action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph6 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell7 = new PdfPCell(ph7);
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell7);
                Phrase ph8 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph8;
            }).map((ph8) -> new PdfPCell(ph8)).map((pcell8) -> {
                pcell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell8;
            }).forEach((pcell8) -> {
                table.addCell(pcell8);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
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
            document.close();

            //onEndPage(writer, document);
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueThisDay + sepa + nomFichier);
        //document.close();
        //onEndPage(writer, document);
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryDayFile(String action) {
        List<String> liste = createHistoryDayFile(action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueThisDay + Util.getSeparateurSys() + chemin, pathFileHistoriqueThisDay + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueThisDay + Util.getSeparateurSys() + chemin);
        file.delete();

        file = new File(pathFileHistoriqueThisDay + Util.getSeparateurSys() + nomfichier);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void downloadFile(File file) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setHeader("Content-Disposition", "attachment;filename=file.txt");
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public Date firstDayOfWeek(Date d) {
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(d);
        //int fDW=cal.getFirstDayOfWeek();
        //int cDW = cal.get(Calendar.DAY_OF_WEEK);
        //cal.add(GregorianCalendar.DATE, fDW-cDW);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    public Date LastDayOfWeek(Date d) {
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(d);
        int cDW = cal.get(Calendar.DAY_OF_WEEK);
        //cal.add(GregorianCalendar.DATE, GregorianCalendar.SUNDAY-cDW);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal.getTime();
    }

    public List<String> getUserWeekDestinataire() {
        String req = "select distinct h.destinataire from historique_produit h where h.action like '%Destockage%' and extract (week from h.date)=extract (week from now())and h.users='" + Util.getUserId() + "'";
        List<String> list = (List<String>) entityManager.createNativeQuery(req).getResultList();
        return list;
    }

    public List<String> getWeekDestinataire() {
        String req = "select distinct h.destinataire from historique_produit h where h.action like '%Destockage%' and extract (week from h.date)=extract (week from now())";
        List<String> list = (List<String>) entityManager.createNativeQuery(req).getResultList();
        return list;
    }

    public List<HistoriqueProduit> getWeekThisHistory(String action) {
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and extract (week from h.date)=extract (week from now())";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and extract (week from h.date)=extract (week from now())";
                break;
            default:
                req = "select * from historique_produit h where extract (week from h.date)=extract (week from now())";
                break;
        }
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getWeekHistory(Date d, String action) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and extract (week from h.date)=extract (week from TIMESTAMP '" + t + "')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and extract (week from h.date)=extract (week from TIMESTAMP '" + t + "')";
                break;
            default:
                req = "select * from historique_produit h where extract (week from h.date)=extract (week from TIMESTAMP '" + t + "')";
                break;
        }
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    //Fonction permettant de créer un fichier d'historique hebdomadaire courant
    public List<String> createHistoryWeekFile(String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueThisWeek = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        Calendar c = Calendar.getInstance();
        Date hh = c.getTime();

        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisWeek = f.getAbsolutePath();

        f = new File(pathFileHistoriqueThisWeek + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisWeek = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueThisWeek + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueThisWeek + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisWeek = f.getAbsolutePath();
        f = new File(pathFileHistoriqueThisWeek + sepa + "Semaines" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisWeek = f.getAbsolutePath();

        //String nomFichier = "Historique_Produit" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueThisWeek + sepa + nomFichier));
            FileOutputStream output = new FileOutputStream(pathFileHistoriqueThisWeek + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentWeekS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentWeekDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentWeek, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueProduitSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
            }

            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Semaine de l'historique:    " + formaterDateOnly.format(this.firstDayOfWeek(hh)) + " - " + formaterDateOnly.format(this.LastDayOfWeek(hh)) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getWeekThisHistory(action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph6 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell7 = new PdfPCell(ph7);
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell7);
                Phrase ph8 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph8;
            }).map((ph8) -> new PdfPCell(ph8)).map((pcell8) -> {
                pcell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell8;
            }).forEach((pcell8) -> {
                table.addCell(pcell8);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            /*Header footer = new Header("Copyright Séssandè corporation aôut 2013","");
						//Footer footer1;
						//footer.setBorder(Rectangle.NO_BORDER);*/
            document.addHeader("EDIFICE - CONSTRUCTION", "EDIFICE - CONSTRUCTION ");//.setFooter(footer);

            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueThisWeek + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryWeekFile(String action) {

        List<String> liste = createHistoryWeekFile(action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueThisWeek + Util.getSeparateurSys() + chemin, pathFileHistoriqueThisWeek + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueThisWeek + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueThisWeek + Util.getSeparateurSys() + nomfichier);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique hebdomadaire courant
    public List<String> createHistoryWeekFile(Date heureGen, Date hh, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueWeek = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());       

        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueWeek = f.getAbsolutePath();

        f = new File(pathFileHistoriqueWeek + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueWeek = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueWeek + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueWeek + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueWeek = f.getAbsolutePath();
        f = new File(pathFileHistoriqueWeek + sepa + "Semaines" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueWeek = f.getAbsolutePath();

        //String nomFichier = "Historique_Produit" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";
        //stamper = "HistoriqueProduitSemaine" + formaterDateFile2.format(hh) + ".pdf";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueWeek + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueWeek + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentWeekS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentWeekDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentWeek, police_entete_f);
                    //
                    stamper = "HistoriqueProduitSemaine" + formaterDateFile2.format(this.firstDayOfWeek(hh)) + "_" + formaterDateFile2.format(this.LastDayOfWeek(hh)) + ".pdf";
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentWeek, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Semaine de l'historique:    " + formaterDateOnly.format(this.firstDayOfWeek(hh)) + " - " + formaterDateOnly.format(this.LastDayOfWeek(hh)) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getWeekHistory(hh, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph6 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell7 = new PdfPCell(ph7);
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell7);
                Phrase ph8 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph8;
            }).map((ph8) -> new PdfPCell(ph8)).map((pcell8) -> {
                pcell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell8;
            }).forEach((pcell8) -> {
                table.addCell(pcell8);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            /*Header footer = new Header("Copyright Séssandè corporation aôut 2013","");
						//Footer footer1;
						//footer.setBorder(Rectangle.NO_BORDER);*/
            document.addHeader("EDIFICE - CONSTRUCTION", "EDIFICE - CONSTRUCTION ");//.setFooter(footer);

            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueWeek + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryWeekFile(Date d, String action) {
        //Calendar c=Calendar.getInstance();
        List<String> liste = createHistoryWeekFile(Calendar.getInstance().getTime(), d, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueWeek + Util.getSeparateurSys() + chemin, pathFileHistoriqueWeek + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueWeek + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueWeek + Util.getSeparateurSys() + nomfichier);
        //String chemin = createHistoryWeekFile(Calendar.getInstance().getTime(), d);
        //File file = new File(pathFileHistoriqueWeek + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<HistoriqueProduit> getMonthHistory(Date d, String action) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and extract (month from h.date)=extract (month from TIMESTAMP '" + t + "')";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and extract (month from h.date)=extract (month from TIMESTAMP '" + t + "')";
                break;
            default:
                req = "select * from historique_produit h where extract (month from h.date)=extract (month from TIMESTAMP '" + t + "')";
                break;
        }
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getMonthThisHistory(String action) {
        String req;
        switch (action) {
            case "Stockage":
                req = "select * from historique_produit h where h.action like '%Stockage%' and extract (month from h.date)=extract (month from now())";
                break;
            case "Destockage":
                req = "select * from historique_produit h where h.action like '%Destockage%' and extract (month from h.date)=extract (month from now())";
                break;
            default:
                req = "select * from historique_produit h where extract (month from h.date)=extract (month from now())";
                break;
        }
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    //Fonction permettant de créer un fichier d'historique hebdomadaire courant
    public List<String> createHistoryMonthFile(String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueThisMonth = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        Calendar c = Calendar.getInstance();
        Date hh = c.getTime();

        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisMonth = f.getAbsolutePath();

        f = new File(pathFileHistoriqueThisMonth + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisMonth = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueThisMonth + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueThisMonth + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisMonth = f.getAbsolutePath();
        f = new File(pathFileHistoriqueThisMonth + sepa + "Mois" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisMonth = f.getAbsolutePath();

        //String nomFichier = "Historique_Produit" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueThisMonth + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueWeek + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentMonthS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentMonthDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriquedestockageMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentMonth, police_entete_f);
                    //
                    stamper = "HistoriqueProduitMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
            }
            //= new Paragraph(titreDocumentMonth, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Mois de l'historique:    " + formaterMonthYearOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getMonthThisHistory(action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph6 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell7 = new PdfPCell(ph7);
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell7);
                //Action, Designation,quantité, famille, destinataire, utilisateur, date
                Phrase ph8 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph8;
            }).map((ph8) -> new PdfPCell(ph8)).map((pcell8) -> {
                pcell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell8;
            }).forEach((pcell8) -> {
                table.addCell(pcell8);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            /*Header footer = new Header("Copyright Séssandè corporation aôut 2013","");
						//Footer footer1;
						//footer.setBorder(Rectangle.NO_BORDER);*/
            document.addHeader("EDIFICE - CONSTRUCTION", "");//.setFooter(footer);

            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueThisMonth + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryMonthFile(String action) {

        List<String> liste = createHistoryMonthFile(action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueThisMonth + Util.getSeparateurSys() + chemin, pathFileHistoriqueThisMonth + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueThisMonth + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueThisMonth + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryMonthFile();
        //File file = new File(pathFileHistoriqueThisMonth + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique hebdomadaire courant
    public List<String> createHistoryMonthFile(Date heureGen, Date hh, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueMonth = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar

        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueMonth = f.getAbsolutePath();

        f = new File(pathFileHistoriqueMonth + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueMonth = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriqueMonth + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriqueMonth + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueMonth = f.getAbsolutePath();
        f = new File(pathFileHistoriqueMonth + sepa + "Mois" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueMonth = f.getAbsolutePath();

        //String nomFichier = "Historique_Produit" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";
        //stamper = "HistoriqueProduitMois" + formaterMonthFile.format(hh) + ".pdf";

        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueMonth + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueWeek + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentMonthS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentMonthDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentMonth, police_entete_f);
                    //
                    stamper = "HistoriqueProduitMois" + formaterMonthFile.format(hh) + ".pdf";
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentMonth, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Mois de l'historique:    " + formaterMonthYearOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getMonthHistory(hh, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph6 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell7 = new PdfPCell(ph7);
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell7);
                Phrase ph8 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph8;
            }).map((ph8) -> new PdfPCell(ph8)).map((pcell8) -> {
                pcell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell8;
            }).forEach((pcell8) -> {
                table.addCell(pcell8);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
            //Image image = Image.getInstance("images/logo-edifice-22.jpg");
            //Chunk c4 = new Chunk(image, -25, -25);
            //Chunk c2 = new Chunk("Copyright Séssandè corporation aôut 2013",FontFactory.getFont(FontFactory.TIMES_ROMAN,10, com.itextpdf.text.Font.ITALIC));
            //Phrase p = new Phrase(c4);
            // ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, p, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin(), 0);
            //Création d'un pied 
            /*Header footer = new Header("Copyright Séssandè corporation aôut 2013","");
						//Footer footer1;
						//footer.setBorder(Rectangle.NO_BORDER);*/
            document.addHeader("EDIFICE - CONSTRUCTION", "");//.setFooter(footer);

            //document.close();
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueMonth + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryMonthFile(Date h, String action) {
        //Calendar c=Calendar.getInstance().gett
        List<String> liste = createHistoryMonthFile(Calendar.getInstance().getTime(), h, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueMonth + Util.getSeparateurSys() + chemin, pathFileHistoriqueMonth + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueMonth + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueMonth + Util.getSeparateurSys() + nomfichier);
        //String chemin = createHistoryMonthFile(Calendar.getInstance().getTime(), h);
        //File file = new File(pathFileHistoriqueMonth + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryPeriodFile(Date heureGen, Date d1, Date d2, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriquePeriod = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = d1.getMonth() + 1;
        //int jour = hh.getDate();
        int year = d1.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriod = f.getAbsolutePath();

        f = new File(pathFileHistoriquePeriod + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriod = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriquePeriod + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriquePeriod + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriod = f.getAbsolutePath();
        f = new File(pathFileHistoriquePeriod + sepa + "Periodes" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriod = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        //String nomFichier = 
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriquePeriod + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentPeriodS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockagePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentPeriodDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockagePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentPeriod, police_entete_f);
                    //
                    stamper = "HistoriqueProduitPeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentPeriod, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Période de l'historique:    " + formaterDateOnly.format(d1) + " - " + formaterDateOnly.format(d2) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getPeriodHistory(d1, d2, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriquePeriod + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryPeriodFile(Date d1, Date d2, String action) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryPeriodFile(hg, d1, d2, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriquePeriod + Util.getSeparateurSys() + chemin, pathFileHistoriquePeriod + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriquePeriod + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriquePeriod + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryPeriodFile(hg, d1, d2);
        //File file = new File(pathFileHistoriquePeriod + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryPeriodFile(Produit pr, Date heureGen, Date d1, Date d2, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriquePeriodProduit = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = d1.getMonth() + 1;
        //int jour = hh.getDate();
        int year = d1.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodProduit = f.getAbsolutePath();

        f = new File(pathFileHistoriquePeriodProduit + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodProduit = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriquePeriodProduit + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriquePeriodProduit + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodProduit = f.getAbsolutePath();
        f = new File(pathFileHistoriquePeriodProduit + sepa + "Produit" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodProduit = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        //String nomFichier = "HistoriqueProduitPeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriquePeriodProduit + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentPeriodProduitS+" "+pr.getDesignation(), police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockagePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentPeriodProduitDs+" "+pr.getDesignation(), police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockagePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentPeriodProduit+" "+pr.getDesignation(), police_entete_f);
                    //
                    stamper = "HistoriqueProduitPeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
                    ;
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentPeriodProduit + pr.toString(), police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Période de l'historique:    " + formaterDateOnly.format(d1) + " - " + formaterDateOnly.format(d2) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getPeriodProduitHistory(pr, d1, d2, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //// PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriquePeriodProduit + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryPeriodProduitFile(Produit p, Date d1, Date d2, String action) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryPeriodFile(p, hg, d1, d2, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriquePeriodProduit + Util.getSeparateurSys() + chemin, pathFileHistoriquePeriodProduit + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriquePeriodProduit + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriquePeriodProduit + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryPeriodFile(p, hg, d1, d2);
        //File file = new File(pathFileHistoriquePeriodProduit + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryYearFile(Date heureGen, Date hh, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueYear = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueYear = f.getAbsolutePath();

        f = new File(pathFileHistoriqueYear + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueYear = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        //String nomFichier = "HistoriqueProduitAnnee" + formaterYearOnly.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueYear + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(heureGen), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentYearS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentYearDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentYear, police_entete_f);
                    //
                    stamper = "HistoriqueProduitAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentYear, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Année de l'historique:    " + formaterYearOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getYearHistory(hh, action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            // PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueYear + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryYearFile(Date h, String action) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryYearFile(hg, h, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueYear + Util.getSeparateurSys() + chemin, pathFileHistoriqueYear + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueYear + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueYear + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryYearFile(String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriqueThisYear = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        Date hh = Calendar.getInstance().getTime();
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisYear = f.getAbsolutePath();

        f = new File(pathFileHistoriqueThisYear + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriqueThisYear = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        //String nomFichier = "HistoriqueProduitAnnee" + formaterYearOnly.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriqueThisYear + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentYearS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentYearDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentYear, police_entete_f);
                    //
                    stamper = "HistoriqueProduitAnnee" + formaterYearOnly.format(hh) + ".pdf";
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentYear, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Année de l'historique:    " + formaterYearOnly.format(hh) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);
//Action, Designation,quantité, famille, destinataire, utilisateur, date
            //Création des différentes lignes du tableau
            getYearThisHistory(action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            // PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueThisYear + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryYearFile(String action) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryYearFile(action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileHistoriqueThisYear + Util.getSeparateurSys() + chemin, pathFileHistoriqueThisYear + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileHistoriqueThisYear + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileHistoriqueThisYear + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createAllHistoryFile(Date hh, String action) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileAllHistory = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + historiqueDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileAllHistory = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper = "";

        //String nomFichier = "HistoriqueProduitTotale" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileAllHistory + sepa + nomFichier));
            //FileOutputStream output = new FileOutputStream(pathFileHistoriqueDay + sepa + nomFichier);
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            document.open();
            //Construction de l'entête
            Image img1 = Image.getInstance("logo-edifice-22.jpg");
            img1.scaleAbsolute(200f, 80f);
            document.add(img1);
            //Construction de l'entête
            //Paragraph entete1 = new Paragraph(nom_entreprise, police_entete_f);
            //Paragraph entete11 = new Paragraph(devise + "                                                             fichier générer: " + formater.format(hh), police_services_f);
            Paragraph entete11 = new Paragraph("fichier générer: " + formater.format(hh), police_services_f);
            entete11.setAlignment(Element.ALIGN_RIGHT);
            Paragraph entete12;
            switch (action) {
                case "Stockage":
                    entete12 = new Paragraph(titreDocumentAllS, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueStockageTotale" + formaterDateFile.format(hh) + ".pdf";
                    break;
                case "Destockage":
                    entete12 = new Paragraph(titreDocumentAllDs, police_entete_f);
                    //entete12.setAlignment(Element.ALIGN_CENTER);
                    stamper = "HistoriqueDestockageTotale" + formaterDateFile.format(hh) + ".pdf";
                    break;
                default:
                    entete12 = new Paragraph(titreDocumentAll, police_entete_f);
                    //
                    stamper = "HistoriqueProduitTotale" + formaterDateFile.format(hh) + ".pdf";
                    break;
            }
            //Paragraph entete12 = new Paragraph(titreDocumentAll, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            //document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Action réalisée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Désignation", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Quantité", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Famille du Produit", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Destinataire", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getAllHistory(action).stream().map((p) -> {
                Phrase ph3 = new Phrase(p.getAction(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(p.getProduit(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(p.getQuantite() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(p.getClasseProduit(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);
                Phrase ph1 = new Phrase(p.getDestinataire(), police_tableau);
                PdfPCell pcell1 = new PdfPCell(ph1);
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell1);
                Phrase ph6 = new Phrase(p.getUsers().toString(), police_tableau);
                PdfPCell pcell6 = new PdfPCell(ph6);
                pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell6);
                Phrase ph7 = new Phrase(formaterDateHistory.format(p.getDate()), police_tableau);
                return ph7;
            }).map((ph7) -> new PdfPCell(ph7)).map((pcell7) -> {
                pcell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell7;
            }).forEach((pcell7) -> {
                table.addCell(pcell7);
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);

            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            // PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileAllHistory + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadAllHistoryFile(String action) {
        System.out.println("Appel a la fonction downloadAllHistoryFile: paramètre: "+ action);
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createAllHistoryFile(hg, action);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(pathFileAllHistory + Util.getSeparateurSys() + chemin, pathFileAllHistory + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(pathFileAllHistory + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(pathFileAllHistory + Util.getSeparateurSys() + nomfichier);

        //String chemin = createAllHistoryFile(hg);
        //File file = new File(pathFileAllHistory + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriqueProduitController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<HistoriqueProduit> getAllHistoryUser() {
        String req = "select * from historique_produit h where h.users='" + Util.getUserId() + "'";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getDayHistoryUser() {
        String req = "select * from historique_produit h where h.users = '" + Util.getUserId() + "' and to_timestamp(h.date::text, 'yyyy-mm-dd')=to_timestamp(now()::text, 'yyyy-mm-dd')";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getWeekHistoryUser() {
        String req = "select * from historique_produit h where h.users = '" + Util.getUserId() + "' and extract (week from h.date)=extract (week from now())";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getMonthHistoryUser() {
        String req = "select * from historique_produit h where h.users = '" + Util.getUserId() + "' and extract (month from h.date)=extract (month from now())";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public List<HistoriqueProduit> getAllHistoryUserStockage() {
        String req = "select * from historique_produit h where h.action like '%Stockage%' and h.users = '" + Util.getUserId() + "'";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserStockageAll() {
        if (this.getAllHistoryUserStockage() != null) {
            return this.getAllHistoryUserStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getDayHistoryUserStockage() {
        String req = "select * from historique_produit h where h.action like '%Stockage%' and h.users = '" + Util.getUserId() + "' and to_timestamp(h.date::text, 'yyyy-mm-dd')=to_timestamp(now()::text, 'yyyy-mm-dd')";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserStockageDay() {
        if (this.getDayHistoryUserStockage() != null) {
            return this.getDayHistoryUserStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getWeekHistoryUserStockage() {
        String req = "select * from historique_produit h where h.users = '" + Util.getUserId() + "' and extract (week from h.date)=extract (week from now()) and h.action like '%Stockage%'";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserStockageWeek() {
        if (this.getWeekHistoryUserStockage() != null) {
            return this.getWeekHistoryUserStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getMonthHistoryUserStockage() {
        String req = "select * from historique_produit h where h.users = '" + Util.getUserId() + "' and extract (month from h.date)=extract (month from now()) and h.action like '%Stockage%'";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserStockageMonth() {
        if (this.getMonthHistoryUserStockage() != null) {
            return this.getMonthHistoryUserStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getAllHistoryUserDeStockage() {
        String req = "select * from historique_produit h where h.action like '%Destockage%' and h.users = '" + Util.getUserId() + "'";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserDeStockageAll() {
        if (this.getAllHistoryUserDeStockage() != null) {
            return this.getAllHistoryUserDeStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getDayHistoryUserDeStockage() {
        String req = "select * from historique_produit h where h.action like '%Destockage%' and h.users = '" + Util.getUserId() + "' and to_timestamp(h.date::text, 'yyyy-mm-dd')=to_timestamp(now()::text, 'yyyy-mm-dd') ";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserDeStockageDay() {
        if (this.getDayHistoryUserDeStockage() != null) {
            return this.getDayHistoryUserDeStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getWeekHistoryUserDeStockage() {
        String req = "select * from historique_produit h where h.action like '%Destockage%' and h.users = '" + Util.getUserId() + "' and extract (week from h.date)=extract (week from now())";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserDeStockageWeek() {
        if (this.getWeekHistoryUserDeStockage() != null) {
            return this.getWeekHistoryUserDeStockage().size();
        }
        return 0;
    }

    public List<HistoriqueProduit> getMonthHistoryUserDeStockage() {
        String req = "select * from historique_produit h where h.action like '%Destockage%' and h.users = '" + Util.getUserId() + "' and extract (month from h.date)=extract (month from now())";
        List<HistoriqueProduit> liste = (List<HistoriqueProduit>) entityManager.createNativeQuery(req, HistoriqueProduit.class).getResultList();
        return liste;
    }

    public int nombreUserDeStockageMonth() {
        if (this.getMonthHistoryUserDeStockage() != null) {
            return this.getMonthHistoryUserDeStockage().size();
        }
        return 0;
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
        current = (HistoriqueProduit) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new HistoriqueProduit();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriqueProduitCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (HistoriqueProduit) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriqueProduitUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (HistoriqueProduit) getItems().getRowData();
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
    
    //Accesseurs des variables des formulaire de stockage et de destockage

    public Boolean getBolJDlgS() {
        return bolJDlgS;
    }

    public void setBolJDlgS(Boolean bolJDlgS) {
        this.bolJDlgS = bolJDlgS;
    }

    public Boolean getBolPDlgS() {
        return bolPDlgS;
    }

    public void setBolPDlgS(Boolean bolPDlgS) {
        this.bolPDlgS = bolPDlgS;
    }

    public Boolean getBolWDlgS() {
        return bolWDlgS;
    }

    public void setBolWDlgS(Boolean bolWDlgS) {
        this.bolWDlgS = bolWDlgS;
    }

    public Boolean getBolMDlgS() {
        return bolMDlgS;
    }

    public void setBolMDlgS(Boolean bolMDlgS) {
        this.bolMDlgS = bolMDlgS;
    }

    public Boolean getBolYDlgS() {
        return bolYDlgS;
    }

    public void setBolYDlgS(Boolean bolYDlgS) {
        this.bolYDlgS = bolYDlgS;
    }

    public Boolean getBolPPDlgS() {
        return bolPPDlgS;
    }

    public void setBolPPDlgS(Boolean bolPPDlgS) {
        this.bolPPDlgS = bolPPDlgS;
    }

    public Boolean getBolJDlgDs() {
        return bolJDlgDs;
    }

    public void setBolJDlgDs(Boolean bolJDlgDs) {
        this.bolJDlgDs = bolJDlgDs;
    }

    public Boolean getBolPDlgDs() {
        return bolPDlgDs;
    }

    public void setBolPDlgDs(Boolean bolPDlgDs) {
        this.bolPDlgDs = bolPDlgDs;
    }

    public Boolean getBolWDlgDs() {
        return bolWDlgDs;
    }

    public void setBolWDlgDs(Boolean bolWDlgDs) {
        this.bolWDlgDs = bolWDlgDs;
    }

    public Boolean getBolMDlgDs() {
        return bolMDlgDs;
    }

    public void setBolMDlgDs(Boolean bolMDlgDs) {
        this.bolMDlgDs = bolMDlgDs;
    }

    public Boolean getBolYDlgDs() {
        return bolYDlgDs;
    }

    public void setBolYDlgDs(Boolean bolYDlgDs) {
        this.bolYDlgDs = bolYDlgDs;
    }

    public Boolean getBolPPDlgDs() {
        return bolPPDlgDs;
    }

    public void setBolPPDlgDs(Boolean bolPPDlgDs) {
        this.bolPPDlgDs = bolPPDlgDs;
    }
    

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriqueProduitDeleted"));
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

    public HistoriqueProduit getHistoriqueProduit(java.lang.Integer id) {
        return ejbFacade.find(id);
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

    @FacesConverter(forClass = HistoriqueProduit.class)
    public static class HistoriqueProduitControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            HistoriqueProduitController controller = (HistoriqueProduitController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "historiqueProduitController");
            return controller.getHistoriqueProduit(getKey(value));
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
            if (object instanceof HistoriqueProduit) {
                HistoriqueProduit o = (HistoriqueProduit) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + HistoriqueProduit.class.getName());
            }
        }

    }

}
