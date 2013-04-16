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
package net.sfr.tv.mom.mgt.formatters;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class ByteFormatter implements Formatter {

    public static final MathContext MATH_CTX = MathContext.DECIMAL64;
    
    @Override
    public String format(Object source) {
        
        BigDecimal bd = new BigDecimal((Long) source, MATH_CTX);
        bd = bd.divide(new BigDecimal(1024)).divide(new BigDecimal(1024));
        
        return bd.toString().concat("MB");
    }
}
