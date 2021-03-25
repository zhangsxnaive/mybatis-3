### MyBatis架构原理

## 1. 架构设计

![image-20210324111301421](https://raw.githubusercontent.com/zhangsxnaive/mybatis-3/master/README.assets/image-20210324111301421.png)

我们把Mybatis的功能架构分为三层:
 (1) API接口层:提供给外部使用的接口 API，开发人员通过这些本地API来操纵数据库。接口层一接收

到 调用请求就会调用数据处理层来完成具体的数据处理。 MyBatis和数据库的交互有两种方式:
 a. 使用传统的MyBati s提供的API ;
 b. 使用Mapper代理的方式

(2) 数据处理层:负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根 据调用的请求完成一次数据库操作。

(3) 基础支撑层:负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是 共 用的东⻄，将他们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑

## 2. 主要构件及其相互关系

| 构件             | 描述                                                         |
| ---------------- | ------------------------------------------------------------ |
| SqlSession       | 作为MyBatis工作的主要顶层API，表示和数据库交互的会话，完成必要数据库增删改查功能 |
| Executor         | MyBatis执行器，是MyBatis调度的核心，负责SQL语句的生成和查询缓存的维护。 |
| StatementHandler | 封装了JDBC Statement操作，负责对JDBC statement的操作，如设置参数、将Statement结果集转换成List集合。 |
| ParameterHandler | 负责对用户传递的参数转换成JDBC Statement所需要的参数         |
| ResultSetHandler | 负责将JDBC返回的ResultSet结果集对象转换成List类型的集合;     |
| TypeHandler      | 负责java数据类型和jdbc数据类型之间的映射和转换               |
| MappedStatement  | MappedStatement维护了一条<select \| update \| delete \| insert>节点的封装 |
| SqlSource        | 负责根据用户传递的parameterObject，动态地生成SQL语句，将信息封装到BoundSql对象中，并返回 |
| BoundSql         | 表示动态生成的SQL语句以及相应的参数信息                      |

![image-20210324111717277](https://raw.githubusercontent.com/zhangsxnaive/mybatis-3/master/README.assets/image-20210324111717277.png)

## 3. 总体流程

1. 加载配置并初始化 

   **触发条件**:加载配置文件

   配置来源于两个地方，一个是配置文件(主配置文件conf.xml,mapper文件*.xml),—个是java代码中的注解，将主配置文件内容解析封装到Configuration,将sql的配置信息加载成为一个mappedstatement 对象，存储在内存之中

2. 接收调用请求

   **触发条件**:调用Mybatis提供的API

   **传入参数**:为SQL的ID和传入参数对象

   **处理过程**:将请求传递给下层的请求处理层进行处理。

3. 处理操作请求

   **触发条件**:API接口层传递请求过来

   **传入参数**:为SQL的ID和传入参数对象

   **处理过程**:

   1. 根据SQL的ID查找对应的MappedStatement对象。

   2. 根据传入参数对象解析MappedStatement对象，得到最终要执行的SQL和执行传入参数。

   3. 获取数据库连接，根据得到的最终SQL语句和执行传入参数到数据库执行，并得到执行结果。

   4. 根据MappedStatement对象中的结果映射配置对得到的执行结果进行转换处理，并得到最终的处 理 结果。

   5. 释放连接资源。

4. 返回处理结果 将最终的处理结果返回。 