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

import net.sfr.tv.mom.mgt.CommandRouter.Command;
import net.sfr.tv.mom.mgt.CommandRouter.Option;

/**
 * Describe a command, it's options and associated handler(s)
 * 
 * @author matthieu.chaplin@sfr.com
 */
@Deprecated
public class CommandBinding {

    private Command cmd;
    private Option opt;
    private CommandHandler handler;
    
    public CommandBinding(Command cmd, Option opt, CommandHandler handler) {
        this.cmd = cmd;
        this.opt = opt;
        this.handler = handler;
    }
    
}
