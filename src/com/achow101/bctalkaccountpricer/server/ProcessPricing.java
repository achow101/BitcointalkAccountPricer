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

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class ProcessPricing implements Runnable {
	
	public void run()
	{
		System.out.println("Starting ProcessPricing thread for processing the requests");
		
		// Infinte loop so that it runs indefinitely
        boolean processNext = false;
		while(true)
		{
            if(!processNext) {
                // TODO: multithreading wait and notify stuff
            }
            processNext = false;

            // Open a database connection
            // (create a new database if it doesn't exist yet):
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/requests.odb");
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<QueueRequest> q = cb.createQuery(QueueRequest.class);
            Root<QueueRequest> reqs = q.from(QueueRequest.class);
            q.select(reqs);
            TypedQuery<QueueRequest> query = em.createQuery(q);
            List<QueueRequest> reqList = query.getResultList();

            // Find the request to process
            QueueRequest reqToProcess = null;
            for(QueueRequest req : reqList)
            {
                if(req.isProcessing())
                {
                    System.out.println("Processing request " + req.getToken());
                    reqToProcess = req;
                }
            }

            // Price the request
            AccountPricer pricer = new AccountPricer(reqToProcess);
            System.out.println("Completed processing request " + reqToProcess.getToken());

            // Change status in db
            em.getTransaction().begin();
            reqToProcess.setProcessing(false);
            reqToProcess.setResult(pricer.getAccountData());
            reqToProcess.setDone(true);
            reqToProcess.setCompletedTime(System.currentTimeMillis() / 1000L);
            reqToProcess.setQueuePos(-5);
            em.getTransaction().commit();

            // Decrement queue pos of all other requests
            for(QueueRequest req : reqList)
            {
                if(req.getQueuePos() > 0) {
                    em.getTransaction().begin();
                    req.setQueuePos(req.getQueuePos() - 1);

                    // Set the next to process
                    if (req.getQueuePos() == 0)
                    {
                        req.setProcessing(true);
                        processNext = true;
                    }

                    em.getTransaction().commit();
                }
            }

            // Close database connection
            em.close();
            emf.close();
		}
	}
}
