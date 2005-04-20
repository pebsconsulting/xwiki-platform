/**
 * ===================================================================
 *
 * Copyright (c) 2003,2004 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details, published at 
 * http://www.gnu.org/copyleft/lesser.html or in lesser.txt in the
 * root folder of this distribution.

 * Created by
 * User: Ludovic Dubost
 * Date: 4 avr. 2005
 * Time: 00:56:48
 */
package com.xpn.xwiki.cache.impl;

import com.xpn.xwiki.cache.api.XWikiCacheService;
import com.xpn.xwiki.cache.api.XWikiCache;
import com.opensymphony.oscache.base.Cache;

public class OSCacheService implements XWikiCacheService {
    public XWikiCache newCache() {
        return new OSCacheCache();
    }
    public XWikiCache newCache(int capacity) {
        return new OSCacheCache(capacity);
    }
}
