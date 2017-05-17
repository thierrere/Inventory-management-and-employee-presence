/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Thierry
 */
@Entity
@Table(name = "historique_produit", catalog = "edifice_stock", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HistoriqueProduit.findAll", query = "SELECT h FROM HistoriqueProduit h"),
    @NamedQuery(name = "HistoriqueProduit.findById", query = "SELECT h FROM HistoriqueProduit h WHERE h.id = :id"),
    @NamedQuery(name = "HistoriqueProduit.findByAction", query = "SELECT h FROM HistoriqueProduit h WHERE h.action = :action"),
    @NamedQuery(name = "HistoriqueProduit.findByProduit", query = "SELECT h FROM HistoriqueProduit h WHERE h.produit = :produit"),
    @NamedQuery(name = "HistoriqueProduit.findByClasseProduit", query = "SELECT h FROM HistoriqueProduit h WHERE h.classeProduit = :classeProduit"),
    @NamedQuery(name = "HistoriqueProduit.findByQuantite", query = "SELECT h FROM HistoriqueProduit h WHERE h.quantite = :quantite"),
    @NamedQuery(name = "HistoriqueProduit.findByDestinataire", query = "SELECT h FROM HistoriqueProduit h WHERE h.destinataire = :destinataire"),
    @NamedQuery(name = "HistoriqueProduit.findByDate", query = "SELECT h FROM HistoriqueProduit h WHERE h.date = :date")})
public class HistoriqueProduit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "action", length = 2147483647)
    private String action;
    @Column(name = "produit", length = 2147483647)
    private String produit;
    @Column(name = "classe_produit", length = 2147483647)
    private String classeProduit;
    @Column(name = "quantite")
    private Integer quantite;
    @Column(name = "destinataire", length = 2147483647)
    private String destinataire;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @JoinColumn(name = "users", referencedColumnName = "id")
    @ManyToOne
    private Users users;

    public HistoriqueProduit() {
    }

    public HistoriqueProduit(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getClasseProduit() {
        return classeProduit;
    }

    public void setClasseProduit(String classeProduit) {
        this.classeProduit = classeProduit;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HistoriqueProduit)) {
            return false;
        }
        HistoriqueProduit other = (HistoriqueProduit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.management.jpa.HistoriqueProduit[ id=" + id + " ]";
    }
    
}
