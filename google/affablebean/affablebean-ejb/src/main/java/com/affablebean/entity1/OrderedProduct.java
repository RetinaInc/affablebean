/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.entity1;

public class OrderedProduct {

	private final Long customerOrderId;
	private final Long productId;
	private final Integer quantity;

	public OrderedProduct(Long customerOrderId, Long productId, Integer quantity) {
		this.customerOrderId = customerOrderId;
		this.productId = productId;
		this.quantity = quantity;
	}

	public Long getCustomerOrderId() {
		return customerOrderId;
	}

	public Long getProductId() {
		return productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "OrderedProduct{" + "customerOrderId=" + customerOrderId
						+ ", productId=" + productId + ", quantity=" + quantity + '}';
	}

}
