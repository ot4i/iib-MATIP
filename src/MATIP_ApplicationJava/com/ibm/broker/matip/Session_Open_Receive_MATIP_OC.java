package com.ibm.broker.matip;

import java.util.*;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class Session_Open_Receive_MATIP_OC extends MbJavaComputeNode {

    public void onInitialize(){
    
    }	
	
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {

			//  The meaning of the input message is defined based on single bit values:
			
			//  Byte01:		Version					00000ZZZ	ZZZ=001 or invalid
			//	Byte02:		Command					YZZZZZZZ	Y=1 means Command packet, Y=0 means Data Packet
			//	Byte03:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length 
			//	Byte04:		Length					00000101	Interpret Bytes 3 & 4 together as a length - will be 5 if OC
			//	Byte05:		Cause of rejection		01ZZZZZZ	ZZZZZZ=000001 : No Traffic Type matching between Sender & Recipient
			//													ZZZZZZ=000010 : Information in SO header incoherent
			//													ZZZZZZ=000011 : Type of Protection mechanism are different
			//													ZZZZZZ=000100 : Client User Not configured in Server
			//													ZZZZZZ=000101 : Connection Request Collision
			//													ZZZZZZ=000110 : Connection disabled by supervisor
			//													ZZZZZZ=000111 : Open request for a Session already open			
			//													ZZZZZZ=001000 : Recipient resource ID mismatch
			//													ZZZZZZ=001001 up to 111111 : Reserved for Future Use			
			//  Byte05:		Acceptance				00000000
			
			//  The meaning of the final output message is defined based on single bit values
			//  For simplicity, assign values to the output message using an array of int values to represent each byte
			//  These arrays are converted into chunks, each one stored in an int value, which is 4 bytes in length
			//  Finally the chunks are converted into a byte array ready for use in the output message.
			
		    //  Construct the output message ...
		    copyMessageHeaders(inMessage, outMessage);		    
		    MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
            MbElement inBody = inParser.getFirstChild();
            byte[] bytesInputmessage = inBody.toBitstream("","","",0,0,0);
   			int inputBitstreamLength = bytesInputmessage.length;
   			if (inputBitstreamLength != 5) {
   				MbService.logError(this.getClass(), 
   		        		"evaluate", 
   		        		MATIPMessages.MessageBundle.MESSAGE_SOURCE,
   		        		MATIPMessages.MessageBundle.WRONG_LENGTH,
   		        		"Investigate this problem with the partner MATIP application.", 
   		        		new String[] { Integer.toString(inputBitstreamLength) });
   			}
   			
   			int[] byte01 = convertByteToArray(bytesInputmessage[0]);	// OpenConfirm:	Version
   			int[] byte02 = convertByteToArray(bytesInputmessage[1]);	// OpenConfirm:	Command
			int[] byte03 = convertByteToArray(bytesInputmessage[2]);	// OpenConfirm:	Length1 
			int[] byte04 = convertByteToArray(bytesInputmessage[3]);	// OpenConfirm:	Length2 
			int[] byte05 = convertByteToArray(bytesInputmessage[4]);	// OpenConfirm:	Cause
   			
   			if ( (byte01[5]==0) && (byte01[6]==0) && (byte01[7]==1) ) 
			{/* Do nothing - this is a valid version number */}
			else {
				MbService.logError(this.getClass(), 
   		        		"evaluate", 
   		        		MATIPMessages.MessageBundle.MESSAGE_SOURCE,
   		        		MATIPMessages.MessageBundle.WRONG_MATIP_VERSION,
   		        		"Investigate this problem with the partner MATIP application.", 
   		        		new String[] { Integer.toString(byte01[5]) + Integer.toString(byte01[6]) + Integer.toString(byte01[7]) });
			}   			
   			if ( (byte02[0]==0)) {
				MbService.logError(this.getClass(), 
   		        		"evaluate", 
   		        		MATIPMessages.MessageBundle.MESSAGE_SOURCE,
   		        		MATIPMessages.MessageBundle.WRONG_MATIP_TYPE,
   		        		"The first bit of the second byte of the OC message should be set to the value 1 to indicate a command packet. Investigate this problem with the partner MATIP application.", 
   		        		new String[] {""});
			}   			
   			if ( Arrays.equals(byte03,new int[] {0,0,0,0,0,0,0,0}) || Arrays.equals(byte04,new int[] {0,0,0,0,0,1,0,1})) 
   			{/* Do nothing - this is the expected first length byte for OC */}
   			else {				
   				MbService.logError(this.getClass(), 
   		        		"evaluate", 
   		        		MATIPMessages.MessageBundle.MESSAGE_SOURCE,
   		        		MATIPMessages.MessageBundle.WRONG_MATIP_OC_LENGTH,
   		        		"The third and fourth bytes of an OC message should carry the value 5, the expected length of the message.  ", 
   		        		new String[] {Arrays.toString(byte03) + Arrays.toString(byte04)});
			} 
   			if ( Arrays.equals(byte05,new int[] {0,0,0,0,0,0,0,0}) )
   			{
   				// This means the OC is telling us that the Session Open worked ...   			 	
   			}   				
   			else {
   				String rejectionCauseNumeric = Integer.toString(byte05[4])+Integer.toString(byte05[5])+Integer.toString(byte05[6])+Integer.toString(byte05[7]);
   				String rejectionCauseString	= "Unrecognised cause, code is reserved for Future Use.";
   				if (rejectionCauseNumeric.equals("0001")) {rejectionCauseString = "No Traffic Type matching between Sender & Recipient";}
   				if (rejectionCauseNumeric.equals("0010")) {rejectionCauseString = "Information in SO header incoherent";}
   				if (rejectionCauseNumeric.equals("0011")) {rejectionCauseString = "Type of Protection mechanism are different";}
   				if (rejectionCauseNumeric.equals("0100")) {rejectionCauseString = "Client User Not configured in Server";}  				
   				if (rejectionCauseNumeric.equals("0101")) {rejectionCauseString = "Connection Request Collision";}
   				if (rejectionCauseNumeric.equals("0110")) {rejectionCauseString = "Connection disabled by supervisor";}
   				if (rejectionCauseNumeric.equals("0111")) {rejectionCauseString = "Open request for a Session already open";}
   				if (rejectionCauseNumeric.equals("1000")) {rejectionCauseString = "Recipient resource ID mismatch";}   				
   				MbService.logError(this.getClass(), 
   		        		"evaluate", 
   		        		MATIPMessages.MessageBundle.MESSAGE_SOURCE,
   		        		MATIPMessages.MessageBundle.MATIP_OC_REJECT,
   		        		"The second bit of the fifth byte of the OC message carried the value 1.  This symbolises a rejection of the OS command.", 
   		        		new String[] {rejectionCauseString});
   			}			
			
			out.propagate(outAssembly);

				
	} finally {

			// clear the outMessage even if there's an exception
			outMessage.clearMessage();
		}
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// iterate though the headers starting with the first child of the root
		// element
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) // stop before
		// the last
		// child
		// (body)
		{
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

	private int[] convertByteToArray(byte inByte) {
		
		int inInt = (int)(inByte);
		int[] outputintarray = new int[8];
		for (int i=0; i < 8; i++) {			
			int compare = 0x00000001 << i;
			if ((inInt & compare) == compare) {outputintarray[7-i] = 1;}
			else {outputintarray[7-i] = 0;}
		}
		return outputintarray;
	}	
	
}
