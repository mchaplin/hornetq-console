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

import net.sfr.tv.mom.mgt.formatters.Formatter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import net.sfr.tv.mom.mgt.CommandHandler;
import net.sfr.tv.mom.mgt.model.Operation;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class InvocationHandler extends CommandHandler {
    
    private Operation operation;
    
    private Formatter formatter;

    public InvocationHandler(String expression, Operation operation, Formatter formatter, String help) {
        this.expression = expression;
        this.operation = operation;
        this.formatter = formatter;
        this.help = help;
    }

    @Override
    public Object execute(MBeanServerConnection connection, Object[] args) {
        
        Object result;
        
        //expression = renderExpression(args);
        
        try {
            //result = connection.invoke(new ObjectName(expression), operation.getName(), new Object[operation.getSignature().length], operation.getSignature());
            result = connection.invoke(new ObjectName(expression), operation.getName(), args, operation.getSignature());
        } catch (MBeanException | InstanceNotFoundException | MalformedObjectNameException | ReflectionException | IOException ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return formatter.format(result);
    }
}
