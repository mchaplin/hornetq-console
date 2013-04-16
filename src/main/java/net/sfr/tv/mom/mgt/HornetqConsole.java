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
import net.sfr.tv.mom.mgt.CommandRouter.Command;
import net.sfr.tv.mom.mgt.CommandRouter.Option;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HornetqConsole {

    private static final Logger LOGGER = Logger.getLogger(HornetqConsole.class.getName());

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
                LOGGER.info("hqconsole.jar -h [JMX host] -p [JMX port]\n");
                System.exit(1);
            }

            StringBuilder jmxServiceUrl = new StringBuilder();
            jmxServiceUrl.append("service:jmx:rmi://").append(jmxHost).append(":").append(jmxPort).append("/jndi/rmi://").append(jmxHost).append(":").append(jmxPort).append("/jmxrmi");

            JMXServiceURL url = new JMXServiceURL(jmxServiceUrl.toString());
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            System.out.println("\nSuccessfully connected to JMX service URL : ".concat(jmxServiceUrl.toString()));
            
            CommandRouter router = new CommandRouter();

            // PRINT SERVER STATUS REPORT
            System.out.print((String) router.get(Command.STATUS, Option.VM).execute(mbsc, null));
            System.out.print((String) router.get(Command.STATUS, Option.SERVER).execute(mbsc, null));
            System.out.print((String) router.get(Command.STATUS, Option.CLUSTER).execute(mbsc, null));
            
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
                    System.out.print("Syntax error !\n");
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
                        System.out.println("Available commands : ");
                        for (Command cmde : Command.values()) {
                            options = router.get(cmde);
                            if (options == null) {
                                continue;
                            }
                            for (Option opt : options) {
                                System.out.println("\t".concat(cmde.name()).toLowerCase().concat(" ").concat(opt.name().toLowerCase()).concat(" : ").concat(router.get(cmde, opt).getHelpMessage()));
                            }
                        }
                        break;
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        echo("\nBye!");

    }

    private static void echo(String msg) {
        System.out.println(msg);
    }
}
