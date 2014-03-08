package com.affablebean.session;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.affablebean.entity.Customer;
import com.affablebean.entity.CustomerOrder;
import com.affablebean.entity.OrderedProduct;
import static com.affablebean.session.EntityMgr.EM;
import static com.affablebean.session.CustomerJpaController.CUST_CTL;
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
public enum CustomerOrderJpaController implements Serializable {

	CUST_ORD_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(CustomerOrder customerOrder) {
		if (customerOrder.getOrderedProductCollection() == null) {
			customerOrder.setOrderedProductCollection(new ArrayList<OrderedProduct>());
		}
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Customer customerId = customerOrder.getCustomerId();
			if (customerId != null) {
				customerId = em.getReference(customerId.getClass(), customerId.getId());
				customerOrder.setCustomerId(customerId);
			}
			Collection<OrderedProduct> attachedOrderedProductCollection = new ArrayList<OrderedProduct>();
			for (OrderedProduct orderedProductCollectionOrderedProductToAttach : customerOrder.getOrderedProductCollection()) {
				orderedProductCollectionOrderedProductToAttach = em.getReference(orderedProductCollectionOrderedProductToAttach.getClass(), orderedProductCollectionOrderedProductToAttach.getOrderedProductPK());
				attachedOrderedProductCollection.add(orderedProductCollectionOrderedProductToAttach);
			}
			customerOrder.setOrderedProductCollection(attachedOrderedProductCollection);
			em.persist(customerOrder);
			if (customerId != null) {
				customerId.getCustomerOrderCollection().add(customerOrder);
				customerId = em.merge(customerId);
			}
			for (OrderedProduct orderedProductCollectionOrderedProduct : customerOrder.getOrderedProductCollection()) {
				CustomerOrder oldCustomerOrderOfOrderedProductCollectionOrderedProduct = orderedProductCollectionOrderedProduct.getCustomerOrder();
				orderedProductCollectionOrderedProduct.setCustomerOrder(customerOrder);
				orderedProductCollectionOrderedProduct = em.merge(orderedProductCollectionOrderedProduct);
				if (oldCustomerOrderOfOrderedProductCollectionOrderedProduct != null) {
					oldCustomerOrderOfOrderedProductCollectionOrderedProduct.getOrderedProductCollection().remove(orderedProductCollectionOrderedProduct);
					oldCustomerOrderOfOrderedProductCollectionOrderedProduct = em.merge(oldCustomerOrderOfOrderedProductCollectionOrderedProduct);
				}
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(CustomerOrder customerOrder) throws IllegalOrphanException, NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			CustomerOrder persistentCustomerOrder = em.find(CustomerOrder.class, customerOrder.getId());
			Customer customerIdOld = persistentCustomerOrder.getCustomerId();
			Customer customerIdNew = customerOrder.getCustomerId();
			Collection<OrderedProduct> orderedProductCollectionOld = persistentCustomerOrder.getOrderedProductCollection();
			Collection<OrderedProduct> orderedProductCollectionNew = customerOrder.getOrderedProductCollection();
			List<String> illegalOrphanMessages = null;
			for (OrderedProduct orderedProductCollectionOldOrderedProduct : orderedProductCollectionOld) {
				if (!orderedProductCollectionNew.contains(orderedProductCollectionOldOrderedProduct)) {
					if (illegalOrphanMessages == null) {
						illegalOrphanMessages = new ArrayList<String>();
					}
					illegalOrphanMessages.add("You must retain OrderedProduct " + orderedProductCollectionOldOrderedProduct + " since its customerOrder field is not nullable.");
				}
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			if (customerIdNew != null) {
				customerIdNew = em.getReference(customerIdNew.getClass(), customerIdNew.getId());
				customerOrder.setCustomerId(customerIdNew);
			}
			Collection<OrderedProduct> attachedOrderedProductCollectionNew = new ArrayList<OrderedProduct>();
			for (OrderedProduct orderedProductCollectionNewOrderedProductToAttach : orderedProductCollectionNew) {
				orderedProductCollectionNewOrderedProductToAttach = em.getReference(orderedProductCollectionNewOrderedProductToAttach.getClass(), orderedProductCollectionNewOrderedProductToAttach.getOrderedProductPK());
				attachedOrderedProductCollectionNew.add(orderedProductCollectionNewOrderedProductToAttach);
			}
			orderedProductCollectionNew = attachedOrderedProductCollectionNew;
			customerOrder.setOrderedProductCollection(orderedProductCollectionNew);
			customerOrder = em.merge(customerOrder);
			if (customerIdOld != null && !customerIdOld.equals(customerIdNew)) {
				customerIdOld.getCustomerOrderCollection().remove(customerOrder);
				customerIdOld = em.merge(customerIdOld);
			}
			if (customerIdNew != null && !customerIdNew.equals(customerIdOld)) {
				customerIdNew.getCustomerOrderCollection().add(customerOrder);
				customerIdNew = em.merge(customerIdNew);
			}
			for (OrderedProduct orderedProductCollectionNewOrderedProduct : orderedProductCollectionNew) {
				if (!orderedProductCollectionOld.contains(orderedProductCollectionNewOrderedProduct)) {
					CustomerOrder oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct = orderedProductCollectionNewOrderedProduct.getCustomerOrder();
					orderedProductCollectionNewOrderedProduct.setCustomerOrder(customerOrder);
					orderedProductCollectionNewOrderedProduct = em.merge(orderedProductCollectionNewOrderedProduct);
					if (oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct != null && !oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct.equals(customerOrder)) {
						oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct.getOrderedProductCollection().remove(orderedProductCollectionNewOrderedProduct);
						oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct = em.merge(oldCustomerOrderOfOrderedProductCollectionNewOrderedProduct);
					}
				}
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Integer id = customerOrder.getId();
				if (findCustomerOrder(id) == null) {
					throw new NonexistentEntityException("The customerOrder with id " + id + " no longer exists.");
				}
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			CustomerOrder customerOrder;
			try {
				customerOrder = em.getReference(CustomerOrder.class, id);
				customerOrder.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The customerOrder with id " + id + " no longer exists.", enfe);
			}
			List<String> illegalOrphanMessages = null;
			Collection<OrderedProduct> orderedProductCollectionOrphanCheck = customerOrder.getOrderedProductCollection();
			for (OrderedProduct orderedProductCollectionOrphanCheckOrderedProduct : orderedProductCollectionOrphanCheck) {
				if (illegalOrphanMessages == null) {
					illegalOrphanMessages = new ArrayList<String>();
				}
				illegalOrphanMessages.add("This CustomerOrder (" + customerOrder + ") cannot be destroyed since the OrderedProduct " + orderedProductCollectionOrphanCheckOrderedProduct + " in its orderedProductCollection field has a non-nullable customerOrder field.");
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			Customer customerId = customerOrder.getCustomerId();
			if (customerId != null) {
				customerId.getCustomerOrderCollection().remove(customerOrder);
				customerId = em.merge(customerId);
			}
			em.remove(customerOrder);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<CustomerOrder> findCustomerOrderEntities() {
		return findCustomerOrderEntities(true, -1, -1);
	}

	public List<CustomerOrder> findCustomerOrderEntities(int maxResults, int firstResult) {
		return findCustomerOrderEntities(false, maxResults, firstResult);
	}

	private List<CustomerOrder> findCustomerOrderEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(CustomerOrder.class));
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

	// date field not returned
	public CustomerOrder findCustomerOrder(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(CustomerOrder.class, id);
		} finally {
			em.close();
		}
	}

	public int getCustomerOrderCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<CustomerOrder> rt = cq.from(CustomerOrder.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<CustomerOrder> findCustomerOrders(Integer custId) {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery(
							"SELECT c FROM CustomerOrder c WHERE c.customerId = :cid");
			q.setParameter("cid", CUST_CTL.findCustomer(custId));
			return q.getResultList();

		} finally {
			em.close();
		}
	}

	public CustomerOrder findCustomerOrder2(Integer id) {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery("SELECT c FROM CustomerOrder c WHERE c.id = :id");
			q.setParameter("id", id);
			List<CustomerOrder> r = q.getResultList();
			return r.get(0);

		} finally {
			em.close();
		}
	}

}
