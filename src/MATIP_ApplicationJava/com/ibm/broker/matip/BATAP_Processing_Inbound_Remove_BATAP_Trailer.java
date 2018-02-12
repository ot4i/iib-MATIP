package com.ibm.broker.matip;

import java.io.UnsupportedEncodingException;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class BATAP_Processing_Inbound_Remove_BATAP_Trailer extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);		
		try {
			
			MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
			byte[] bytesInputmessage = inParser.toBitstream("","","",0,0,0);
   			try {
   				String StringInputmessage = new String(bytesInputmessage,"utf-8");   				
   				int LengthStringInputmessage = StringInputmessage.length();
   				String StringOutputmessage = StringInputmessage.substring(0, LengthStringInputmessage-4);   			
   				// Record the Trailer, SRLN, Duplicate marker etc. in the BATAP section of LocalEnvironment tree
   				MbElement LocalEnvironment = inAssembly.getLocalEnvironment().getRootElement();		
   				MbElement BATAP = LocalEnvironment.createElementAsLastChild(MbElement.TYPE_NAME,"BATAP",null);
   				BATAP.createElementAsLastChild(MbElement.TYPE_NAME,"Trailer",StringInputmessage.substring(LengthStringInputmessage-4,LengthStringInputmessage));
   				BATAP.createElementAsLastChild(MbElement.TYPE_NAME,"SRLN",StringInputmessage.substring(LengthStringInputmessage-3,LengthStringInputmessage));
   				if (StringInputmessage.substring(LengthStringInputmessage-4,LengthStringInputmessage-3).equals(" ")) {
   					BATAP.createElementAsLastChild(MbElement.TYPE_NAME,"NormalOrPDM","Normal");	
   				}	
   				if (StringInputmessage.substring(LengthStringInputmessage-4,LengthStringInputmessage-3).equals("P")) {
   					BATAP.createElementAsLastChild(MbElement.TYPE_NAME,"NormalOrPDM","PDM");	
   				}						
   				//  Construct the output message ...
   				byte [] bytesOutputmessage = StringOutputmessage.getBytes("utf-8");
   				copyMessageHeaders(inMessage, outMessage);		    
   				MbElement outRoot = outMessage.getRootElement();
 				outRoot.createElementAsLastChildFromBitstream(bytesOutputmessage, MbBLOB.PARSER_NAME,"","","",0,0,0);		        										
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
