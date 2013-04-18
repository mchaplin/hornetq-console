/**
 * Copyright 2012,2013 - SFR (http://www.sfr.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sfr.tv.mom.mgt.handlers;

import java.util.ArrayDeque;
import java.util.Queue;
import javax.management.MBeanServerConnection;
import net.sfr.tv.mom.mgt.CommandHandler;
import net.sfr.tv.mom.mgt.model.Attribute;
import net.sfr.tv.mom.mgt.model.Attribute.Type;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class VmStatusHandler extends CommandHandler {

    public VmStatusHandler() {
        this.title = "VM Status Report";
        this.help = "Query VM status";
    }
    
    @Override
    public Object execute(MBeanServerConnection connection, Object[] args) {
        
        StringBuilder output = getOutput();
        
        QueryHandler query = new QueryHandler("java.lang:type=Runtime", new String[] {"Name", "Uptime"}, null);
        output.append((String) query.execute(connection, null));
        
        Queue<Attribute> attrs = new ArrayDeque<>();
        attrs.add(new Attribute("HeapMemoryUsage", Type.Byte));
        query = new QueryHandler("java.lang:type=Memory", attrs, null);
        output.append((String) query.execute(connection, null));
        
        query = new QueryHandler("java.lang:type=Threading", new String[] {"ThreadCount", "PeakThreadCount"}, null);
        output.append((String) query.execute(connection, null));
        
        query = new QueryHandler("java.lang:type=OperatingSystem", new String[] {"OpenFileDescriptorCount", "MaxFileDescriptorCount", "SystemLoadAverage", "SystemCpuLoad", "ProcessCpuLoad"}, null);
        output.append((String) query.execute(connection, null));
        
        return output.toString();
    }
}
