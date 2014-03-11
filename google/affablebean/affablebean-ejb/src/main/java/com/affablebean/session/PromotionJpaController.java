package com.affablebean.session;

import com.affablebean.entity.Promotion;

import static com.affablebean.session.EntityMgr.EM;

import com.affablebean.session.exceptions.NonexistentEntityException;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author osman
 */
public enum PromotionJpaController implements Serializable {
	PROMO_CTL;
	private final EntityManagerFactory emf = EM.emf;

	public void create(Promotion promotion) {
		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();
			em.persist(promotion);
			em.getTransaction().commit();

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(Promotion promotion)
					throws NonexistentEntityException, Exception {

		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();
			promotion = em.merge(promotion);
			em.getTransaction().commit();

		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();

			if (msg == null || msg.length() == 0) {
				Integer id = promotion.getId();

				if (findPromotion(id) == null) {
					throw new NonexistentEntityException(
									"The promotion with id " + id + " no longer exists.");
				}
			}

			throw ex;

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Integer id) throws NonexistentEntityException {
		EntityManager em = null;

		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Promotion promotion;

			try {
				promotion = em.getReference(Promotion.class, id);
				promotion.getId();

			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException(
								"The promotion with id " + id + " no longer exists.", enfe);
			}

			em.remove(promotion);
			em.getTransaction().commit();

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<Promotion> findCategories() {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery(
							"SELECT p FROM Promotion p WHERE p.categoryId > 0");
			return q.getResultList();

		} finally {
			em.close();
		}
	}

	public List<Promotion> findProducts() {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery(
							"SELECT p FROM Promotion p WHERE p.productId > 0");
			return q.getResultList();

		} finally {
			em.close();
		}
	}

	public Promotion findSale() {
		EntityManager em = getEntityManager();

		try {
			Query q = em.createQuery(
							"SELECT p FROM Promotion p WHERE p.sale = :sale");
			q.setParameter("sale", true);
			List<Promotion> promos = q.getResultList();

			// only the first found sale is used, rest ignored
			return (!promos.isEmpty()) ? promos.get(0) : null;

		} finally {
			em.close();
		}
	}

	public List<Promotion> findPromotionEntities() {
		return findPromotionEntities(true, -1, -1);
	}

	public List<Promotion> findPromotionEntities(int maxResults, int firstResult) {
		return findPromotionEntities(false, maxResults, firstResult);
	}

	public Promotion findPromotion(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(Promotion.class, id);
		} finally {
			em.close();
		}
	}

	public int getPromotionCount() {
		EntityManager em = getEntityManager();

		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<Promotion> rt = cq.from(Promotion.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);

			return ((Long) q.getSingleResult()).intValue();

		} finally {
			em.close();
		}
	}

	private List<Promotion> findPromotionEntities(boolean all, int maxResults,
					int firstResult) {

		EntityManager em = getEntityManager();

		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(Promotion.class));
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
