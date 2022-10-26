package com.emlogis.test.providers;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.services.notification.NotificationUtil;
import com.sun.mail.imap.IMAPSSLStore;

public class EmailReceiveTest {

	private final static Logger logger = LoggerFactory
			.getLogger(EmailReceiveTest.class);
	
	static String getSplittedResult(String input, String first, String second ){
		String retStr = "";
		try{
			if(input!=null){
				if(input.contains(first)){
					String[] split1 = input.split(first);
					if(split1!=null && split1.length>=1){
						String[] split2 = split1[1].split(second);
						if(split2!=null && split2.length>=1){
							retStr = split2[0];
						}
					}
				}
			}
			
		}catch (Exception e) {
		}
		return retStr;
	}

	public static void main(String[] args) {
		
		Session session;
		
		Session smtpSession;
		
		Store store = null;
		Folder folder = null;

		final String username = "devmail@emlogis.net";
		final String password = "EmLogis123";
		final String hostName = "mail.emlogis.net";

		Properties properties = new Properties();

		properties.put("port", 7993);
		properties.put("protocal", "imap");
		properties.put("host", hostName);
		
		session = Session.getInstance(properties, null);
		
		URLName urln = new URLName(properties.getProperty("protocal"), properties.getProperty("host"), 7993, null,
				username, password);
		
		
		store = new IMAPSSLStore(session, urln);	
		
		try {
			
			logger.debug("in initMail(), Trying to connect to Host : ");
			logger.debug(properties.getProperty("host"));
			
			if(store!=null && !store.isConnected()){
			    logger.debug("initMail():store got connected..");
				store.connect(properties.getProperty("host"), username, password);
			}
			
			logger.debug("--Email Controller connected successfully using protocol= " + 
					properties.getProperty("protocal"));
			
			folder = store.getFolder("rjack.emlogis.com");
			folder.open(Folder.READ_WRITE);
			
			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message[] newMessages = folder.search(unseenFlagTerm);	
			
			if (newMessages.length == 0)
				logger.debug("No messages found.");

			for (Message message : newMessages) {

				String uuid = NotificationUtil.getSplittedResult(
						NotificationUtil.getMailMessageText(message), "<", ">");

				// String uuid1 =
				// NotificationUtil.getSplittedResult("Id:b48f860a-95f0-458d-bfae-45817b135deb:",
				// "Id:", ":");

				String messageContent = NotificationUtil.getMailMessageText(
						message).substring(0, 50);
				String testConent = message.getContent().toString();

				logger.debug("The subject is: " + message.getSubject());

				logger.debug("The content is: " + messageContent);

				Boolean approved = NotificationUtil
						.getEmailResponseApproval(messageContent);

				logger.debug("The UUID is: " + uuid);

			}
		   
		   folder.setFlags(newMessages, new Flags(Flags.Flag.SEEN), true);
		   
		   folder.close(false);
			
		} catch (MessagingException e) {
			logger.error("Error",e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
		}
		

	}

}
