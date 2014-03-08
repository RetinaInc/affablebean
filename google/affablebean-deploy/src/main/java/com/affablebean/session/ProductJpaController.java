package com.affablebean.session;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.affablebean.entity.Category;
import com.affablebean.entity.OrderedProduct;
import com.affablebean.entity.Product;
import static com.affablebean.session.CategoryJpaController.CAT_CTL;
import static com.affablebean.session.EntityMgr.EM;
import com.affablebean.session.exceptions.IllegalOrphanException;
import com.affablebean.session.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author osman
 */
public enum ProductJpaController implements Serializable {

	PROD_CTL;
	private final EntityManagerFactory emf = EM.emf;

	public void create(Product product) {
		if (product.getOrderedProductCollection() == null) {
			product.setOrderedProductCollection(new ArrayList<OrderedProduct>());
		}

		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Category categoryId = product.getCategoryId();

			if (categoryId != null) {
				categoryId = em.getReference(categoryId.getClass(), categoryId.getId());
				product.setCategoryId(categoryId);
			}

			Collection<OrderedProduct> OrderedProductCollection = new ArrayList<>();

			for (OrderedProduct op : product.getOrderedProductCollection()) {
				op = em.getReference(op.getClass(), op.getOrderedProductPK());
				OrderedProductCollection.add(op);
			}

			product.setOrderedProductCollection(OrderedProductCollection);
			em.persist(product);

			if (categoryId != null) {
				categoryId.getProductCollection().add(product);
				categoryId = em.merge(categoryId);
			}

			for (OrderedProduct op2 : product.getOrderedProductCollection()) {
				Product oldProduct = op2.getProduct();
				op2.setProduct(product);
				op2 = em.merge(op2);

				if (oldProduct != null) {
					oldProduct.getOrderedProductCollection().remove(op2);
					oldProduct = em.merge(oldProduct);
				}
			}

			em.getTransaction().commit();

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(Product product)
					throws IllegalOrphanException, NonexistentEntityException, Exception {

		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();

			Product persistentProduct = em.find(Product.class, product.getId());
			Category categoryIdOld = persistentProduct.getCategoryId();
			Category categoryIdNew = product.getCategoryId();

			Collection<OrderedProduct> orderedProductCollectionOld
							= persistentProduct.getOrderedProductCollection();
			Collection<OrderedProduct> orderedProductCollectionNew
							= product.getOrderedProductCollection();
			List<String> illegalOrphanMessages = null;

			for (OrderedProduct op : orderedProductCollectionOld) {
				if (!orderedProductCollectionNew.contains(op)) {

					if (illegalOrphanMessages == null) {
						illegalOrphanMessages = new ArrayList<>();
					}

					illegalOrphanMessages.add("You must retain OrderedProduct " + op
									+ " since its product field is not nullable.");
				}
			}

			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}

			if (categoryIdNew != null) {
				categoryIdNew = em.getReference(categoryIdNew.getClass(),
								categoryIdNew.getId());
				product.setCategoryId(categoryIdNew);
			}

			Collection<OrderedProduct> attachedOrderedProductCollectionNew = new ArrayList<>();

			for (OrderedProduct op2 : orderedProductCollectionNew) {
				op2 = em.getReference(op2.getClass(), op2.getOrderedProductPK());
				attachedOrderedProductCollectionNew.add(op2);
			}

			orderedProductCollectionNew = attachedOrderedProductCollectionNew;
			product.setOrderedProductCollection(orderedProductCollectionNew);
			product = em.merge(product);

			if (categoryIdOld != null && !categoryIdOld.equals(categoryIdNew)) {
				categoryIdOld.getProductCollection().remove(product);
				categoryIdOld = em.merge(categoryIdOld);
			}

			if (categoryIdNew != null && !categoryIdNew.equals(categoryIdOld)) {
				categoryIdNew.getProductCollection().add(product);
				categoryIdNew = em.merge(categoryIdNew);
			}

			for (OrderedProduct op3 : orderedProductCollectionNew) {
				if (!orderedProductCollectionOld.contains(op3)) {
					Product oldProductOfOrderedProductCollectionNewOrderedProduct = op3.getProduct();
					op3.setProduct(product);
					op3 = em.merge(op3);

					if (oldProductOfOrderedProductCollectionNewOrderedProduct != null && !oldProductOfOrderedProductCollectionNewOrderedProduct.equals(product)) {
						oldProductOfOrderedProductCollectionNewOrderedProduct.getOrderedProductCollection().remove(op3);
						oldProductOfOrderedProductCollectionNewOrderedProduct = em.merge(oldProductOfOrderedProductCollectionNewOrderedProduct);
					}
				}
			}

			em.getTransaction().commit();

		} catch (IllegalOrphanException ex) {
			String msg = ex.getLocalizedMessage();

			if (msg == null || msg.length() == 0) {
				Integer id = product.getId();

				if (findProduct(id) == null) {
					throw new NonexistentEntityException(
									"The product with id " + id + " no longer exists.");
				}
			}

			throw ex;

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Integer id)
					throws IllegalOrphanException, NonexistentEntityException {

		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Product product;

			try {
				product = em.getReference(Product.class, id);
				product.getId();

			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException(
								"The product with id " + id + " no longer exists.", enfe);
			}

			List<String> illegalOrphanMessages = null;
			Collection<OrderedProduct> orderedProductCollectionOrphanCheck
							= product.getOrderedProductCollection();

			for (OrderedProduct op : orderedProductCollectionOrphanCheck) {

				if (illegalOrphanMessages == null) {
					illegalOrphanMessages = new ArrayList<String>();
				}

				illegalOrphanMessages.add("This Product (" + product
								+ ") cannot be destroyed since the OrderedProduct " + op
								+ " in its orderedProductCollection field has a non-nullable product field.");
			}

			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}

			Category categoryId = product.getCategoryId();

			if (categoryId != null) {
				categoryId.getProductCollection().remove(product);
				categoryId = em.merge(categoryId);
			}

			em.remove(product);
			em.getTransaction().commit();

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<Product> findProductEntities() {
		return findProductEntities(true, -1, -1);
	}

	public List<Product> findProductEntities(int maxResults, int firstResult) {
		return findProductEntities(false, maxResults, firstResult);
	}

	public Product findProduct(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Product.class, id);
		} finally {
			em.close();
		}
	}

	public int getProductCount() {
		EntityManager em = getEntityManager();

		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<Product> rt = cq.from(Product.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);

			return ((Long) q.getSingleResult()).intValue();

		} finally {
			em.close();
		}
	}

	public List<Product> findAllCategoryProducts(int id) {
		EntityManager em = getEntityManager();

		try {
			String sql = "SELECT p FROM Product p JOIN p.categoryId c "
							+ "WHERE p.categoryId = :cid ORDER BY p.name";
			Query q = em.createQuery(sql);
			q.setParameter("cid", CAT_CTL.findCategory((short) id));
			return q.getResultList();

		} finally {
			em.close();
		}
	}

	private List<Product> findProductEntities(boolean all, int maxResults,
					int firstResult) {
		EntityManager em = getEntityManager();

		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(Product.class));
			Query q = em.createQuery(cq);

			if (!all) {
				q.setMaxResults(maxResults);
				q.setFirstResult(firstResult);
			}

			return q.getResultList();

		} finally {
			em.close();
		}
	}

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

}
