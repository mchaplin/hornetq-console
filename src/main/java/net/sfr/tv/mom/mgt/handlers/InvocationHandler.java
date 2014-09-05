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

import net.sfr.tv.mom.mgt.CommandHandler;
import net.sfr.tv.mom.mgt.formatters.Formatter;
import net.sfr.tv.mom.mgt.model.Operation;
import org.apache.commons.lang3.StringUtils;

import javax.management.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class InvocationHandler extends CommandHandler {

    private static final Logger LOGGER = Logger.getLogger(InvocationHandler.class.getName());

    private final Operation operation;
    
    private final Formatter formatter;

    public InvocationHandler(final String expression, final Operation operation, final Formatter formatter,
                             final String help) {
        if (StringUtils.isEmpty(expression)) throw new IllegalArgumentException("Expression must be not null or empty");
        if (null == operation) throw new IllegalArgumentException("Operation must be not null");
        if (null == formatter) throw new IllegalArgumentException("Formatter must be not null");
        this.expression = expression;
        this.operation = operation;
        this.formatter = formatter;
        this.help = help;
    }

    @Override
    public Object execute(MBeanServerConnection connection, Object[] args) {
        if (this.expression.indexOf("{") != -1) {
            this.expression = renderExpression(new Object[]{"\"".concat(args[0].toString()).concat("\"")});
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        
        try {
            final Set<ObjectName> oNames = connection.queryNames(new ObjectName(expression), null);
            if (oNames == null || oNames.isEmpty()) {
                LOGGER.severe("No object names returns for expression '"+ expression +"'");
                return null;
            }
            else {
                final Object result = connection.invoke(oNames.iterator().next(), operation.getName(),
                        args, operation.getSignature());
                if (result == null) {
                    LOGGER.warning("Result of operation '" + operation.getName() + "'is null");
                    return result;
                }
                return formatter.format(result);
            }
            //result = connection.invoke(new ObjectName(expression), operation.getName(), new Object[operation.getSignature().length], operation.getSignature());
        }
        catch (MBeanException | IllegalArgumentException | InstanceNotFoundException | MalformedObjectNameException |
                ReflectionException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String printSignature() {
        
        if (operation.getSignature().length == 0) {
            return "";
        }
        
        final StringBuilder sb = new StringBuilder(" (");
        for (String arg : operation.getSignature()) {
            sb.append(arg);
        }
        sb.append(")");
        return sb.toString();
    }
}
