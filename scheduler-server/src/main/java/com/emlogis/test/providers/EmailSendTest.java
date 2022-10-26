package com.emlogis.test.providers;

import com.emlogis.common.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class EmailSendTest {
	
	private final static Logger logger = LoggerFactory.getLogger(EmailSendTest.class);
	
	public static void main(String[] args) {
		
//		int portNumber = 587;
//		String portString = "587";
//		
//		final String username = "roberttester5@gmail.com";
//		final String password = "Apkn-O*uE7JS";
//		final String hostName = "smtp.gmail.com";
		
		int portNumber = 25;
		String portString = "25";
		
		final String username = "devmail@emlogis.net";
		final String password = "EmLogis123";
		final String hostName = "mail.emlogis.net";		
		
		Properties props = new Properties();

		props.put("mail.smtp.host", hostName);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", portString);
		props.put("mail.smtp.starttls.enable", "true");
		// To see what is going on behind the scene
		//props.put("mail.debug", "false");
		
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		
		try {
			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress("rjack@emlogis.com"));
			
			InternetAddress[] replyToAddress = {new InternetAddress(username)};
			
			msg.setReplyTo(replyToAddress);
			
			InternetAddress[] address = {new InternetAddress("rjack2@gmail.com")};
			
			msg.setRecipients(Message.RecipientType.TO, address);
			

			msg.setSentDate(new Date());
			
			String mailId = UniqueId.getId();

			msg.setSubject("Very long, long, long, long Test Message: " + "<" + mailId + ">");
			
			msg.setText("Test body");
			
			msg.setDescription("Emlogis Server");
			Transport.send(msg);

			//transp.close();
			
		}
		catch (MessagingException mex) {
			// Prints all nested (chained) exceptions as well
			logger.error("Encountered error trying to deliver email", mex);

			// How to access nested exceptions
			while (mex.getNextException() != null) {
				// Get next exception in chain
				Exception ex = mex.getNextException();

				logger.error("Nested error trying to deliver email", ex);
				if (!(ex instanceof MessagingException))
					break;
				else mex = (MessagingException) ex;
			}
		}
		
		
	}

}
