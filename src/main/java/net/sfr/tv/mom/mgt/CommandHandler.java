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
package net.sfr.tv.mom.mgt;

import java.text.MessageFormat;
import javax.management.MBeanServerConnection;
import net.sfr.tv.contributed.Ansi;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public abstract class CommandHandler {

    protected String title;
    
    protected String expression;
    
    protected String help;
    
    public abstract Object execute(MBeanServerConnection connection, Object[] args);
    
    public String getHelpMessage() {
        return help != null ? help : "";
    }
    
    protected StringBuilder getOutput() {
        StringBuilder output = new StringBuilder();
        if (title != null) {
            output.append("\n").append(Ansi.format("# ".concat(title), Ansi.Color.GREEN)).append(" : \n");
        }
        return output;
    }
    
    protected String renderExpression(Object[] args) {
        
        String result;
        
        if (expression.indexOf("{") != -1) {
            if (args == null) {
                args = new String[] {"*"};
            }
            result = MessageFormat.format(expression, args);
        } else {
            result = expression;
        }
        
        return result;
    }
}
