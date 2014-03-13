/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.affablebean.entity;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author osman
 */
@Entity
@Table(name = "msg_feedback")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "MsgFeedback.findAll", query = "SELECT m FROM MsgFeedback m"),
	@NamedQuery(name = "MsgFeedback.findById", query = "SELECT m FROM MsgFeedback m WHERE m.id = :id"),
	@NamedQuery(name = "MsgFeedback.findByName", query = "SELECT m FROM MsgFeedback m WHERE m.name = :name"),
	@NamedQuery(name = "MsgFeedback.findByEmail", query = "SELECT m FROM MsgFeedback m WHERE m.email = :email"),
	@NamedQuery(name = "MsgFeedback.findByMsg", query = "SELECT m FROM MsgFeedback m WHERE m.msg = :msg")})
public class MsgFeedback implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "id")
	private Integer id;
	@Basic(optional = false)
  @Column(name = "name")
	private String name;
	@Basic(optional = false)
  @Column(name = "email")
	private String email;
	@Basic(optional = false)
  @Column(name = "msg")
	private String msg;
	@JoinColumn(name = "subject_id", referencedColumnName = "id")
  @ManyToOne
	private MsgSubject subjectId;

	public MsgFeedback() {
	}

	public MsgFeedback(Integer id) {
		this.id = id;
	}

	public MsgFeedback(Integer id, String name, String email, String msg) {
		this(name, email, msg);
		this.id = id;
	}

	public MsgFeedback(String name, String email, String msg) {
		this.name = name;
		this.email = email;
		this.msg = msg;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public MsgSubject getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(MsgSubject subjectId) {
		this.subjectId = subjectId;
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
		if (!(object instanceof MsgFeedback)) {
			return false;
		}
		MsgFeedback other = (MsgFeedback) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.affablebean.entity1.MsgFeedback[ id=" + id + " ]";
	}
	
}
