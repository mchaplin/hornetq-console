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
package net.sfr.tv.mom.mgt.model;

import java.text.Collator;
import java.util.Comparator;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class AttributeNameComparator implements Comparator<Attribute> {

    private Collator collator;
    
    public AttributeNameComparator() {
        collator = Collator.getInstance();
    }
    
    @Override
    public int compare(Attribute o1, Attribute o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
    
    
}
