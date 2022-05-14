package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DataSource, JdbcUtils 사용
 *
 * DataSource 의존관계 주입
 *
 * DataSource 는 표준 인터페이스이기 때문에
 * DriverManagerDataSource 에서 HikariDataSource 로 변경되어도 해당 코드를 변경하지 않아도 된다.
 */
@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 회원 등록
     *
     * @param member
     * @return
     * @throws SQLException
     */
    public Member save(Member member) throws SQLException {

        // 데이터베이스에 전달할 SQL
        String sql = "INSERT INTO member(memberId, money) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            // connection 획득
            conn = getConnection();

            // 데이터베이스에 전달할 SQL 준비
            pstmt = conn.prepareStatement(sql);

            // 파라미터로 전달할 데이터들을 준비
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // Statement 를 통해 준비된 SQL 을 커넥션을 통해 실제 데이터베이스에 전달
            // 영향받은 DB row 수를 반환하므로 int 반환
            pstmt.executeUpdate();

            return member;

        } catch (SQLException e) {

            log.info("DB Error = {}", e);

            throw e;

        } finally {

            // 예외가 발생하든, 하지 않든 항상 수행되어야 하므로 finally 구문에 주의해서 작성 필요
            // 커넥션이 끊어지지 않고 계속 유지되어 커넥션 부족으로 장애가 발생 가능 = 리소스 누수
            close(conn, pstmt, null);
        }
    }

    /**
     * 회원 조회
     *
     * @param memberId
     * @return
     * @throws SQLException
     */
    public Member findById(String memberId) throws SQLException {

        // 데이터베이스에 전달할 SQL
        String sql = "SELECT * FROM member WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, memberId);

            // 결과를 ResultSet 에 담아서 반환
            rs = pstmt.executeQuery();

            if (rs.next()) {

                Member member = new Member();

                member.setMemberId(rs.getString("memberId"));
                member.setMoney(rs.getInt("money"));

                return member;
            }
            else {

                throw new NoSuchElementException("member not found. memberId = " + memberId);
            }

        } catch (SQLException e) {

            log.info("DB Error = {}", e);

            throw e;

        } finally {

            close(conn, pstmt, rs);
        }
    }

    /**
     * 회원 수정
     *
     * @param memberId
     * @param money
     * @throws SQLException
     */
    public void update(String memberId, int money) throws SQLException {

        String sql = "UPDATE member SET money = ? WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            // executeUpdate() 는 쿼리를 실행하고 영향받은 row 수를 반환
            int resultSize = pstmt.executeUpdate();

            log.info("resultSize = {}", resultSize);

        } catch (SQLException e) {

            log.error("DB Error = {}", e);

            throw e;

        } finally {

            close(conn, pstmt, null);
        }
    }

    /**
     * 회원 삭제
     *
     * @param memberId
     * @throws SQLException
     */
    public void delete(String memberId) throws SQLException {

        String sql = "DELETE FROM member WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, memberId);

            // executeUpdate() 는 쿼리를 실행하고 영향받은 row 수를 반환
            int resultSize = pstmt.executeUpdate();

            log.info("resultSize = {}", resultSize);

        } catch (SQLException e) {

            log.error("DB Error = {}", e);

            throw e;

        } finally {

            close(conn, pstmt, null);
        }
    }

    private Connection getConnection() throws SQLException {

        Connection con = dataSource.getConnection();

        log.info("get connection = {}, class = {}", con, con.getClass());

        return con;
    }

    /**
     * JdbcUtils 편의 메서드 사용하여 커넥션 close
     *
     * @param conn
     * @param st
     * @param rs
     */
    private void close(Connection conn, Statement st, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(st);
        JdbcUtils.closeConnection(conn);
    }
}
