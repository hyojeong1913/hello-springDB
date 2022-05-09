package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

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
    }
}