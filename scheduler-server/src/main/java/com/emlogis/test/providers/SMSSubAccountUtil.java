package com.emlogis.test.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.services.notification.NotificationReadSMSService;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.AccountFactory;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.AccountList;
import com.twilio.sdk.resource.list.IncomingPhoneNumberList;

public class SMSSubAccountUtil {
	
	private static final String accsid = "AC0d8f962374d04d35ad50ca7b5c97caa2";
    private static  final String acckey = "cc6f45dfde026083bf04f9376cfa8b88";
    private static final String mercurySID = "AC1ab1c5257e072918725841a3dacaca32";
    
    private final static Logger logger = LoggerFactory
			.getLogger(SMSSubAccountUtil.class);
	
	public static void main(String[] args) {
		// Create Sub Account
//		Account account = createSubAccount(); 
//	    System.out.println(account.getSid());
//	    logger.info("The sub Account Id is: " + account.getSid());
	
		// Get account by ID
//		Account account = null;
//		// Get Sub Account Status
//		String id = "AC1ab1c5257e072918725841a3dacaca32";
//	    account = getAccountStatusById(id);
		
		// Get Account by name
//		AccountList accounts = getAccountStatusByName("Mercury");
		
		// Display phone numbers owned by account limited by map
//		ArrayList<String> filterList = new ArrayList();
//		filterList.add("+16502044649");
//		filterList.add("+17136365406");
//		filterList.add("+17136365404");
//		
		IncomingPhoneNumberList phoneNumberList = getPhoneNumbersForAccount(mercurySID, null);
		
		// Change Phone numbers to Sub account
//		ArrayList<String> phoneSIDList = new ArrayList<>();
//		phoneSIDList.add("PN07983ce1962546aa81b0305f6ac21c78");
//		phoneSIDList.add("PN5ab94b8bf91f4199926a00918e5df094");
//		phoneSIDList.add("PN6351d3f1598f2fd1d17311128eed1cfc");
//		
//		for (String phoneSID: phoneSIDList) {
//			switchPhoneNumber(phoneSID, mercurySID);
//		}
	}
	
	private static void switchPhoneNumber(String  numberSId, String newAccountSID) {
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);
		 
	    try {
			// Get an object from its sid. If you do not have a sid,
			// check out the list resource examples on this page
			IncomingPhoneNumber number = client.getAccount().getIncomingPhoneNumber(numberSId);
			// Build a filter for the IncomingPhoneNumberList
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("AccountSid", newAccountSID));
			number.update(params);
		} catch (TwilioRestException e) {
			logger.error("Unable to change phone number account", e);
		}
		
	}
	
	private static IncomingPhoneNumberList getPhoneNumbersForAccount(String accountSID, List filterList) {
		IncomingPhoneNumberList phoneNumberList = null;
		
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);

	    IncomingPhoneNumberList numbers = client.getAccount(accountSID).getIncomingPhoneNumbers();
	     
	    String phoneNumberString;
	    boolean printInfo = true;
	    // Loop over numbers and print out a property for each one.
	    for (IncomingPhoneNumber number : numbers) {
	    	
	      phoneNumberString = number.getPhoneNumber();
	      
	      if( (filterList != null) && (!filterList.contains(phoneNumberString)) ) {
	    	  printInfo = false; 
	      } else {
	    	  printInfo = true; 
	      }
	      
	      if(printInfo){
	      
		      logger.info("The phone number is: " + number.getPhoneNumber());
		      logger.info("The phone SID is: " + number.getSid());
		      logger.info("The phone friendly Name is: " + number.getFriendlyName());
	      
	      }
	    }
		
		return phoneNumberList;
	}
	
	private static Account getAccountStatusById(String id) {
		Account account;
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);
	 
	    // Get an object from its sid. If you do not have a sid,
	    // check out the list resource examples on this page
	    account = client.getAccount(id);
	    logger.info("The account status is:" + account.getStatus());
	    return account;
	}
	
	private static AccountList getAccountStatusByName(String name) {
		
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);
		
		// Build a filter for the AccountList
	    Map<String, String> params = new HashMap<String, String>();
	     
	    params.put("FriendlyName", name);
	     
	    AccountList accounts = client.getAccounts(params);
	   
	    
	 // Loop over accounts and print out a property for each one.
	    for (Account account : accounts) {
	      logger.info("The account name is: " + account.getFriendlyName());
	      logger.info("The account SID is: " + account.getSid());
	      logger.info("The account status is: " +account.getStatus());
	    }
	    
	    
	    
	    return accounts;
	}

	private static Account createSubAccount() {
		Account account = null;
		try {
			TwilioRestClient client = new TwilioRestClient(accsid, acckey);
			
			// Get the account and call factory class
			Account acct = client.getAccount();
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("FriendlyName", "Mercury"));
			
			AccountFactory accountFactory = client.getAccountFactory();
			account = accountFactory.create(params);
		} catch (TwilioRestException e) {
			logger.error("Failed to create sub account", e);
		}
		return account;
	}

}
