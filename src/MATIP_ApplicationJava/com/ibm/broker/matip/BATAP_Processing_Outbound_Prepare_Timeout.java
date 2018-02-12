package com.ibm.broker.matip;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class BATAP_Processing_Outbound_Prepare_Timeout extends
		MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,
				outMessage);

		try {
			copyMessageHeaders(inMessage, outMessage);
			MbElement inRoot = inMessage.getRootElement();
            MbElement inParser = inRoot.getLastChild();
            //MbElement inBody = inParser.getFirstChild();
            //byte[] bytesInputmessage = inBody.toBitstream("","","",0,0,0);
            byte[] bytesInputmessage = inParser.toBitstream("","","",0,0,0);
            MbElement outRoot = outMessage.getRootElement();	           
			outRoot.createElementAsLastChildFromBitstream(bytesInputmessage, MbBLOB.PARSER_NAME,"","","",0,0,0);		        										
			// LocalEnvironment contains the TimeoutRequest instruction .. Format of the message should be like this:			
			//<TimeoutRequest>
			//  <Action>SET | CANCEL</Action>
			//  <Identifier>String (any alphanumeric string)</Identifier>
			//  <StartDate>String (TODAY | yyyy-mm-dd)</StartDate>
			//  <StartTime>String (NOW | hh:mm:ss)</StartTime>
			//  <Interval>Integer (seconds)</Interval>
			//  <Count>Integer (greater than 0 or -1)</Count>
			//  <IgnoreMissed>TRUE | FALSE</IgnoreMissed>
			//  <AllowOverwrite>TRUE | FALSE</AllowOverwrite>
			//</TimeoutRequest>
			MbElement outLocalEnvironment = outAssembly.getLocalEnvironment().getRootElement();		
			MbElement TimeoutRequest = outLocalEnvironment.createElementAsLastChild(MbElement.TYPE_NAME, "TimeoutRequest", null);
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"Action","SET");
			MbElement Environment = inAssembly.getGlobalEnvironment().getRootElement();
			String SRLNstring = Environment.getFirstElementByPath("SRLN").getValue().toString();			
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"Identifier",SRLNstring);			
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm:ss");
			System.out.println("The current time is: "+sdftime.format(cal.getTime()));
	        int interval = (Integer)getUserDefinedAttribute("IMAWaitTime");
	        cal.add(Calendar.SECOND,interval);
	        String startDate = sdfdate.format(cal.getTime());
	        String startTime = sdftime.format(cal.getTime());
			System.out.println("The time of first timeout is: "+sdftime.format(cal.getTime()));
	        TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"StartDate",startDate);			
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"StartTime",startTime);
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"Interval",interval);			
			// Set Count of repeats of Timeout to be 1 more than MaximumPDMs ... 
			// This 1 extra Timeout propagation is used to remove the Window entry once the max retries have been completed!
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"Count",((Integer)getUserDefinedAttribute("MaximumPDMs")+1));
			//TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"Count",1);
			TimeoutRequest.createElementAsLastChild(MbElement.TYPE_NAME,"AllowOverwrite","TRUE");			
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
