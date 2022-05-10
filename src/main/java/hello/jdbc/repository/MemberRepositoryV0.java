package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

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

    /**
     * DBConnectionUtil 를 통해서 데이터베이스 connection 획득
     *
     * @return
     */
    private Connection getConnection() {

        return DBConnectionUtil.getConnection();
    }

    /**
     * 리소스 정리
     *
     * 항상 역순으로 정리 필요
     *
     * @param conn
     * @param st
     * @param rs
     */
    private void close(Connection conn, Statement st, ResultSet rs) {

        if (rs != null) {

            try {
                rs.close();
            } catch (SQLException e) {
                log.info("rs ERROR = {}", e);
            }
        }

        if (st != null) {

            try {
                st.close();
            } catch (SQLException e) {
                log.info("st ERROR = {}", e);
            }
        }

        if (conn != null) {

            try {
                conn.close();
            } catch (SQLException e) {
                log.info("conn ERROR = {}", e);
            }
        }
    }
}
