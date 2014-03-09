package com.affablebean.session;

import com.affablebean.entity.Category;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.affablebean.entity.Product;
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
public enum CategoryJpaController implements Serializable {

	CAT_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(Category category) {
		if (category.getProductCollection() == null) {
			category.setProductCollection(new ArrayList<Product>());
		}
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Collection<Product> attachedProductCollection = new ArrayList<>();
			for (Product productCollectionProductToAttach : category.getProductCollection()) {
				productCollectionProductToAttach = em.getReference(productCollectionProductToAttach.getClass(), productCollectionProductToAttach.getId());
				attachedProductCollection.add(productCollectionProductToAttach);
			}
			category.setProductCollection(attachedProductCollection);
			em.persist(category);
			for (Product productCollectionProduct : category.getProductCollection()) {
				Category oldCategoryIdOfProductCollectionProduct = productCollectionProduct.getCategoryId();
				productCollectionProduct.setCategoryId(category);
				productCollectionProduct = em.merge(productCollectionProduct);
				if (oldCategoryIdOfProductCollectionProduct != null) {
					oldCategoryIdOfProductCollectionProduct.getProductCollection().remove(productCollectionProduct);
					oldCategoryIdOfProductCollectionProduct = em.merge(oldCategoryIdOfProductCollectionProduct);
				}
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(Category category) throws IllegalOrphanException, NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Category persistentCategory = em.find(Category.class, category.getId());
			Collection<Product> productCollectionOld = persistentCategory.getProductCollection();
			Collection<Product> productCollectionNew = category.getProductCollection();
			List<String> illegalOrphanMessages = null;
			for (Product productCollectionOldProduct : productCollectionOld) {
				if (!productCollectionNew.contains(productCollectionOldProduct)) {
					if (illegalOrphanMessages == null) {
						illegalOrphanMessages = new ArrayList<>();
					}
					illegalOrphanMessages.add("You must retain Product " + productCollectionOldProduct + " since its categoryId field is not nullable.");
				}
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			Collection<Product> attachedProductCollectionNew = new ArrayList<>();
			for (Product productCollectionNewProductToAttach : productCollectionNew) {
				productCollectionNewProductToAttach = em.getReference(productCollectionNewProductToAttach.getClass(), productCollectionNewProductToAttach.getId());
				attachedProductCollectionNew.add(productCollectionNewProductToAttach);
			}
			productCollectionNew = attachedProductCollectionNew;
			category.setProductCollection(productCollectionNew);
			category = em.merge(category);
			for (Product productCollectionNewProduct : productCollectionNew) {
				if (!productCollectionOld.contains(productCollectionNewProduct)) {
					Category oldCategoryIdOfProductCollectionNewProduct = productCollectionNewProduct.getCategoryId();
					productCollectionNewProduct.setCategoryId(category);
					productCollectionNewProduct = em.merge(productCollectionNewProduct);
					if (oldCategoryIdOfProductCollectionNewProduct != null && !oldCategoryIdOfProductCollectionNewProduct.equals(category)) {
						oldCategoryIdOfProductCollectionNewProduct.getProductCollection().remove(productCollectionNewProduct);
						oldCategoryIdOfProductCollectionNewProduct = em.merge(oldCategoryIdOfProductCollectionNewProduct);
					}
				}
			}
			em.getTransaction().commit();
		} catch (IllegalOrphanException ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Short id = category.getId();
				if (findCategory(id) == null) {
					throw new NonexistentEntityException("The category with id " + id + " no longer exists.");
				}
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Short id) throws IllegalOrphanException, NonexistentEntityException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Category category;
			try {
				category = em.getReference(Category.class, id);
				category.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The category with id " + id + " no longer exists.", enfe);
			}
			List<String> illegalOrphanMessages = null;
			Collection<Product> productCollectionOrphanCheck = category.getProductCollection();
			for (Product productCollectionOrphanCheckProduct : productCollectionOrphanCheck) {
				if (illegalOrphanMessages == null) {
					illegalOrphanMessages = new ArrayList<>();
				}
				illegalOrphanMessages.add("This Category (" + category + ") cannot be destroyed since the Product " + productCollectionOrphanCheckProduct + " in its productCollection field has a non-nullable categoryId field.");
			}
			if (illegalOrphanMessages != null) {
				throw new IllegalOrphanException(illegalOrphanMessages);
			}
			em.remove(category);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<Category> findCategoryEntities() {
		return findCategoryEntities(true, -1, -1);
	}

	public List<Category> findCategoryEntities(int maxResults, int firstResult) {
		return findCategoryEntities(false, maxResults, firstResult);
	}

	private List<Category> findCategoryEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(Category.class));
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

	public Category findCategory(Short id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Category.class, id);
		} finally {
			em.close();
		}
	}

	public int getCategoryCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<Category> rt = cq.from(Category.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<Category> findCategories() {
		EntityManager em = getEntityManager();
		try {
			Query q = em.createQuery("SELECT c FROM Category c ORDER BY c.name");
			return q.getResultList();
		} finally {
			em.close();
		}
	}
	
}
