package com.ibm.broker.matip;

import java.util.ListResourceBundle;

public class MATIPMessages {
	
/**
 * This class is the ResourceBundle containing all the messages.
 */

public static class MessageBundle extends ListResourceBundle {
		public static final String MESSAGE_SOURCE = MATIPMessages.class.getName(); 
		public static final String INVALID_SENDER_RESOURCE_ID = "INVALID_SENDER_RESOURCE_ID";   
		public static final String INVALID_RECIPIENT_RESOURCE_ID = "INVALID_RECIPIENT_RESOURCE_ID"; 
	    public static final String CONNECTION_REFUSED = "CONNECTION_REFUSED";   
	    public static final String WRONG_LENGTH = "WRONG_LENGTH";
	    public static final String WRONG_MATIP_VERSION = "WRONG_MATIP_VERSION";
	    public static final String WRONG_MATIP_TYPE = "WRONG_MATIP_TYPE";
	    public static final String WRONG_MATIP_OC_LENGTH = "WRONG_MATIP_OC_LENGTH";
	    public static final String MATIP_OC_REJECT = "MATIP_OC_REJECT";		
	    public static final String NO_SSQ_EXPECTED = "NO_SSQ_EXPECTED";
	    public static final String INVALID_IMA = "INVALID_IMA";
	    public static final String NO_SESSIONSTATE = "NO_SESSIONSTATE";
	    public static final String SESSIONSTATE_EXISTS = "SESSIONSTATE_EXISTS";
	    
		private Object[][] messages  = {
			{INVALID_SENDER_RESOURCE_ID, "%1 is not a valid sender resource id. Please change the Flow UDP and redeploy."},
  			{INVALID_RECIPIENT_RESOURCE_ID, "%1 is not a valid recipient resource id. Please change the Flow UDP and redeploy."},
    		{CONNECTION_REFUSED, "The attempt to start a client connection failed.  The connection was refused due to cause: %1"},
    		{WRONG_LENGTH, "The Open Confirm message received was an invalid length.  OC messages should be 5 bytes long, but the data received was %1 bytes long"},
    		{WRONG_MATIP_VERSION, "The Open Confirm message received contained an invalid version.  OC messages should be version 001, but the data received was %1"},
    		{WRONG_MATIP_TYPE, "The Open Confirm message received was a Data message and not a command message."},
    		{WRONG_MATIP_OC_LENGTH, "The Open Confirm message received had an incorrect length. OC messages should claim a length of 5 bytes.  The data received in bytes 3 and 4 was %1"},
    		{MATIP_OC_REJECT, "The Open Confirm message received rejected the OS.  The reason for the rejection was: %1"},			
    		{NO_SSQ_EXPECTED, "An unexpected Session Status Query message has been received.  If you wish the flow to respond to SSQ messages then please reconfigure the Flow UDP accordingly and redeploy."},
    		{INVALID_IMA, "An IMA message was received from the MATIP network but it did not contain a whole number of 3 digit SRLN numbers!"},
    		{NO_SESSIONSTATE,"A request was made to remove the MATIP sessionstate from the broker's memory, but no sessionstate was found for id: %1"},
    		{SESSIONSTATE_EXISTS,"A request was made to remove the MATIP sessionstate from the broker's memory and a sessionstate was found for id: %1"}
		};
		
		public Object[][] getContents() {
			return messages;
		}
	}	

}
