package org.team12.ticketing.concurrency.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.team12.ticketing.concurrency.user.domain.User;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

//@Component
@RequiredArgsConstructor
public class InitData {

    private final JdbcTemplate jdbcTemplate;

//    @PostConstruct
    @Transactional
    public void init() {

        // 생성할 데이터 수
        final int MAX_DATA_SIZE = 1000000;
        // 한 번의 쿼리에 담을 데이터 수
        final int BATCH_SIZE = 10000;

        List<User> userList = new ArrayList<>();
        Faker faker = new Faker();

        for (int i = 0; i < MAX_DATA_SIZE; i++) {
            userList.add(new User(
                    faker.color().name() + faker.name().lastName() + faker.name().firstName()
            ));
        }

        String sql = "INSERT INTO user (nickname) VALUES (?)";

        jdbcTemplate.batchUpdate(sql,
                userList,
                BATCH_SIZE,
                (PreparedStatement ps, User user) -> {
                    ps.setString(1, user.getNickname());
                });
    }
}
