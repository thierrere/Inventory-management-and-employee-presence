package com.management.controllers;

import com.management.jpa.HistoriquePresence;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.jpa.Employe;
import com.management.sessionbeans.HistoriquePresenceFacade;
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

@ManagedBean(name="historiquePresenceController")
@SessionScoped
public class HistoriquePresenceController implements Serializable {

    private HistoriquePresence current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.HistoriquePresenceFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private List<HistoriquePresence> allHistory;
    private final String historiqueDirectory = "Historique_Presence";
    //String pathFileHistoriqueThisDay, pathFileHistoriqueThisWeek, pathFileHistoriqueThisMonth, pathFileHistoriqueThisYear;
    String pathFileHistoriqueDay, pathFileHistoriqueWeek, pathFileHistoriqueMonth;
    String pathFileHistoriquePeriod, pathFileHistoriqueYear, pathFileAllHistory, pathFileHistoriquePeriodEmploye;
    static final int TAILLE_TAMPON = 10240; // 10 ko
    byte[] tampon = new byte[TAILLE_TAMPON];
    private Boolean bolJDlg = false, bolPDlg = false, bolWDlg = false, bolMDlg = false, bolYDlg = false, bolPPDlg = false, bolPointage = false;
    private Date dateY, dateM, dateW, dateP1, dateP2, dateJ, datePP1, datePP2;

    Employe employe;
    String presence="Absent(e)";
    Boolean valuePresence=false;

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }
    
    public void EmployePresence(){
        if(presence.equals("Présent(e)")){
            valuePresence=true;
        }else if(presence.equals("Absent(e)")){
            valuePresence=false;
        }
    }

    public Boolean getValuePresence() {
        return valuePresence;
    }
    
    //Flux d'écriture
    BufferedOutputStream sortie;

    //La partie pour les formulaire caché servant à la génération des pointages
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

    public void annulerJDlg() {
        bolJDlg = false;
        dateJ = null;
        //return bolDlg;
    }

    public void ouvrirJDlg() {
        bolJDlg = true;
        bolPDlg = false;
        bolPPDlg = false;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = false;
        bolPointage = false;
        //return bolDlg;
    }

    public void takeDateHist() {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateJ != null) {
            downloadHistoryDayFile(dateJ);
            dateJ = null;
            bolJDlg = false;
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

    public void annulerWDlg() {
        bolWDlg = false;
        dateW = null;
        //return bolDlg;
    }

    public void ouvrirWDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolPPDlg = false;
        bolWDlg = true;
        bolMDlg = false;
        bolYDlg = false;
        bolPointage = false;
    }

    public void takeWeekHist() {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateW != null) {
            downloadHistoryWeekFile(dateW);
            dateW = null;
            bolWDlg = false;
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

    public void annulerMDlg() {
        bolMDlg = false;
        dateM = null;
        //return bolDlg;
    }

    public void ouvrirMDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolPPDlg = false;
        bolWDlg = false;
        bolMDlg = true;
        bolYDlg = false;
        bolPointage = false;
    }

    public void takeMonthHist() {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateM != null) {
            downloadHistoryMonthFile(dateM);
            dateM = null;
            bolMDlg = false;
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

    public void annulerPDlg() {
        bolPDlg = false;
        dateP1 = null;
        dateP2 = null;
        //return bolDlg;
    }

    public void ouvrirPDlg() {
        bolJDlg = false;
        bolPDlg = true;
        bolPPDlg = false;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = false;
        bolPointage = false;
    }

    public void takePeriodHist() {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if ((dateP1 == null) || (dateP2 == null)) {
            JsfUtil.addErrorMessage("Veuillez bien délimiter la période grâce aux dates!!");
        } else {
            downloadHistoryPeriodFile(dateP1, dateP2);
            dateP1 = null;
            dateP2 = null;
            bolPDlg = false;
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

    public void annulerPPDlg() {
        bolPPDlg = false;
        datePP1 = null;
        datePP2 = null;
        //return bolDlg;
    }

    public void ouvrirPPDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolPPDlg = true;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = false;
        bolPointage = false;
    }

    public void takePeriodPHist() {

        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (employe == null || datePP1 == null || datePP2 == null) {
            JsfUtil.addErrorMessage("Veuillez saisir le nom de l'employe et délimiter la période");
        } else {
            downloadHistoryPeriodEmployeFile(employe, datePP1, datePP2);
            datePP1 = null;
            datePP2 = null;
            bolPPDlg = false;
            employe = null;
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

    public void annulerYDlg() {
        bolYDlg = false;
        dateY = null;
        //return bolDlg;
    }

    public void ouvrirYDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolPPDlg = false;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = true;
        bolPointage = false;
    }

    public void takeYearHist() {
        //System.out.println("Date selectionnée: "+formaterTimestamp.format(date1));
        if (dateY == null) {
            JsfUtil.addErrorMessage("Veuillez choisir une date dans l'année souhaitée");
        } else {
            downloadHistoryYearFile(dateY);
            dateY = null;
            bolYDlg = false;
        }
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    //Partie pour le formulaire de saisie de pointage
    public Boolean getBolPointage() {
        return bolPointage;
    }

    public void setBolPointage(Boolean bolDlg) {
        this.bolPointage = bolDlg;
    }

    public void annulerCreateDlg() {
        bolPointage = false;
        valuePresence=false;
        presence="Absent(e)";
        //return bolDlg;
    }

    public void ouvrirCreateDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolPPDlg = false;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = false;
        bolPointage = true;
        //return bolDlg;
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
    //String nom_entreprise = "EDIFICE CONSTRUCTION";
    //String devise = "La qualité notre engagement";
    String titreDocumentDay = "Historique Journalier des employés";
    String titreDocumentWeek = "Historique Hebdomadaire des employés";
    String titreDocumentMonth = "Historique Mensuel des employés";
    String titreDocumentYear = "Historique annuel des employés";
    String titreDocumentPeriod = "Historique sur une période";
    String titreDocumentPeriodEmploye = "Historique sur une période de l'Employé: ";
    String titreDocumentAll = "Toute l'Historique";
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

    public HistoriquePresenceController() {
    }

    public HistoriquePresence getSelected() {
        if (current == null) {
            current = new HistoriquePresence();
            selectedItemIndex = -1;
        }
        return current;
    }

    private HistoriquePresenceFacade getFacade() {
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
    public SimpleDateFormat getFormaterDateOnly() {
        return formaterDateOnly;
    }

    public void setFormaterDateOnly(SimpleDateFormat formaterDateOnly) {
        this.formaterDateOnly = formaterDateOnly;
    }

    public void annulerDlg() {
        bolJDlg = false;
        bolPDlg = false;
        bolWDlg = false;
        bolMDlg = false;
        bolYDlg = false;
        //return bolDlg;
    }

    public List<HistoriquePresence> getAllHistory() {
        if(allHistory == null){
        String req = "select * from historique_presence h order by h.journee desc, h.heure_arrivee asc";
        //allHistory = this.getFacade().findAll();
        allHistory = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        }
        return allHistory;
    }

    public List<HistoriquePresence> getYearThisHistory() {
        Calendar c = Calendar.getInstance();
        List<HistoriquePresence> liste = getYearHistory(c.getTime());
        return liste;
    }

    public List<HistoriquePresence> getYearHistory(Date d) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req = "select * from historique_presence h where extract(year from h.journee)=extract (year from TIMESTAMP'" + t + "') order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    public List<HistoriquePresence> getDayHistory(Date d) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        //Datetime t=
        //String req = "select h from historique_presence h where to_timestamp(h.journee::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        String req = "select * from historique_presence h where to_timestamp(h.journee::text, 'yyyy-mm-dd') = to_timestamp('" + t + "'::text, 'yyyy-mm-dd') order by h.heure_arrivee asc";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        //System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + dayHistory.size());
        return liste;
    }

    public List<HistoriquePresence> getPeriodHistory(Date d1, Date d2) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t1;
        t1 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d1)));
        java.sql.Date t2;
        t2 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d2)));
        //Datetime t=
        //String req = "select h from historique_presence h where to_timestamp(h.journee::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        String req = "select * from historique_presence h where to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.journee::text, 'yyyy-mm-dd') and to_timestamp(h.journee::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd') order by h.journee desc, h.heure_arrivee asc";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        //System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + periodHistory.size());
        return liste;
    }

    public List<HistoriquePresence> getPeriodEmployeHistory(Employe p, Date d1, Date d2) {
        //System.out.println("Date à convertir: "+formaterTimestamp.format(d));
        java.sql.Date t1;
        t1 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d1)));
        java.sql.Date t2;
        t2 = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d2)));
        //Datetime t=
        //String req = "select h from historique_presence h where to_timestamp(h.journee::text, 'yyyy-mm-dd') = to_timestamp(now()::text, 'yyyy-mm-dd')";
        String req = "select * from historique_presence h where h.employe='" + p.getId() + "'  and to_timestamp('" + t1 + "'::text, 'yyyy-mm-dd') <= to_timestamp(h.journee::text, 'yyyy-mm-dd') and to_timestamp(h.journee::text, 'yyyy-mm-dd') <= to_timestamp('" + t2 + "'::text, 'yyyy-mm-dd') order by h.journee desc, h.heure_arrivee asc";
        //System.out.println("Requête à executer pour l'historique jour: "+req);
        List<HistoriquePresence> list = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        //System.out.println("Requête à executer pour l'historique jour: " + req + " taille résultat: " + list.size());
        return list;
    }

    public void downloadPresenceDayFile() {
        downloadHistoryDayFile(Calendar.getInstance().getTime());
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryDayFile(Date heureGen, Date hh) {
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
        String stamper;
        stamper = "PointageJour" + formaterDateFile.format(hh) + ".pdf";
        //String nomFichier = "HistoriquePresenceJour" + formaterDateFile.format(hh) + ".pdf";

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
            Paragraph entete12 = new Paragraph(titreDocumentDay, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(date du pointage:    " + formaterDateOnly.format(hh) + ")", police_entete_1_f);
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
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getDayHistory(hh).stream().map((h) -> {
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueDay + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryDayFile(Date h) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryDayFile(hg, h);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriqueDay + Util.getSeparateurSys() + chemin, this.pathFileHistoriqueDay + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriqueDay + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriqueDay + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryDayFile(hg, h);
        //File file = new File(pathFileHistoriqueDay + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    /*public List<HistoriquePresence> getWeekThisHistory() {
        String req = "select * from historique_presence h where extract (week from h.journee)=extract (week from now())";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }*/
    
    public Date firstDayOfWeek(Date d){
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(d);
        //int fDW=cal.getFirstDayOfWeek();
        //int cDW = cal.get(Calendar.DAY_OF_WEEK);
        //cal.add(GregorianCalendar.DATE, fDW-cDW);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }   
    
    public Date LastDayOfWeek(Date d){
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(d);
        int cDW = cal.get(Calendar.DAY_OF_WEEK);
        //cal.add(GregorianCalendar.DATE, GregorianCalendar.SUNDAY-cDW);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal.getTime();
    } 
    
    public List<HistoriquePresence> getWeekHistory(Date d) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req = "select * from historique_presence h where extract (week from h.journee)=extract (week from TIMESTAMP '" + t + "') order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    //Fonction permettant de créer un fichier d'historique hebdomadaire
    public List<String> createHistoryWeekFile(Date heureGen, Date hh) {
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

        //String nomFichier = "Historique_Presence" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "PointageSemaine" + formaterDateFile.format(hh) + ".pdf";
        //String nomFichier;
        //nomFichier = "HistoriquePresenceSemaine" + formaterDateFile.format(hh) + ".pdf";

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
            Paragraph entete12 = new Paragraph(titreDocumentWeek, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Semaine du pointage:    " +formaterDateOnly.format(this.firstDayOfWeek(hh))+" - "+formaterDateOnly.format(this.LastDayOfWeek(hh)) + ")", police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_CENTER);
            //System.out.println("FirstDay: "+formaterDateOnly.format(this.firstDayOfWeek(hh)));
            //System.out.println("LastDay: "+formaterDateOnly.format(this.LastDayOfWeek(hh)));
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete14);
            document.add(entete4);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getWeekHistory(hh).stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueWeek + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadPresenceWeekFile() {
        downloadHistoryWeekFile(Calendar.getInstance().getTime());
    }

    public void downloadHistoryWeekFile(Date d) {
        //Calendar c=Calendar.getInstance();

        List<String> liste = createHistoryWeekFile(Calendar.getInstance().getTime(), d);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriqueWeek + Util.getSeparateurSys() + chemin, this.pathFileHistoriqueWeek + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriqueWeek + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriqueWeek + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }    

    public List<HistoriquePresence> getMonthHistory(Date d) {
        java.sql.Date t;
        t = java.sql.Date.valueOf(LocalDate.parse(formaterTimestamp.format(d)));
        String req = "select * from historique_presence h where extract (month from h.journee)=extract (month from TIMESTAMP '" + t + "') order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    /*public List<HistoriquePresence> getMonthThisHistory() {
        String req = "select * from historique_presence h where extract (month from h.journee)=extract (month from now())";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }*/
    //Fonction permettant de créer un fichier d'historique hebdomadaire 
    public List<String> createHistoryMonthFile(Date heureGen, Date hh) {
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

        //String nomFichier = "Historique_Presence" + jour + mois + year + h_m_s + ".pdf";
        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "PointageMois" + formaterMonthFile.format(hh) + ".pdf";
        //String nomFichier;
        //nomFichier = "HistoriquePresenceMois" + formaterMonthFile.format(hh) + ".pdf";

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
            Paragraph entete12 = new Paragraph(titreDocumentMonth, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Mois du pointage:    " + formaterMonthYearOnly.format(hh) + ")", police_entete_1_f);
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
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getMonthHistory(hh).stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueMonth + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadPresenceMonthFile() {
        downloadHistoryMonthFile(Calendar.getInstance().getTime());
    }

    public void downloadHistoryMonthFile(Date h) {
        //Calendar c=Calendar.getInstance().gett

        List<String> liste = createHistoryMonthFile(Calendar.getInstance().getTime(), h);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriqueMonth + Util.getSeparateurSys() + chemin, this.pathFileHistoriqueMonth + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriqueMonth + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriqueMonth + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryPeriodFile(Date heureGen, Date d1, Date d2) {
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
        String stamper;
        stamper = "PointagePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";

        //String nomFichier = "HistoriquePresencePeriod" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
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
            Paragraph entete12 = new Paragraph(titreDocumentPeriod, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Période du pointage:    " + formaterDateOnly.format(d1) + " - " + formaterDateOnly.format(d2) + ")", police_entete_1_f);
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
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getPeriodHistory(d1, d2).stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriquePeriod + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryPeriodFile(Date d1, Date d2) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryPeriodFile(hg, d1, d2);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriquePeriod + Util.getSeparateurSys() + chemin, this.pathFileHistoriquePeriod + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriquePeriod + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriquePeriod + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
               Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createHistoryPeriodFile(Employe pr, Date heureGen, Date d1, Date d2) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        File f = new File(".");
        pathFileHistoriquePeriodEmploye = f.getAbsolutePath();
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
        pathFileHistoriquePeriodEmploye = f.getAbsolutePath();

        f = new File(pathFileHistoriquePeriodEmploye + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodEmploye = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileHistoriquePeriodEmploye + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileHistoriquePeriodEmploye + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodEmploye = f.getAbsolutePath();
        f = new File(pathFileHistoriquePeriodEmploye + sepa + "Employe" + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileHistoriquePeriodEmploye = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "HistoriquePeriodEmploye" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";

        //String nomFichier = "HistoriquePeriodEmploye" + formaterDateFile2.format(d1) + "_" + formaterDateFile2.format(d2) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileHistoriquePeriodEmploye + sepa + nomFichier));
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
            Paragraph entete12 = new Paragraph(titreDocumentPeriodEmploye + pr.toString(), police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Période du pointage:    " + formaterDateOnly.format(d1) + " - " + formaterDateOnly.format(d2) + ")", police_entete_1_f);
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
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getPeriodEmployeHistory(pr, d1, d2).stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriquePeriodEmploye + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryPeriodEmployeFile(Employe p, Date d1, Date d2) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryPeriodFile(p, hg, d1, d2);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriquePeriodEmploye + Util.getSeparateurSys() + chemin, this.pathFileHistoriquePeriodEmploye + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriquePeriodEmploye + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriquePeriodEmploye + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryPeriodFile(p, hg, d1, d2);
        //File file = new File(pathFileHistoriquePeriodEmploye + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique annuel
    public List<String> createHistoryYearFile(Date heureGen, Date hh) {
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
        String stamper;
        stamper = "PointageAnnée" + formaterYearOnly.format(hh) + ".pdf";

        //String nomFichier = "HistoriquePresenceAnnée" + formaterYearOnly.format(hh) + ".pdf";
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
            Paragraph entete12 = new Paragraph(titreDocumentYear, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete14 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete14.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph("(Année du pointage:    " + formaterYearOnly.format(hh) + ")", police_entete_1_f);
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
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getYearHistory(hh).stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileHistoriqueYear + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadHistoryYearFile(Date h) {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createHistoryYearFile(hg, h);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileHistoriqueYear + Util.getSeparateurSys() + chemin, this.pathFileHistoriqueYear + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileHistoriqueYear + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileHistoriqueYear + Util.getSeparateurSys() + nomfichier);

        //String chemin = createHistoryYearFile(hg, h);
        //File file = new File(pathFileHistoriqueYear + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //Fonction permettant de créer un fichier d'historique journalier
    public List<String> createAllHistoryFile(Date hh) {
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
        String stamper;
        stamper = "PointageTotal" + formaterDateFile.format(hh) + ".pdf";

        //String nomFichier = "HistoriquePresenceTotale" + formaterDateFile.format(hh) + ".pdf";
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
            Paragraph entete12 = new Paragraph(titreDocumentAll, police_entete_f);
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
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(98);//c'etait 95
            
            PdfPCell c0 = new PdfPCell(new Phrase("Journée", police_premiere_ligne_tableau));
            c0.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c0);

            PdfPCell c1 = new PdfPCell(new Phrase("Noms employés", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure d'arrivée", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Heure de départ", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Date Saisie", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Utilisateur", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau (Noms employés, heure d'arrivée, heure de départ, date saisie, utilisateur)
            getAllHistory().stream().map((h) -> {
                Phrase ph0 = new Phrase(formaterDateOnly.format(h.getJournee()), police_tableau);
                PdfPCell pcell0 = new PdfPCell(ph0);
                pcell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell0);
                Phrase ph3 = new Phrase(h.getEmploye().toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);
                Phrase ph2 = new Phrase(h.getHeureArrivee(), police_tableau);
                PdfPCell pcell2 = new PdfPCell(ph2);
                pcell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell2);
                Phrase ph4 = new Phrase(h.getHeureDepart() + "", police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);
                Phrase ph5 = new Phrase(formaterDateHistory.format(h.getDateSaisie()), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                table.addCell(pcell5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                Phrase ph1 = new Phrase(h.getUsers().toString(), police_tableau);
                return ph1;
            }).map((ph1) -> new PdfPCell(ph1)).map((pcell1) -> {
                pcell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                return pcell1;
            }).forEach((pcell1) -> {
                table.addCell(pcell1);
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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileAllHistory + sepa + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    public void downloadAllHistoryFile() {
        Calendar c = Calendar.getInstance();
        Date hg = c.getTime();

        List<String> liste = createAllHistoryFile(hg);
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileAllHistory + Util.getSeparateurSys() + chemin, this.pathFileAllHistory + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileAllHistory + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileAllHistory + Util.getSeparateurSys() + nomfichier);

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
            Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(HistoriquePresenceController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<HistoriquePresence> getAllHistoryUser() {
        String req = "select * from historique_presence h where h.users='" + Util.getUserId() + "' order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    public List<HistoriquePresence> getDayHistoryUser() {
        String req = "select * from historique_presence h where h.users = '" + Util.getUserId() + "' and to_timestamp(h.journee::text, 'yyyy-mm-dd')=to_timestamp(now()::text, 'yyyy-mm-dd') order by h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    public List<HistoriquePresence> getWeekHistoryUser() {
        String req = "select * from historique_presence h where h.users = '" + Util.getUserId() + "' and extract (week from h.journee)=extract (week from now()) order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
    }

    public List<HistoriquePresence> getMonthHistoryUser() {
        String req = "select * from historique_presence h where h.users = '" + Util.getUserId() + "' and extract (month from h.journee)=extract (month from now()) order by h.journee desc, h.heure_arrivee asc";
        List<HistoriquePresence> liste = (List<HistoriquePresence>) entityManager.createNativeQuery(req, HistoriquePresence.class).getResultList();
        return liste;
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
        current = (HistoriquePresence) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public void prepareCreate() {
        current = new HistoriquePresence();
        selectedItemIndex = -1;
        //return "Create";
    }

    public void create() {
        if (current.getJournee() == null) {
            JsfUtil.addErrorMessage("Aucune journée de pointage choisie!! Veuillez choisir avant de valider");
            return;
        }
        if (current.getEmploye() == null) {
            JsfUtil.addErrorMessage("Saisir le nom de l'employé!! Veuillez saisir avant de valider");
            return;
        }
        if(!(valuePresence)){current.setHeureDepart("Absent(e)"); current.setHeureArrivee("Absent(e)");}
        else if(current.getHeureArrivee().equals("")|| current.getHeureDepart().equals("")){
            JsfUtil.addErrorMessage("Saisir l'heure d'arrivée de l'employé!! Veuillez saisir avant de valider");
            JsfUtil.addErrorMessage("Saisir l'heure de départ de l'employé!! Veuillez saisir avant de valider");
            return;
        }
        try {
            current.setUsers(Util.getUsers());
            current.setDateSaisie(Calendar.getInstance().getTime());
            getFacade().create(current);
            valuePresence=false;
            presence="Absent(e)";
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriquePresenceCreated"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Pointage crée avec succès!!");
            recreateModel();
            prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
            //return null;
        }
    }

    public String prepareEdit() {
        current = (HistoriquePresence) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriquePresenceUpdated"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Pointage mis à jour avec succès!!");
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (HistoriquePresence) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HistoriquePresenceDeleted"));
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Pointage supprimé avec succès!!");
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
        allHistory=null;
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

    public HistoriquePresence getHistoriquePresence(java.lang.Integer id) {
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

    @FacesConverter(forClass = HistoriquePresence.class)
    public static class HistoriquePresenceControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            HistoriquePresenceController controller = (HistoriquePresenceController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "historiquePresenceController");
            return controller.getHistoriquePresence(getKey(value));
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
            if (object instanceof HistoriquePresence) {
                HistoriquePresence o = (HistoriquePresence) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + HistoriquePresence.class.getName());
            }
        }

    }

}
