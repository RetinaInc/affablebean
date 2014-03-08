package com.affablebean.session;

import com.affablebean.cart.ShoppingCart;
import com.affablebean.cart.ShoppingCartItem;
import com.affablebean.entity1.Category;
import com.affablebean.entity1.Customer;
import com.affablebean.entity1.CustomerOrder;
import com.affablebean.entity1.MsgFeedback;
import com.affablebean.entity1.MsgSubject;
import com.affablebean.entity1.OrderedProduct;
import com.affablebean.entity1.Product;
import com.affablebean.entity1.Promotion;
import static com.affablebean.session.EntityMgr.EM;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author osman
 */
public class Facade {

	private static final Random random = new Random();
	private static final SimpleDateFormat sm
					= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public static List<Category> findAllCategories() {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM category ORDER BY name";
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<Category> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Category((Integer) row[0], (String) row[1]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	public static List<Product> findAllCategoryProducts(int id) {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM product JOIN category "
							+ "ON product.category_id = category.id "
							+ "WHERE category.id = " + id + " ORDER BY product.name";
			Query query = em.createNativeQuery(sql);
			List<Object> result = query.getResultList();
			List<Product> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Product((Long) row[0], (String) row[1], (BigDecimal) row[2],
								(String) row[3], (Date) row[4], (Integer) row[5]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	public static List<Customer> findAllCustomers() {
		String sql = "SELECT * FROM customer ORDER BY name";
		return getAllCustomers(sql);
	}

	public static List<CustomerOrder> findAllCustomerOrders() {
		String sql = "SELECT * FROM customer_order ORDER BY id";
		return getAllCustomerOrders(sql);
	}

	public static List<MsgFeedback> findAllFeedback() {
		String sql = "SELECT msg_feedback.*, msg_subject.name AS sub_name "
						+ "FROM msg_feedback JOIN msg_subject "
						+ "ON msg_feedback.subject_id = msg_subject.id ORDER BY msg_feedback.name";
		return getAllFeedback(sql);
	}

	public static List<MsgSubject> findAllSubjects() {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM msg_subject ORDER BY name";
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<MsgSubject> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new MsgSubject((Long) row[0], (String) row[1]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	public static List<OrderedProduct> findOrderedProducts(Long id) {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM ordered_product "
							+ "WHERE customer_order_id = " + id;
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<OrderedProduct> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new OrderedProduct((Long) row[0], (Long) row[1], (Integer) row[2]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	public static Category findCategory(int id) {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM category WHERE id = " + id;
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<Category> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Category((Integer) row[0], (String) row[1]));
			}

			return l.get(0);

		} finally {
			em.close();
		}
	}

	public static Product findProduct(Long id) {
		EntityManager em = EM.create();

		try {
			String sql = "SELECT * FROM product WHERE id = " + id;
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<Product> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Product((Long) row[0], (String) row[1], (BigDecimal) row[2],
								(String) row[3], (Date) row[4], (Integer) row[5]));
			}

			return l.get(0);

		} finally {
			em.close();
		}
	}

	public static Customer findCustomer(Long id) {
		String sql = "SELECT * FROM customer WHERE id = " + id;
		return getAllCustomers(sql).get(0);
	}

	public static List<CustomerOrder> findCustomerOrders(Long id) {
		String sql = "SELECT * FROM customer_order WHERE customer_id = " + id;
		return getAllCustomerOrders(sql);
	}

	public static CustomerOrder findOrder(Long id) {
		String sql = "SELECT * FROM customer_order WHERE id = " + id;
		return getAllCustomerOrders(sql).get(0);
	}

	public static MsgFeedback findFeedback(Long id) {
		String sql = "SELECT msg_feedback.*, msg_subject.name AS sub_name "
						+ "FROM msg_feedback JOIN msg_subject "
						+ "ON msg_feedback.subject_id = msg_subject.id "
						+ "WHERE msg_feedback.id = " + id;
		return getAllFeedback(sql).get(0);
	}

	public static List<Promotion> findPromoCategories() {
		String sql = "SELECT * FROM promotion WHERE category_id > 0";
		return getAllPromotions(sql);
	}

	public static List<Promotion> findPromoProducts() {
		String sql = "SELECT * FROM promotion WHERE product_id > 0";
		return getAllPromotions(sql);
	}

	public static Promotion findSale() {
		String sql = "SELECT * FROM promotion WHERE sale = true Limit 1";
		return getAllPromotions(sql).get(0);
	}

	public static Map<String, Object> getOrderDetails(Long id) {
		// get order
		CustomerOrder order = findOrder(id);

		// get customer
		Customer customer = findCustomer(order.getCustomerId());

		// get all ordered products
		List<OrderedProduct> orderedProducts = findOrderedProducts(id);

		// get product details for ordered items
		List<Product> products = new ArrayList<>();

		for (OrderedProduct op : orderedProducts) {
			Product p = findProduct(op.getProductId());
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

	public static void saveFeedback(String... msg) {
		EntityManager em = null;

		try {
			em = EM.create();
			em.getTransaction().begin();

			String sql = "INSERT INTO msg_feedback (name, email, msg, subject_id) VALUES "
							+ "('" + msg[0] + "', '" + msg[1] + "', '" + msg[2] + "', "
							+ msg[3] + ")";

			em.createNativeQuery(sql).executeUpdate();
			em.getTransaction().commit();

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public static int saveOrder(ShoppingCart cart, String surcharge,
					String... order) {

		EntityManager em = null;

		try {
			em = EM.create();
			em.getTransaction().begin();

			Customer customer = addCustomer(em, order);
			CustomerOrder co = addOrder(em, customer, cart, surcharge);
			addOrderedItems(em, co, cart);

			em.getTransaction().commit();
			return co.getId().intValue();

		} catch (Exception e) {
			e.printStackTrace();
			return 0;

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	private static Customer addCustomer(EntityManager em, String... c) {
		String sql = "INSERT INTO customer (name, email, phone, address, "
						+ "city_region, cc_number) VALUES "
						+ "('" + c[0] + "', '" + c[1] + "', '" + c[2] + "', '" + c[3]
						+ "', '" + c[4] + "', '" + c[5] + "')";

		em.createNativeQuery(sql).executeUpdate();

		// return new id
		sql = "SELECT * FROM customer ORDER BY id DESC LIMIT 1";
		return getAllCustomers(sql).get(0);
	}

	private static CustomerOrder addOrder(EntityManager em, Customer customer,
					ShoppingCart cart, String surcharge) {

		// create confirmation number
		int cno = random.nextInt(999999999);

		BigDecimal amount = BigDecimal.valueOf(cart.getSubtotal()
						+ Double.parseDouble(surcharge));

		// set up customer order
		String sql = "INSERT INTO customer_order (amount, date_created, "
						+ "confirmation_number, customer_id) VALUES "
						+ "(" + amount.toString() + ", '" + sm.format(new Date()) + "', "
						+ cno + ", " + customer.getId() + ")";

		em.createNativeQuery(sql).executeUpdate();

		// return new id
		sql = "SELECT * FROM customer_order ORDER BY id DESC LIMIT 1";
		return getAllCustomerOrders(sql).get(0);
	}

	private static void addOrderedItems(EntityManager em, CustomerOrder order,
					ShoppingCart cart) {
		em.flush();

		Collection<ShoppingCartItem> items = cart.getItems();

		// iterate through shopping cart and create OrderedProducts
		for (ShoppingCartItem scItem : items) {
			// set up ordered product
			String sql = "INSERT INTO ordered_product (customer_order_id, product_id, "
							+ "quantity) VALUES (" + order.getId() + ", "
							+ scItem.getProduct().getId() + ", " + scItem.getQuantity() + ")";

			em.createNativeQuery(sql).executeUpdate();
		}
	}

	private static List<Customer> getAllCustomers(String sql) {
		EntityManager em = EM.create();

		try {
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<Customer> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Customer((Long) row[0], (String) row[1], (String) row[2],
								(String) row[3], (String) row[4], (String) row[5],
								(String) row[6]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	private static List<CustomerOrder> getAllCustomerOrders(String sql) {
		EntityManager em = EM.create();

		try {
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<CustomerOrder> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new CustomerOrder((Long) row[0], (BigDecimal) row[1], (Date) row[2],
								(Long) row[3], (Long) row[4]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	private static List<MsgFeedback> getAllFeedback(String sql) {
		EntityManager em = EM.create();

		try {
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<MsgFeedback> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new MsgFeedback((Long) row[0], (String) row[1], (String) row[2],
								(String) row[3], (Long) row[4], (String) row[5]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	private static List<Promotion> getAllPromotions(String sql) {
		EntityManager em = EM.create();

		try {
			List<Object> result = em.createNativeQuery(sql).getResultList();
			List<Promotion> l = new ArrayList<>();

			for (Object o : result) {
				Object[] row = (Object[]) o;
				l.add(new Promotion((Long) row[0], (String) row[1], (Long) row[2],
								(Boolean) row[3], (Long) row[4], (Long) row[5],
								(Long) row[6], (BigDecimal) row[7], (String) row[8]));
			}

			return l;

		} finally {
			em.close();
		}
	}

	private Facade() {
	}
}
