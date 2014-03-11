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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author osman
 */
public enum MsgSubjectJpaController implements Serializable {

	MSG_SUB_CTL;
	private final EntityManagerFactory emf = EM.emf;

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void create(MsgSubject msgSubject) {
		if (msgSubject.getMsgFeedbackCollection() == null) {
			msgSubject.setMsgFeedbackCollection(new ArrayList<MsgFeedback>());
		}
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			Collection<MsgFeedback> attachedMsgFeedbackCollection = new ArrayList<MsgFeedback>();
			for (MsgFeedback msgFeedbackCollectionMsgFeedbackToAttach : msgSubject.getMsgFeedbackCollection()) {
				msgFeedbackCollectionMsgFeedbackToAttach = em.getReference(msgFeedbackCollectionMsgFeedbackToAttach.getClass(), msgFeedbackCollectionMsgFeedbackToAttach.getId());
				attachedMsgFeedbackCollection.add(msgFeedbackCollectionMsgFeedbackToAttach);
			}
			msgSubject.setMsgFeedbackCollection(attachedMsgFeedbackCollection);
			em.persist(msgSubject);
			for (MsgFeedback msgFeedbackCollectionMsgFeedback : msgSubject.getMsgFeedbackCollection()) {
				MsgSubject oldSubjectIdOfMsgFeedbackCollectionMsgFeedback = msgFeedbackCollectionMsgFeedback.getSubjectId();
				msgFeedbackCollectionMsgFeedback.setSubjectId(msgSubject);
				msgFeedbackCollectionMsgFeedback = em.merge(msgFeedbackCollectionMsgFeedback);
				if (oldSubjectIdOfMsgFeedbackCollectionMsgFeedback != null) {
					oldSubjectIdOfMsgFeedbackCollectionMsgFeedback.getMsgFeedbackCollection().remove(msgFeedbackCollectionMsgFeedback);
					oldSubjectIdOfMsgFeedbackCollectionMsgFeedback = em.merge(oldSubjectIdOfMsgFeedbackCollectionMsgFeedback);
				}
			}
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(MsgSubject msgSubject) throws NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			MsgSubject persistentMsgSubject = em.find(MsgSubject.class, msgSubject.getId());
			Collection<MsgFeedback> msgFeedbackCollectionOld = persistentMsgSubject.getMsgFeedbackCollection();
			Collection<MsgFeedback> msgFeedbackCollectionNew = msgSubject.getMsgFeedbackCollection();
			Collection<MsgFeedback> attachedMsgFeedbackCollectionNew = new ArrayList<MsgFeedback>();
			for (MsgFeedback msgFeedbackCollectionNewMsgFeedbackToAttach : msgFeedbackCollectionNew) {
				msgFeedbackCollectionNewMsgFeedbackToAttach = em.getReference(msgFeedbackCollectionNewMsgFeedbackToAttach.getClass(), msgFeedbackCollectionNewMsgFeedbackToAttach.getId());
				attachedMsgFeedbackCollectionNew.add(msgFeedbackCollectionNewMsgFeedbackToAttach);
			}
			msgFeedbackCollectionNew = attachedMsgFeedbackCollectionNew;
			msgSubject.setMsgFeedbackCollection(msgFeedbackCollectionNew);
			msgSubject = em.merge(msgSubject);
			for (MsgFeedback msgFeedbackCollectionOldMsgFeedback : msgFeedbackCollectionOld) {
				if (!msgFeedbackCollectionNew.contains(msgFeedbackCollectionOldMsgFeedback)) {
					msgFeedbackCollectionOldMsgFeedback.setSubjectId(null);
					msgFeedbackCollectionOldMsgFeedback = em.merge(msgFeedbackCollectionOldMsgFeedback);
				}
			}
			for (MsgFeedback msgFeedbackCollectionNewMsgFeedback : msgFeedbackCollectionNew) {
				if (!msgFeedbackCollectionOld.contains(msgFeedbackCollectionNewMsgFeedback)) {
					MsgSubject oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback = msgFeedbackCollectionNewMsgFeedback.getSubjectId();
					msgFeedbackCollectionNewMsgFeedback.setSubjectId(msgSubject);
					msgFeedbackCollectionNewMsgFeedback = em.merge(msgFeedbackCollectionNewMsgFeedback);
					if (oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback != null && !oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback.equals(msgSubject)) {
						oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback.getMsgFeedbackCollection().remove(msgFeedbackCollectionNewMsgFeedback);
						oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback = em.merge(oldSubjectIdOfMsgFeedbackCollectionNewMsgFeedback);
					}
				}
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Integer id = msgSubject.getId();
				if (findMsgSubject(id) == null) {
					throw new NonexistentEntityException("The msgSubject with id " + id + " no longer exists.");
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
			MsgSubject msgSubject;
			try {
				msgSubject = em.getReference(MsgSubject.class, id);
				msgSubject.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The msgSubject with id " + id + " no longer exists.", enfe);
			}
			Collection<MsgFeedback> msgFeedbackCollection = msgSubject.getMsgFeedbackCollection();
			for (MsgFeedback msgFeedbackCollectionMsgFeedback : msgFeedbackCollection) {
				msgFeedbackCollectionMsgFeedback.setSubjectId(null);
				msgFeedbackCollectionMsgFeedback = em.merge(msgFeedbackCollectionMsgFeedback);
			}
			em.remove(msgSubject);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<MsgSubject> findMsgSubjectEntities() {
		return findMsgSubjectEntities(true, -1, -1);
	}

	public List<MsgSubject> findMsgSubjectEntities(int maxResults, int firstResult) {
		return findMsgSubjectEntities(false, maxResults, firstResult);
	}

	private List<MsgSubject> findMsgSubjectEntities(boolean all, int maxResults, int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(MsgSubject.class));
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

	public MsgSubject findMsgSubject(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(MsgSubject.class, id);
		} finally {
			em.close();
		}
	}

	public int getMsgSubjectCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<MsgSubject> rt = cq.from(MsgSubject.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<MsgSubject> findSubjects() {
		EntityManager em = getEntityManager();
		try {
			Query q = em.createQuery("SELECT s FROM MsgSubject s ORDER BY s.name");
			return q.getResultList();
		} finally {
			em.close();
		}
	}

}
