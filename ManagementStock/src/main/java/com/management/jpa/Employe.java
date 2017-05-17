/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Thierry
 */
@Entity
@Table(name = "employe", catalog = "edifice_stock", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Employe.findAll", query = "SELECT e FROM Employe e"),
    @NamedQuery(name = "Employe.findById", query = "SELECT e FROM Employe e WHERE e.id = :id"),
    @NamedQuery(name = "Employe.findByNom", query = "SELECT e FROM Employe e WHERE e.nom = :nom"),
    @NamedQuery(name = "Employe.findByPrenom", query = "SELECT e FROM Employe e WHERE e.prenom = :prenom"),
    @NamedQuery(name = "Employe.findByTelephone", query = "SELECT e FROM Employe e WHERE e.telephone = :telephone"),
    @NamedQuery(name = "Employe.findByDateEmbauche", query = "SELECT e FROM Employe e WHERE e.dateEmbauche = :dateEmbauche"),
    @NamedQuery(name = "Employe.findByDateDebauche", query = "SELECT e FROM Employe e WHERE e.dateDebauche = :dateDebauche"),
    @NamedQuery(name = "Employe.findByStatus", query = "SELECT e FROM Employe e WHERE e.status = :status")})
public class Employe implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "nom", length = 2147483647)
    private String nom;
    @Column(name = "prenom", length = 2147483647)
    private String prenom;
    @Column(name = "telephone", length = 2147483647)
    private String telephone;
    @Column(name = "date_embauche")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEmbauche;
    @Column(name = "date_debauche")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebauche;
    @Column(name = "status")
    private Character status;
    @OneToMany(mappedBy = "employe")
    private List<HistoriquePresence> historiquePresenceList;
    @JoinColumn(name = "role", referencedColumnName = "id")
    @ManyToOne
    private RoleEmploye role;

    public Employe() {
    }

    public Employe(Integer id) {
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Date getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(Date dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public Date getDateDebauche() {
        return dateDebauche;
    }

    public void setDateDebauche(Date dateDebauche) {
        this.dateDebauche = dateDebauche;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    @XmlTransient
    public List<HistoriquePresence> getHistoriquePresenceList() {
        return historiquePresenceList;
    }

    public void setHistoriquePresenceList(List<HistoriquePresence> historiquePresenceList) {
        this.historiquePresenceList = historiquePresenceList;
    }

    public RoleEmploye getRole() {
        return role;
    }

    public void setRole(RoleEmploye role) {
        this.role = role;
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
        if (!(object instanceof Employe)) {
            return false;
        }
        Employe other = (Employe) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.management.jpa.Employe[ id=" + id + " ]";
    }
    
}
