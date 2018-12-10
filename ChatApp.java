import java.io.IOException;
import java.net.URISyntaxException;

import java.security.InvalidKeyException;
import java.util.EnumSet;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.*;
import com.microsoft.azure.storage.core.*;

public class ChatApp {
	String username;
	CloudQueueClient queueClient;
	CloudQueue queue;
	CloudQueue queue2;
	int counter = 0;
	Iterable<CloudQueueMessage> peekedMessage;
	public static final String storageConnectionString = "DefaultEndpointsProtocol=https;" +
			"AccountName=omnianasystem;" +
			"AccountKey=QS6dQ+Zyba8CMGR1myHAi04wKu3303DURkJD2+aQdozwRiEQpJHEN9GcGXKEamLQ0sMTPBQ8+/yZa7Z377jXkA==";
	
	public ChatApp() throws InvalidKeyException, RuntimeException, IOException, URISyntaxException, ClassNotFoundException{
		//StorageCredentials storageCredentials = new StorageCredentials("omnianasystem","QS6dQ+Zyba8CMGR1myHAi04wKu3303DURkJD2+aQdozwRiEQpJHEN9GcGXKEamLQ0sMTPBQ8+/yZa7Z377jXkA==" );
		//CloudStorageAccount cloudStorageAccount = new CloudStorageAccount(storageCredentials, useHttps: true);
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		queueClient = storageAccount.createCloudQueueClient();
	}
	
	public void setUsername(String u) {
		username = u;
	}
	public void createQueue(String name) {
		try
		{
		    // Retrieve storage account from connection-string.
			System.out.println("creating queue with name "+name);

		   // Retrieve a reference to a queue.
		   queue = queueClient.getQueueReference(name);

		   // Create the queue if it doesn't already exist.
		   queue.createIfNotExists();
		   queue2 = queueClient.getQueueReference(name + "-1");

		   // Create the queue if it doesn't already exist.
		   if (queue2.createIfNotExists()) {
			   CloudQueueMessage m = new CloudQueueMessage("0");
			   queue2.addMessage(m);
		   }
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
	}
	//
	public void sendMessage(String q, String m) {
		try
		{
		    // Create a message and add it to the queue.
			System.out.println("Sending message " + m);
		    CloudQueueMessage message = new CloudQueueMessage(m);
		    queue.addMessage(message);
		    CloudQueueMessage retrievedCount = queue2.retrieveMessage();

		    while(retrievedCount == null) {
		    	Thread.sleep(1);
		    	retrievedCount = queue2.retrieveMessage();
		    }
		    System.out.println("message retrieved from queue2 is" + retrievedCount.getMessageContentAsString());
		    System.out.println("=============");
		    int temp = Integer.parseInt(retrievedCount.getMessageContentAsString())+1;
		    System.out.println("Updating queue count "+temp);
		    queue2.deleteMessage(retrievedCount);
		    CloudQueueMessage tempMessage = new CloudQueueMessage(""+temp);
		    queue2.addMessage(tempMessage);
		    //retrievedCount.setMessageContent("" + temp);
		            // Update the message.
	        //queue2.updateMessage(retrievedCount, 1);
	        System.out.println("updated queue2 with message"+queue2.peekMessage().getMessageContentAsString());
	        counter++;
	        System.out.println("Value of counter is: " + counter);
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
	}
	public boolean isChangePresent(String q) throws URISyntaxException, StorageException{
		queue2 = queueClient.getQueueReference(q+"-1");
	    CloudQueueMessage m = queue2.peekMessage();
	    System.out.println("in IsChangePresent counter ="+counter+"messages in queue ="+m.getMessageContentAsString());
	    if (m!=null) {
	    	if (Integer.parseInt(m.getMessageContentAsString()) > counter) {
	    		counter = Integer.parseInt(m.getMessageContentAsString());
	    		return true;
	    	}
	    }
		return false;
	}
	public Iterable<CloudQueueMessage> readMessages(String q){
		try
		{
		    // Retrieve storage account from connection-string.

		    // Retrieve a reference to a queue.
			System.out.println("in readmessage");
		    queue = queueClient.getQueueReference(q);
		    
		    peekedMessage = queue.peekMessages(32);
		    System.out.println("peeked message "+ peekedMessage.toString());
		    // Output the message value.
		    if (peekedMessage != null)
		   {
		    	return (peekedMessage);
		   }
	    }
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		Iterable<CloudQueueMessage> peekedM = null;
		return peekedM;
	}
	public void limitLength(String q) {
		try {
			System.out.println("Limiting queue length");
			queue = queueClient.getQueueReference(q);
			queue.downloadAttributes();
			long messageCount = queue.getApproximateMessageCount();
			System.out.println("message count is "+messageCount);
			while (messageCount >= 32) {
				CloudQueueMessage retrievedMessage = queue.retrieveMessage();
				System.out.println("message being deleted is "+retrievedMessage);
			    if (retrievedMessage != null)
			    {
			        queue.deleteMessage(retrievedMessage);
			    }
			    queue.downloadAttributes();
			    messageCount = queue.getApproximateMessageCount();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void setQueueName(String newQueueName) throws URISyntaxException, StorageException {
		queue = queueClient.getQueueReference(newQueueName);

		   // Create the queue if it doesn't already exist.
		queue.createIfNotExists();
	}
}
