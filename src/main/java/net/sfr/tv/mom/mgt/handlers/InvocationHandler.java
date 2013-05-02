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

import net.sfr.tv.mom.mgt.formatters.Formatter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
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
        
        Object result = null;
        
        if (this.expression.indexOf("{") != -1) {
            this.expression = renderExpression(new Object[]{"\"".concat(args[0].toString()).concat("\"")});
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        
        try {
            Set<ObjectName> oNames = connection.queryNames(new ObjectName(expression), null);
            if (oNames != null && !oNames.isEmpty()) {
                result = connection.invoke(oNames.iterator().next(), operation.getName(), args, operation.getSignature());
            }
            //result = connection.invoke(new ObjectName(expression), operation.getName(), new Object[operation.getSignature().length], operation.getSignature());
        } catch (MBeanException | IllegalArgumentException | InstanceNotFoundException | MalformedObjectNameException | ReflectionException | IOException ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return formatter.format(result);
    }
    
    public String printSignature() {
        
        if (operation.getSignature().length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(" (");
        for (String arg : operation.getSignature()) {
            sb.append(arg);
        }
        sb.append(")");
        return sb.toString();
    }
}
