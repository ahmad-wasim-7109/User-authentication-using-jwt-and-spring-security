package com.github.splitbuddy;

import com.github.splitbuddy.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserAuthApplicationTests {

	@Autowired
	RedisService redisConfiguration;
	@Test
	void contextLoads() {
		redisConfiguration.put("testKey", "testValue");
		String value = (String) redisConfiguration.get("testKey");
	}

}
