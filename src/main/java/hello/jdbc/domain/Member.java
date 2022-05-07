package hello.jdbc.domain;

import lombok.Data;

/**
 * 회원의 ID와 해당 회원이 소지한 금액을 표현하는 단순한 클래스
 */
@Data
public class Member {

    private String memberId;
    private int money;

    public Member() {
    }

    public Member(String memberId, int money) {
        this.memberId = memberId;
        this.money = money;
    }
}
