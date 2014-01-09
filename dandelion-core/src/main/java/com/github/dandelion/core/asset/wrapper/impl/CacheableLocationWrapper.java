/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.dandelion.core.asset.wrapper.impl;

import static com.github.dandelion.core.DevMode.isDevModeEnabled;
import static com.github.dandelion.core.asset.cache.AssetCacheSystem.generateCacheKey;
import static com.github.dandelion.core.asset.cache.AssetCacheSystem.storeCacheContent;
import static com.github.dandelion.core.asset.web.AssetServlet.DANDELION_ASSETS_URL;
import static com.github.dandelion.core.asset.web.AssetRequestContext.get;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.utils.RequestUtils;

/**
 * Base for Wrapper with caching faculty
 */
public abstract class CacheableLocationWrapper implements AssetLocationWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public String wrapLocation(Asset asset, HttpServletRequest request) {

        String location = asset.getLocations().get(locationKey());
        String context = RequestUtils.getCurrentUrl(request, true);
        context = context.replaceAll("\\?", "_").replaceAll("&", "_");

        String cacheKey = generateCacheKey(context, location, asset.getName(), asset.getType());

        Map<String, Object> parameters = get(request).getParameters(asset.getName());
        if (isDevModeEnabled() || !AssetCacheSystem.checkCacheKey(cacheKey)) {
            String content = getContent(asset, location, parameters, request);
            storeCacheContent(context, location, asset.getName(), asset.getType(), content);
        }

        return RequestUtils.getBaseUrl(request) + DANDELION_ASSETS_URL + cacheKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWrappedContent(Asset asset, HttpServletRequest request) {
        String location = asset.getLocations().get(locationKey());
        String prefixCacheKey = RequestUtils.getBaseUrl(request) + DANDELION_ASSETS_URL;
        String cacheKey = location.replaceAll(prefixCacheKey, "");
        return AssetCacheSystem.getCacheContent(cacheKey);
    }

    protected abstract String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request);
}
