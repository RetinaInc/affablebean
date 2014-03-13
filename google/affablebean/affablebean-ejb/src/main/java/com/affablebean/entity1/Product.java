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

public class Product {

	private final Long id;
	private final String name;
	private final BigDecimal price;
	private final String description;
	private final Date lastUpdate;
	private final Integer categoryId;

	public Product(Long id, String name, BigDecimal price, String description, 
					Date lastUpdate, Integer category_id) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.lastUpdate = lastUpdate;
		this.categoryId = category_id;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public Integer getCategory_id() {
		return categoryId;
	}

	@Override
	public String toString() {
		return "Product{" + "id=" + id + ", name=" + name + ", description="
						+ description + ", price=" + price + ", lastUpdate="
						+ lastUpdate + ", category_id=" + categoryId + '}';
	}

}
