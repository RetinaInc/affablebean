/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.session;

import com.affablebean.cart.ShoppingCart;
import com.affablebean.cart.ShoppingCartItem;
import com.affablebean.entity.Customer;
import com.affablebean.entity.CustomerOrder;
import com.affablebean.entity.OrderedProduct;
import com.affablebean.entity.OrderedProductPK;
import com.affablebean.entity.Product;
import static com.affablebean.session.EntityMgr.EM;
import static com.affablebean.session.CustomerOrderJpaController.CUST_ORD_CTL;
import static com.affablebean.session.OrderedProductJpaController.ORD_PROD_CTL;
import static com.affablebean.session.ProductJpaController.PROD_CTL;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author tgiunipero
 */
public enum OrderManager {

	ORD_MGR;
	private static final Random random = new Random();
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public int placeOrder(ShoppingCart cart, String surcharge, String... order) {
		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();

			Customer customer = addCustomer(em, order);
			CustomerOrder co = addOrder(em, customer, cart, surcharge);
			addOrderedItems(em, co, cart);

			em.getTransaction().commit();

			return co.getId();

		} catch (Exception e) {
			em.getTransaction().rollback();
			return 0;

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public Map<String, Object> getOrderDetails(int orderId) {
		// get order
		CustomerOrder order = CUST_ORD_CTL.findCustomerOrder(orderId);
		// why is this null in gae when it has been set in mysql?		
		order.setDateCreated(new Date()); 

		// get customer
		Customer customer = order.getCustomerId();

		// get all ordered products
		List<OrderedProduct> orderedProducts
						= ORD_PROD_CTL.findOrderedProducts(orderId);

		// get product details for ordered items
		List<Product> products = new ArrayList<>();

		for (OrderedProduct op : orderedProducts) {
			Product p = PROD_CTL.findProduct(op.getOrderedProductPK().getProductId());
			products.add(p);
		}

		// add each item to orderMap
		Map<String, Object> orderMap = new HashMap<>();

		orderMap.put("orderRecord", order);
		orderMap.put("customer", customer);
		orderMap.put("orderedProducts", orderedProducts);
		orderMap.put("products", products);

		return orderMap;
	}

	private Customer addCustomer(EntityManager em, String... cust) {
		Customer customer = new Customer();

		customer.setName(cust[0]);
		customer.setEmail(cust[1]);
		customer.setPhone(cust[2]);
		customer.setAddress(cust[3]);
		customer.setCityRegion(cust[4]);
		customer.setCcNumber(cust[5]);

		em.persist(customer);
		return customer;
	}

	private CustomerOrder addOrder(EntityManager em, Customer customer,
					ShoppingCart cart, String surcharge) {

		CustomerOrder order = new CustomerOrder();

		order.setCustomerId(customer);
		order.setAmount(BigDecimal.valueOf(cart.getSubtotal()
						+ Double.parseDouble(surcharge)));
		order.setConfirmationNumber(random.nextInt(999999999));

		em.persist(order);
		return order;
	}

	private void addOrderedItems(EntityManager em, CustomerOrder order,
					ShoppingCart cart) {

		em.flush();
		Collection<ShoppingCartItem> items = cart.getItems();

		// iterate through shopping cart and create OrderedProducts
		for (ShoppingCartItem scItem : items) {
			int productId = scItem.getProduct().getId();

			// set up primary key object
			OrderedProductPK orderedProductPK = new OrderedProductPK();
			orderedProductPK.setCustomerOrderId(order.getId());
			orderedProductPK.setProductId(productId);

			// create ordered item using PK object
			OrderedProduct orderedItem = new OrderedProduct(orderedProductPK);

			// set quantity
			orderedItem.setQuantity(scItem.getQuantity());

			em.persist(orderedItem);
		}
	}
}
