import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * @author 张书新
 * @version 1.0
 * @date 2021/3/25 10:20 上午
 */
public interface UserMapper {
  List<Object> findAll();
}
