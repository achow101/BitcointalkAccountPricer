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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

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
		processPricing.stopThread();
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
        emf = Persistence.createEntityManagerFactory("$objectdb/db/requests.odb");

        // Start threads
		processPricing = new ProcessPricing();
        processPricingThread = new Thread(processPricing);
        processPricingThread.start();
		pricingService = new PricingServiceImpl();
        pricingServiceThread = new Thread(pricingService);
        pricingServiceThread.start();
	}
}
