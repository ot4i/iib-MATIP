package com.ibm.broker.matip;

import java.nio.*;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class Session_Open_Construct_MATIP_SO extends MbJavaComputeNode {

	private String senderResourceID;
	private String recipientResourceID;
	
	public void onInitialize() throws MbException
	{
		senderResourceID = (String) getUserDefinedAttribute("SenderResourceID");
		recipientResourceID = (String) getUserDefinedAttribute("RecipientResourceID");
    	}
	
    public void onDelete() {

	}		
	
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {
			
			//  The meaning of the final output message is defined based on single bit values:
			
			//  Byte01:		Version					00000ZZZ	ZZZ=001 or invalid
			//	Byte02:		Command					YZZZZZZZ	Y=1 means Command packet, Y=0 means Data Packet
			//	Byte03:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length 
			//	Byte04:		Length					ZZZZZZZZ	Interpret Bytes 3 & 4 together as a length
			//	Byte05:		Coding					00000ZZZ	ZZZ=000 means 5 bits, ZZZ=010 means 6 bits, 
			//													ZZZ=100 means 7 bits (ASCII), ZZZ=110 means 8 bits (EBCDIC) 			
			//	Byte06:		Protec (1st nibble)		0010		0010 means BATAP
			//				BFlag (2nd nibble)		ZZYY		YY=00 means Sender & Recipient ID not included, 
			//													YY=10 means Sender & Recipient ID in Bytes 7-10.
			//	Byte07:		Sender Resource ID		ZZZZZZZZ	Interpret Bytes 7 & 8 together as a length
			//	Byte08:		Sender Resource ID		ZZZZZZZZ	Interpret Bytes 7 & 8 together as a length
			//	Byte09:		Recipient Resource ID	ZZZZZZZZ	Interpret Bytes 9 & 10 together as a length
			//	Byte10:		Recipient Resource ID	ZZZZZZZZ	Interpret Bytes 9 & 10 together as a length					 

			//  The meaning of the final output message is defined based on single bit values
			//  For simplicity, assign values to the output message using an array of int values to represent each byte
			//  These arrays are converted into chunks, each one stored in an int value, which is 4 bytes in length
			//  Finally the chunks are converted into a byte array ready for use in the output message.

		    // Prepare buffers ...
		    int outputBitstreamLength = 10;
		    int bitstreamIntBoundaryLength = outputBitstreamLength;
		    bitstreamIntBoundaryLength -= (bitstreamIntBoundaryLength%4); 
		    bitstreamIntBoundaryLength += 4;		    
			byte[] bytesFinal = new byte[outputBitstreamLength];
			byte[] bytesIntBoundary = new byte[bitstreamIntBoundaryLength];
		    ByteBuffer buf = ByteBuffer.wrap(bytesIntBoundary);		    
		    		
			// Prepare values ...
			int[] byte01 = {0,0,0,0,0,0,0,1};	// SessionOpen:	Version 		001
			int[] byte02 = {1,0,0,0,0,0,0,0};	// SessionOpen:	Command			"Command packet"
			int[] byte03 = {0,0,0,0,0,0,0,0};	// SessionOpen:	Length1 		00000000 
			int[] byte04 = {0,0,0,0,1,0,1,0};	// SessionOpen:	Length2			00001010 = dec 10 = hex A 
			int[] byte05 = {0,0,0,0,0,1,0,0};	// SessionOpen:	Coding			ASCII
			int[] byte06 = {0,0,1,0,0,0,1,0};	// SessionOpen:	Protec/BFlag	00100010 = hex 22
			int[] byte07 = MATIPUtilities.convertResourceID(senderResourceID.substring(0,2));		// SessionOpen:	SenderResourceID 1st 2 chars from UDP
			int[] byte08 = MATIPUtilities.convertResourceID(senderResourceID.substring(2,4));		// SessionOpen:	SenderResourceID 2nd 2 chars from UDP
			int[] byte09 = MATIPUtilities.convertResourceID(recipientResourceID.substring(0,2));	// SessionOpen:	RecipientResourceID 1st 2 chars from UDP
			int[] byte10 = MATIPUtilities.convertResourceID(recipientResourceID.substring(2,4));	// SessionOpen:	RecipientResourceID 2nd 2 chars from UDP			
			int[] byte11 = {0,0,0,0,0,0,0,0};	// SessionOpen: Not used ... not included in bytesFinal 
			int[] byte12 = {0,0,0,0,0,0,0,0};	// SessionOpen: Not used ... not included in bytesFinal
			
			// Convert the byte arrays into 4-byte chunks 
		    int bytes1to4 = MATIPUtilities.inflateBits(byte01,byte02,byte03,byte04);
		    int bytes5to8 = MATIPUtilities.inflateBits(byte05,byte06,byte07,byte08);
		    int bytes9to12 = MATIPUtilities.inflateBits(byte09,byte10,byte11,byte12);
		    buf.putInt(0,bytes1to4);
		    buf.putInt(4,bytes5to8);
		    buf.putInt(8,bytes9to12);		    

		    //  Construct the output message ...
		    copyMessageHeaders(inMessage, outMessage);		    
		    MbElement outRoot = outMessage.getRootElement();
            MbElement outParser = outRoot.createElementAsLastChild(MbBLOB.PARSER_NAME);
		    System.arraycopy(buf.array(), 0, bytesFinal, 0, outputBitstreamLength);
            outParser.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "BLOB", bytesFinal);

            out.propagate(outAssembly);

		} finally {
			// clear the outMessage even if there's an exception
			outMessage.clearMessage();
		}
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		
		MbElement outRoot = outMessage.getRootElement();
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) 
		{
			outRoot.addAsLastChild(header.copy());
			header = header.getNextSibling();
		}
	}
			
}
