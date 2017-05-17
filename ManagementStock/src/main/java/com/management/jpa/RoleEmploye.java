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
@Table(name = "role_employe", catalog = "edifice_stock", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RoleEmploye.findAll", query = "SELECT r FROM RoleEmploye r"),
    @NamedQuery(name = "RoleEmploye.findById", query = "SELECT r FROM RoleEmploye r WHERE r.id = :id"),
    @NamedQuery(name = "RoleEmploye.findByRole", query = "SELECT r FROM RoleEmploye r WHERE r.role = :role")})
public class RoleEmploye implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "role", length = 2147483647)
    private String role;
    @OneToMany(mappedBy = "role")
    private List<Employe> employeList;

    public RoleEmploye() {
    }

    public RoleEmploye(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @XmlTransient
    public List<Employe> getEmployeList() {
        return employeList;
    }

    public void setEmployeList(List<Employe> employeList) {
        this.employeList = employeList;
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
        if (!(object instanceof RoleEmploye)) {
            return false;
        }
        RoleEmploye other = (RoleEmploye) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.management.jpa.RoleEmploye[ id=" + id + " ]";
    }
    
}
