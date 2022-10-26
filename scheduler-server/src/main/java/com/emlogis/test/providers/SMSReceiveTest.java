package com.emlogis.test.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.MessageList;

public class SMSReceiveTest {
	
	public static void main(String[] args) {
		
		int sendStatus = -1;
		
		String accsid = "AC0d8f962374d04d35ad50ca7b5c97caa2";
	    String acckey = "cc6f45dfde026083bf04f9376cfa8b88";
	    String mercurySID = "AC1ab1c5257e072918725841a3dacaca32";
	    
	    String twilioNumber = "6502044649";
	    
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);
		
		
		
	    Map filterMap = new HashMap<String, String>();
	    filterMap.put("To", twilioNumber);
		
		// Get the account and call factory class
	    Account acct = client.getAccount(mercurySID);
	    MessageList messageList = acct.getMessages(filterMap);
	    int total = messageList.getNumPages();
	    
	    for (Message message: messageList) {
	    	// System.out.println(message.getBody());
	    	try {
	    		System.out.println("\nThe Next Message is: " + message.getBody());
				message.delete();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
		
	}

}
