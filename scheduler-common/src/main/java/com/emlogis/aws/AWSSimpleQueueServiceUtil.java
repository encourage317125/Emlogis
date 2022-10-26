package com.emlogis.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
 
/**
 * Created by IntelliJ IDEA.
 * User: Niraj Singh
 * Date: 3/19/13
 * Time: 10:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class AWSSimpleQueueServiceUtil {
    private BasicAWSCredentials credentials;
    private AmazonSQS sqs;
    private String simpleQueue = "scheduler-engine";
    private static volatile  AWSSimpleQueueServiceUtil awssqsUtil = new AWSSimpleQueueServiceUtil();
 
    /**
     * instantiates a AmazonSQSClient http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sqs/AmazonSQSClient.html
     * Currently using  BasicAWSCredentials to pass on the credentials.
     * For SQS you need to set your regions endpoint for sqs.
     */
    public   AWSSimpleQueueServiceUtil(){
        try{
            //Properties properties = new Properties();
            //properties.load(new FileInputStream("D:/samayik/adkonnection/src/main/resources/AwsCredentials.properties"));
            //this.credentials = new   BasicAWSCredentials(properties.getProperty("accessKey"),
             //                                            properties.getProperty("secretKey"));
            this.credentials = new   BasicAWSCredentials("AKIAJJME7CF4ATJ2RYMA", "aqclCY2WUmXEnluOb6dp+lTsW9fjC2pyFoocMdeI");
            
            this.simpleQueue = "scheduler-engine";
 
            this.sqs = new AmazonSQSClient(this.credentials);
            /**
             * My queue is in singapore region which has following endpoint for sqs
             * https://sqs.ap-southeast-1.amazonaws.com
             * you can find your endpoints here
             * http://docs.aws.amazon.com/general/latest/gr/rande.html
             *
             * Overrides the default endpoint for this client ("sqs.us-east-1.amazonaws.com")
             */
            this.sqs.setEndpoint("sqs.us-west-2.amazonaws.com");
            /*
               You can use this in your web app where    AwsCredentials.properties is stored in web-inf/classes
             */
            //AmazonSQS sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());
 
        }catch(Exception e){
            System.out.println("exception while creating awss3client : " + e);
        }
    }
 
    public static AWSSimpleQueueServiceUtil getInstance(){
        return awssqsUtil;
    }
 
    public AmazonSQS getAWSSQSClient(){
         return awssqsUtil.sqs;
    }
 
    public String getQueueName(){
         return awssqsUtil.simpleQueue;
    }
 
    /**
     * Creates a queue in your region and returns the url of the queue
     * @param queueName
     * @return
     */
    public String createQueue(String queueName){
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        String queueUrl = this.sqs.createQueue(createQueueRequest).getQueueUrl();
        return queueUrl;
    }
 
    /**
     * returns the queueurl for for sqs queue if you pass in a name
     * @param queueName
     * @return
     */
    public String getQueueUrl(String queueName){
        GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
        return this.sqs.getQueueUrl(getQueueUrlRequest).getQueueUrl();
    }
 
    /**
     * lists all your queue.
     * @return
     */
    public ListQueuesResult listQueues(){
       return this.sqs.listQueues();
    }
 
    /**
     * send a single message to your sqs queue
     * @param queueUrl
     * @param message
     */
    public void sendMessageToQueue(String queueUrl, String message){
        SendMessageResult messageResult =  this.sqs.sendMessage(new SendMessageRequest(queueUrl, message));
        System.out.println(messageResult.toString());
    }
 
    /**
     * gets messages from your queue
     * @param queueUrl
     * @return
     */
    public List<Message> getMessagesFromQueue(String queueUrl){
       ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
       receiveMessageRequest.setMaxNumberOfMessages(10);
       receiveMessageRequest.setWaitTimeSeconds(20);
       List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
       return messages;
    }
 
    /**
     * deletes a single message from your queue.
     * @param queueUrl
     * @param message
     */
    public void deleteMessageFromQueue(String queueUrl, Message message){
        String messageRecieptHandle = message.getReceiptHandle();
        System.out.println("message deleted : " + message.getBody() + "." + message.getReceiptHandle());
        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
    }
 
    public static void main(String[] args){
    	AWSSimpleQueueServiceUtil sqs = new AWSSimpleQueueServiceUtil();
    	List<Message> messages = sqs.getMessagesFromQueue("https://sqs.us-west-2.amazonaws.com/881705819348/scheduler-engine");
    	for (Message message : messages) {
    	    System.out.println("  Message");
    	    System.out.println("    MessageId:     " + message.getMessageId());
    	    System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
    	    System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
    	    System.out.println("    Body:          " + message.getBody());

        	try {
    			HashMap<String,String> body = new ObjectMapper().readValue(message.getBody(), new TypeReference<HashMap<String,String>>() {});
    			System.out.println("    Body: " + body);
    			if(body.containsKey("EC2InstanceId")) {
    				
    			}
        	} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	
 
    }
    
 
}
