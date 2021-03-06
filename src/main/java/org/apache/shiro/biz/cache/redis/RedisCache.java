/*
 * Copyright (c) 2018 (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.shiro.biz.cache.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.biz.cache.redis.io.InternalSessionSerializer;
import org.apache.shiro.biz.cache.redis.io.SessionSerializer;
import org.apache.shiro.biz.utils.SerializeUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

  
public class RedisCache<K,V> implements Cache<K,V> {  
  
    private Logger LOG = LoggerFactory.getLogger(this.getClass());  
    
    /** 
     * The wrapped Jedis instance. 
     */  
    private RedisManager cache;  

    private SessionSerializer<V> serializer = new InternalSessionSerializer<V>();
    
    /** 
     * The Redis key prefix for the sessions  
     */  
    private String keyPrefix = "shiro_redis_session:";  
      
    /** 
     * Returns the Redis session keys 
     * prefix. 
     * @return The prefix 
     */  
    public String getKeyPrefix() {  
        return keyPrefix;  
    }  
  
    /** 
     * 
     * Sets the Redis sessions key  
     * prefix. 
     * @param keyPrefix The prefix 
     */
    public void setKeyPrefix(String keyPrefix) {  
        this.keyPrefix = keyPrefix;  
    }  
      
    /** 
     * 通过一个JedisManager实例构造RedisCache 
     * @param cache {@link RedisManager} 对象
     */
    public RedisCache(RedisManager cache){  
         if (cache == null) {  
             throw new IllegalArgumentException("Cache argument cannot be null.");  
         }  
         this.cache = cache;  
    }  
      
    /** 
     * Constructs a cache instance with the specified 
     * Redis manager and using a custom key prefix. 
     * @param cache The cache manager instance 
     * @param prefix The Redis key prefix 
     */  
    public RedisCache(RedisManager cache, String prefix){  
           
        this( cache );  
          
        // set the prefix  
        this.keyPrefix = prefix;  
    }  
      
    /** 
     * 获得byte[]型的key 
     * @param key 
     * @return session key
     */  
    private byte[] getByteKey(K key){  
        if(key instanceof String){  
            String preKey = this.keyPrefix + key;  
            return preKey.getBytes();  
        }else{  
            return SerializeUtils.serialize(key);  
        }  
    }  
      
    @Override  
    public V get(K key) throws CacheException {  
        LOG.debug("根据key从Redis中获取对象 key [" + key + "]");  
        try {  
            if (key == null) {  
                return null;  
            }else{  
                byte[] rawValue = cache.get(getByteKey(key));  
                V value = (V)  serializer.deserialize(new String(rawValue));  
                return value;  
            }  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
  
    }  
  
    @Override  
    public V put(K key, V value) throws CacheException {  
        LOG.debug("根据key从存储 key [" + key + "]");  
        try {  
            cache.set(getByteKey(key), serializer.serialize(value).getBytes());  
            return value;  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
  
    @Override  
    public V remove(K key) throws CacheException {  
        LOG.debug("从redis中删除 key [" + key + "]");  
        try {  
            V previous = get(key);  
            cache.del(getByteKey(key));  
            return previous;  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
  
    @Override  
    public void clear() throws CacheException {  
        LOG.debug("从redis中删除所有元素");  
        try {  
            cache.flushDB();  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
  
    @Override  
    public int size() {  
        try {  
            Long longSize = new Long(cache.dbSize());  
            return longSize.intValue();  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
  
    @SuppressWarnings("unchecked")  
    @Override  
    public Set<K> keys() {  
        try {  
            Set<byte[]> keys = cache.keys(this.keyPrefix + "*");  
            if (CollectionUtils.isEmpty(keys)) {  
                return Collections.emptySet();  
            }else{  
                Set<K> newKeys = new HashSet<K>();  
                for(byte[] key:keys){  
                    newKeys.add((K)key);  
                }  
                return newKeys;  
            }  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
  
    @Override  
    public Collection<V> values() {  
        try {  
            Set<byte[]> keys = cache.keys(this.keyPrefix + "*");  
            if (!CollectionUtils.isEmpty(keys)) {  
                List<V> values = new ArrayList<V>(keys.size());  
                for (byte[] key : keys) {  
                    @SuppressWarnings("unchecked")  
                    V value = get((K)key);  
                    if (value != null) {  
                        values.add(value);  
                    }  
                }  
                return Collections.unmodifiableList(values);  
            } else {  
                return Collections.emptyList();  
            }  
        } catch (Throwable t) {  
            throw new CacheException(t);  
        }  
    }  
    
    
}  