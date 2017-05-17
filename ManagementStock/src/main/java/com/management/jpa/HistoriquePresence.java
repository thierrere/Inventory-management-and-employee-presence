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
@Table(name = "historique_presence", catalog = "edifice_stock", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HistoriquePresence.findAll", query = "SELECT h FROM HistoriquePresence h"),
    @NamedQuery(name = "HistoriquePresence.findById", query = "SELECT h FROM HistoriquePresence h WHERE h.id = :id"),
    @NamedQuery(name = "HistoriquePresence.findByJournee", query = "SELECT h FROM HistoriquePresence h WHERE h.journee = :journee"),
    @NamedQuery(name = "HistoriquePresence.findByHeureArrivee", query = "SELECT h FROM HistoriquePresence h WHERE h.heureArrivee = :heureArrivee"),
    @NamedQuery(name = "HistoriquePresence.findByHeureDepart", query = "SELECT h FROM HistoriquePresence h WHERE h.heureDepart = :heureDepart"),
    @NamedQuery(name = "HistoriquePresence.findByDateSaisie", query = "SELECT h FROM HistoriquePresence h WHERE h.dateSaisie = :dateSaisie")})
public class HistoriquePresence implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "journee")
    @Temporal(TemporalType.TIMESTAMP)
    private Date journee;
    @Column(name = "heure_arrivee", length = 2147483647)
    private String heureArrivee;
    @Column(name = "heure_depart", length = 2147483647)
    private String heureDepart;
    @Column(name = "date_saisie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaisie;
    @JoinColumn(name = "employe", referencedColumnName = "id")
    @ManyToOne
    private Employe employe;
    @JoinColumn(name = "users", referencedColumnName = "id")
    @ManyToOne
    private Users users;

    public HistoriquePresence() {
    }

    public HistoriquePresence(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getJournee() {
        return journee;
    }

    public void setJournee(Date journee) {
        this.journee = journee;
    }

    public String getHeureArrivee() {
        return heureArrivee;
    }

    public void setHeureArrivee(String heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public String getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Date getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(Date dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
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
        if (!(object instanceof HistoriquePresence)) {
            return false;
        }
        HistoriquePresence other = (HistoriquePresence) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.management.jpa.HistoriquePresence[ id=" + id + " ]";
    }
    
}
