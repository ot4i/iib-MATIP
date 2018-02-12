package com.ibm.broker.matip;

import java.io.UnsupportedEncodingException;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class BATAP_Processing_Inbound_Construct_ACK extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {

			// Gather the SRLN for adding to acknowledgement message
			MbElement LocalEnvironment = inAssembly.getLocalEnvironment().getRootElement();						
   			try {
   				String StringOutputmessage = "IMA" + LocalEnvironment.getFirstElementByPath("BATAP/SRLN").getValue().toString();
   				byte [] bytesOutputmessage = StringOutputmessage.getBytes("utf-8");
   				//  Construct the output message ...
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
