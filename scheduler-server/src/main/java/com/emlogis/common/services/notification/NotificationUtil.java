package com.emlogis.common.services.notification;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.emlogis.common.Constants;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.model.notification.dto.NotificationMessageDTO;

public class NotificationUtil {
	
 private static final Logger logger = Logger.getLogger(NotificationUtil.class);

	public static String getSplittedResult(String input, String first,
			String second) {
		String retStr = "";
		try {
			if (input != null) {
				if (input.contains(first)) {
					String[] split1 = input.split(first);
					if (split1 != null && split1.length >= 1) {
						String[] split2 = split1[1].split(second);
						if (split2 != null && split2.length >= 1) {
							retStr = split2[0];
						}
					}
				}
			}

		} catch (Exception e) {
		}
		return retStr;
	}

	public static void copyCommonNotificationData(Notification notificationSource, Notification notificationDest) {
		notificationDest.setProviderId(notificationSource.getProviderId());
		notificationDest.setTenantId(notificationSource.getTenantId());
		notificationDest.setSenderType(notificationSource.getSenderType());
		notificationDest.setSenderUserId(notificationSource.getSenderUserId());
		notificationDest.setSenderName(notificationSource.getSenderName());
		notificationDest.setSenderService(notificationSource.getSenderService());
		notificationDest.setReceiverUserId(notificationSource.getReceiverUserId());
		
		notificationDest.setReceiverName(notificationSource.getReceiverName());
		notificationDest.setDeliveryType(notificationSource.getDeliveryType());
		notificationDest.setDeliveryFormat(notificationSource.getDeliveryFormat());
		
		notificationDest.setNotificationOperation(notificationSource.getNotificationOperation());
		notificationDest.setNotificationCategory(notificationSource.getNotificationCategory());
		notificationDest.setNotificationRole(notificationSource.getNotificationRole());
		notificationDest.setIsWorkflowType(notificationSource.getIsWorkflowType());
		
		notificationDest.setFromAddress(notificationSource.getFromAddress());
		notificationDest.setToAddress(notificationSource.getToAddress());
		notificationDest.setPriorityType(notificationSource.getPriorityType());
		notificationDest.setQueuedOn(notificationSource.getQueuedOn());

		notificationDest.setStatus(notificationSource.getStatus());
		notificationDest.setAppServerId(notificationSource.getAppServerId());
		notificationDest.setProviderId(notificationSource.getProviderId());
		notificationDest.setProviderMessageId(notificationSource.getProviderMessageId());
		
		notificationDest.setUserResponseCode(notificationSource.getUserResponseCode());
		notificationDest.setEmailOnly(notificationSource.getEmailOnly());
		notificationDest.setMessageAttributes(notificationSource.getMessageAttributes());
	}
	
	public static SendNotification copyNotificationMessage(NotificationMessageDTO notificationMessageDTO) {
		SendNotification sendNotification = new SendNotification();

        sendNotification.setTenantId(notificationMessageDTO.getTenantId());
        sendNotification.setSenderUserId(notificationMessageDTO.getSenderUserId());
        sendNotification.setReceiverUserId(notificationMessageDTO.getReceiverUserId());

        sendNotification.setEmailOnly(notificationMessageDTO.isEmailOnly());
        sendNotification.setNotificationOperation(notificationMessageDTO.getNotificationOperation());
        sendNotification.setNotificationCategory(notificationMessageDTO.getNotificationCategory());
        sendNotification.setNotificationRole(notificationMessageDTO.getNotificationRole());
        sendNotification.setIsWorkflowType(notificationMessageDTO.getIsWorkflowType());
        sendNotification.setMessageAttributes(notificationMessageDTO.getMessageAttributes());

        sendNotification.setPriorityType(notificationMessageDTO.getPriorityType());

        sendNotification.setTobeDeliveredOn(notificationMessageDTO.getTobeDeliveredOn());
        
        return sendNotification;
	}
	
	public static NotificationMessageDTO copyNotificationToMessage(ReceiveNotification receiveNotification) {
		NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
		
		notificationMessageDTO.setApproved(receiveNotification.isApproved());
		notificationMessageDTO.setEmailOnly(receiveNotification.getEmailOnly());
		notificationMessageDTO.setIsWorkflowType(receiveNotification.getIsWorkflowType());
		notificationMessageDTO.setMessageAttributes(receiveNotification.getMessageAttributes());
		notificationMessageDTO.setNotificationCategory(receiveNotification.getNotificationCategory());
		notificationMessageDTO.setNotificationOperation(receiveNotification.getNotificationOperation());
		
		notificationMessageDTO.setNotificationRole(receiveNotification.getNotificationRole());
		notificationMessageDTO.setPriorityType(receiveNotification.getPriorityType());
		notificationMessageDTO.setReceiverUserId(receiveNotification.getReceiverUserId());
		notificationMessageDTO.setReplyToId(receiveNotification.getReplyToId());
		notificationMessageDTO.setSenderUserId(receiveNotification.getSenderUserId());
		notificationMessageDTO.setTenantId(receiveNotification.getTenantId());		
		
		return notificationMessageDTO;
		
	}
	
   

    /**
     * Return the primary text content of the message.
     */
    public static String getMailMessageText(Part p) throws
                MessagingException, IOException {
    	boolean textIsHtml = false;
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getMailMessageText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getMailMessageText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getMailMessageText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getMailMessageText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
    
    public static String addressesToString(Address[] addresses) {
    	String retVal = null;
    	
    	for(Address address: addresses) {
    		retVal += address  + ",";
    	}
    	
    	retVal = StringUtils.chop(retVal);
    	
    	return retVal;
    }
    
    public static int getSMSResponseCode(Random aRandom) {
    	return getRandomInteger(Constants.MIN_SMS_CODE, Constants.MAX_SMS_CODE, aRandom);
    }
    
    private static int getRandomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
          throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        return  (int)(fraction + aStart);    
     }
    
    public static String getResponseWithOutWhiteSpace(String response) {
    	
    	String retVal = response;
    	retVal = retVal.replaceAll("\n", "");
    	retVal = retVal.replaceAll("\r", "");
    	retVal = retVal.replaceAll(" ", "");
    	
    	return retVal;
    }
    
    public static Boolean getEmailResponseApproval(String response) {
    	Boolean retVal = null;
    	boolean approved = false;
    	
    	if( response.toUpperCase().contains("YES") ) {
    		retVal = true;
    	} else {
    		if( response.toUpperCase().contains("NO")){
    			retVal = false;
    		}
    	}
    	
    	return retVal;
    }
    
    public static Boolean getSMSResponseApproval(String response) {
    	Boolean retVal = null;
    	boolean approved = false;
    	
    	// Want to check for both yes and no
    	// Will return false if neither matches
    	
    	approved = responseMatchesYes(response);
    	if(approved) {
    		retVal = true;
    	} else {
    		approved = responseMatchesNo(response);
    		if(approved){
    			retVal = false;
    		}
    	}
    	
    	return retVal;
    }
    
    private static boolean responseMatchesYes(String response) {
    	boolean retVal = false;

		Matcher matcher;

        matcher = Constants.REG_EXP_YES_PATTERN.matcher(response);
        retVal = matcher.matches();
    
    	return retVal;
    }
    
    private static boolean responseMatchesNo(String response) {
    	boolean retVal = false;

		Matcher matcher;

        matcher = Constants.REG_EXP_NO_PATTERN.matcher(response);
        retVal = matcher.matches();
    
    	return retVal;
    }
    
    public static String getSMSReponseCode(String response) {
    	String retVal = "";

		Matcher matcher;
        
        matcher = Constants.REG_EXP_FOR_DIGITS_PATTERN.matcher(response);
        
 	    while(matcher.find()){
	 	    retVal = matcher.group();
	 	    
	 		if(retVal!=null && retVal.length()==5){
	 		    break;
	 		}
  	    }    
    
    	return retVal;
    }
    
	public static void main(String[] args) {
		Random random = new Random();
		int testNumber;
		for (int idx = 0; idx < 1000; ++idx) {
			testNumber = getRandomInteger(11111, 99999, random);

			logger.info("The next number is: " + testNumber);

		}
	}

}
