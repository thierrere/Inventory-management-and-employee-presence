/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Thierry
 */
@Entity
@Table(name = "classe_produit", catalog = "edifice_stock", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ClasseProduit.findAll", query = "SELECT c FROM ClasseProduit c"),
    @NamedQuery(name = "ClasseProduit.findById", query = "SELECT c FROM ClasseProduit c WHERE c.id = :id"),
    @NamedQuery(name = "ClasseProduit.findByNom", query = "SELECT c FROM ClasseProduit c WHERE c.nom = :nom")})
public class ClasseProduit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "nom", length = 2147483647)
    private String nom;
    @OneToMany(mappedBy = "classe")
    private List<Produit> produitList;

    public ClasseProduit() {
    }

    public ClasseProduit(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @XmlTransient
    public List<Produit> getProduitList() {
        return produitList;
    }

    public void setProduitList(List<Produit> produitList) {
        this.produitList = produitList;
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
        if (!(object instanceof ClasseProduit)) {
            return false;
        }
        ClasseProduit other = (ClasseProduit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.management.jpa.ClasseProduit[ id=" + id + " ]";
    }
    
}
