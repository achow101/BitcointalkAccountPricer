package com.achow101.bctalkaccountpricer.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

public class Config implements ServletContextListener {

	public static BlockingQueue<QueueRequest> requestsToProcess = new LinkedBlockingQueue<QueueRequest>();
	public static BlockingQueue<QueueRequest> processedRequests = new LinkedBlockingQueue<QueueRequest>();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		(new Thread(new ProcessPricing())).start();
		(new Thread(new PricingServiceImpl())).start();
	}

}
