package com.emlogis.test.providers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SMSTest {
	
	public static void main(String[] args) {
		SmsFactory smsFactory = null;
		int sendStatus = -1;
		
		String accsid = "AC0d8f962374d04d35ad50ca7b5c97caa2";
	    String acckey = "cc6f45dfde026083bf04f9376cfa8b88";
	    String mercurySID = "AC1ab1c5257e072918725841a3dacaca32";
	    
	    String fromNumber = "6502044649";
	    String toNumber = "972-836-9214";
	    String body = "Test Message";
	    
		TwilioRestClient client = new TwilioRestClient(accsid, acckey);
		
		// Get the account and call factory class
	    Account acct = client.getAccount();
	    smsFactory = acct.getSmsFactory();
	    
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    
	    params.add(new BasicNameValuePair("Body", body));
	    params.add(new BasicNameValuePair("To", toNumber));
	    params.add(new BasicNameValuePair("From", fromNumber));
	    
       String sid = null;
       try {
           // send an sms messae
           // ( This makes a POST request to the Messages resource)
    	   MessageFactory messageFactory = client.getAccount(mercurySID).getMessageFactory();
    	   Message message = messageFactory.create(params);
           sid = message.getSid();
           sendStatus = 0;
       }
       catch (Exception e) {
    
    	   String exception = e.getMessage();
           sendStatus = -1;
           if(exception.toLowerCase().contains("not a valid phone number")){
               sendStatus = -2;
           }
       }
		
	}

}
