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

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
        EntityManagerFactory emf = (EntityManagerFactory)arg0.getServletContext().getAttribute("emf");
        emf.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
        // ObjectDB database intializer
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/guest.odb");
        arg0.getServletContext().setAttribute("emf", emf);

        // Start threads
		(new Thread(new ProcessPricing())).start();
		(new Thread(new PricingServiceImpl())).start();
	}
}
