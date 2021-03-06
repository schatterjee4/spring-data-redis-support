/**
 * 
 */
package org.springframework.data.redis.cache;

import static org.springframework.util.Assert.notNull;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;

/**
 * use read only RedisOperations for get.
 * 
 * @author yoshidan
 */
public class MasterSlaveRedisCache extends RedisCache{
	
    @SuppressWarnings("rawtypes")
    private final RedisOperations redisReadOperations;
    private final RedisCacheMetadata cacheMetadata;
    private final CacheValueAccessor cacheValueAccessor;

    /**
     * Constructs a new <code>MasterSlaveRedisCache</code> instance.
     * 
     * @param name cache name
     * @param prefix
     * @param redisOperations
     * @param expiration
     */
    public MasterSlaveRedisCache(String name, byte[] prefix, RedisOperations<? extends Object, ? extends Object> redisOperations,
        long expiration,RedisOperations<? extends Object, ? extends Object> redisReadOperations) {
	    super(name,prefix,redisOperations,expiration);

        this.redisReadOperations = redisReadOperations;
        this.cacheMetadata = new RedisCacheMetadata(name, prefix);
        this.cacheMetadata.setDefaultExpiration(expiration);
      
        this.cacheValueAccessor = new CacheValueAccessor(redisOperations.getValueSerializer());

    }

    /**
     * @see RedisCache#get(RedisCacheKey)
     */
    @SuppressWarnings("unchecked")
    @Override
    public RedisCacheElement get(final RedisCacheKey cacheKey) {

        notNull(cacheKey, "CacheKey must not be null!");
        
        byte[] bytes = (byte[]) redisReadOperations.execute(new AbstractRedisCacheCallback<byte[]>(new BinaryRedisCacheElement(
                new RedisCacheElement(cacheKey, null), cacheValueAccessor), cacheMetadata) {

            @Override
            public byte[] doInRedis(BinaryRedisCacheElement element, RedisConnection connection) throws DataAccessException {
                return connection.get(element.getKeyBytes());
            }
        });

        return (bytes == null ? null : new RedisCacheElement(cacheKey, cacheValueAccessor.deserializeIfNecessary(bytes)));
    }

}
