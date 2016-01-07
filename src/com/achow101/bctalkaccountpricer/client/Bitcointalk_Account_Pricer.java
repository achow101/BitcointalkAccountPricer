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
package com.achow101.bctalkaccountpricer.client;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Bitcointalk_Account_Pricer implements EntryPoint {
	
	//TODO: Prior to running this code, please remember to change the password in AccountPricer.java for accountbot.

	private QueueRequest request;
	
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final PricingServiceAsync pricingService = GWT
			.create(PricingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
				
		// Add Gui stuff		
		final Button sendButton = new Button("Estimate Price");
		final TextBox nameField = new TextBox();
		nameField.setText("User ID/Token");
		final Label errorLabel = new Label();
		final Label uidLabel = new Label();
		final Label usernameLabel = new Label();
		final Label postsLabel = new Label();
		final Label activityLabel = new Label();
		final Label potActivityLabel = new Label();
		final Label postQualityLabel = new Label();
		final Label trustLabel = new Label();
		final Label priceLabel = new Label();
		final Label loadingLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		RootPanel.get("uidLabelContainer").add(uidLabel);
		RootPanel.get("usernameLabelContainer").add(usernameLabel);
		RootPanel.get("postsLabelContainer").add(postsLabel);
		RootPanel.get("activityLabelContainer").add(activityLabel);
		RootPanel.get("potActivityLabelContainer").add(potActivityLabel);
		RootPanel.get("postQualityLabelContainer").add(postQualityLabel);
		RootPanel.get("trustLabelContainer").add(trustLabel);
		RootPanel.get("priceLabelContainer").add(priceLabel);
		RootPanel.get("loadingLabelContainer").add(loadingLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();
		
		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				
				// Add request to queue
				addToQueue();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addToQueue();
				}
			}
			
			// Adds the request to server queue
			private void addToQueue()
			{

				// Clear previous output
				uidLabel.setText("");
				usernameLabel.setText("");
				postsLabel.setText("");
				activityLabel.setText("");
				potActivityLabel.setText("");
				postQualityLabel.setText("");
				trustLabel.setText("");
				priceLabel.setText("");
				sendButton.setEnabled(false);
				errorLabel.setText("");
				loadingLabel.setText("");
				
				// Create and add request
				request = new QueueRequest();
				if(nameField.getText().matches("^[0-9]+$"))
					request.setUid(Integer.parseInt(escapeHtml(nameField.getText())));
				else
				{
					request.setToken(escapeHtml(nameField.getText()));
					request.setOldReq();
				}
				
				// send request to server
				pricingService.queueServer(request, new AsyncCallback<QueueRequest>()
				{

					@Override
					public void onFailure(Throwable caught) {
						errorLabel.setText("Request Queuing failed. Please try again.");
						sendButton.setEnabled(true);
						pricingService.removeRequest(request, null);
						
					}

					@Override
					public void onSuccess(QueueRequest result) {
						
						if(result.getQueuePos() == -3)
						{
							loadingLabel.setText("Please wait for your previous request to finish and try again");
							sendButton.setEnabled(true);
						}
						
						else if(result.getQueuePos() == -2)
						{
							loadingLabel.setText("Please wait 10 minutes before requesting again.");
							sendButton.setEnabled(true);
						}
						
						else if(result.getQueuePos() == -4)
						{
							loadingLabel.setText("Invalid token");
							sendButton.setEnabled(true);
						}
						
						else
						{
							if(!result.isProcessing() && !result.isDone())
							{
								loadingLabel.setText("Please wait. Your token is " + result.getToken() + " and you are number " + result.getQueuePos() + " in the queue.");
							}
							if(result.isProcessing())
							{
								loadingLabel.setText("Request is processing. Please wait. Your token is " + result.getToken());
							}
							if(result.isDone())
							{										
								// Clear other messages
								errorLabel.setText("");
								loadingLabel.setText("");
								
								// Output results
								uidLabel.setText(result.getResult()[0]);
								usernameLabel.setText(result.getResult()[1]);
								postsLabel.setText(result.getResult()[2]);
								activityLabel.setText(result.getResult()[3]);
								potActivityLabel.setText(result.getResult()[4]);
								postQualityLabel.setText(result.getResult()[5]);
								trustLabel.setText(result.getResult()[6]);
								priceLabel.setText(result.getResult()[7]);
							}
							request = result;
							sendButton.setEnabled(true);
						}													
					}
				});
		}			
	}

	// Add a handler to send the name to the server
	MyHandler handler = new MyHandler();
	sendButton.addClickHandler(handler);
	nameField.addKeyUpHandler(handler);
}
	
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
