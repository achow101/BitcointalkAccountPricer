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
                synchronized (this)
                {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            processNext = false;

            // Open a database connection
            // (create a new database if it doesn't exist yet):
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/requests.odb");
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<QueueRequestDB> q = cb.createQuery(QueueRequestDB.class);
            Root<QueueRequestDB> reqs = q.from(QueueRequestDB.class);
            q.select(reqs);
            TypedQuery<QueueRequestDB> query = em.createQuery(q);
            List<QueueRequestDB> reqList = query.getResultList();

            // Find the request to process
            QueueRequestDB reqToProcess = null;
            for(QueueRequestDB req : reqList)
            {
                if(req.isProcessing())
                {
                    System.out.println("Processing request " + req.getToken());
                    reqToProcess = req;
                }
            }

            // Price the request
            AccountPricer pricer = new AccountPricer(reqToProcess);
            String[] priceData = pricer.getAccountData();
            System.out.println("Completed processing request " + reqToProcess.getToken());

            // Change status in db
            em.getTransaction().begin();
            reqToProcess.setProcessing(false);
            reqToProcess.setResult(priceData);
            reqToProcess.setDone(true);
            reqToProcess.setCompletedTime(System.currentTimeMillis() / 1000L);
            reqToProcess.setQueuePos(-5);
            em.getTransaction().commit();

            // Decrement queue pos of all other requests
            for(QueueRequestDB req : reqList)
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
