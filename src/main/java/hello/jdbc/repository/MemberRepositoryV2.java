package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 *
 * 애플리케이션에서 DB 트랜잭션을 사용하려면 트랜잭션을 사용하는 동안 같은 커넥션을 유지
 *
 * 커넥션을 파라미터로 전달해서 같은 커넥션이 사용되도록 유지
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
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

    /**
     * 파라미터로 넘어온 커넥션을 사용
     *
     * @param conn
     * @param memberId
     * @return
     * @throws SQLException
     */
    public Member findById(Connection conn, String memberId) throws SQLException {

        String sql = "SELECT * FROM member WHERE memberId = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
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

            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
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

    /**
     * 파라미터로 넘어온 커넥션을 사용
     *
     * @param conn
     * @param memberId
     * @param money
     * @throws SQLException
     */
    public void update(Connection conn, String memberId, int money) throws SQLException {

        String sql = "UPDATE member SET money = ? WHERE memberId = ?";

        PreparedStatement pstmt = null;

        try {
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

            JdbcUtils.closeStatement(pstmt);
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

        Connection con = dataSource.getConnection();

        log.info("get connection = {}, class = {}", con, con.getClass());

        return con;
    }

    private void close(Connection conn, Statement st, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(st);
        JdbcUtils.closeConnection(conn);
    }
}
