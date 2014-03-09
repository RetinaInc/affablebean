package com.affablebean.session;

import java.io.Serializable;

import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.affablebean.entity.Customer;
import com.affablebean.entity.CustomerOrder;

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
public enum CustomerJpaController implements Serializable {

	CUST_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(Customer customer) {
		if (customer.getCustomerOrderCollection() == null) {
			customer.setCustomerOrderCollection(new ArrayList<CustomerOrder>());
		}
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Collection<CustomerOrder> attachedCustomerOrderCollection = new ArrayList<CustomerOrder>();
			for (CustomerOrder customerOrderCollectionCustomerOrderToAttach : customer.getCustomerOrderCollection()) {
				customerOrderCollectionCustomerOrderToAttach = em.getReference(customerOrderCollectionCustomerOrderToAttach.getClass(), customerOrderCollectionCustomerOrderToAttach.getId());
				attachedCustomerOrderCollection.add(customerOrderCollectionCustomerOrderToAttach);
			}
			customer.setCustomerOrderCollection(attachedCustomerOrderCollection);
			em.persist(customer);
			for (CustomerOrder customerOrderCollectionCustomerOrder : customer.getCustomerOrderCollection()) {
				Customer oldCustomerIdOfCustomerOrderCollectionCustomerOrder = customerOrderCollectionCustomerOrder.getCustomerId();
				customerOrderCollectionCustomerOrder.setCustomerId(customer);
				customerOrderCollectionCustomerOrder = em.merge(customerOrderCollectionCustomerOrder);
				if (oldCustomerIdOfCustomerOrderCollectionCustomerOrder != null) {
					oldCustomerIdOfCustomerOrderCollectionCustomerOrder.getCustomerOrderCollection().remove(customerOrderCollectionCustomerOrder);
					oldCustomerIdOfCustomerOrderCollectionCustomerOrder = em.merge(oldCustomerIdOfCustomerOrderCollectionCustomerOrder);
				}
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(Customer customer) throws IllegalOrphanException, NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Customer persistentCustomer = em.find(Customer.class, customer.getId());
			Collection<CustomerOrder> customerOrderCollectionOld = persistentCustomer.getCustomerOrderCollection();
			Collection<CustomerOrder> customerOrderCollectionNew = customer.getCustomerOrderCollection();
			List<String> illegalOrphanMessages = null;
			for (CustomerOrder customerOrderCollectionOldCustomerOrder : customerOrderCollectionOld) {
				if (!customerOrderCollectionNew.contains(customerOrderCollectionOldCustomerOrder)) {
					if (illegalOrphanMessages == null) {
						illegalOrphanMessages = new ArrayList<String>();
					}
					illegalOrphanMessages.add("You must retain CustomerOrder " + customerOrderCollectionOldCustomerOrder + " since its customerId field is not nullable.");
				}
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			Collection<CustomerOrder> attachedCustomerOrderCollectionNew = new ArrayList<CustomerOrder>();
			for (CustomerOrder customerOrderCollectionNewCustomerOrderToAttach : customerOrderCollectionNew) {
				customerOrderCollectionNewCustomerOrderToAttach = em.getReference(customerOrderCollectionNewCustomerOrderToAttach.getClass(), customerOrderCollectionNewCustomerOrderToAttach.getId());
				attachedCustomerOrderCollectionNew.add(customerOrderCollectionNewCustomerOrderToAttach);
			}
			customerOrderCollectionNew = attachedCustomerOrderCollectionNew;
			customer.setCustomerOrderCollection(customerOrderCollectionNew);
			customer = em.merge(customer);
			for (CustomerOrder customerOrderCollectionNewCustomerOrder : customerOrderCollectionNew) {
				if (!customerOrderCollectionOld.contains(customerOrderCollectionNewCustomerOrder)) {
					Customer oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder = customerOrderCollectionNewCustomerOrder.getCustomerId();
					customerOrderCollectionNewCustomerOrder.setCustomerId(customer);
					customerOrderCollectionNewCustomerOrder = em.merge(customerOrderCollectionNewCustomerOrder);
					if (oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder != null && !oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder.equals(customer)) {
						oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder.getCustomerOrderCollection().remove(customerOrderCollectionNewCustomerOrder);
						oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder = em.merge(oldCustomerIdOfCustomerOrderCollectionNewCustomerOrder);
					}
				}
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Integer id = customer.getId();
				if (findCustomer(id) == null) {
					throw new NonexistentEntityException("The customer with id " + id + " no longer exists.");
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
			Customer customer;
			try {
				customer = em.getReference(Customer.class, id);
				customer.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The customer with id " + id + " no longer exists.", enfe);
			}
			List<String> illegalOrphanMessages = null;
			Collection<CustomerOrder> customerOrderCollectionOrphanCheck = customer.getCustomerOrderCollection();
			for (CustomerOrder customerOrderCollectionOrphanCheckCustomerOrder : customerOrderCollectionOrphanCheck) {
				if (illegalOrphanMessages == null) {
					illegalOrphanMessages = new ArrayList<String>();
				}
				illegalOrphanMessages.add("This Customer (" + customer + ") cannot be destroyed since the CustomerOrder " + customerOrderCollectionOrphanCheckCustomerOrder + " in its customerOrderCollection field has a non-nullable customerId field.");
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			em.remove(customer);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<Customer> findCustomerEntities() {
		return findCustomerEntities(true, -1, -1);
	}

	public List<Customer> findCustomerEntities(int maxResults, int firstResult) {
		return findCustomerEntities(false, maxResults, firstResult);
	}

	private List<Customer> findCustomerEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(Customer.class));
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

	public Customer findCustomer(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Customer.class, id);
		} finally {
			em.close();
		}
	}

	public int getCustomerCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<Customer> rt = cq.from(Customer.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

}
