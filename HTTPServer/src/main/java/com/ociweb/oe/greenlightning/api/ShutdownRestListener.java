package com.ociweb.oe.greenlightning.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.HTTPRequestReader;
import com.ociweb.gl.api.HTTPResponseService;
import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.gl.api.RestListener;

public class ShutdownRestListener implements RestListener{

	private HTTPResponseService responseService;
	private GreenCommandChannel newCommandChannel;
	private final long keyFieldId;
	private final byte[] pass = "shutdown".getBytes();
	private static final Logger logger = LoggerFactory.getLogger(ShutdownRestListener.class);
	
	public ShutdownRestListener(GreenRuntime runtime, long keyFieldId) {
		this.newCommandChannel = runtime.newCommandChannel();
		this.responseService = newCommandChannel.newHTTPResponseService();		
		this.keyFieldId = keyFieldId;
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.structured().isEqual(keyFieldId, pass)) {
			
			if (!responseService.hasRoomFor(2)) {//reponse then shutdown
				return false;
			}
			
			if (responseService.publishHTTPResponse(request, 200)) {		
				
				while (!responseService.shutdown()){
					logger.error("Checked for room yet the shutdown was blocked...");
				}
				
				return true;
			} 
			return false;
		} else {
			if (responseService.publishHTTPResponse(request, 404)) {	
				return true;
			} 
			return false;
		}
	}

}
