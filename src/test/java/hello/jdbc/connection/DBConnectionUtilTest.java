package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class DBConnectionUtilTest {

    @Test
    void connection() {

        // 데이터베이스에 연결
        Connection connection = DBConnectionUtil.getConnection();

        // H2 데이터베이스 드라이버가 제공하는 H2 전용 커넥션 반환 여부
        assertThat(connection).isNotNull();
    }
}
