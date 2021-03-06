/*
* Copyright © 2018. TIBCO Software Inc.
* This file is subject to the license terms contained
* in the license file that is distributed with this file.
 */
package general.activity.log;

import com.tibco.dovetail.core.runtime.engine.Context;
import com.tibco.dovetail.core.runtime.activity.IActivity;
import com.tibco.dovetail.core.runtime.services.IContainerService;
import com.tibco.dovetail.core.runtime.services.ILogService;

public class logger implements IActivity {

    public void eval(Context context) throws IllegalArgumentException{
        String msg = context.getInput("message").toString();
        
        Object ctnr = context.getInput("containerServiceStub");
		if (ctnr == null)
			ctnr = context.getContainerService();
		
		if(ctnr == null)
			throw new IllegalArgumentException("containerServicesStub is not mapped");
		
        ILogService logger = ((IContainerService)ctnr).getLogService();
        	logger.info(msg);
    }
}
