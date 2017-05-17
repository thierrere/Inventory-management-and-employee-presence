/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.sessionbeans;

import com.management.controllers.util.ProduitASortir;
import com.management.jpa.ClasseProduit;
import com.management.jpa.HistoriqueProduit;
import com.management.jpa.Produit;
import com.management.utils.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

/**
 *
 * @author Thierry
 */
@TransactionManagement(value = TransactionManagementType.BEAN)
@Stateless
public class ProduitFacade extends AbstractFacade<Produit> {

    @EJB
    private com.management.sessionbeans.HistoriqueProduitFacade ejbHistoProduitFacade;

    @EJB
    private com.management.sessionbeans.ClasseProduitFacade ejbClasseProduitFacade;

    @PersistenceContext(unitName = "Edifice_StockPU")
    private EntityManager em;

    @Resource
    UserTransaction userTransaction;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProduitFacade() {
        super(Produit.class);
    }
    
    public int createNew(Produit current, Date date) throws Exception{
        int retour=-1;
        try {
            userTransaction.begin();
            HistoriqueProduit histoProd = new HistoriqueProduit();
            histoProd.setAction("Stockage");
            histoProd.setUsers(Util.getUsers());
                this.create(current);
                histoProd.setClasseProduit(current.getClasse().getNom());
                histoProd.setDate(date);
                histoProd.setProduit(current.getDesignation() + "(" + current.getNombreExemplaire() + ")");
                histoProd.setQuantite(current.getQuantite());
                histoProd.setDestinataire("");
                this.ejbHistoProduitFacade.create(histoProd);
                userTransaction.commit();
                retour=0;
        } catch (Exception e) {
          userTransaction.rollback();
        }
        return retour;
    }
    
    public int updateProduitQuantity(int updateQuantity, Produit updateQuantityProd, Date date)throws Exception{
        int retour=-1;
        try {
            userTransaction.begin();
            HistoriqueProduit histoProd = new HistoriqueProduit();
            //Le produit existe ajout de la quantité du produit
            System.out.println("Ajout de la quantité du produit existant "+ updateQuantityProd.getDesignation() +" !!");
            int q = updateQuantityProd.getQuantite() + updateQuantity;
            updateQuantityProd.setQuantite(q);
            //getFacade().edit(updateQuantityProd);
            this.edit(updateQuantityProd);
            //entityManager.merge(updateQuantityProd);

            //sauvegarde de l'historique
            histoProd.setAction("Stockage-Ajout quantité");
            histoProd.setUsers(Util.getUsers());
            histoProd.setClasseProduit(updateQuantityProd.getClasse().getNom());
            histoProd.setDate(date);
            histoProd.setProduit(updateQuantityProd.getDesignation() + "(" + updateQuantityProd.getNombreExemplaire() + ")");
            histoProd.setQuantite(updateQuantity);
            histoProd.setDestinataire("");
            ejbHistoProduitFacade.create(histoProd);
            userTransaction.commit();
            retour=0;
            System.out.println("Produit " + updateQuantityProd.toString() + " ajout de la quantité: " + updateQuantity);
        } catch (Exception e) {
            userTransaction.rollback();
        }
        return retour;
    }
    
    public int update(Produit p) throws Exception{
        int retour=-1;
        
        try{
            userTransaction.begin();
            this.edit(p);
            userTransaction.commit();
            retour=0;
        }
        catch(Exception e){
            userTransaction.rollback();
        }
        
        return retour;
    }
    
    public List<Produit> orderProduitClasse(int famille){
        String req = "select * from produit p where p.classe='"+famille+"'  order by p.designation ASC";
        List<Produit> list = (List<Produit>) em.createNativeQuery(req, Produit.class).getResultList();
        return list;
    }

    public int savePAS(List<ProduitASortir> liste, String destinataire, Date date) throws Exception {
        int retour = -1;

        try {
            userTransaction.begin();
            liste.stream().forEach((p) -> {
                // this.getListProduitASortir().stream().forEach((p) -> {
                int qBD = p.getProduit().getQuantite();
                int qAS = p.getQuantite();
                p.getProduit().setQuantite(qBD - qAS);
                this.edit(p.getProduit());

                //Sauvegarde de l'historique des événements
                HistoriqueProduit histoProd = new HistoriqueProduit();
                histoProd.setAction("Destockage");
                histoProd.setUsers(Util.getUsers());
                histoProd.setClasseProduit(p.getProduit().getClasse().getNom());
                histoProd.setDate(date);
                histoProd.setProduit(p.getProduit().getDesignation() + "(" + p.getProduit().getNombreExemplaire() + ")");
                histoProd.setQuantite(qAS);
                histoProd.setDestinataire(destinataire);
                ejbHistoProduitFacade.create(histoProd);
                System.out.println("Produit " + p.getProduit().toString() + " destocker de la quantité: " + qAS);
                //});
            });

            userTransaction.commit();
            retour = 0;
        } catch (Exception e) {
            userTransaction.rollback();
        }
        return retour;
    }

    public int readCVSFile(String pathcsvf, Date date) throws Exception {
        //StringTokenizer st = null;
        int retour = -1;
        String line = null;
        BufferedReader stream = null;

        try {
            userTransaction.begin();
            stream = new BufferedReader(new FileReader(pathcsvf));
            //On saute la lecture de l'entête du fichier
            for (int i = 0; i < 1; i++) {
                stream.readLine();
            }
            //Maintenant nous allons lire la 18ième ligne
            //Où commence les produits
            //Split range les éléments entre les ";" dans un tableau ce qui facilite la récupération
            //Des colones
            System.out.println("Désignation            Quantité              Famille             Nombre Exemplaire");
            while ((line = stream.readLine()) != null) {
                String[] splitted = line.split(";");
                //System.out.println("length Splitted: "+splitted.length);
                if (splitted.length != 0) {
                    System.out.println(splitted[0] + "  " + splitted[1] + "  " + splitted[2] + "  " + splitted[3]);
                    //Appel de la fonction de stockage
                    stockerCSVProd(splitted[0], Integer.parseInt(splitted[1]), splitted[2], Integer.parseInt(splitted[3]), date);
                }
            }
            userTransaction.commit();
            retour = 0;
        } catch (Exception e) {
            userTransaction.rollback();
            retour = -1;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return retour;
    }

    //Fonction qui permet de stocker les produits(Utlisée par la fonction lisant le fichier ".csv")
    public void stockerCSVProd(String design, int qte, String famille, int nbreE, Date date) {
        //Historique des produits
        HistoriqueProduit histoProd = new HistoriqueProduit();
        Produit p;
        ClasseProduit c;
        //s.setNumSerie(num_serie);
        //Recherche d'abord la désignation du produit
        p = this.verifyDesignationProduit(design);
        //Verification de l'unicité des familles
        c = verifyUnicityFamille(famille);
        if (c == null) {
            //Famille non existante, nous la récuperons
            c = new ClasseProduit();
            c.setNom(famille);
            ejbClasseProduitFacade.create(c);
            c = verifyUnicityFamille(famille);
        }

        if (p != null) {
            //Le produit existe ajout de la quantité du produit
            System.out.println("Produit existant!!");
            int q = p.getQuantite() + qte;
            p.setQuantite(q);
            p.setClasse(c);
            this.edit(p);

            //sauvegarde de l'historique
            histoProd.setAction("Stockage CSV-Ajout quantité");
            histoProd.setUsers(Util.getUsers());
            histoProd.setClasseProduit(c.getNom());
            histoProd.setDate(date);
            histoProd.setProduit(p.getDesignation() + "(" + p.getNombreExemplaire() + ")");
            histoProd.setQuantite(qte);
            histoProd.setDestinataire("");
            ejbHistoProduitFacade.create(histoProd);
            System.out.println("Produit " + p.toString() + " ajout de la quantité: " + qte);

        } else {
            //C'est une nouvelle classe de produit à créer
            System.out.println("Produit non existant!!");
            p = new Produit();
            p.setDesignation(design);
            p.setNombreExemplaire(nbreE);
            p.setQuantite(qte);
            p.setClasse(c);
            this.create(p);

            //sauvegarde de l'historique
            histoProd.setAction("Stockage CSV");
            histoProd.setUsers(Util.getUsers());
            histoProd.setClasseProduit(p.getClasse().getNom());
            histoProd.setDate(date);
            histoProd.setProduit(p.getDesignation() + "(" + p.getNombreExemplaire() + ")");
            histoProd.setQuantite(qte);
            histoProd.setDestinataire("");
            ejbHistoProduitFacade.create(histoProd);
            System.out.println(" Stockage Produit " + p.toString() + " avec la quantité: " + qte);

        }
    }

    public Produit verifyDesignationProduit(String designation) {
        Produit retour = null;
        //Boolean exist = true;
        try {
            retour = em.createNamedQuery("Produit.findByDesignation", Produit.class).setParameter("designation", designation).getSingleResult();
        } catch (NoResultException e) {
            retour = null;
            //exist = false;
        }
        return retour;
    }

    public ClasseProduit verifyUnicityFamille(String nom) {
        ClasseProduit retour;
        try {
            retour = em.createNamedQuery("ClasseProduit.findByNom", ClasseProduit.class).setParameter("nom", nom).getSingleResult();
        } catch (NoResultException e) {
            retour = null;
        }
        return retour;
    }

}
