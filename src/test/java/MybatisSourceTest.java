import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

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

  }
}
