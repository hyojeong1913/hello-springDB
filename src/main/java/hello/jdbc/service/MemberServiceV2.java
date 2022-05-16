package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 트랜잭션을 시작하려면 커넥션이 필요
        Connection conn = dataSource.getConnection();

        try {

            // 트랜잭션 시작
            // 트랜잭션을 시작하려면 자동 커밋 모드를 꺼야한다.
            conn.setAutoCommit(false);

            // 트랜잭션이 시작된 커넥션을 전달하면서 비즈니스 로직을 수행
            bizLogic(conn, fromId, toId, money);

            // 비즈니스 로직이 정상 수행되면 트랜잭션을 커밋
            conn.commit();
        }
        catch (Exception e) {

            // 비즈니스 로직 수행 도중에 예외가 발생하면 트랜잭션을 롤백
            conn.rollback();

            throw new IllegalStateException(e);
        }
        finally {

            // 커넥션을 모두 사용하고 나면 안전하게 종료
            release(conn);
        }
    }

    /**
     * 비즈니스 로직
     *
     * 트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 구분하기 위해 소스 분리
     *
     * @param conn
     * @param fromId
     * @param toId
     * @param money
     * @throws SQLException
     */
    private void bizLogic(Connection conn, String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(conn, fromId);
        Member toMember = memberRepository.findById(conn, toId);

        // fromId 회원의 돈을 money 만큼 감소
        memberRepository.update(conn, fromId, fromMember.getMoney() - money);

        validation(toMember);

        // toId 회원의 돈을 money 만큼 증가
        memberRepository.update(conn, toId, toMember.getMoney() + money);
    }

    /**
     * 커넥션 종료
     *
     * 커넥션 풀을 사용하면 con.close() 를 호출 했을 때 커넥션이 종료되는 것이 아니라 풀에 반납된다.
     * 현재 수동 커밋 모드로 동작하기 때문에 풀에 돌려주기 전에 기본 값인 자동 커밋 모드로 변경하는 것이 안전하다.
     *
     * @param conn
     */
    private void release(Connection conn) {
        
        if (conn != null) {

            try {

                // 커넥션 풀 고려
                conn.setAutoCommit(true);

                conn.close();
            }
            catch (Exception e) {

                log.info("ERROR", e);
            }
        }
    }

    /**
     * 예외 상황을 테스트해보기 위해 toId 가 "ex" 인 경우 예외 발생
     *
     * @param toMember
     */
    private void validation(Member toMember) {

        if (toMember.getMemberId().equals("ex")) {

            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
