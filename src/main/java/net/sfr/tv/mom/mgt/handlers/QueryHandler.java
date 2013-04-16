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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import net.sfr.tv.mom.mgt.CommandHandler;
import net.sfr.tv.mom.mgt.model.Attribute.Type;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class QueryHandler extends CommandHandler {
    
    private Queue<net.sfr.tv.mom.mgt.model.Attribute> attributes;

    public QueryHandler(String expression, Queue<net.sfr.tv.mom.mgt.model.Attribute> attributes, String help) {
        this(null, expression, attributes, help);
    }
    
    public QueryHandler(String title, String expression, Queue<net.sfr.tv.mom.mgt.model.Attribute> attributes, String help) {
        this.title = title;
        this.expression = expression;
        this.attributes = attributes;
        this.help = help;
    }
   
    public QueryHandler(String expression, String[] attributes, String help) {
        this(null, expression, attributes, help);
    }
    
    public QueryHandler(String title, String expression, String[] attributes, String help) {
        this.title = title;
        this.expression = expression;
        this.help = help;
        
        Queue<net.sfr.tv.mom.mgt.model.Attribute> attrs = new ArrayDeque<>();
        for (String att : attributes) {
            attrs.add(new net.sfr.tv.mom.mgt.model.Attribute(att, Type.String));
        }
        this.attributes = attrs;
        
    }

    @Override
    public Object execute(MBeanServerConnection connection, Object[] args) {
        
        Set<ObjectName> result;
        List<Attribute> jmxAttributes;
        
        // Surround argument value with quotes : \"<value>\"
        String[] sArgsQuoted = null;
        if (args != null) {
            String[] sArgs = (String[]) args;
            sArgsQuoted = new String[sArgs.length];
            int i=0;
            for (String arg : sArgs) {
                sArgsQuoted[i++] = "\"".concat(arg).concat("\"");
            }   
        }
        
        String parsedExpr = renderExpression(sArgsQuoted);
        
        StringBuilder output = getOutput();
        
        try {
            result = connection.queryNames(new ObjectName(parsedExpr), null);
            
            Attribute att;
            for (ObjectName name : result) {
                //System.out.println("\tObjectName = " + name);
                jmxAttributes = connection.getAttributes(name, getAttributes()).asList();
                
                for (net.sfr.tv.mom.mgt.model.Attribute atr : attributes) {
                    
                    att = fetch(atr, jmxAttributes);
                    if (att == null || att.getValue() == null) {
                        continue;
                    }
                    
                    if (att.getValue() instanceof String[]) {
                        List<String> values = Arrays.asList((String[]) att.getValue());
                        if (values.size() == 1) {
                            // DISPLAY BOTH ATTR. NAME & VALUE OVER THE SAME LINE
                            output.append("\t").append(att.getName()).append(" : ").append(values.get(0)).append("\n");
                        } else {
                            // FORMAT MULTIPLE VALUES
                            output.append("\t").append(att.getName()).append(" : ").append("\n");
                            Collections.sort(values);
                            for (String value : values) {
                                output.append("\t\t".concat(value)).append("\n");
                            }
                        }
                    } else if (CompositeDataSupport.class.isAssignableFrom(att.getValue().getClass())) {
                        
                        CompositeDataSupport cds = (CompositeDataSupport) att.getValue();
                        
                        Set<String> keys = cds.getCompositeType().keySet();
                        output.append("\t".concat(att.getName())).append("\n");
                        for (String k : keys) {
                            // TODO : Depuis atr (Attribute)#Type, déduire & invoker le formatter
                            //output.append("\t\t".concat(k).concat(" : ").concat(String.valueOf(new ByteFormatter().format(cds.get(k))))).append("\n");
                            output.append("\t\t").append(k).append(" : ").append(atr.format(cds.get(k))).append("\n");
                        }
                        
                    } else {
                        output.append("\t").append(att.getName()).append(" : ").append(att.getValue()).append("\n");
                    }
                }
                output.append("\n");
            }
            
        } catch (InstanceNotFoundException | MalformedObjectNameException | ReflectionException | IOException ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return output.toString();
    }
    
    private String[] getAttributes() {
        String[] ret = new String[attributes.size()];
        int i=0;
        for(net.sfr.tv.mom.mgt.model.Attribute a : attributes) {
            ret[i++] = a.getName();
        }
        return ret;
    }
    
    private Attribute fetch(net.sfr.tv.mom.mgt.model.Attribute attr, List<Attribute> list) {
        for (Attribute l : list) {
            if (l.getName().equals(attr.getName())) {
                return l;
            }
        }
        return null;
    }
}
