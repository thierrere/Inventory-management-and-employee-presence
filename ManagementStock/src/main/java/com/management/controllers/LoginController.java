package com.management.controllers;

import com.management.jpa.Users;
import com.management.sessionbeans.UsersFacade;
import com.management.utils.Util;
import java.io.File;
import java.io.IOException;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@ManagedBean(name = "loginBean")
@ApplicationScoped
public class LoginController implements Serializable {

    @EJB
    private com.management.sessionbeans.UsersFacade ejbFacade;
    private Users user;
    private String password;
    private String message, uname;
    SimpleDateFormat formaterDateFile = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");

    public LoginController() {
        ejbFacade = new com.management.sessionbeans.UsersFacade();
    }

    public String loginProject() {
        System.out.println("LoginBean: Tentative de connection!!!!!!!");
        Logger.getLogger(LoginController.class.getName()).log(Level.INFO,"Tentative de connection!!!");
        user = getEjbFacade().login(uname, password);
        if (user != null) {
            //Save information about connection
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            user.setLastConnection(date);
            getEjbFacade().edit(user);

            //Création repertoire edifice_stock sur le disque
            File f = new File(".");
            String pathFile = f.getAbsolutePath();
            //Partie qui déterminera le type de séparateur
            String sepa = "";
            if (pathFile.contains("\\")) {
                sepa = "\\";
            } else if (pathFile.contains("/")) {
                sepa = "/";
            }
            int in = pathFile.lastIndexOf(".");
            pathFile = pathFile.substring(0, in);
            System.out.println("PathFile: " + pathFile);
            System.out.println("Sepa: " + sepa);
            //Création d'un repétoire dans le repertoire courant si aucun repertoire existant
            f = new File(sepa + "Edifice_Stock" + sepa);
            if (!f.exists()) {
                f.mkdir();
            }

            //Partie pour bloquer l'accès à l'administration
            //Partie pour le super-Administrateur
            // get Http Session and store username
            HttpSession session = Util.getSession();
            session.setAttribute("directoryParent", f.getAbsolutePath());
            session.setAttribute("separateurSys", sepa);
            session.setAttribute("userid", user.getId());
            session.setAttribute("username", user.getFirstname() + "  " + user.getLastname());
            user.setPassword("");
            session.setAttribute("user", user);
            Logger.getLogger(LoginController.class.getName()).log(Level.INFO,"Tentative de connection reussie!!!");
            saveBD();
            return "/faces/home";
        } else {

            System.out.println("User not in database! or User Desactivated");
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "(Login ou Mot de Passe) invalides ou Utilisateur bloqué!",
                            "Veuillez ressayer ou contacter un administrateur!"));
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE,"Tentative de connection echouée!!!");
            //message = "Invalid Login. Please Try Again!";
            return "/faces/auth/login";
        }
    }

    public String logout() {
        HttpSession session = Util.getSession();
        session.invalidate();
        user = null;
        Logger.getLogger(LoginController.class.getName()).log(Level.INFO,"Déconnexion!!!");
        return "/faces/auth/login";
    }
    
    public String home(){
        return "/faces/home";
    }
    
    public String reconnecter() {
        HttpSession session = Util.getSession();
        session.invalidate();
        user = null;
        Logger.getLogger(LoginController.class.getName()).log(Level.INFO,"Erreur de session apparue!!!");
        return "/faces/auth/login";
    }

    public UsersFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(UsersFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    /*private UsersFacade getFacade() {
     return ejbFacade;
     }*/
    public String getUsername() {
        return Util.getUserName();
    }

    public Integer getUserid() {
        return Util.getUserId();
    }

    public Users getUserconnected() {
        return Util.getUsers();
    }

    public Boolean ifAdmin() {
        return Util.ifAdmin();
    }

    public Boolean ifSuperAdmin() {
        return Util.ifSuperAdmin();
    }

    public Boolean ifMagasin() {
        return Util.ifMagasin();
    }

    public List<String> lesRoles() {
        List<String> list = new ArrayList<>();
        if (this.ifAdmin()) {
            list = new ArrayList<>();
            list.add("Magasinier(e)");
            list.add("Administrateur");
            list.add("Utilisateur");
        } else if (this.ifSuperAdmin()) {
            list = new ArrayList<>();
            list.add("Super-Administrateur");
            list.add("Magasinier(e)");
            list.add("Administrateur");
            list.add("Utilisateur");
        }
        return list;
    }

    public Boolean thisIsAdmin() {
        return (this.ifAdmin() || this.ifSuperAdmin());
    }

    public Boolean thisIsNotAdmin() {
        return !(this.ifAdmin() || this.ifSuperAdmin());
    }

    public Boolean roleSuperieur(String role) {
        
        if(Util.ifSuperAdmin()){
            //System.out.println("Ici je suis: "+Util.getUsers().getRole()+" Role a comparer: "+role);
            return (role.equals("Administrateur") || role.equals("Magasinier(e)") || role.equals("Utilisateur"));
        }
        else if (Util.ifAdmin()) {
            return (role.equals("Magasinier(e)") || role.equals("Utilisateur"));
        }
        else{
            return false;
        }
    }
    
    public Boolean connected(String login){
        return !(Util.getUsers().getLogin().equals(login));
    }
    
    public void saveBD(){
        String pathFileSaveDB="";
        String saveDBDirectory= "SauvegardeDB";
        Date hh=Calendar.getInstance().getTime();
        File f = new File(".");        
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

        String nomFichier = "saveDBConnexion" + formaterDateFile.format(hh) + ".backup"; 
        
        try {
           String cmd = "pg_dump.exe -h 127.0.0.1 -p 5432 -U postgres -w -f \""+pathFileSaveDB+sepa+nomFichier+"\" edifice_stock";
            System.out.println("Sauvegarder la Base de données!");
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Sauvegarde Base de données en cours!!");
            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            java.lang.Process p = rt.exec(cmd);
            Logger.getLogger(ProduitController.class.getName()).log(Level.INFO,"Sauvegarde Base de données reussie!!");
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void restoreBD(){
        try {
           String cmd = "psql -h 127.0.0.1 -p 5432 -U postgres -w -f \"C:\\Users\\Thierry\\Desktop\\testSave.backup\" edifice_stock";
            System.out.println("Restauration Base de données!");
            java.lang.Runtime rt = java.lang.Runtime.getRuntime();
            java.lang.Process p = rt.exec(cmd);
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
