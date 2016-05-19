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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Config implements ServletContextListener {

	public static BlockingQueue<QueueRequestDB> requestsToProcess = new LinkedBlockingQueue<QueueRequestDB>();
	public static BlockingQueue<QueueRequestDB> processedRequests = new LinkedBlockingQueue<QueueRequestDB>();

    public static EntityManagerFactory emf;

	private static ProcessPricing processPricing;
	private static PricingServiceImpl pricingService;
    private static Thread processPricingThread;
    private static Thread pricingServiceThread;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		// Stop threads
        if(processPricingThread.isAlive())
		    processPricing.stopThread();
        if(processPricingThread.isAlive())
		    pricingService.stopThread();

        while(!pricingServiceThread.isAlive() || !processPricingThread.isAlive())
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Close db
        System.out.println("Closing database");
        emf.close();

        System.out.println("Bitcointalk Account Price Estimator fully stopped.");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
        // ObjectDB database intializer
		System.out.println("Opening database");
        emf = Persistence.createEntityManagerFactory("../../db/requests.odb");

        // Get requests that need to be queued and processed
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<QueueRequestDB> q = cb.createQuery(QueueRequestDB.class);
        Root<QueueRequestDB> reqs = q.from(QueueRequestDB.class);
        q.select(reqs);
        TypedQuery<QueueRequestDB> query = em.createQuery(q);
        List<QueueRequestDB> reqList = query.getResultList();
        int index = 0;
        int nextQueuePos = 0;
        while(!reqList.isEmpty())
        {
            QueueRequestDB req = reqList.get(index);
            if(req.getQueuePos() < 0)
                reqList.remove(req);
            if(req.getQueuePos() == nextQueuePos)
            {
                try {
                    requestsToProcess.put(req);
                    System.out.println("Placing " + req.getToken() + " back into queue");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                reqList.remove(req);
                nextQueuePos++;
            }
            if(index < reqList.size() - 1)
                index++;
            else
                index = 0;
        }

        // Start threads
		processPricing = new ProcessPricing();
        processPricingThread = new Thread(processPricing);
        processPricingThread.start();
		pricingService = new PricingServiceImpl();
        pricingServiceThread = new Thread(pricingService);
        pricingServiceThread.start();
	}
}
