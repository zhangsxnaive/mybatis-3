/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * @author Clinton Begin
 */
public interface Interceptor {

  /**
   * 功能: 主要目的是为了增强逻辑，对之前的方法进行增强
   * 只要被拦截的目标对象的目标方法被执行时，每次都会执行intercept方法
   * @param invocation
   * @return java.lang.Object
   * @date 2021/3/23 10:47 上午
   */
  Object intercept(Invocation invocation) throws Throwable;

  /**
   * 把当前的拦截器生成代理，存到拦截器链中
   * @param target 要代理的对象
   * @return 代理对象
   */
  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  /**
   * 获取配置文件的参数
   * @param properties 配置文件plugin标签下的properties标签
   */
  default void setProperties(Properties properties) {
    // NOP
  }

}
