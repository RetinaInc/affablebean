package com.affablebean.entity1;

import java.math.BigDecimal;

/**
 *
 * @author osman
 */
public class Promotion {

	private final Long id;
	private final String name;
	private final Long discount;
	private final Boolean sale;
	private final Long categoryId;
	private final Long productId;
	private final Long qty;
	private final BigDecimal sold;
	private final String description;

	public Promotion(Long id, String name, Long discount, Boolean sale,
					Long categoryId, Long productId, Long qty, BigDecimal sold,
					String description) {

		this.id = id;
		this.name = name;
		this.discount = discount;
		this.sale = sale;
		this.categoryId = categoryId;
		this.productId = productId;
		this.qty = qty;
		this.sold = sold;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getDiscount() {
		return discount;
	}

	public Boolean isSale() {
		return sale;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public Long getProductId() {
		return productId;
	}

	public Long getQty() {
		return qty;
	}

	public BigDecimal getSold() {
		return sold;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "Promotion{" + "id=" + id + ", name=" + name + ", discount="
						+ discount + ", sale=" + sale + ", categoryId=" + categoryId
						+ ", productId=" + productId + ", qty=" + qty + ", sold=" + sold
						+ ", description=" + description + '}';
	}

}
