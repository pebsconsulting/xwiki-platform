/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details, published at
 * http://www.gnu.org/copyleft/gpl.html or in gpl.txt in the
 * root folder of this distribution.

 * Created by
 * User: Ludovic Dubost
 * Date: 23 janv. 2004
 * Time: 18:34:56
 */
package com.xpn.xwiki.notify;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class DocObjectChangedRule extends DocChangeRule {
    private String className;

    public DocObjectChangedRule() {
    }

    public DocObjectChangedRule(XWikiDocChangeNotificationInterface target) {
        setTarget(target);
    }

    public DocObjectChangedRule(XWikiDocChangeNotificationInterface target,String classname) {
        setTarget(target);
        setClassName(classname);
    }

    public boolean hasEqualsObjectsFromClass(XWikiDocument newdoc, XWikiDocument olddoc, String classname) {
        Vector vobj1 = newdoc.getObjects(classname);
        Vector vobj2 = olddoc.getObjects(classname);
        if ((vobj1==null)&&(vobj2==null))
            return true;
        if ((vobj1==null)||(vobj2==null))
            return false;
        if (vobj1.size()!=vobj2.size())
            return false;
        for (int i=0;i<vobj1.size();i++) {
            if ((vobj1.get(i)==null)&&(vobj2.get(i)!=null))
                return false;
            if (!vobj1.get(i).equals(vobj2.get(i)))
                return false;
        }
        return true;
    }

    public boolean hasEqualsObjectsForAllClasses(XWikiDocument newdoc, XWikiDocument olddoc) {
        Map fields1 = newdoc.getxWikiObjects();
        Map fields2 = olddoc.getxWikiObjects();
        if ((fields1==null)&&(fields2==null))
            return true;
        if ((fields1==null)||(fields2==null))
            return false;
        if (fields1.size()!=fields2.size())
            return false;
        for (Iterator it=fields1.keySet().iterator();it.hasNext();) {
            if (!hasEqualsObjectsFromClass(newdoc, olddoc, (String)it.next()))
                return false;
        }
        return true;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean hasEqualObjects(XWikiDocument newdoc, XWikiDocument olddoc, String className) {
        if (className==null)
            return hasEqualsObjectsForAllClasses(newdoc, olddoc);
        else
            return hasEqualsObjectsFromClass(newdoc, olddoc, className);
    }

    public void verify(XWikiDocument newdoc, XWikiDocument olddoc, XWikiContext context) {
        if (!hasEqualObjects(newdoc, olddoc, className))
            getTarget().notify(this, newdoc, olddoc, XWikiDocChangeNotificationInterface.EVENT_CHANGE, context);
    }



}
