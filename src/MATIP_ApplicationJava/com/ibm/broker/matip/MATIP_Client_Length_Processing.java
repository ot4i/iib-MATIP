package com.ibm.broker.matip;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class MATIP_Client_Length_Processing extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {

			//  Byte01:		Version					00000ZZZ	ZZZ=001 or invalid
			//	Byte02:		Command					YZZZZZZZ	Y=1 means Command packet, Y=0 means Data Packet
			//	Byte03:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length 
			//	Byte04:		Length					00000101	Interpret Bytes 3 & 4 together as a length - will be 5 if OC

		    MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
            MbElement inBody = inParser.getFirstChild();			
			byte[] bytesInputmessage = inBody.toBitstream("","","",0,0,0);			
			int[] byte01 = convertByteToArray(bytesInputmessage[0]);	// OpenConfirm:	Version
   			int[] byte02 = convertByteToArray(bytesInputmessage[1]);	// OpenConfirm:	Command
			int[] byte03 = convertByteToArray(bytesInputmessage[2]);	// OpenConfirm:	Length1 
			int[] byte04 = convertByteToArray(bytesInputmessage[3]);	// OpenConfirm:	Length2 
			
			MbElement LocalEnvironment = inAssembly.getLocalEnvironment().getRootElement();		
			MbElement MATIP = LocalEnvironment.createElementAsLastChild(MbElement.TYPE_NAME,"MATIP",null);
			MATIP.createElementAsLastChild(MbElement.TYPE_NAME,"Version",Integer.toString(byte01[5])+Integer.toString(byte01[6])+Integer.toString(byte01[7]));
			if (byte02[0] == 0) {MATIP.createElementAsLastChild(MbElement.TYPE_NAME,"CommandOrData","Data"); } else { 
				MATIP.createElementAsLastChild(MbElement.TYPE_NAME,"CommandOrData","Command");
				String CommandIdentifier = Integer.toString(byte02[5])+Integer.toString(byte02[6])+Integer.toString(byte02[7]);
				MATIP.createElementAsLastChild(MbElement.TYPE_NAME,"Command","XXX");
				if (CommandIdentifier.equals("000")) {MATIP.getFirstElementByPath("Command").setValue("RTR");}
				if (CommandIdentifier.equals("001")) {MATIP.getFirstElementByPath("Command").setValue("STR");}
				if (CommandIdentifier.equals("011")) {MATIP.getFirstElementByPath("Command").setValue("SSQ");}
				if (CommandIdentifier.equals("010")) {MATIP.getFirstElementByPath("Command").setValue("SSR");}			
				// The following messages shouldn't occur in normal operation ... catch them here just in case!
				if (CommandIdentifier.equals("101")) {MATIP.getFirstElementByPath("Command").setValue("OC");}
				if (CommandIdentifier.equals("110")) {MATIP.getFirstElementByPath("Command").setValue("SO");}
				
			}
			
			// Calculate the remaining length which needs to be taken from the TCPIP stream
			// and set this in the LocalEnvironment to control the following TCPIPReceive node			
			int TCPIPLength = convert2ByteLength(byte03,byte04);			
			MATIP.createElementAsLastChild(MbElement.TYPE_NAME,"Length",TCPIPLength);			
			MbElement TCPIP = LocalEnvironment.getFirstElementByPath("TCPIP");
			// Remember to subtract 4 from the length, as this is the amount of data we read initially
			TCPIP.createElementAsLastChild(MbElement.TYPE_NAME,"Receive",null).createElementAsLastChild(MbElement.TYPE_NAME,"Length",TCPIPLength-4);
			out.propagate(outAssembly);

		} finally {

			// clear the outMessage even if there's an exception
			outMessage.clearMessage();
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

		private int convert2ByteLength(int[] intarray1,int[] intarray2) {
			
			int int1 = 0;
			int int2 = 0;
			for (int i=0; i < 8; i++) {
				if (intarray1[7-i] == 1) {int1 = int1 | (0x00000001 << i);}
				if (intarray2[7-i] == 1) {int2 = int2 | (0x00000001 << i);}
				
			}		
			int1 = int1 << 8;
			int bytequad = (0 | 0 | int1 | int2 );
			return bytequad;
		}




}




