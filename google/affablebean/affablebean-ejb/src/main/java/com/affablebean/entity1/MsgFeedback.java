/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.entity1;

public class MsgFeedback {

	private final Long id;
	private final String name;
	private final String email;
	private final String msg;
	private final Long subjectId;
	private final String subject;

	public MsgFeedback(Long id, String name, String email, String msg,
					Long subjectId) {
		this(id, name, email, msg, subjectId, "");
	}

	public MsgFeedback(Long id, String name, String email, String msg,
					Long subjectId, String subject) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.msg = msg;
		this.subjectId = subjectId;
		this.subject = subject;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getMsg() {
		return msg;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public String toString() {
		return "MsgFeedback{" + "id=" + id + ", name=" + name + ", email=" + email
						+ ", msg=" + msg + ", subjectId=" + subjectId + ", subject="
						+ subject + '}';
	}

}
