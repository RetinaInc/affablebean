package com.affablebean.session;

import java.io.Serializable;

import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.affablebean.entity.MsgFeedback;
import com.affablebean.entity.MsgSubject;

import static com.affablebean.session.EntityMgr.EM;

import com.affablebean.session.exceptions.NonexistentEntityException;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author osman
 */
public enum MsgFeedbackJpaController implements Serializable {

	MSG_FEED_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(MsgFeedback msgFeedback) {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			MsgSubject subjectId = msgFeedback.getSubjectId();
			if (subjectId != null) {
				subjectId = em.getReference(subjectId.getClass(), subjectId.getId());
				msgFeedback.setSubjectId(subjectId);
			}
			em.persist(msgFeedback);
			if (subjectId != null) {
				subjectId.getMsgFeedbackCollection().add(msgFeedback);
				subjectId = em.merge(subjectId);
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(MsgFeedback msgFeedback) throws NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			MsgFeedback persistentMsgFeedback = em.find(MsgFeedback.class, msgFeedback.getId());
			MsgSubject subjectIdOld = persistentMsgFeedback.getSubjectId();
			MsgSubject subjectIdNew = msgFeedback.getSubjectId();
			if (subjectIdNew != null) {
				subjectIdNew = em.getReference(subjectIdNew.getClass(), subjectIdNew.getId());
				msgFeedback.setSubjectId(subjectIdNew);
			}
			msgFeedback = em.merge(msgFeedback);
			if (subjectIdOld != null && !subjectIdOld.equals(subjectIdNew)) {
				subjectIdOld.getMsgFeedbackCollection().remove(msgFeedback);
				subjectIdOld = em.merge(subjectIdOld);
			}
			if (subjectIdNew != null && !subjectIdNew.equals(subjectIdOld)) {
				subjectIdNew.getMsgFeedbackCollection().add(msgFeedback);
				subjectIdNew = em.merge(subjectIdNew);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Integer id = msgFeedback.getId();
				if (findMsgFeedback(id) == null) {
					throw new NonexistentEntityException("The msgFeedback with id " + id + " no longer exists.");
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
			MsgFeedback msgFeedback;
			try {
				msgFeedback = em.getReference(MsgFeedback.class, id);
				msgFeedback.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The msgFeedback with id " + id + " no longer exists.", enfe);
			}
			MsgSubject subjectId = msgFeedback.getSubjectId();
			if (subjectId != null) {
				subjectId.getMsgFeedbackCollection().remove(msgFeedback);
				subjectId = em.merge(subjectId);
			}
			em.remove(msgFeedback);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<MsgFeedback> findMsgFeedbackEntities() {
		return findMsgFeedbackEntities(true, -1, -1);
	}

	public List<MsgFeedback> findMsgFeedbackEntities(int maxResults, int firstResult) {
		return findMsgFeedbackEntities(false, maxResults, firstResult);
	}

	private List<MsgFeedback> findMsgFeedbackEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(MsgFeedback.class));
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

	public MsgFeedback findMsgFeedback(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(MsgFeedback.class, id);
		} finally {
			em.close();
		}
	}

	public int getMsgFeedbackCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<MsgFeedback> rt = cq.from(MsgFeedback.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

}
