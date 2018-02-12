package com.ibm.broker.matip;

import java.nio.ByteBuffer;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class MATIP_Processing_Inbound_Construct_SC extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {

			//  Byte01:		Version					00000ZZZ	ZZZ=001 or invalid
			//	Byte02:		Command					YZZZZZZZ	Y=1 means Command packet, Y=0 means Data Packet
			//	Byte03:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length 
			//	Byte04:		Length					00000101	Interpret Bytes 3 & 4 together as a length - will be 5 for SC
			//  Byte05:     Cause                   00000000    Normal close
			//
			//  Putting all the above together with Version=001, Command Identifier for SC is 100, 
			//  Length of message is 5 bytes, so Byte 4 must reflect this: 00000101

			//  This can be hardcoded as ALL SC messages must look like this:
			
			// Prepare values ...
			int[] byte01 = {0,0,0,0,0,0,0,1};	// SessionClose:	Version 
			int[] byte02 = {1,1,1,1,1,1,0,0};	// SessionClose:	Command	
			int[] byte03 = {0,0,0,0,0,0,0,0};	// SessionClose:	Length1  
			int[] byte04 = {0,0,0,0,0,1,0,1};	// SessionClose:	Length2
			int[] byte05 = {0,0,0,0,0,0,0,0};	// SessionClose:	Cause
			int[] byteempty = {0,0,0,0,0,0,0,0};	//	Filler	
			
			// Convert the byte arrays into 4-byte chunks 
		    int bytes1to4 = MATIPUtilities.inflateBits(byte01,byte02,byte03,byte04);
		    int bytes5to8 = MATIPUtilities.inflateBits(byte05,byteempty,byteempty,byteempty);
		    ByteBuffer buf = ByteBuffer.wrap(new byte[4]);			
			buf.putInt(0,bytes1to4);
			buf.putInt(4,bytes5to8);
			byte[] bytesFinal = new byte[5];
			
			// Change the session state to Closed
			
			
			//  Construct the output message ...
		    copyMessageHeaders(inMessage, outMessage);		    
		    MbElement outRoot = outMessage.getRootElement();
            MbElement outParser = outRoot.createElementAsLastChild(MbBLOB.PARSER_NAME);            
            System.arraycopy(buf.array(), 0, bytesFinal, 0, 5);
            outParser.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "BLOB", buf.array());						
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

}
