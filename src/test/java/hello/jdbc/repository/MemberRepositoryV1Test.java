package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {

        // 기본 DriverManager - 항상 새로운 커넥션 획득
        // DriverManagerDataSource 를 사용하면 conn0~5 번호를 통해서 항상 새로운 커넥션이 생성되어서 사용되는 것을 확인
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 커넥션 풀링
        // 커넥션 풀 사용 시 conn0 커넥션이 재사용된 것을 확인
        // 웹 애플리케이션에 동시에 여러 요청이 들어오면 여러 쓰레드에서 커넥션 풀의 커넥션을 다양하게 가져가는 상황을 확인 가능
        // MemberRepositoryV1 는 DataSource 인터페이스에만 의존하기 때문에
        // DriverManagerDataSource HikariDataSource 로 변경해도 MemberRepositoryV1 의 코드는 전혀 변경하지 않아도 된다.
        // (dataSource 의 장점. DI + OCP)
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }

    /**
     * 회원 등록 테스트
     *
     * 2번 이상 실행하는 경우 PK 중복 오류가 일어나므로
     * DELETE FROM member;
     * 쿼리로 데이터 삭제 후 확인 필요
     *
     * + 회원 조회 테스트
     *
     * @Data 어노테이션이 toString() 을 적절히 오버라이딩 해서 보여주기 때문에 실행 결과 member 에 실제 데이터가 보인다.
     *
     * + 회원 수정 테스트
     *
     * @throws SQLException
     */
    @Test
    void crud() throws SQLException {

        Member member = new Member("memberV0", 10000);

        // 등록
        repository.save(member);

        // 조회
        Member findMember = repository.findById(member.getMemberId());

        log.info("findMember = {}", findMember);
        log.info("member == findMember : {}", member == findMember);
        log.info("member equals findMember : {}", member.equals(findMember));

        assertThat(findMember).isEqualTo(member);

        // 수정
        repository.update(member.getMemberId(), 20000);

        Member updatedMember = repository.findById(member.getMemberId());

        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // 삭제
        repository.delete(member.getMemberId());

        // 삭제하여 회원이 없기 때문에 NoSuchElementException 이 발생
        // assertThatThrownBy() 를 통해 해당 예외가 발생 검증
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

        try {

            Thread.sleep(1000);
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }
    }
}