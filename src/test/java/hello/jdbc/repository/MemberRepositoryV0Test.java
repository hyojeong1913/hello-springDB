package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    /**
     * 회원 등록 테스트
     *
     * 2번 이상 실행하는 경우 PK 중복 오류가 일어나므로
     * DELETE FROM member;
     * 쿼리로 데이터 삭제 후 확인 필요
     *
     * @throws SQLException
     */
    @Test
    void crud() throws SQLException {

        Member member = new Member("memberV0", 10000);

        repository.save(member);
    }
}