/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.controller;

import com.affablebean.entity.Customer;
import com.affablebean.entity.MsgFeedback;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import static com.affablebean.session.OrderManager.ORD_MGR;
import static com.affablebean.session.CustomerJpaController.CUST_CTL;
import static com.affablebean.session.CustomerOrderJpaController.CUST_ORD_CTL;
import static com.affablebean.session.MsgFeedbackJpaController.MSG_FEED_CTL;

/**
 *
 * @author tgiunipero
 */
public final class AdminCtrl extends HttpServlet {

	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request,
					HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	private void processRequest(HttpServletRequest request,
					HttpServletResponse response) throws ServletException, IOException {

		String userPath = request.getServletPath();

		switch (userPath) {
			case "/admin/viewCustomers":
				request.setAttribute("customerList", CUST_CTL.findCustomerEntities());
				break;

			case "/admin/viewFeedback":
				request.setAttribute("feedbackList",
								MSG_FEED_CTL.findMsgFeedbackEntities());
				break;

			case "/admin/viewOrders":
				request.setAttribute("orderList", CUST_ORD_CTL.findCustomerOrderEntities());
				break;

			case "/admin/customerRecord":
				getCustomerRecord(request);
				break;

			case "/admin/feedbackRecord":
				getFeedbackRecord(request);
				break;

			case "/admin/orderRecord":
				getOrderRecord(request);
				break;

			case "/admin/logout":
				request.getSession().invalidate();   // terminate session
				response.sendRedirect("/AffableBean/admin/");
				return;

			default:
				System.err.println("No handler found for " + userPath);
		}

		// use RequestDispatcher to forward request internally
		request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
	}

	private void getCustomerRecord(HttpServletRequest request)
					throws NumberFormatException {

		// get customer ID from request
		String customerId = request.getQueryString();
		final int id = Integer.parseInt(customerId);

		// get customer details
		Customer customer = CUST_CTL.findCustomer(id);
		request.setAttribute("customerRecord", customer);

		// get customer orders
		request.setAttribute("orders", CUST_ORD_CTL.findCustomerOrders(id));
	}

	private void getFeedbackRecord(HttpServletRequest request)
					throws NumberFormatException {

		String msgId = request.getQueryString();

		MsgFeedback msg = MSG_FEED_CTL.findMsgFeedback(Integer.parseInt(msgId));
		request.setAttribute("feedbackRecord", msg);
	}

	private void getOrderRecord(HttpServletRequest request)
					throws NumberFormatException {

		// get customer ID from request
		String orderId = request.getQueryString();

		// get order details
		Map<String, Object> orderMap
						= ORD_MGR.getOrderDetails(Integer.parseInt(orderId));

		// place order details in request scope
		request.setAttribute("customer", orderMap.get("customer"));
		request.setAttribute("products", orderMap.get("products"));
		request.setAttribute("orderRecord", orderMap.get("orderRecord"));
		request.setAttribute("orderedProducts", orderMap.get("orderedProducts"));
	}
}
