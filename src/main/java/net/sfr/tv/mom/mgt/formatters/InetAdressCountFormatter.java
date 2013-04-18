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
package net.sfr.tv.mom.mgt.formatters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class InetAdressCountFormatter implements Formatter {
    
    @Override
    public String format(Object source) {
        
        if (!String[].class.isAssignableFrom(source.getClass())) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        
        //Map<String,Integer> countResult = new TreeMap<>(new StringValueComparator());
        Map<String,Integer> countResult = new HashMap<>();
        String ip;
        Integer ct;

        List<String> values = Arrays.asList((String[]) source);
        //Collections.sort(values);
        for (String value : values) {

            // EXTRACT IP FROM RAW STRING. FORMAT EX. = /10.211.226.37:48806
            ip = value.split("\\:")[0].substring(1);
            if (!countResult.containsKey(ip)) {
                countResult.put(ip, 1);
            } else {
                ct = countResult.get(ip);
                countResult.put(ip, ++ct);                    
            }
        }

        for (String key : countResult.keySet()) {
            sb.append("\t").append(key).append(" : ").append(String.valueOf(countResult.get(key))).append("\n");
        }
        
        return sb.toString();
    }
}
