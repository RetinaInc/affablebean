/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.entity1;

public class Customer {

	private final Long id;
	private final String name;
	private final String email;
	private final String phone;
	private final String address;
	private final String cityRegion;
	private final String ccNumber;

	public Customer(Long id, String name, String email, String phone,
					String address, String cityRegion, String ccNumber) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
		this.cityRegion = cityRegion;
		this.ccNumber = ccNumber;
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

	public String getPhone() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public String getCityRegion() {
		return cityRegion;
	}

	public String getCcNumber() {
		return ccNumber;
	}

	@Override
	public String toString() {
		return "Customer{" + "id=" + id + ", name=" + name + ", email=" + email
						+ ", phone=" + phone + ", address=" + address + ", cityRegion="
						+ cityRegion + ", ccNumber=" + ccNumber + '}';
	}

}
