package com.ibm.broker.matip;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class MATIP_Processing_Outbound_Construct_MATIP_Header extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {

			// The final MATIP message which is transmitted to the MATIP network is structured as follows:
			//
			// <MATIP header><Start of Header sequence><Actual Data><End of Text sequence><BATAP Trailer>
			//
			// For the moment the message flow assumes that:
			//
			//		<MATIP Header>					4 byte MATIP header		
			//		<Actual Data>					This is the Body of the WMQ message from original input queue 
			//		<Start of Header sequence>		Hardcoded for the moment - possibly a future UDP ?
			//		<End of Text sequence>			Hardcoded for the moment - possibly a future UDP ?
			//		<BATAP Trailer>					4 bytes consisting of <SPACE> and 3 digit SRLN
			//
			// At this stage in the message flow the input to this node has constructed everything besides the MATIP header
			// The format of the MATIP header is a fixed length of 4 bytes:
			// 
			//  Byte01:		Version					00000ZZZ	ZZZ=001 or invalid
			//	Byte02:		Command					YZZZZZZZ	Y=1 means Command packet, Y=0 means Data Packet
			//	Byte03:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length 
			//	Byte04:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length
			//
			//  So in total the MATIP header will be 00000001,00000000,<Length>
			//  Build up the final output message using single bit values
			//  For simplicity, assign values to the output message using an array of int values to represent each byte
			//  These arrays are converted into chunks, each one stored in an int value, which is 4 bytes in length
			//  Finally the chunks are converted into a byte array ready for use in the output message.
			
			MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
            byte[] bytesInputmessage = inParser.toBitstream("","","",0,0,0);
            
            try {
            	String StringInputmessage = new String(bytesInputmessage,"utf-8");
				String binaryValueForLength = Integer.toBinaryString(StringInputmessage.length() + 4); // +4 because of MATIP header being added
				int test = binaryValueForLength.length();
				for (int i=0; i < 16 - test; i++) {
					// This ensures the string representation is padded to 16 chars in length.
					binaryValueForLength = "0" + binaryValueForLength;				
				}	
				String byte03String = binaryValueForLength.substring(0,8);
				String byte04String = binaryValueForLength.substring(8,16);
				int[] byte01 = {0,0,0,0,0,0,0,1};
				int[] byte02 = {0,0,0,0,0,0,0,0};
				int[] byte03 = new int[8];
				int[] byte04 = new int[8];
				for (int i=0; i < 8; i++) {
					if (byte03String.substring(i,i+1).equals("1")) {byte03[i] = 1;}
					if (byte04String.substring(i,i+1).equals("1")) {byte04[i] = 1;}
				}
						
				// Construct the final output
				byte[] bytesFinal = new byte[StringInputmessage.length()+4];
				int bytes1to4 = MATIPUtilities.inflateBits(byte01,byte02,byte03,byte04);				
				ByteBuffer buf = ByteBuffer.wrap(new byte[4]);
				buf.putInt(0,bytes1to4);
		    	System.arraycopy(buf.array(), 0, bytesFinal, 0, 4);
		    	System.arraycopy(StringInputmessage.getBytes("utf-8"), 0, bytesFinal, 4, StringInputmessage.length());		  
				copyMessageHeaders(inMessage, outMessage);		    
		    	MbElement outRoot = outMessage.getRootElement();
 				outRoot.createElementAsLastChildFromBitstream(bytesFinal, MbBLOB.PARSER_NAME,"","","",0,0,0);		        										
 				out.propagate(outAssembly);
			} catch (UnsupportedEncodingException e) {
   				e.printStackTrace();
   			}

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

}
