/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * 此类包含所有在会话期间要添加到二级缓存的缓存条目
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back.
 * 调用commit时将所有缓存数据存储到二级缓存中，如果会话回滚则将缓存的条目丢弃。
 * Blocking cache support has been added.
 * Therefore any get() that returns a cache miss will be followed by a put() so any lock associated with the key can be released.
 *
 * Cache的装饰类，添加事务管理功能。数据不直接存到缓存中，否则会出现脏数据问题
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  /**
   * 委托的 Cache 对象
   */
  private final Cache delegate;

  /**
   * 提交时，清空entriesToAddOnCommit
   * 初始值为false，清空后为true
   */
  private boolean clearOnCommit;

  /**
   * 事务提交前，所有从数据库中查询的数据缓存在此集合中
   */
  private final Map<Object, Object> entriesToAddOnCommit;

  /**
   * 在事务提交前，当缓存未命中时，CacheKey将会被存储在此
   */
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public Object getObject(Object key) {
    // issue #116
    // 直接查询二级缓存
    Object object = delegate.getObject(key);
    // 如果缓存中没有命中，则将key添加到entriesMissedInCache
    if (object == null) {
      entriesMissedInCache.add(key);
    }
    // issue #146
    // 如果clearOnCommit为true，说明缓存在清空状态中，返回null
    if (clearOnCommit) {
      return null;
    } else {
      return object;
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    // 将键值对存到entriesToAddOnCommit中，并没有放到二级缓存中
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  /**
   * 清空缓存
   */
  @Override
  public void clear() {
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    // 如果clearOnCommit为true，则清空缓存
    if (clearOnCommit) {
      delegate.clear();
    }
    // 将entriesToAddOnCommit、entriesMissedInCache刷入缓存中（相当于BufferedRead的缓冲区）
    flushPendingEntries();
    // 重置
    reset();
  }

  public void rollback() {
    unlockMissedEntries();
    reset();
  }

  private void reset() {
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    // 将缓冲区中的数据写入到缓存中
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    // 对于一开始没在缓存中命中的key，之后会去数据库中查询。
    // 如果查询到了，就会在entriesToAddOnCommit缓存一份。
    // 对于没有查询到数据，说明数据库中也不存在，所以在缓存中value为null
    for (Object entry : entriesMissedInCache) {
      if (!entriesToAddOnCommit.containsKey(entry)) {
        delegate.putObject(entry, null);
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
