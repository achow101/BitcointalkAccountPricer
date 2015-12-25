/*******************************************************************************
 * Copyright (C) 2015  Andrew Chow
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
 ******************************************************************************/
package com.achow101.bctalkaccountpricer.server;

import java.util.ArrayList;
import java.util.List;

import com.achow101.bctalkaccountpricer.client.PricingService;
import com.achow101.bctalkaccountpricer.shared.FieldVerifier;
import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PricingServiceImpl extends RemoteServiceServlet implements
		PricingService {
	
	public static List<QueueRequest> requestList = new ArrayList<QueueRequest>();
	public static List<QueueRequest> ipWait = new ArrayList<QueueRequest>();

	public String[] pricingServer(String input, QueueRequest request) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"User ID must only be numbers");
		}
		
		System.out.println("Num Requests: " + requestList.size());

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		
		// get UserID
		int uid = Integer.parseInt(input);
		
		// Create pricer object
		AccountPricer pricer = new AccountPricer(uid);
		
		// Create output array
		String[] out = pricer.getAccountData();
		
		// Remove request from list
		removeRequest(request);
		
		return out;
	}

	/**3
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	@Override
	public QueueRequest queueServer(QueueRequest request)
	{
		if(request.isNew())
		{
			// Set remaining fields
			request.setIp(getThreadLocalRequest().getRemoteAddr());
			request.setTime(System.currentTimeMillis() / 1000L);
			
			// Check if Ip needs to wait
			for(QueueRequest req : ipWait)
			{
				// TODO: Remove negative before publishing!
				if(req.getIp().equals(request.getIp()) && request.getTime() - req.getTime() <= -120)
				{
					request.setQueuePos(-2);
					return request;
				}
			}
			
			for(QueueRequest req : requestList)
			{
				// Check if ip already requested
				if(req.getIp().equals(request.getIp()))
				{
					request.setQueuePos(-3);
					return request;
				}
			}
			
			request.setOldReq();
			request.setGo(false);
			requestList.add(request);
		}
		
		if(request.getQueuePos() == 0 && requestList.indexOf(request) == 0)
		{
			request.setGo(true);
		}
		if(request.getQueuePos() == -1)
		{
			requestList.remove(request);
		}
		
		for(QueueRequest req : requestList)
		{
			
			// Remove -1 position requests
			if(req.getQueuePos() == -1)
			{
				requestList.remove(req);
			}
			
			// Set first request to go
			if(req.getQueuePos() == 0 && requestList.indexOf(req) == 0)
			{
				req.setGo(true);
			}
			
			// Set position of request to position in list
			req.setQueuePos(requestList.indexOf(req));
			
			// Set request to return to match actual request in list
			if(req.getIp().equals(request.getIp()) && req.getTime() == request.getTime())
			{
				request = req;
			}
			
		}
		return request;
	}

	@Override
	public boolean removeRequest(QueueRequest request){
		for(QueueRequest req : requestList)
		{
			if(req.getIp().equals(request.getIp()) && req.getTime() == request.getTime())
			{
				requestList.remove(req);
				request.setTime(System.currentTimeMillis() / 1000L);
				ipWait.add(request);
				return true;
			}
			
		}
		return false;
	}
	
	private void removeOldIPWaits()
	{
		for(QueueRequest req : ipWait)
		{
			if((System.currentTimeMillis() / 1000L) - req.getTime() >= 300)
			{
				ipWait.remove(req);
			}
		}
	}
}
