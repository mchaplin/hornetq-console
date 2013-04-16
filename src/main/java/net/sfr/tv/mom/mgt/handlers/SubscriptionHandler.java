/**
 * Copyright 2012,2013 - Société Française de Radiotéléphonie (http://www.sfr.com/)
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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import net.sfr.tv.mom.mgt.CommandHandler;

/**
 * Handler class for subscription related commands.
 * 
 * @author matthieu.chaplin@sfr.com
 */
public class SubscriptionHandler extends CommandHandler {

    public SubscriptionHandler() {
        this.help = "Drop a client subscription";
    }

    @Override
    public Object execute(MBeanServerConnection connection, Object[] args) {
        
        ObjectInstance result;
        String[] sArgs = (String[]) args;
        
        try {
            result = connection.getObjectInstance(new ObjectName(sArgs[0]));
            System.out.println(result.getObjectName() + " : " + result.getClassName());
            
            connection.invoke(new ObjectName(sArgs[0]), "dropDurableSubscription", new Object[] {sArgs[1], sArgs[2]}, new String[] {String.class.getName(), String.class.getName()});
            
            
        } catch (Exception ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        return null;
    }
}
