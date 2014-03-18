package com.affablebean.economy;

import com.affablebean.entity.Category;
import com.affablebean.entity.MsgSubject;
import com.affablebean.entity.Product;
import com.affablebean.entity.Promotion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * save on hosting costs
 *
 * @author osman
 */
public final class FakeDB {

	private static final List<Category> categories = new ArrayList<>();
	private static final List<Promotion> promos = new ArrayList<>();
	private static final List<Product> products = new ArrayList<>();
	private static final List<MsgSubject> subjects = new ArrayList<>();

	public static Category findCategory(int id) {
		return categories.get(id - 1);
	}

	public static List<Product> findAllCategoryProducts(int id) {
		List<Product> r = new ArrayList<>();

		switch (id) {
			case 1:
				addProducts(r, 8, 12);
				break;

			case 2:
				addProducts(r, 16, 20);
				break;

			case 3:
				addProducts(r, 0, 4);
				break;

			case 4:
				addProducts(r, 20, 24);
				break;

			case 5:
				addProducts(r, 12, 16);
				break;

			case 6:
				addProducts(r, 4, 8);
				break;

			default:
		}

		return r;
	}

	public static List<Category> findCategories() {
		return categories;
	}

	public static Product findProduct(int id) {
		return products.get(id - 1);
	}

	public static List<Promotion> findCatProms() {
		List<Promotion> r = new ArrayList<>();

		for (Promotion p : promos) {
			Integer c = p.getCategoryId();

			if (c != null) {
				r.add(p);
			}
		}

		return r;
	}

	public static List<Promotion> findProdProms() {
		List<Promotion> r = new ArrayList<>();

		for (Promotion p : promos) {
			Integer c = p.getProductId();

			if (c != null) {
				r.add(p);
			}
		}

		return r;
	}
	
	public static Promotion findSale() {
		for (Promotion p : promos) {
			Boolean isSale = p.getSale();

			if (isSale != null && isSale == Boolean.TRUE) {
				return p;
			}
		}
		return null;
	}

	public static List<MsgSubject> findSubjects() {
		return subjects;
	}

	private FakeDB() {
	}

	static {
		loadCategories();
		loadPromotions();
		loadProducts();
		loadSubjects();
	}

	private static void addProducts(List<Product> r, int first, int last) {
		for (int i = first; i < last; i++) {
			r.add(products.get(i));
		}
	}

	private static void loadCategories() {
		categories.add(new Category((short) 1, "bakery"));
		categories.add(new Category((short) 2, "cereals"));
		categories.add(new Category((short) 3, "dairy"));
		categories.add(new Category((short) 4, "drinks"));
		categories.add(new Category((short) 5, "fruitveg"));
		categories.add(new Category((short) 6, "meats"));
	}

	private static void loadPromotions() {
		Promotion p = new Promotion(1, "10% OFF SALE", 10);
		p.setDescription("10% OFF ALL ITEMS!! HURRY!! OFFER MUST END SOON!!");
		p.setSale(Boolean.TRUE);
		promos.add(p);

		p = new Promotion(2, "5% EXTRA OFF ALL DRINKS", 5);
		p.setDescription("5% EXTRA OFF ALL DRINKS");
		p.setCategoryId(4);
		promos.add(p);

		p = new Promotion(3, "20% OFF OATS", 20);
		p.setDescription("20% EXTRA OFF WHEN YOU BUY 2 OR MORE OAT PRODUCTS! (not implemented");
		p.setProductId(18);
		promos.add(p);

		p = new Promotion(4, "20% OFF OATS", 20);
		p.setDescription("20% EXTRA OFF WHEN YOU BUY 2 OR MORE OAT PRODUCTS! (not implemented");
		p.setProductId(19);
		promos.add(p);
	}

	private static void loadProducts() {
		products.add(new Product(1, "butter", new BigDecimal(1.09), "unsalted (250g)",
						categories.get(2)));
		products.add(new Product(2, "cheese", new BigDecimal(2.39), "mild cheddar (330g)",
						categories.get(2)));
		products.add(new Product(3, "free range eggs", new BigDecimal(1.76),
						"medium-sized (6 eggs)", categories.get(2)));
		products.add(new Product(4, "milk", new BigDecimal(1.70), "semi skimmed (1L)",
						categories.get(2)));

		products.add(new Product(5, "chicken leg", new BigDecimal(2.59), "free range (250g)",
						categories.get(5)));
		products.add(new Product(6, "organic meat patties", new BigDecimal(2.29),
						"rolled in fresh herbs 2 patties (250g)", categories.get(5)));
		products.add(new Product(7, "parma ham", new BigDecimal(3.49),
						"matured, organic (70g)", categories.get(5)));
		products.add(new Product(8, "sausages", new BigDecimal(3.55),
						"reduced fat, pork 3 sausages (350g)", categories.get(5)));

		products.add(new Product(9, "chocolate cookies", new BigDecimal(2.39),
						"contain peanuts (3 cookies)", categories.get(1)));
		products.add(new Product(10, "pumpkin seed bun", new BigDecimal(1.15),
						"4 buns", categories.get(1)));
		products.add(new Product(11, "sesame seed bagel", new BigDecimal(1.19),
						"4 bagels", categories.get(1)));
		products.add(new Product(12, "sunflower seed loaf", new BigDecimal(1.89),
						"600g", categories.get(1)));

		products.add(new Product(13, "broccoli", new BigDecimal(1.29),
						"500g", categories.get(5)));
		products.add(new Product(14, "corn on the cob", new BigDecimal(1.59),
						"2 pieces", categories.get(5)));
		products.add(new Product(15, "red currants", new BigDecimal(2.49),
						"150g", categories.get(5)));
		products.add(new Product(16, "seedless watermelon", new BigDecimal(1.49),
						"250g", categories.get(5)));

		products.add(new Product(17, "granola", new BigDecimal(3.99),
						"Apple & Cinnamon Granola (400g)", categories.get(2)));
		products.add(new Product(18, "jumbo oats", new BigDecimal(1.99),
						"Jumbo Oats (500g)", categories.get(2)));
		products.add(new Product(19, "porridge oats", new BigDecimal(2.75),
						"Organic Porridge Oats (1kg)", categories.get(2)));
		products.add(new Product(20, "rice flakes", new BigDecimal(2.99),
						"Organic Rice Flakes (500g)", categories.get(2)));

		products.add(new Product(21, "green tea", new BigDecimal(1.99),
						"Organic Green Tea (15 bags)", categories.get(4)));
		products.add(new Product(22, "herbal tea", new BigDecimal(2.50),
						"Herbal Tea (20 bags)", categories.get(4)));
		products.add(new Product(23, "organic coffee", new BigDecimal(4.75),
						"Organic Fairtrade Italian Roast Ground Coffee (227g)", categories.get(4)));
		products.add(new Product(24, "wholebean coffee", new BigDecimal(10.75),
						"Organic Fairtrade Wholebean Coffee (500g)", categories.get(4)));
	}

	private static void loadSubjects() {
		subjects.add(new MsgSubject(1, "Brands or product"));
		subjects.add(new MsgSubject(2, "Investor relations"));
		subjects.add(new MsgSubject(3, "Media enquiry"));
		subjects.add(new MsgSubject(4, "Other"));
		subjects.add(new MsgSubject(5, "Sustainability"));
		subjects.add(new MsgSubject(6, "The Company"));
		subjects.add(new MsgSubject(7, "Website feedback"));
	}
}
