/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.entity1;

import java.math.BigDecimal;
import java.util.Date;

public class CustomerOrder {

	private final Long id;
	private final BigDecimal amount;
	private final Date dateCreated;
	private final Long confirmationNumber;
	private final Long customerId;

	public CustomerOrder(Long id, BigDecimal amount, Date dateCreated,
					Long confirmationNumber, Long customerId) {
		this.id = id;
		this.amount = amount;
		this.dateCreated = dateCreated;
		this.confirmationNumber = confirmationNumber;
		this.customerId = customerId;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Long getConfirmationNumber() {
		return confirmationNumber;
	}

	public Long getCustomerId() {
		return customerId;
	}

	@Override
	public String toString() {
		return "CustomerOrder{" + "id=" + id + ", amount=" + amount
						+ ", dateCreated=" + dateCreated + ", confirmationNumber="
						+ confirmationNumber + ", customerId=" + customerId + '}';
	}

	public Customer getCustomer() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
