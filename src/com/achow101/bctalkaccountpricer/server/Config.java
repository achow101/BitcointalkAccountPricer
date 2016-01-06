package com.achow101.bctalkaccountpricer.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;

public class Config implements ServletContextListener {
	
	public static List<QueueRequest> requestList = new ArrayList<QueueRequest>();
	public static List<QueueRequest> ipWait = new ArrayList<QueueRequest>();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		//TODO: Start processing thread Async
	}

}
