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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import net.sfr.tv.contributed.Ansi;
import net.sfr.tv.contributed.Ansi.Color;
import net.sfr.tv.mom.mgt.CommandRouter.Command;
import net.sfr.tv.mom.mgt.CommandRouter.Option;
import net.sfr.tv.mom.mgt.handlers.InvocationHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HornetqConsole {

    private static final Logger LOGGER = Logger.getLogger(HornetqConsole.class.getName());
    
    private static final String VERSION = "0.6.1";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            String jmxHost = "127.0.0.1";
            String jmxPort = "6001";

            // Process command line arguments
            String arg;
            for (int i = 0; i < args.length; i++) {
                arg = args[i];
                switch (arg) {
                    case "-h":
                        jmxHost = args[++i];
                        break;
                    case "-p":
                        jmxPort = args[++i];
                        break;
                    default:
                        break;
                }
            }

            // Check for arguments consistency
            if (jmxHost == null || jmxHost.trim().equals("") || jmxPort == null || jmxPort.equals("")) {
                LOGGER.info("Usage : ");
                LOGGER.info("hq-console.jar <-h host(127.0.0.1)> <-p port(6001)>\n");
                System.exit(1);
            }

            System.out.println("\n".concat(Ansi.format("HornetQ Console ".concat(VERSION), Color.CYAN)));
            
            StringBuilder jmxServiceUrl = new StringBuilder();
            jmxServiceUrl.append("service:jmx:rmi://").append(jmxHost).append(":").append(jmxPort).append("/jndi/rmi://").append(jmxHost).append(":").append(jmxPort).append("/jmxrmi");

            JMXServiceURL url = new JMXServiceURL(jmxServiceUrl.toString());
            JMXConnector jmxc = null;
            
            CommandRouter router = new CommandRouter();
            
            try {
                jmxc = JMXConnectorFactory.connect(url, null);
                assert jmxc != null; // jmxc must be not null
            }
            catch (Throwable t) {
                System.out.println("\n".concat(Ansi.format("Unable to connect to JMX service URL : ".concat(jmxServiceUrl.toString()), Color.RED)));
                System.out.println("\n".concat(Ansi.format("Did you set the com.sun.management.jmxremote.port option ?", Color.CYAN)));
                printHelp(router);
            }

            System.out.println("\n".concat(Ansi.format("Successfully connected to JMX service URL : ".concat(jmxServiceUrl.toString()), Color.YELLOW)));
            
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            // PRINT SERVER STATUS REPORT
            System.out.print((String) router.get(Command.STATUS, Option.VM).execute(mbsc, null));
            System.out.print((String) router.get(Command.STATUS, Option.SERVER).execute(mbsc, null));
            System.out.print((String) router.get(Command.STATUS, Option.CLUSTER).execute(mbsc, null));
            
            printHelp(router);
            
            // START COMMAND LINE
            Scanner scanner = new Scanner(System.in);
            System.out.print("> ");
            String input;
            while (!(input = scanner.nextLine().concat(" ")).equals("exit ")) {

                String[] cliArgs = input.split("\\ ");
                CommandHandler handler;

                if (cliArgs.length < 1) {
                    System.out.print("> ");
                    continue;
                }
                
                Command cmd = Command.fromString(cliArgs[0]);
                if (cmd == null) {
                    System.out.print(Ansi.format("Syntax error !", Color.RED).concat("\n"));
                    cmd = Command.HELP;
                }
                
                switch (cmd) {
                    case STATUS :
                    case LIST :    
                    case DROP :

                        Set<Option> options = router.get(cmd);
                        
                        for (Option opt : options) {

                            if (cliArgs[1].equals(opt.toString())) {
                                handler = router.get(cmd, opt);

                                String[] cmdOpts = null;
                                if (cliArgs.length > 2) {
                                    cmdOpts = new String[cliArgs.length - 2];
                                    for (int i = 0; i < cmdOpts.length; i++) {
                                        cmdOpts[i] = cliArgs[2 + i];
                                    }
                                }

                                Object result = handler.execute(mbsc, cmdOpts);
                                if (result != null && String.class.isAssignableFrom(result.getClass())) {
                                    System.out.print((String) result);
                                }
                                System.out.print("> ");
                            }
                        }

                        break;

                    case FORK :
                        // EXECUTE SYSTEM COMMAND
                        ProcessBuilder pb = new ProcessBuilder(Arrays.copyOfRange(cliArgs, 1, cliArgs.length));
                        pb.inheritIO();
                        pb.start();
                        break;

                    case HELP :
                        printHelp(router);
                        break;
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        echo("\n".concat(Ansi.format("Bye!", Color.CYAN)));

    }

    private static void echo(String msg) {
        System.out.println(msg);
    }

    private static void printHelp(CommandRouter router) {
        
        Set<Option> options;
        System.out.println(Ansi.format("Available commands : ", Color.YELLOW));
        for (Command cmde : Command.values()) {
            options = router.get(cmde);
            if (options == null) {
                continue;
            }
            for (Option opt : options) {
                System.out.println("\t"
                        .concat(cmde.name()).toLowerCase()
                        .concat(" ").concat(opt.name().toLowerCase())
                        .concat(InvocationHandler.class.isAssignableFrom(router.get(cmde, opt).getClass()) ? ((InvocationHandler) router.get(cmde, opt)).printSignature() : "")
                        .concat(" : ")
                        .concat(router.get(cmde, opt).getHelpMessage()));
            }
        }
    }
}
