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

package com.github.dandelion.core.asset.cache;

import java.util.ServiceLoader;

import com.github.dandelion.core.asset.cache.impl.HashMapAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.utils.Sha1Utils;

public class AssetCacheSystem {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetCacheSystem.class);

    private static ServiceLoader<AssetCache> loader = ServiceLoader.load(AssetCache.class);
    private static AssetCache assetCache;

    private AssetCacheSystem() {
    }

    private static void initializeAssetsCache() {
        if(assetCache == null) {
            initializeAssetsCacheIfNeeded();
        }
    }

    synchronized private static void initializeAssetsCacheIfNeeded() {
        if(assetCache != null) return;

        for (AssetCache ac : loader) {
            if (assetCache != null) {
                LOG.info("found {} assets cache but it's already configured with {} cache system", ac.getAssetsCacheName(), assetCache.getAssetsCacheName());
            } else if (!ac.getAssetsCacheName().equals("default")) {
                assetCache = ac;
                LOG.info("setup assets cache with {} cache system", assetCache.getAssetsCacheName());
            }
        }

        if (assetCache == null) {
            assetCache = new HashMapAssetCache();
            LOG.info("setup assets cache with {} cache system", assetCache.getAssetsCacheName());
        }
    }

    public static String generateCacheKey(String context, String location, String assetName, AssetType assetType) {
        String generatedKey = Sha1Utils.generateSha1(context + "|" + location, true) + "-" + assetName + "." + assetType.name();
        LOG.debug("generate SHA1 key {} from context {}, location {}, asset name {}, asset type {}.", generatedKey, context, location, assetName, assetType);
        return generatedKey;
    }

    public static boolean checkCacheKey(String cacheKey) {
        initializeAssetsCache();
        LOG.debug("check cache for key {}", cacheKey);
        return assetCache.checkCacheKey(cacheKey);
    }

    public static String getCacheContent(String cacheKey) {
        initializeAssetsCache();
        LOG.debug("get content of key {}", cacheKey);
        return assetCache.getCacheContent(cacheKey);
    }

    public static void storeCacheContent(String context, String location, String resourceName, AssetType type, String content) {
        initializeAssetsCache();
        String generatedKey = generateCacheKey(context, location, resourceName, type);
        LOG.debug("store in cache the key {} with content [{}]", generatedKey, content);
        assetCache.storeCacheContent(generatedKey, content);
    }

    public static String getAssetsCacheName() {
        initializeAssetsCache();
        return assetCache.getAssetsCacheName();
    }
}