/*
 * Copyright (C) 2016  Andrew Chow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.achow101.bctalkaccountpricer.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import com.achow101.bctalkaccountpricer.client.PricingService;
import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PricingServiceImpl extends RemoteServiceServlet implements PricingService {

	private static SecureRandom random = new SecureRandom();

	@Override
	public QueueRequest queueServer(QueueRequest request)
	{
		// Open a database connection
		// (create a new database if it doesn't exist yet):
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/requests.odb");
		EntityManager em = emf.createEntityManager();

        QueueRequestDB foundReq = em.find(QueueRequestDB.class, request.getToken());
        if(foundReq != null)
            return foundReq.getQueueRequest();

        // Get list of results
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<QueueRequestDB> q = cb.createQuery(QueueRequestDB.class);
        Root<QueueRequestDB> reqs = q.from(QueueRequestDB.class);
        q.select(reqs);
        TypedQuery<QueueRequestDB> query = em.createQuery(q);
        List<QueueRequestDB> reqList = query.getResultList();

        // Check if another request is processing
        boolean anotherIsProcessing = false;
        CriteriaQuery<QueueRequestDB> qProcessing = cb.createQuery(QueueRequestDB.class);
        Root<QueueRequestDB> rProcReqs = qProcessing.from(QueueRequestDB.class);
        qProcessing.select(rProcReqs);
        ParameterExpression<Boolean> rProc = cb.parameter(Boolean.class);
        qProcessing.where(cb.equal(rProcReqs.get("processing"), rProc));
        TypedQuery<QueueRequestDB> procQuery = em.createQuery(qProcessing);
        List<QueueRequestDB> procReqList = procQuery.setParameter(rProc, true).getResultList();
        if(procReqList.size() == 1)
            anotherIsProcessing = true;

        // Go through list and figure out what to do with requests
        int hiQueuePos = 0;
        for(QueueRequestDB req : reqList)
        {
            // Remove expired ones
            if(req.isExpired())
            {
                // Remove request from db
                em.getTransaction().begin();
                em.remove(req);
                em.getTransaction().commit();
            }

            // Check if Ip needs to wait
            // TODO: Remove negative before publishing!
            if (!request.isNew() && req.getIp().equals(request.getIp()) && request.getRequestedTime() - req.getRequestedTime() <= -120 && !request.isPoll()) {
                request.setQueuePos(-2);
                // Close database connection
                em.close();
                emf.close();
                return request;
            }

            // Check if ip already requested
            if (false && req.getIp().equals(request.getIp()) && request.isNew() && !request.isPoll()) {
                request.setQueuePos(-3);
                // Close database connection
                em.close();
                emf.close();
                return request;
            }

			// Set first request go
			if(req.getQueuePos() == 0 && !req.isProcessing() && !anotherIsProcessing) {
                em.getTransaction().begin();
                req.setProcessing(true);
                em.getTransaction().commit();
                synchronized (Config.processPricing)
                {
                    notify();
                }
            }

            // Find the highest queue position to set queue position
            if(req.getQueuePos() > hiQueuePos)
                hiQueuePos = req.getQueuePos();
        }

		if(request.isNew())
		{
			// Set remaining fields
			request.setIp(getThreadLocalRequest().getRemoteAddr());
			request.setRequestedTime(System.currentTimeMillis() / 1000L);
            request.setQueuePos(hiQueuePos);
			if(request.getToken().equals("NO TOKEN") && request.getUid() == 0)
			{
				request.setQueuePos(-4);
				return request;
			}

			// add the token
			request.setToken(new BigInteger(40, random).toString(32));
			request.setOldReq();
			request.setGo(false);
			
			// Add request to db
            QueueRequestDB requestDb = new QueueRequestDB(request);
            em.getTransaction().begin();
            em.persist(requestDb);
            em.getTransaction().commit();
            System.out.println("Added request " + request.getToken() + " to queue.");

            // Set processing
            if(request.getQueuePos() == 0){
                request.setProcessing(true);
                em.getTransaction().begin();
                requestDb.setProcessing(true);
                em.getTransaction().commit();
                synchronized (Config.processPricing)
                {
                    Config.processPricing.notify();
                }
            }

            // Close database connection
            em.close();
            emf.close();
			
			return request;
		}

        // Close database connection
        em.close();
        emf.close();
		
		// If any request makes it this far, then it is bad.
		request.setQueuePos(-4);
		return request;
	}

	@Override
	public boolean removeRequest(QueueRequest request){

        // Open a database connection
        // (create a new database if it doesn't exist yet):
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/requests.odb");
        EntityManager em = emf.createEntityManager();

        QueueRequestDB foundReq = em.find(QueueRequestDB.class, request.getToken());
        if(foundReq == null)
            return false;

        em.getTransaction().begin();
        em.remove(foundReq);
        em.getTransaction().commit();

        // Close database connection
        em.close();
        emf.close();

        return true;
	}
}
