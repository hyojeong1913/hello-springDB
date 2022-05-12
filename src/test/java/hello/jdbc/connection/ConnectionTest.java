package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    /**
     * DriverManager 를 통해서 커넥션을 획득
     *
     * DriverManager 는 커넥션을 획득할 때마다 URL, USERNAME, PASSWORD 같은 파라미터를 계속 전달해야 하는 단점 존재
     *
     * @throws SQLException
     */
    @Test
    void driverManager() throws SQLException {

        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        log.info("connection1 = {}, class = {}", con1, con1.getClass());
        log.info("connection2 = {}, class = {}", con2, con2.getClass());
    }

    /**
     * 스프링이 제공하는 DataSource 가 적용된 DriverManager 인 DriverManagerDataSource 를 사용
     *
     * DataSource 를 사용하는 방식은 처음 객체를 생성할 때만 필요한 파리미터를 넘겨두고,
     * 커넥션을 획득할 때는 단순히 dataSource.getConnection() 만 호출하면 된다.
     *
     * @throws SQLException
     */
    @Test
    void dataSourceDriverManger() throws SQLException {

        // DriverManagerDataSource - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        useDataSource(dataSource);
    }

    /**
     * DataSource Connection Pool 추가
     *
     * HikariCP 커넥션 풀을 사용
     *
     * 커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 쓰레드에서 작동
     *
     * 별도의 쓰레드에서 동작하기 때문에 테스트가 먼저 종료되어 버리므로
     * Thread.sleep 을 통해 대기 시간을 주어야 쓰레드 풀에 커넥션이 생성되는 로그를 확인 가능
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {

        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10); // 커넥션 풀 최대 사이즈를 10 으로 지정
        dataSource.setPoolName("MyPool"); // 풀의 이름을 MyPool 이라고 지정

        useDataSource(dataSource);

        // 커넥션 풀에서 커넥션 생성 시간 대기
        Thread.sleep(1000);
    }

    /**
     * 설정과 사용의 분리
     *
     * 필요한 데이터를 DataSource 가 만들어지는 시점에 미리 다 넣어두게 되면,
     * DataSource 를 사용하는 곳에서는 dataSource.getConnection() 만 호출하면 되므로,
     * URL, USERNAME, PASSWORD 같은 속성들에 의존하지 않아도 된다.
     *
     * @param dataSource
     * @throws SQLException
     */
    private void useDataSource(DataSource dataSource) throws SQLException {

        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();

        log.info("connection1 = {}, class = {}", con1, con1.getClass());
        log.info("connection2 = {}, class = {}", con2, con2.getClass());
    }
}
