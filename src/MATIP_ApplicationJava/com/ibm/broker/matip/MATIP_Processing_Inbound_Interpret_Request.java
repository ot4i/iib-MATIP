package com.ibm.broker.matip;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class MATIP_Processing_Inbound_Interpret_Request extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,outMessage);

		try {
			copyMessageHeaders(inMessage, outMessage);
			outMessage = inMessage;			
			// Input command messages look something like this:
			// <MATIP><Command>SSC</Command><Cause>00000000</Cause></MATIP>
			MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
            MbElement inBody = inParser.getFirstChild();
			MbElement LocalEnvironment = inAssembly.getLocalEnvironment().getRootElement();		
			MbElement Command = inBody.getFirstElementByPath("Command");				
			MbElement DestinationData = LocalEnvironment.createElementAsLastChild("Destination").createElementAfter("RouterList").createElementAfter("DestinationData");
			DestinationData.createElementAsLastChild(MbElement.TYPE_NAME,"label",Command.getValue().toString());								
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
