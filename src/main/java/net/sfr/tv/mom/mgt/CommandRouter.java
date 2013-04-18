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

import net.sfr.tv.mom.mgt.model.Operation;
import net.sfr.tv.mom.mgt.handlers.QueryHandler;
import net.sfr.tv.mom.mgt.handlers.InvocationHandler;
import net.sfr.tv.mom.mgt.formatters.InetAdressCountFormatter;
import net.sfr.tv.mom.mgt.formatters.DefaultFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sfr.tv.mom.mgt.handlers.VmStatusHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class CommandRouter {
 
    private Map<Command,Map<Option,CommandHandler>> bindings;
    
    public enum Command {
        HELP("help"),
        STATUS("status"),
        LIST("list"),
        DROP("drop"),
        FORK("\\!");
        
        private String value;
        
        private Command(String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {return this.value;}
        
        public static Command fromString(String string) {
            for (Command value : values()) {
                if (value.toString().equals(string)) {
                    return value;
                }
            }

            return null;
        }
    }
    
    public enum Option {
        VM("vm"),
        SERVER("server"),
        TOPIC("topic"),
        QUEUE("queue"),
        CONNS("conns"),
        CLUSTER("cluster"),
        JMS("jms"),
        SUBSCRIPTION("subscription"),
        MESSAGES("messages");
        
        private String value;
        
        private Option(String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {return this.value;}
    }
    
    public CommandRouter() {
        
        bindings = new HashMap<Command,Map<Option,CommandHandler>>();
        
        Map<Option,CommandHandler> statusBindings = new HashMap<>();
        statusBindings.put(Option.VM, new VmStatusHandler());
        statusBindings.put(Option.SERVER, new QueryHandler(
                "Server Status Report",
                "org.hornetq:module=Core,type=Server", 
                new String[] {"Version", "ConnectionCount", "QueueNames", "DivertNames"},
                "Query server instance status"));
        statusBindings.put(Option.CLUSTER, new QueryHandler(
                "Cluster Status Report",
                "org.hornetq:module=Core,type=ClusterConnection,name=*", 
                new String[] {"Name", "Address", "NodeID", "Nodes", "MaxHops"},
                "Query cluster(s) membership status"));
        statusBindings.put(Option.QUEUE, new QueryHandler(
                "Queue Status Report",
                "org.hornetq:module=Core,type=Queue,address=*,name={0}", 
                new String[] {"Address","ConsumerCount","DeadLetterAddress","DeliveringCount","Durable","ExpiryAddress","Filter","ID","MessageCount","MessagesAdded","Name","Paused"},
                "Query physical queues status"));
        
        bindings.put(Command.STATUS, statusBindings);
        
        
        Map<Option,CommandHandler> listBindings = new HashMap<>();
        listBindings.put(Option.CONNS, new InvocationHandler(
                "org.hornetq:module=Core,type=Server", 
                new Operation("listRemoteAddresses", new String[0]), 
                new InetAdressCountFormatter(),
                "List connections to server instance by source IPs"));
        
        listBindings.put(Option.JMS, new QueryHandler(
                "org.hornetq:module=JMS,type={0},name=*", 
                new String[] {"JNDIBindings", "Address", "DurableMessageCount", "DurableSubscriptionCount", "MessageCount", "MessageAdded"},
                "List JNDI bindings"));
        
        listBindings.put(Option.QUEUE, new QueryHandler(
                "org.hornetq:module=Core,type=Queue,address={0},name=*", 
                new String[] {"Address","Name"},
                "List existing core queues"));
        
        bindings.put(Command.LIST, listBindings);
        
        Map<Option,CommandHandler> dropBindings = new HashMap<>();
        //dropBindings.put(Option.SUBSCRIPTION, new SubscriptionHandler());
        dropBindings.put(Option.CONNS, new InvocationHandler(
                "org.hornetq:module=Core,type=Server", 
                new Operation("closeConnectionsForAddress", new String[] {"java.lang.String"}), 
                new DefaultFormatter(),
                "Drop a client connection"));
        dropBindings.put(Option.QUEUE, new InvocationHandler(
                "org.hornetq:module=Core,type=Server", 
                new Operation("destroyQueue", new String[] {"java.lang.String"}), 
                new DefaultFormatter(),
                "Drops a core queue"));
        dropBindings.put(Option.MESSAGES, new InvocationHandler(
                "{0}", 
                new Operation("removeMessages", new String[] {"java.lang.String"}), 
                new DefaultFormatter(),
                "Drop messages from a core queue"));
        
        bindings.put(Command.DROP, dropBindings);
        
    }
    
    public Set<Option> get(Command cmd) {
        return bindings.get(cmd) != null ? bindings.get(cmd).keySet() : null;
    }
    
    public CommandHandler get(Command cmd, Option opt) {
        return bindings.get(cmd).get(opt);
    }
    
}
