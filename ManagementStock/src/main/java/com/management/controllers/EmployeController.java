package com.management.controllers;

import com.management.jpa.Employe;
import com.management.controllers.util.JsfUtil;
import com.management.controllers.util.PaginationHelper;
import com.management.sessionbeans.EmployeFacade;
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
import javax.persistence.PersistenceContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;

@ManagedBean(name="employeController")
@SessionScoped
public class EmployeController implements Serializable {

    private Employe current;
    private DataModel items = null;
    @EJB
    private com.management.sessionbeans.EmployeFacade ejbFacade;
    @PersistenceContext
    private EntityManager entityManager;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private DataModel<Employe> allEmploye;
    private Boolean bol1 = false;
    private Boolean bol2 = false;
    static final int TAILLE_TAMPON = 10240; // 10 ko
    byte[] tampon = new byte[TAILLE_TAMPON];
    private Employe employe;
    String pathFileAllEmploye, pathFileEmbauchEmploy, pathFileDebauchEmploy;
    private final String employeDirectory = "Employe";

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
    //String nom_entreprise = "EDIFICE CONSTRUCTION";
    //String devise = "La qualité notre engagement";
    String titreDocumentAllEmploy = "Liste de tous les employés";
    String titreDocumentEmployEmbauche = "Liste des employés (Embauchés)";
    String titreDocumentEmployDebauche = "Liste des employés (Débauchés)";
    String acteurPrincipal = "Liste généré par : ";

    SimpleDateFormat formater = new SimpleDateFormat("'le' dd/MM/yyyy 'à' HH:mm:ss");
    SimpleDateFormat formaterDateFile = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

    public EmployeController() {
    }

    public Employe getSelected() {
        if (current == null) {
            current = new Employe();
            selectedItemIndex = -1;
        }
        return current;
    }

    private EmployeFacade getFacade() {
        return ejbFacade;
    }

    public List<Employe> completeNomEmploye(String query) {
        String req = "select * from employe e where e.status='E'";
        List<Employe> list = (List<Employe>) entityManager.createNativeQuery(req, Employe.class).getResultList();
        List<Employe> filtered = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Employe motif = list.get(i);
            if (motif.toString().toLowerCase().contains(query)) {
                filtered.add(motif);
            }
        }

        return filtered;
    }

    public DataModel<Employe> getAllEmploye() {
        if (allEmploye == null) {
            String req = "select * from employe order by nom ASC";
            List<Employe> list = (List<Employe>) entityManager.createNativeQuery(req, Employe.class).getResultList();
            allEmploye = new ListDataModel<>();
            allEmploye.setWrappedData(list);
        }
        return allEmploye;
    }

    public void setAllEmploye(DataModel<Employe> AllEmploye) {
        this.allEmploye = AllEmploye;
    }

    public List<Employe> getEmbauchEmploy() {
        String req = "select * from employe e where e.status='E' order by nom ASC";
        List<Employe> list = (List<Employe>) entityManager.createNativeQuery(req, Employe.class).getResultList();
        return list;
    }

    public List<Employe> getDebauchEmploy() {
        String req = "select * from employe e where e.status='D' order by nom ASC";
        List<Employe> list = (List<Employe>) entityManager.createNativeQuery(req, Employe.class).getResultList();
        return list;
    }

    public Employe getEmploye() {
        if (employe == null) {
            employe = new Employe();
        }
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
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

    //Une partie du code pour les formulaire de création et de modification des employés
    public Boolean getBol1() {
        return bol1;
    }

    public Boolean getBol2() {
        return bol2;
    }

    public void afFormCreerEmploy() {
        bol1 = true;
        bol2 = false;
    }

    public void afFormEditEmploy() {
        bol2 = true;
        bol1 = false;
    }

    public void annuler12() {
        bol1 = false;
        bol2 = false;
        current = new Employe();
    }

    public Boolean cacheButton() {
        return !((bol1 == true) || (bol2 == true));
    }

    public void debaucher() {
        current = (Employe) getAllEmploye().getRowData();
        try {
            current.setStatus('D');
            current.setDateDebauche(Calendar.getInstance().getTime());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeUpdated"));
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Employé mis à jour avec succès!!");
            prepareCreate();
            //return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.INFO, "Une erreur de persistance!!");
            prepareCreate();
            //return null;
        }
    }

    public void embaucher() {
        current = (Employe) getAllEmploye().getRowData();
        try {
            current.setStatus('E');
            current.setDateEmbauche(Calendar.getInstance().getTime());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeUpdated"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.INFO, "Employé mis à jour avec succès!!");
            prepareCreate();
            //return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.INFO, "Une erreur de persistence!!");
            prepareCreate();
            //return null;
        }
    }

    public Employe verifyNomPrenomEmploye(String nom, String prenom) {
        Employe retour;
        //Boolean exist = true;
        try {
            String req = "select * from employe e where e.nom='" + nom + "' and e.prenom='" + prenom + "'";
            retour = (Employe) entityManager.createNativeQuery(req, Employe.class).getSingleResult();
            //retour = entityManager.createNamedQuery("Produit.findByDesignation", Produit.class).setParameter("designation", designation).getSingleResult();
        } catch (NoResultException e) {
            retour = null;
            //exist = false;
        }
        return retour;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Employe) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public void prepareCreate() {
        current = new Employe();
        selectedItemIndex = -1;
        //return "Create";
    }

    public void create() {
        if (getSelected().getNom().equals("")) {
            JsfUtil.addErrorMessage("Aucun nom saisie!! Veuillez saisir le nom avant de valider");
            return;
        }
        if (getSelected().getTelephone().equals("")) {
            JsfUtil.addErrorMessage("Entrez le numéro de téléphone de l'employé!!");
            return;
        }
        if (getSelected().getRole() == null) {
            JsfUtil.addErrorMessage("Aucun rôle selectionné!! Veuillez choisir le rôle avant de valider");
            return;
        }
        try {
            Employe p = verifyNomPrenomEmploye(this.getSelected().getNom(), this.getSelected().getPrenom());
            if (p != null) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeNotUnique"));
                Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, "Employe déjà existant!!");
                /*int q=p.getQuantite()+getSelected().getQuantite();
                p.setQuantite(q);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeUpdated"));*/
            } else {
                Calendar c = Calendar.getInstance();
                Date date = c.getTime();
                current.setDateEmbauche(date);
                current.setStatus('E');
                getFacade().create(current);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeCreated"));
                Logger.getLogger(ProduitController.class.getName()).log(Level.INFO, "Employé crée avec succès!!");
                recreateModel();
                prepareCreate();

            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
            //return null;
        }
    }

    public void prepareEdit() {
        current = (Employe) getAllEmploye().getRowData();
        this.afFormEditEmploy();
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        //return "Edit";
    }

    public void update() {
        if (getSelected().getNom().equals("")) {
            JsfUtil.addErrorMessage("Aucun nom saisie!! Veuillez saisir le nom avant de valider");
            return;
        }
        if (getSelected().getTelephone().equals("")) {
            JsfUtil.addErrorMessage("Entrez le numéro de téléphone de l'employé!!");
            return;
        }
        if (getSelected().getRole() == null) {
            JsfUtil.addErrorMessage("Aucun rôle selectionné!! Veuillez choisir le rôle avant de valider");
            return;
        }
        try {
            getFacade().edit(current);
            this.annuler12();
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeUpdated"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.INFO, "Employe mis à jour avec succès!!");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void destroy() {
        current = (Employe) getAllEmploye().getRowData();
        System.out.println("Employé à supprimer " + current.toString());
        performDestroy();
        recreateModel();
        JsfUtil.addSuccessMessage("Employe supprimé avec succès!!");
        Logger.getLogger(EmployeController.class.getName()).log(Level.INFO, "Employe supprimé avec succès!!");
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("EmployeDeleted"));
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
        allEmploye = null;
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

    public Employe getEmploye(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    /* String convertInt(int n) {
        if (n < 10) {
            return "0" + n;
        }
        return n + "";
    }*/
    //Fonction permettant de créer le fichier de la liste des employés
    public List<String> createAllEmployeFile(Date hh) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        //Flux d'écriture
        BufferedOutputStream sortie;
        File f = new File(".");
        pathFileAllEmploye = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + employeDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileAllEmploye = f.getAbsolutePath();

        f = new File(pathFileAllEmploye + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileAllEmploye = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileAllEmploye + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileAllEmploye + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileAllEmploye = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "ListeDeTousLesEmployes" + formaterDateFile.format(hh) + ".pdf";

        //String nomFichier = "ListeDeTousLesEmployes" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileAllEmploye + sepa + nomFichier));
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
            Paragraph entete12 = new Paragraph(titreDocumentAllEmploy, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_LEFT);
            //Paragraph entete5 = new Paragraph(acteurAutre + destinataireS, police_entete_1_f);entete5.setAlignment(Element.ALIGN_LEFT);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete4);
            //document.add(entete5);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Nom & Prénom", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Téléphone", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Rôle", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Status", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            for (Employe h : getAllEmploye()) {

                Phrase ph3 = new Phrase(h.toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);

                Phrase ph4 = new Phrase(h.getTelephone(), police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);

                Phrase ph5 = new Phrase(h.getRole().getRole(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);

                if (h.getStatus() == 'E') {
                    Phrase ph6 = new Phrase("Embauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);
                } else {
                    Phrase ph6 = new Phrase("Débauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);

                }

            }
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);
            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileAllEmploye + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    //Fonction pour le téléchargement du fichier du destockage en cours
    public void downloadAllEmployeFile() {
        Calendar c = Calendar.getInstance();

        List<String> liste = createAllEmployeFile(c.getTime());
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileAllEmploye + Util.getSeparateurSys() + chemin, this.pathFileAllEmploye + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileAllEmploye + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileAllEmploye + Util.getSeparateurSys() + nomfichier);

        //String chemin = createAllEmployeFile(c.getTime());
        //File file = new File(pathFileAllEmploye + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<String> createEmbauchEmployeFile(Date hh) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        //Flux d'écriture
        BufferedOutputStream sortie;
        File f = new File(".");
        pathFileEmbauchEmploy = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + employeDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileEmbauchEmploy = f.getAbsolutePath();

        f = new File(pathFileEmbauchEmploy + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileEmbauchEmploy = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileEmbauchEmploy + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileEmbauchEmploy + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileEmbauchEmploy = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "ListeDesEmployesEmbauches" + formaterDateFile.format(hh) + ".pdf";

        //String nomFichier = "ListeDesEmployesEmbauches" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileEmbauchEmploy + sepa + nomFichier));
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
            Paragraph entete12 = new Paragraph(titreDocumentEmployEmbauche, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_LEFT);
            //Paragraph entete5 = new Paragraph(acteurAutre + destinataireS, police_entete_1_f);entete5.setAlignment(Element.ALIGN_LEFT);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete4);
            //document.add(entete5);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Nom & Prénom", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Téléphone", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Rôle", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Status", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            getEmbauchEmploy().stream().forEach((h) -> {
                Phrase ph3 = new Phrase(h.toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);

                Phrase ph4 = new Phrase(h.getTelephone(), police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);

                Phrase ph5 = new Phrase(h.getRole().getRole(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);

                if (h.getStatus() == 'E') {
                    Phrase ph6 = new Phrase("Embauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);
                } else {
                    Phrase ph6 = new Phrase("Débauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);

                }
            });
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);
            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileEmbauchEmploy + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    //Fonction pour le téléchargement du fichier du destockage en cours
    public void downloadEmbauchEmployeFile() {
        Calendar c = Calendar.getInstance();

        List<String> liste = createEmbauchEmployeFile(c.getTime());
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileEmbauchEmploy + Util.getSeparateurSys() + chemin, this.pathFileEmbauchEmploy + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileEmbauchEmploy + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileEmbauchEmploy + Util.getSeparateurSys() + nomfichier);

        //String chemin = createEmbauchEmployeFile(c.getTime());
        //File file = new File(pathFileEmbauchEmploy + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<String> createDebauchEmployeFile(Date hh) {
        //Document Itext
        Document document = new Document(PageSize.A4);
        //Flux d'écriture
        BufferedOutputStream sortie;
        File f = new File(".");
        pathFileDebauchEmploy = f.getAbsolutePath();
        //System.out.println("PathFile: " + getPathFile());
        //Calendar
        int mois = hh.getMonth() + 1;
        //int jour = hh.getDate();
        int year = hh.getYear() + 1900;
        //String h_m_s = convertInt(hh.getHours()) + "h" + convertInt(hh.getMinutes()) + "m" + convertInt(hh.getSeconds()) + "s";
        //Partie qui déterminera le type de séparateur
        String sepa = Util.getSeparateurSys();
        //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
        f = new File(Util.getDirectoryParent() + sepa + employeDirectory + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDebauchEmploy = f.getAbsolutePath();

        f = new File(pathFileDebauchEmploy + sepa + "Annee " + year + sepa);
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDebauchEmploy = f.getAbsolutePath();

        if (mois == 1) {
            f = new File(pathFileAllEmploye + sepa + "1er_mois" + sepa);
        } else {
            f = new File(pathFileDebauchEmploy + sepa + mois + "e_mois" + sepa);
        }
        if (!f.exists()) {
            f.mkdir();
        }
        System.out.println("Chemin absolu de file mkdir crée avec separateur: " + f.getAbsolutePath());
        pathFileDebauchEmploy = f.getAbsolutePath();

        String nomFichier;
        nomFichier = "tempPdf.pdf";
        String stamper;
        stamper = "ListeDesEmployesDebauches" + formaterDateFile.format(hh) + ".pdf";

        // String nomFichier = "ListeDesEmployesDebauches" + formaterDateFile.format(hh) + ".pdf";
        try {
            //Partie de l'écriture du fichier
            sortie = new BufferedOutputStream(new FileOutputStream(pathFileDebauchEmploy + sepa + nomFichier));
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
            Paragraph entete12 = new Paragraph(titreDocumentAllEmploy, police_entete_f);
            entete12.setAlignment(Element.ALIGN_CENTER);
            Paragraph entete4 = new Paragraph(acteurPrincipal + Util.getUsers().getRole() + ":" + Util.getUserName(), police_entete_1_f);
            entete4.setAlignment(Element.ALIGN_LEFT);
            //Paragraph entete5 = new Paragraph(acteurAutre + destinataireS, police_entete_1_f);entete5.setAlignment(Element.ALIGN_LEFT);
            //document.add(entete1);
            document.add(entete11);
            document.add(entete12);
            document.add(entete4);
            //document.add(entete5);

            Paragraph deuxieme_ligne = new Paragraph("                          ");
            document.add(deuxieme_ligne);

            //Création du tableau pour afficher le rapport
            //On commence par créer la première ligne du tableau qui indique les noms des différentes colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(98);//c'etait 95

            PdfPCell c1 = new PdfPCell(new Phrase("Nom & Prénom", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Téléphone", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Rôle", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            c1 = new PdfPCell(new Phrase("Status", police_premiere_ligne_tableau));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            //le nombre de lignes constituant la ligne des titres
            table.setHeaderRows(1);

            //Création des différentes lignes du tableau
            for (Employe h : getAllEmploye()) {
                Phrase ph3 = new Phrase(h.toString(), police_tableau);
                PdfPCell pcell3 = new PdfPCell(ph3);
                pcell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell3);

                Phrase ph4 = new Phrase(h.getTelephone(), police_tableau);
                PdfPCell pcell4 = new PdfPCell(ph4);
                pcell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell4);

                Phrase ph5 = new Phrase(h.getRole().getRole(), police_tableau);
                PdfPCell pcell5 = new PdfPCell(ph5);
                pcell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(pcell5);

                if (h.getStatus() == 'E') {
                    Phrase ph6 = new Phrase("Embauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);
                } else {
                    Phrase ph6 = new Phrase("Débauché", police_tableau);
                    PdfPCell pcell6 = new PdfPCell(ph6);
                    pcell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(pcell6);

                }
            }
            //affiche("nombre de colonnes: "+table.getNumberOfColumns());
            document.add(table);
            Phrase espace = new Phrase("      ");
            document.add(espace);
            //Création du pied de page : attention il n'apparaît que sur la dernière page de votre document .pdf
            //PdfContentByte cb1 = writer.getDirectContent();
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //f=new File(pathFile+nomFichier);*
        System.out.println("Le fichier générée: " + pathFileDebauchEmploy + nomFichier);
        document.close();
        List<String> liste = new ArrayList<>();
        liste.add(nomFichier);
        liste.add(stamper);
        return liste;
    }

    //Fonction pour le téléchargement du fichier du destockage en cours
    public void downloadDebauchEmployeFile() {
        Calendar c = Calendar.getInstance();

        List<String> liste = createDebauchEmployeFile(c.getTime());
        String chemin = liste.get(0);
        String nomfichier = liste.get(1);
        try {
            manipulatePdf(this.pathFileDebauchEmploy + Util.getSeparateurSys() + chemin, this.pathFileDebauchEmploy + Util.getSeparateurSys() + nomfichier);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //On supprime le fichier qui ne sert plus
        File file = new File(this.pathFileDebauchEmploy + Util.getSeparateurSys() + chemin);
        file.delete();

        //String chemin = createHistoryWeekFile();
        file = new File(this.pathFileDebauchEmploy + Util.getSeparateurSys() + nomfichier);

        //String chemin = createDebauchEmployeFile(c.getTime());
        //File file = new File(pathFileDebauchEmploy + Util.getSeparateurSys() + chemin);
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
            Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                Logger.getLogger(EmployeController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
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

    @FacesConverter(forClass = Employe.class)
    public static class EmployeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EmployeController controller = (EmployeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "employeController");
            return controller.getEmploye(getKey(value));
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
            if (object instanceof Employe) {
                Employe o = (Employe) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Employe.class.getName());
            }
        }

    }

}
