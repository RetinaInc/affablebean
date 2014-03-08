package com.affablebean.json;

import com.affablebean.cart.ShoppingCart;
import com.affablebean.cart.ShoppingCartItem;
import com.affablebean.entity.Category;
import com.affablebean.entity.Customer;
import com.affablebean.entity.CustomerOrder;
import com.affablebean.entity.MsgSubject;
import com.affablebean.entity.OrderedProduct;
import com.affablebean.entity.Product;
import com.affablebean.entity.Promotion;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import static com.affablebean.session.ProductJpaController.PROD_CTL;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author osman
 */
public final class ABJson {

	private static final JsonFactory jfactory = new JsonFactory();

	public static void cartResponse(Writer response, HttpSession session,
					ServletContext ctx) {

		ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

		if (cart == null) {
			return;
		}

		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartObject();
			writeCart(response, gen, cart);
			gen.writeEndObject();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void categoryList(Writer response, ServletContext ctx) {
		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartArray();

			for (Category category : (List<Category>) ctx.getAttribute("categories")) {
				gen.writeStartObject();
				gen.writeNumberField("id", category.getId());
				gen.writeStringField("name", category.getName());
				gen.writeEndObject();
			}

			gen.writeEndArray();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void categoryResponse(Writer response, HttpSession session,
					ServletContext ctx) {

		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			Category cat = (Category) session.getAttribute("selectedCategory");
			gen.writeStartObject(); // {

			writeCategoryProducts(response, gen,
							(Collection<Product>) session.getAttribute("categoryProducts"));
			writeCatPromotions(response, gen,
							(List<Promotion>) ctx.getAttribute("catProms"), (short) cat.getId());
			writeProductPromotions(response, gen,
							(List<Promotion>) ctx.getAttribute("prodProms"),
							(Collection<Product>) session.getAttribute("categoryProducts"));

			gen.writeEndObject();
		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void confResponse(Writer response, HttpServletRequest request,
					ServletContext ctx) {

		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartObject();

			writeCustomer(response, gen, (Customer) request.getAttribute("customer"));
			writeCustomerOrder(response, gen,
							(CustomerOrder) request.getAttribute("orderRecord"));
			writeCOProducts(response, gen,
							(List<OrderedProduct>) request.getAttribute("orderedProducts"));

			gen.writeEndObject();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void feedbackResponse(Writer response,
					HttpServletRequest request) {

		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartObject();

			if (request.getAttribute("nameError") != null) {
				gen.writeStringField("name", "name: null,empty,> 45 chars");
			}

			if (request.getAttribute("emailError") != null) {
				gen.writeStringField("email", "email: null,empty,missing '@'");
			}

			if (request.getAttribute("msgError") != null) {
				gen.writeStringField("msg", "msg: null,empty,< 10 chars");
			}

			gen.writeEndObject();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void indexResponse(Writer response, ServletContext ctx) {
		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartObject();

			writeCategories(response, gen,
							(List<Category>) ctx.getAttribute("categories"));
			writeSalePromotion(response, gen, (Promotion) ctx.getAttribute("sale"));

			gen.writeObjectFieldStart("properties");
			gen.writeStringField("surcharge", ctx.getInitParameter("deliverySurcharge"));
			gen.writeEndObject();

			gen.writeEndObject();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static Map<String, String> getData(InputStream is) {
		Map<String, String> data = new HashMap<>();

		try (JsonParser parser = jfactory.createParser(is)) {
			parser.nextToken();// JsonToken.START_OBJECT

			while (parser.nextToken() == JsonToken.FIELD_NAME) {
				String field = parser.getText();
				parser.nextToken();
				String value = parser.getValueAsString();
				data.put(field, value);
			}

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}

		return data;
	}

	public static void subjectList(Writer response, ServletContext ctx) {
		try (JsonGenerator gen = jfactory.createGenerator(response)) {
			gen.writeStartArray();

			for (MsgSubject subject : (List<MsgSubject>) ctx.getAttribute("subjects")) {
				gen.writeStartObject();
				gen.writeNumberField("id", subject.getId());
				gen.writeStringField("name", subject.getName());
				gen.writeEndObject();
			}

			gen.writeEndArray();

		} catch (IOException ex) {
			Logger.getLogger(ABJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void writeCart(Writer response, JsonGenerator gen,
					ShoppingCart shoppingCart) throws IOException {

		gen.writeArrayFieldStart("cartItems");

		for (ShoppingCartItem item : shoppingCart.getItems()) {
			gen.writeStartObject();
			gen.writeNumberField("product", item.getProduct().getId());
			gen.writeStringField("name", item.getProduct().getName());
			gen.writeNumberField("price", item.getProduct().getPrice());
			gen.writeNumberField("qty", item.getQuantity());
			gen.writeNumberField("total", item.getTotal());
			gen.writeEndObject();
		}

		gen.writeEndArray();

		gen.writeObjectFieldStart("properties");
		gen.writeNumberField("items", shoppingCart.getNumberOfItems());
		gen.writeNumberField("subtotal", shoppingCart.getSubtotal());
		gen.writeEndObject();

	}

	private static void writeCategories(Writer response, JsonGenerator gen,
					List<Category> data) throws IOException {

		gen.writeArrayFieldStart("categories");

		for (Category category : data) {
			gen.writeStartObject();
			gen.writeNumberField("id", category.getId());
			gen.writeStringField("name", category.getName());
			gen.writeEndObject();
		}

		gen.writeEndArray();
	}

	private static void writeCategoryProducts(Writer response, JsonGenerator gen,
					Collection<Product> data) throws IOException {

		gen.writeArrayFieldStart("products");

		for (Product product : data) {
			gen.writeStartObject();

			gen.writeNumberField("id", product.getId());
			gen.writeStringField("name", product.getName());
			gen.writeNumberField("price", product.getPrice());
			gen.writeStringField("description", product.getDescription());
			gen.writeNumberField("categoryId", product.getCategoryId().getId());

			gen.writeEndObject();
		}

		gen.writeEndArray();
	}

	private static void writeCatPromotions(Writer response, JsonGenerator gen,
					List<Promotion> data, short selectedCategory) throws IOException {

		gen.writeArrayFieldStart("categoryPromotions");

		for (Promotion promo : data) {
			if (promo.getCategoryId() == selectedCategory) {
				writePromotion(gen, promo);
			}
		}

		gen.writeEndArray();
	}

	private static void writeCustomer(Writer response, JsonGenerator gen,
					Customer customer) throws IOException {

		gen.writeObjectFieldStart("customer");

		gen.writeNumberField("id", customer.getId());
		gen.writeStringField("name", customer.getName());
		gen.writeStringField("address", customer.getAddress());
		gen.writeStringField("region", customer.getCityRegion());
		gen.writeStringField("phone", customer.getPhone());
		gen.writeStringField("email", customer.getEmail());
		gen.writeStringField("ccNumber", customer.getCcNumber());

		gen.writeEndObject();
	}

	private static void writeCustomerOrder(Writer response, JsonGenerator gen,
					CustomerOrder co) throws IOException {

		gen.writeObjectFieldStart("customerOrder");
		gen.writeNumberField("id", co.getId());
		gen.writeNumberField("amount", co.getAmount());
		gen.writeNumberField("confNumber", co.getConfirmationNumber());
		gen.writeStringField("dateCreated", new Date().toString());
		gen.writeEndObject();
	}

	private static void writeCOProducts(Writer response, JsonGenerator gen,
					List<OrderedProduct> orderedProducts) throws IOException {

		gen.writeArrayFieldStart("orderedProducts");

		for (OrderedProduct op : orderedProducts) {
			gen.writeStartObject();

			Product product = PROD_CTL.findProduct(op.getProduct().getId());

			int coId = -1;

			if (op.getOrderedProductPK() != null) {
				coId = op.getOrderedProductPK().getCustomerOrderId();
			}

			gen.writeNumberField("coId", coId);
			gen.writeNumberField("prodId", product.getId());
			gen.writeStringField("name", product.getName());
			gen.writeNumberField("price", product.getPrice());
			gen.writeNumberField("qty", op.getQuantity());
			gen.writeEndObject();
		}

		gen.writeEndArray();
	}

	private static void writePromotions(Writer response, JsonGenerator gen,
					List<Promotion> data, String title) throws IOException {

		gen.writeArrayFieldStart(title);

		for (Promotion promo : data) {
			writePromotion(gen, promo);
		}

		gen.writeEndArray();
	}

	private static void writeProductPromotions(Writer response, JsonGenerator gen,
					List<Promotion> data, Collection<Product> catProds) throws IOException {

		gen.writeArrayFieldStart("productPromotions");

		for (Promotion promo : data) {
			int prodId = promo.getProductId();

			for (Product product : catProds) {
				if (product.getId() == prodId) {
					writePromotion(gen, promo);
					break;
				}
			}
		}

		gen.writeEndArray();
	}

	private static void writeSalePromotion(Writer response, JsonGenerator gen,
					Promotion data) throws IOException {

		gen.writeArrayFieldStart("salePromotion");
		writePromotion(gen, data);
		gen.writeEndArray();
	}

	private static void writePromotion(JsonGenerator gen, Promotion promo)
					throws IOException {

		gen.writeStartObject();

		gen.writeNumberField("id", promo.getId());
		gen.writeStringField("name", promo.getName());
		gen.writeNumberField("discount", promo.getDiscount());
		gen.writeStringField("isSale", promo.getSale() != null
						? promo.getSale().toString() : "(null)");
		gen.writeStringField("categoryId", promo.getCategoryId() != null
						? promo.getCategoryId().toString() : "(null)");
		gen.writeStringField("productId", promo.getProductId() != null
						? promo.getProductId().toString() : "(null)");
		gen.writeStringField("qty", promo.getQty() != null
						? promo.getQty().toString() : "(null)");
		gen.writeStringField("sold", promo.getSold() != null
						? promo.getSold().toString() : "(null)");
		gen.writeStringField("description", promo.getDescription() != null
						? promo.getDescription() : "(null)");

		gen.writeEndObject();
	}

	private ABJson() {
	}
}
