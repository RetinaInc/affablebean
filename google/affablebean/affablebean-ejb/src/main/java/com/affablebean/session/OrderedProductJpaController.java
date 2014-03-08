package com.affablebean.session;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.affablebean.entity.Product;
import com.affablebean.entity.CustomerOrder;
import com.affablebean.entity.OrderedProduct;
import com.affablebean.entity.OrderedProductPK;
import static com.affablebean.session.EntityMgr.EM;
import static com.affablebean.session.CustomerOrderJpaController.CUST_ORD_CTL;
import com.affablebean.session.exceptions.NonexistentEntityException;
import com.affablebean.session.exceptions.PreexistingEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author osman
 */
public enum OrderedProductJpaController implements Serializable {

	ORD_PROD_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(OrderedProduct orderedProduct) throws PreexistingEntityException, Exception {
		if (orderedProduct.getOrderedProductPK() == null) {
			orderedProduct.setOrderedProductPK(new OrderedProductPK());
		}
		orderedProduct.getOrderedProductPK().setProductId(orderedProduct.getProduct().getId());
		orderedProduct.getOrderedProductPK().setCustomerOrderId(orderedProduct.getCustomerOrder().getId());
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Product product = orderedProduct.getProduct();
			if (product != null) {
				product = em.getReference(product.getClass(), product.getId());
				orderedProduct.setProduct(product);
			}
			CustomerOrder customerOrder = orderedProduct.getCustomerOrder();
			if (customerOrder != null) {
				customerOrder = em.getReference(customerOrder.getClass(), customerOrder.getId());
				orderedProduct.setCustomerOrder(customerOrder);
			}
			em.persist(orderedProduct);
			if (product != null) {
				product.getOrderedProductCollection().add(orderedProduct);
				product = em.merge(product);
			}
			if (customerOrder != null) {
				customerOrder.getOrderedProductCollection().add(orderedProduct);
				customerOrder = em.merge(customerOrder);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			if (findOrderedProduct(orderedProduct.getOrderedProductPK()) != null) {
				throw new PreexistingEntityException("OrderedProduct " + orderedProduct + " already exists.", ex);
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(OrderedProduct orderedProduct) throws NonexistentEntityException, Exception {
		orderedProduct.getOrderedProductPK().setProductId(orderedProduct.getProduct().getId());
		orderedProduct.getOrderedProductPK().setCustomerOrderId(orderedProduct.getCustomerOrder().getId());
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			OrderedProduct persistentOrderedProduct = em.find(OrderedProduct.class, orderedProduct.getOrderedProductPK());
			Product productOld = persistentOrderedProduct.getProduct();
			Product productNew = orderedProduct.getProduct();
			CustomerOrder customerOrderOld = persistentOrderedProduct.getCustomerOrder();
			CustomerOrder customerOrderNew = orderedProduct.getCustomerOrder();
			if (productNew != null) {
				productNew = em.getReference(productNew.getClass(), productNew.getId());
				orderedProduct.setProduct(productNew);
			}
			if (customerOrderNew != null) {
				customerOrderNew = em.getReference(customerOrderNew.getClass(), customerOrderNew.getId());
				orderedProduct.setCustomerOrder(customerOrderNew);
			}
			orderedProduct = em.merge(orderedProduct);
			if (productOld != null && !productOld.equals(productNew)) {
				productOld.getOrderedProductCollection().remove(orderedProduct);
				productOld = em.merge(productOld);
			}
			if (productNew != null && !productNew.equals(productOld)) {
				productNew.getOrderedProductCollection().add(orderedProduct);
				productNew = em.merge(productNew);
			}
			if (customerOrderOld != null && !customerOrderOld.equals(customerOrderNew)) {
				customerOrderOld.getOrderedProductCollection().remove(orderedProduct);
				customerOrderOld = em.merge(customerOrderOld);
			}
			if (customerOrderNew != null && !customerOrderNew.equals(customerOrderOld)) {
				customerOrderNew.getOrderedProductCollection().add(orderedProduct);
				customerOrderNew = em.merge(customerOrderNew);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				OrderedProductPK id = orderedProduct.getOrderedProductPK();
				if (findOrderedProduct(id) == null) {
					throw new NonexistentEntityException("The orderedProduct with id " + id + " no longer exists.");
				}
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(OrderedProductPK id) throws NonexistentEntityException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			OrderedProduct orderedProduct;
			try {
				orderedProduct = em.getReference(OrderedProduct.class, id);
				orderedProduct.getOrderedProductPK();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The orderedProduct with id " + id + " no longer exists.", enfe);
			}
			Product product = orderedProduct.getProduct();
			if (product != null) {
				product.getOrderedProductCollection().remove(orderedProduct);
				product = em.merge(product);
			}
			CustomerOrder customerOrder = orderedProduct.getCustomerOrder();
			if (customerOrder != null) {
				customerOrder.getOrderedProductCollection().remove(orderedProduct);
				customerOrder = em.merge(customerOrder);
			}
			em.remove(orderedProduct);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<OrderedProduct> findOrderedProductEntities() {
		return findOrderedProductEntities(true, -1, -1);
	}

	public List<OrderedProduct> findOrderedProductEntities(int maxResults, int firstResult) {
		return findOrderedProductEntities(false, maxResults, firstResult);
	}

	private List<OrderedProduct> findOrderedProductEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(OrderedProduct.class));
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

	public OrderedProduct findOrderedProduct(OrderedProductPK id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(OrderedProduct.class, id);
		} finally {
			em.close();
		}
	}

	public int getOrderedProductCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<OrderedProduct> rt = cq.from(OrderedProduct.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<OrderedProduct> findOrderedProducts(Integer orderId) {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery(
							"SELECT o FROM OrderedProduct o WHERE o.customerOrder = :id");
			q.setParameter("id", CUST_ORD_CTL.findCustomerOrder(orderId));
			return q.getResultList();

		} finally {
			em.close();
		}
	}
}
