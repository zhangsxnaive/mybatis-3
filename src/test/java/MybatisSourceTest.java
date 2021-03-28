import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author 张书新
 * @version 1.0
 * @date 2021/3/24 10:52 下午
 */
public class MybatisSourceTest {
  @Test
  public void test1() throws IOException {
    // 1. 读取配置文件，读取成字节输入流
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");

    // 2. 解析配置文件，封装Configuration对象，创建DefaultSqlSessionFactory
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);

    Configuration configuration = build.getConfiguration();

    // 3. 生产一个 DefaultSqlSession 对象，设置了事务不自动提交，完成 Executor 对象的创建并设置到DefaultSqlSession
    SqlSession sqlSession = build.openSession();

    // 4. 根据 statementId 从 configuration 中的map集合中，获取到了MappedStatement对象
    //    将查询任务委派给了Executor执行器
    List<Object> objects = sqlSession.selectList("namespace.id");

    sqlSession.commit();

    sqlSession.close();
  }

  @Test
  public void test2() throws IOException {
    // 1. 读取配置文件，读取成字节输入流
    InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");

    // 2. 解析配置文件，封装Configuration对象，创建DefaultSqlSessionFactory
    SqlSessionFactory build = new SqlSessionFactoryBuilder().build(resourceAsStream);

    // 3. 生产一个 DefaultSqlSession 对象，设置了事务不自动提交，完成 Executor 对象的创建并设置到DefaultSqlSession
    SqlSession sqlSession = build.openSession();

    // 4. 使用JDK动态代理，生成一个Mapper接口的代理对象
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);

    // 5. 代理对象调用接口中的任意方法，都执行动态代理中的invoke方法
    List<Object> all = mapper.findAll();

  }
}
