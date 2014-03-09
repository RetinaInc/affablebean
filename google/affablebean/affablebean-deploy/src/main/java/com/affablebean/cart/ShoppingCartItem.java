/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.cart;

import com.affablebean.entity.Product;

import java.io.Serializable;

/**
 *
 * @author tgiunipero
 */
public final class ShoppingCartItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Product product;
	private short quantity = 1;

	public ShoppingCartItem(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public short getQuantity() {
		return quantity;
	}


	public double getTotal() {
		return quantity * product.getPrice().doubleValue();
	}

	void setQuantity(short quantity) {
		this.quantity = quantity;
	}

	void incrementQuantity() {
		quantity++;
	}

	void decrementQuantity() {
		quantity--;
	}

}
