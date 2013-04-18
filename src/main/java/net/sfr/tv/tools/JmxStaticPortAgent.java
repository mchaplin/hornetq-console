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
package net.sfr.tv.tools;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

/**
 * @author matthieu.chaplin@sfr.com
 * 
 * Original credits goes to Daniel Fuchs
 * https://blogs.oracle.com/jmxetc/entry/java_5_premain_rmi_connectors
 */
public class JmxStaticPortAgent {
    
    private static RMIServerSocketFactory serverSocketFactory;
    private static RMIClientSocketFactory clientSocketFactory;
    
    private static InetAddress serverAddress;
    private static ServerSocket serverSocket;

    private JmxStaticPortAgent() {
    }
    
    public static void main(String[] args) throws Exception {
        startStaticPortJmxServer();
    }

    public static void premain(String agentArgs) throws IOException {
        startStaticPortJmxServer();
    }
    
    private static void startStaticPortJmxServer() throws RemoteException, IOException, UnknownHostException, MalformedURLException {
        
        String host = System.getProperty("static.jmx.agent.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("static.jmx.agent.port", "6002"));
        
        String[] bytes = host.split("\\.");
        Integer i;
        int idx = 0;
        byte[] ip = new byte[4];
        for (String v : bytes) {
            i = new Integer(v);
            i = i > 127 ? i - 256 : i;
            ip[idx++] = i.byteValue();
        }
        
        serverAddress = InetAddress.getByAddress(ip);
        //serverAddress = InetAddress.getByAddress(new byte[] {127,0,0,1});
        
        serverSocket = new ServerSocket(port, 0, serverAddress);
        serverSocketFactory = new SingleAddressRMIServerSocketFactory(serverSocket);
        clientSocketFactory = new SingleAddressRMIClientSocketFactory(host);
        //clientSocketFactory = new RMISocketFactory();
        
        // Ensure cryptographically strong random number generator used to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");
        //System.setProperty("java.rmi.server.hostname", "127.0.0.1");

        // Start an RMI registry on port specified by example.rmi.agent.port (default 6002).
        System.out.println("Creating an RMI registry on port " + port);
        LocateRegistry.createRegistry(port, clientSocketFactory, serverSocketFactory);

        // Retrieve the PlatformMBeanServer.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // This where we would enable security - left out of this for the sake of the example....

        // Create an RMI connector server.
        //
        // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the serverAddress host on
        // port 3000 with the name "jmxrmi". This is the same name the
        // out-of-the-box management agent uses to register the RMIServer
        // stub too.
        //
        // The port specified in "service:jmx:rmi://"+hostname+":"+port
        // is the second port, where RMI connection objects will be exported.
        // Here we use the same port as that we choose for the RMI registry. 
        // The port for the RMI registry is specified in the second part
        // of the URL, in "rmi://"+hostname+":"+port
        //

        String jmxServiceUrl = 
                "service:jmx:rmi://"
                .concat(host).concat(":").concat(String.valueOf(port))
                .concat("/jndi/rmi://").concat(host).concat(":").concat(String.valueOf(port))
                .concat("/jmxrmi");
        
        JMXServiceURL url = new JMXServiceURL(jmxServiceUrl);
        
        System.out.println("Creating an RMI connector server, bound to : ".concat(jmxServiceUrl));
        
        // Environment map.
        HashMap<String, Object> env = new HashMap<String, Object>();
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverSocketFactory);
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientSocketFactory);
        
        // Now create the server from the JMXServiceURL
        //
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

        // Start the RMI connector server.
        System.out.println("Starting the RMI connector server...");
        cs.start();
        
    }

    public static class SingleAddressRMIServerSocketFactory implements RMIServerSocketFactory {

        private final ServerSocket socket;

        public SingleAddressRMIServerSocketFactory(ServerSocket socket) {
            this.socket = socket;
        }

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            //System.out.println("createServerSocket(" + port + ") called");
            return this.socket;
        }
    }
    
    public static class SingleAddressRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {

        private final String host;
        
        public SingleAddressRMIClientSocketFactory(String host) {
            this.host = host;
        }       
        
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            // Here is the trick, override hostname of RMI Stubs with our custom one.
            return new Socket(this.host, port);
        }
    }
}
