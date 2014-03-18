/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software
 * except in compliance with the terms of the license at:
 * http://developer.sun.com/berkeley_license.html
 */
package com.affablebean.controller;

import com.affablebean.cart.ShoppingCart;
import com.affablebean.cart.ShoppingCartItem;
import com.affablebean.economy.FakeDB;
import com.affablebean.entity.Category;
import com.affablebean.entity.Customer;
import com.affablebean.entity.CustomerOrder;
import com.affablebean.entity.OrderedProduct;
import com.affablebean.entity.MsgFeedback;
import com.affablebean.entity.MsgSubject;
import com.affablebean.entity.Product;
import com.affablebean.json.ABJson;
import com.affablebean.session.EntityMgr;
import com.affablebean.validate.Validator;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static com.affablebean.session.OrderManager.ORD_MGR;
import static com.affablebean.session.MsgFeedbackJpaController.MSG_FEED_CTL;
import static com.affablebean.session.MsgSubjectJpaController.MSG_SUB_CTL;
import static com.affablebean.session.CategoryJpaController.CAT_CTL;
import static com.affablebean.session.ProductJpaController.PROD_CTL;
import static com.affablebean.session.PromotionJpaController.PROMO_CTL;

/**
 *
 * @author tgiunipero
 */
public final class CustCtrl extends HttpServlet {

	private static final Random random = new Random();
	private String surcharge;
	private boolean economy; // save money on hosting costs!

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		// initialize servlet with configuration information
		surcharge = getServletContext().getInitParameter("deliverySurcharge");

		// economy setting to reduct hosting costs
		economy = (getServletContext().getInitParameter("economy").equalsIgnoreCase("1"));

		if (!economy) {
			getServletContext().setAttribute("categories", CAT_CTL.findCategories());
			getServletContext().setAttribute("subjects", MSG_SUB_CTL.findSubjects());
			getServletContext().setAttribute("sale", PROMO_CTL.findSale());
			getServletContext().setAttribute("catProms", PROMO_CTL.findCategories());
			getServletContext().setAttribute("prodProms", PROMO_CTL.findProducts());

		} else {
			getServletContext().setAttribute("categories", FakeDB.findCategories());
			getServletContext().setAttribute("subjects", FakeDB.findSubjects());
			getServletContext().setAttribute("sale", FakeDB.findSale());
			getServletContext().setAttribute("catProms", FakeDB.findCatProms());
			getServletContext().setAttribute("prodProms", FakeDB.findProdProms());
		}
	}

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

		String userPath = request.getServletPath().substring(1);
		// true even if string reads "false"!
		boolean json = (request.getParameter("json") != null);

		// if category page is requested
		switch (userPath) {
			case "category":
				getCategoryProducts(request, json);

				if (json) {
					ABJson.categoryResponse(response.getWriter(), request.getSession(),
									getServletContext());
					return;
				}

				break;

			case "categoryList":
				if (json) {
					ABJson.categoryList(response.getWriter(), getServletContext());
					return;
				}

				break;

			case "chooseLanguage":
				userPath = setLanguage(request);
				break;

			case "contact":
				if (json) {
					ABJson.subjectList(response.getWriter(), getServletContext());
					return;
				}
				break;

			case "main":
				if (json) {
					ABJson.mainResponse(response.getWriter(), getServletContext());
					return;
				}
				break;

			case "viewCart":
				checkCart(request);

				if (json) {
					ABJson.cartResponse(response.getWriter(), request.getSession(),
									getServletContext());
					return;
				}

				userPath = "cart";
				break;

			default:
//				System.err.println("No request handler found for " + userPath);
		}

		dispatchRequest(userPath, request, response, json);
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {

		// ensures that user input is interpreted as 8-bit Unicode (e.g., for 
		// Czech characters)
		request.setCharacterEncoding("UTF-8");
		String userPath = request.getServletPath().substring(1);
		// true even if string reads "false"!
		boolean json = (request.getParameter("json") != null);

		switch (userPath) {
			case "addToCart":
				addToCart(request);

				if (json) {
					ABJson.cartResponse(response.getWriter(), request.getSession(),
									getServletContext());
					return;
				}

				userPath = "category";
				break;

			case "feedback":
				if (json) {
					if (!economy && saveFeedbackJson(request)) {
						ABJson.feedbackResponse(response.getWriter(), request);
					}

					return;
				}

				if (!economy) {
					String fb = saveFeedback(request);

					if (!fb.isEmpty()) {
						userPath = fb;
					}

				} else {
					userPath = "index";
				}

				break;

			case "purchase":
				if (json) {
					if (purchaseJson(request)) {
						ABJson.confResponse(response.getWriter(), request,
										getServletContext());
					}

					return;
				}

				if (purchase(request)) {
					int showJson = Integer.valueOf(
									getServletContext().getInitParameter("showJson"));

					if (showJson != 0) {
						ABJson.confResponse(response.getWriter(), request,
										getServletContext());
						return;
					}

					userPath = "confirmation";

				} else {
					userPath = "checkout";
				}

				break;

			case "showJSON":
				showJSON(request, response.getWriter());
				return;

			case "updateCart":
				updateCart(request);

				if (json) {
					ABJson.cartResponse(response.getWriter(), request.getSession(),
									getServletContext());
					return;
				}

				userPath = "cart";
				break;

			default:
//				System.err.println("No action handler found for " + userPath);
		}

		dispatchRequest(userPath, request, response, json);
	}

	@Override
	public void destroy() {
		super.destroy();
		EntityMgr.EM.close();
	}

	private void addToCart(HttpServletRequest request)
					throws NumberFormatException {

		HttpSession session = request.getSession();
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

		// if user is adding item to cart for first time create cart object and 
		// attach it to user session
		if (cart == null) {
			cart = new ShoppingCart();
			session.setAttribute("cart", cart);
		}

		// get user input from request		
		String productId = request.getParameter("productId");

		if (!productId.isEmpty()) {
			int id = Integer.parseInt(productId);
			Product product = (!economy)
							? PROD_CTL.findProduct(id) : FakeDB.findProduct(id);
			cart.addItem(product);
		}
	}

	private void checkCart(HttpServletRequest request) {
		boolean clear = (request.getParameter("clear") != null);

		if (clear) {
			HttpSession session = request.getSession();
			ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
			cart.clear();
		}
	}

	private void dispatchRequest(String userPath, HttpServletRequest request,
					HttpServletResponse response, boolean json)
					throws ServletException, IOException {

		String url = "/WEB-INF/view/" + userPath + ".jsp";
		request.getRequestDispatcher(url).forward(request, response);
	}

	private void fakeOrder(HttpServletRequest request, String[] order,
					ShoppingCart cart) throws NumberFormatException {

		// fake execution for confirmation screen
		request.setAttribute("customer", new Customer(-1, (String) order[0],
						(String) order[1], (String) order[2], (String) order[3],
						(String) order[4], (String) order[5]));

		List<Product> products = new ArrayList<>();

		for (ShoppingCartItem item : cart.getItems()) {
			products.add(item.getProduct());
		}

		request.setAttribute("products", products);

		request.setAttribute("orderRecord", new CustomerOrder(-1,
						BigDecimal.valueOf(cart.getSubtotal() + Double.parseDouble(surcharge)),
						new Date(), random.nextInt()));

		List<OrderedProduct> op = new ArrayList<>();

		for (ShoppingCartItem item : cart.getItems()) {
			OrderedProduct o = new OrderedProduct();
			o.setProduct(item.getProduct());
			o.setQuantity(item.getQuantity());
			op.add(o);
		}

		request.setAttribute("orderedProducts", op);
	}

	private void getCategoryProducts(HttpServletRequest request, boolean json)
					throws NumberFormatException {

		// get categoryId from request
		String categoryId = request.getParameter("id");

		if (categoryId != null) {
			HttpSession session = request.getSession();
			short id = Short.parseShort(categoryId);

			if (!economy) {
				session.setAttribute("selectedCategory", CAT_CTL.findCategory(id));
				session.setAttribute("categoryProducts",
								PROD_CTL.findAllCategoryProducts(id));

			} else {
				session.setAttribute("selectedCategory", FakeDB.findCategory(id));
				session.setAttribute("categoryProducts", FakeDB.findAllCategoryProducts(id));
			}
		}
	}

	private boolean purchase(HttpServletRequest request) {
		HttpSession session = request.getSession();
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

		if (cart == null) {
			return false;
		}

		// extract user data from request
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");
		String address = request.getParameter("address");
		String cityRegion = "1"; // dont'care
		String ccNumber = request.getParameter("creditcard");

		// validate user data
		boolean valid = Validator.validateCheckOutForm(request, name, email, phone,
						address, cityRegion, ccNumber);

		if (valid) {
			return saveOrder(request, name, email, phone, address, cityRegion,
							ccNumber);
		} else {
			request.setAttribute("validationErrorFlag", true);
			return false;
		}
	}

	private boolean purchaseJson(HttpServletRequest request) throws IOException {
		HttpSession session = request.getSession();
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

		if (cart == null) {
			return false;
		}

		String name, email, phone, address, ccNumber;
		String cityRegion = "1"; // dont'care

		Map<String, String> data = ABJson.getData(request.getInputStream());

		name = data.get("name");
		email = data.get("email");
		phone = data.get("phone");
		address = data.get("address");
		ccNumber = data.get("creditcard");

		// validate user data
		boolean valid = Validator.validateCheckOutForm(request, name, email,
						phone, address, cityRegion, ccNumber);

		if (valid) {
			return saveOrder(request, name, email, phone, address, cityRegion, ccNumber);
		}

		return false;
	}

	private String setLanguage(HttpServletRequest request) {
		// get language choice
		String language = request.getParameter("language");
		// place in request scope
		request.setAttribute("language", language);

		HttpSession session = request.getSession();
		String userView = (String) session.getAttribute("view");
		String userPath;

		if ((userView != null) && (!userView.equals("/index"))) {
			// index.jsp exists outside 'view' folder so must be forwarded separately
			userPath = userView;
		} else {
			// if previous view is index or cannot be determined, send user to
			// welcome page
			userPath = "/index";
		}

		return userPath.substring(1);
	}

	private void showJSON(HttpServletRequest request, Writer response) {
		HttpSession session = request.getSession();
		String source = (String) request.getParameter("src").substring(1);
		source = source.substring(source.lastIndexOf('/') + 1, source.lastIndexOf(".jsp"));

		switch (source) {
			case "cart":
				ABJson.cartResponse(response, session, getServletContext());
				break;

			case "category":
				ABJson.categoryResponse(response, session, getServletContext());
				break;

			case "index":
				ABJson.mainResponse(response, getServletContext());
				break;

			default:
		}
	}

	private String saveFeedback(HttpServletRequest request) {
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String msg = request.getParameter("msg");
		String subject = request.getParameter("subject");

		if (Validator.validateContactForm(request, name, email, msg)) {
			writeFeedback(name, email, msg, subject);
			return "index";

		} else {
			request.setAttribute("validationErrorFlag", true);
			return "contact";
		}
	}

	private boolean saveFeedbackJson(HttpServletRequest request)
					throws IOException {

		Map<String, String> data = ABJson.getData(request.getInputStream());

		String name = data.get("name");
		String email = data.get("email");
		String msg = data.get("msg");
		String subject = data.get("subject");

		boolean valid = Validator.validateContactForm(request, name, email, msg);

		if (valid) {
			writeFeedback(name, email, msg, subject);
		}

		return valid;
	}

	private void writeFeedback(String name, String email, String msg,
					String subject) {

		MsgSubject sub = null;

		try {
			sub = MSG_SUB_CTL.findMsgSubject(Integer.valueOf(subject));
		} catch (NumberFormatException e) {
		}

		MsgFeedback feedback = new MsgFeedback(name, email, msg);
		feedback.setSubjectId(sub);

		MSG_FEED_CTL.create(feedback);
	}

	private boolean saveOrder(HttpServletRequest request, String... order) {

		HttpSession session = request.getSession();
		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

		// see method call for order element types
		// fake order execution for economy
		int orderId = (!economy)
						? ORD_MGR.placeOrder(cart, surcharge, order[0], order[1],
										order[2], order[3], order[4], order[5])
						: -1;

		// if order processed successfully send user to confirmation page
		if (orderId != 0) {
			// in case language was set using toggle, get language choice before 
			// destroying session
			Locale locale = (Locale) session.getAttribute(
							"javax.servlet.jsp.jstl.fmt.locale.session");
			String language = "";

			if (locale != null) {
				language = (String) locale.getLanguage();
			}

			if (!economy) {
				// get order details
				Map<String, Object> orderMap = ORD_MGR.getOrderDetails(orderId);

				// place order details in request scope
				request.setAttribute("customer", orderMap.get("customer"));
				request.setAttribute("products", orderMap.get("products"));
				request.setAttribute("orderRecord", orderMap.get("orderRecord"));
				request.setAttribute("orderedProducts", orderMap.get("orderedProducts"));

			} else {
				fakeOrder(request, order, cart);
			}

			// dissociate shopping cart from session
			cart = null;

			// end session
			session.invalidate();

			// if user changed language using the toggle, reset the language attribute 
			// otherwise language will be switched on confirmation page!			
			if (!language.isEmpty()) {
				request.setAttribute("language", language);
			}

			return true;

			// otherwise, send back to checkout page and display error
		} else {
			request.setAttribute("orderFailureFlag", true);
			return false;
		}
	}

	private void updateCart(HttpServletRequest request) throws
					NumberFormatException {

		// get input from request
		String productId = request.getParameter("productId");
		String quantity = request.getParameter("quantity");

		if (Validator.validateQuantity(productId, quantity)) {
			int id = Integer.parseInt(productId);
			Product product = (!economy)
							? PROD_CTL.findProduct(id) : FakeDB.findProduct(id);
			HttpSession session = request.getSession();
			ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");
			cart.update(product, quantity);
		}
	}
}
