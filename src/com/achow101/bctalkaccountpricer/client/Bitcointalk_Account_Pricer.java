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
package com.achow101.bctalkaccountpricer.client;

import java.util.Date;

import com.achow101.bctalkaccountpricer.shared.QueueRequest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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
		final Label tokenLabel = new Label();
		final InlineHTML estimateShareLabel = new InlineHTML();
		final InlineHTML reportTimeStamp = new InlineHTML();
		final RadioButton radioNormal = new RadioButton("merch", "Normal");
		final RadioButton radioMerchant = new RadioButton("merch", "Merchant");
		
		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		radioNormal.setValue(true);
		radioMerchant.setValue(false);
		
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
		RootPanel.get("tokenLabelContainer").add(tokenLabel);
		RootPanel.get("tokenShareLabelContainer").add(estimateShareLabel);
		RootPanel.get("radioNormalContainer").add(radioNormal);
		RootPanel.get("radioMerchantContainer").add(radioMerchant);
		RootPanel.get("reportTimeStamp").add(reportTimeStamp);
		
		// Create activity breakdown panel
		final VerticalPanel actPanel = new VerticalPanel();
		final FlexTable actTable = new FlexTable();
		actPanel.add(actTable);
		RootPanel.get("activityBreakdown").add(actPanel);
		
		// Create posts breakdown panel
		final VerticalPanel postsPanel = new VerticalPanel();
		final FlexTable postsTable = new FlexTable();
		postsPanel.add(postsTable);
		RootPanel.get("postsBreakdown").add(postsPanel);
		
		// Create addresses breakdown panel
		final VerticalPanel addrPanel = new VerticalPanel();
		final FlexTable addrTable = new FlexTable();
		postsPanel.add(addrTable);
		RootPanel.get("addrBreakdown").add(addrTable);

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
				tokenLabel.setText("");
				estimateShareLabel.setText("");
				reportTimeStamp.setText("");
				actTable.removeAllRows();
				postsTable.removeAllRows();
				addrTable.removeAllRows();
				
				// Create and add request
				request = new QueueRequest();
				request.setMerchant(radioMerchant.getValue());
				if(nameField.getText().matches("^[0-9]+$"))
					request.setUid(Integer.parseInt(escapeHtml(nameField.getText())));
				else
				{
					request.setToken(escapeHtml(nameField.getText()));
					request.setOldReq();
				}
				
				final String urlPath = com.google.gwt.user.client.Window.Location.getPath();
				final String host = com.google.gwt.user.client.Window.Location.getHost();
				final String protocol = com.google.gwt.user.client.Window.Location.getProtocol();
				final String url = protocol + "//" + host + urlPath + "?token=";
				
				// Request check loop
				Timer requestTimer = new Timer()
				{
					public void run()
					{
						// send request to server
						pricingService.queueServer(request, new AsyncCallback<QueueRequest>()
						{
								@Override
								public void onFailure(Throwable caught) {
									errorLabel.setText("Request Queuing failed. Please try again.");
									sendButton.setEnabled(true);
									pricingService.removeRequest(request, null);
									cancel();
								}

								@Override
								public void onSuccess(QueueRequest result) {
									
									if(result.getQueuePos() == -3)
									{
										loadingLabel.setText("Please wait for your previous request to finish and try again");
										sendButton.setEnabled(true);
										cancel();
									}
									
									else if(result.getQueuePos() == -2)
									{
										loadingLabel.setText("Please wait 2 minutes before requesting again.");
										sendButton.setEnabled(true);
										cancel();
									}
									
									else if(result.getQueuePos() == -4)
									{
										loadingLabel.setText("Invalid token");
										sendButton.setEnabled(true);
										cancel();
									}
									
									else
									{
										tokenLabel.setText("Your token is " + result.getToken());
										estimateShareLabel.setHTML("Share this estimate: <a href=\"" + url + result.getToken() +
												"\">" + url + result.getToken() + "</a>");
										if(!result.isProcessing() && !result.isDone())
										{
											loadingLabel.setText("Please wait. You are number " + result.getQueuePos() + " in the queue.");
										}
										if(result.isProcessing())
										{
											loadingLabel.setText("Request is processing. Please wait.");
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
											int indexOfLastAct = 0;
											int startAddrIndex = 0;
											for(int i = 8; i < result.getResult().length; i++)
											{
												if(result.getResult()[i].equals("<b>Post Sections Breakdown</b>"))
												{
													indexOfLastAct = i;
													break;
												}
												actTable.setHTML(i - 8, 0, result.getResult()[i]);
											}
											for(int i = indexOfLastAct; i < result.getResult().length; i++)
											{
												if(result.getResult()[i].contains("<b>Addresses posted in non-quoted text</b>"))
												{
													startAddrIndex = i;
													break;
												}
												postsTable.setHTML(i - indexOfLastAct, 0, result.getResult()[i]);
											}
											if(!result.isMerchant())
											{
												for(int i = startAddrIndex; i < result.getResult().length; i++)
												{
													addrTable.setHTML(i - startAddrIndex, 0, result.getResult()[i]);
												}
											}
											
											// Set the right radio
											radioMerchant.setValue(result.isMerchant());
											radioNormal.setValue(!result.isMerchant());
											
											// Report the time stamp
											DateTimeFormat fmt = DateTimeFormat.getFormat("MMMM dd, yyyy, hh:mm:ss a");
											Date completedDate = new Date(1000L*result.getCompletedTime());
											Date expireDate = new Date(1000L*(result.getCompletedTime() + result.getExpirationTime()));
											reportTimeStamp.setHTML("<i>Report generated at " + fmt.format(completedDate) 
												+ " and expires at " + fmt.format(expireDate) + "</i>");

											// Kill the timer after everything is done
											cancel();
										}
										request = result;
										request.setPoll(true);
										sendButton.setEnabled(true);									}													
								}
							});
						}
					};
					requestTimer.scheduleRepeating(2000);
					
					Timer setOldReqTimer = new Timer()
					{
						public void run()
						{
							request.setOldReq();
						}
					};
					setOldReqTimer.schedule(2001);

		}			
	}

	// Add a handler to send the name to the server
	MyHandler handler = new MyHandler();
	sendButton.addClickHandler(handler);
	nameField.addKeyUpHandler(handler);
	
	// Check the URL for URL parameters
	String urlTokenParam = com.google.gwt.user.client.Window.Location.getParameter("token");
	if(!urlTokenParam.isEmpty())
	{
		nameField.setText(urlTokenParam);
		handler.addToQueue();
	}
}
	
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
