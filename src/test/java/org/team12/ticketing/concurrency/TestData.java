package org.team12.ticketing.concurrency;

import org.team12.ticketing.concurrency.concert.domain.Concert;

public class TestData {

    public static Concert createConcert() {
        return new Concert(
                "테스트 콘서트1",
                "테스트 가수1",
                "테스트 콘서트1 입니다.",
                30L
        );
    }
}
