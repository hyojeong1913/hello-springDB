package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 *
 * DataSourceUtils.getConnection()
 * - 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환
 * - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 새로운 커넥션을 생성해서 반환
 *
 * DataSourceUtils.releaseConnection()
 * - 커넥션을 con.close() 를 사용해서 직접 닫아버리면 커넥션이 유지되지 않는 문제가 발생하는데,
 *   DataSourceUtils.releaseConnection() 를 사용하면 트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지
 * - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫는다.
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {

        String sql = "INSERT INTO member(memberId, money) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            pstmt.executeUpdate();

            return member;
        }
        catch (SQLException e) {

            log.info("DB Error = {}", e);

            throw e;
        }
        finally {

            close(conn, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {

        String sql = "SELECT * FROM member WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, memberId);

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
        }
        catch (SQLException e) {

            log.info("DB Error = {}", e);

            throw e;
        }
        finally {

            close(conn, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {

        String sql = "UPDATE member SET money = ? WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();

            log.info("resultSize = {}", resultSize);
        }
        catch (SQLException e) {

            log.error("DB Error = {}", e);

            throw e;
        }
        finally {

            close(conn, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {

        String sql = "DELETE FROM member WHERE memberId = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, memberId);

            int resultSize = pstmt.executeUpdate();

            log.info("resultSize = {}", resultSize);
        }
        catch (SQLException e) {

            log.error("DB Error = {}", e);

            throw e;
        }
        finally {

            close(conn, pstmt, null);
        }
    }

    private Connection getConnection() throws SQLException {

        // DataSourceUtils 를 통해 트랜잭션 동기화 사용
        Connection con = DataSourceUtils.getConnection(dataSource);

        log.info("get connection = {}, class = {}", con, con.getClass());

        return con;
    }

    private void close(Connection conn, Statement st, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(st);

        // DataSourceUtils 를 통해 트랜잭션 동기화 사용
        DataSourceUtils.releaseConnection(conn, dataSource);
    }
}
