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

import com.achow101.bctalkaccountpricer.shared.AccountPricer;
import com.achow101.bctalkaccountpricer.shared.FieldVerifier;
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
	private final PricingServiceAsync greetingService = GWT
			.create(PricingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
				
		// Add Gui stuff		
		final Button sendButton = new Button("Estimate Price");
		final TextBox nameField = new TextBox();
		nameField.setText("User ID");
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
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// Clear previous output
				uidLabel.setText("");
				usernameLabel.setText("");
				postsLabel.setText("");
				activityLabel.setText("");
				potActivityLabel.setText("");
				postQualityLabel.setText("");
				trustLabel.setText("");
				priceLabel.setText("");
				
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("User ID must only be numbers");
					return;
				}
				
				// Loading message
				loadingLabel.setText("Loading... Please wait. This can take a few minutes.");
				
				// Get uid
				//int uid = Integer.parseInt(textToServer);
				
				// Get data from pricer
				//AccountPricer pricer = new AccountPricer(uid);
				//String[] result = pricer.getAccountData();

				// Get data from server
				sendButton.setEnabled(false);
				greetingService.pricingServer(textToServer,
						new AsyncCallback<String[]>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								errorLabel.setText("Remote Procedure Call - Failure. Please try again");
							}
							
							public void onSuccess(String[] result)
							{
								// Display data
												
								// Clear other messages
								errorLabel.setText("");
								loadingLabel.setText("");
								
								// Output results
								uidLabel.setText(result[0]);
								usernameLabel.setText(result[1]);
								postsLabel.setText(result[2]);
								activityLabel.setText(result[3]);
								potActivityLabel.setText(result[4]);
								postQualityLabel.setText(result[5]);
								trustLabel.setText(result[6]);
								priceLabel.setText(result[7]);
								sendButton.setEnabled(true);
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}
}
