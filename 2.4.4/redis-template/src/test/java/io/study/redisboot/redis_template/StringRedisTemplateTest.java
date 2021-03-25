package io.study.redisboot.redis_template;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

@SpringBootTest
public class StringRedisTemplateTest {

	@Autowired
	StringRedisTemplate redisTemplate;

	@Test
	@DisplayName("opsForValue_테스트")
	void opsForValue_테스트(){
		final String key_apple = "apple";

		final ValueOperations<String, String> priceTable = redisTemplate.opsForValue();

		priceTable.set(key_apple, "1000");
		String apple_price_first_try = priceTable.get(key_apple);
		Assertions.assertThat(apple_price_first_try).isEqualTo("1000");
		System.out.println("apple_price_first_try = " + apple_price_first_try);

		priceTable.set(key_apple, "2000");
		String apple_price_second_try = priceTable.get(key_apple);
		Assertions.assertThat(apple_price_second_try).isEqualTo("2000");
		System.out.println("apple_price_second_try = " + apple_price_second_try);

		System.out.println("\n####### after delete (" + key_apple + ")");
		redisTemplate.delete(key_apple);
		System.out.println(priceTable.get(key_apple));
	}

	@Test
	@DisplayName("opsForList_테스트")
	void opsForList_테스트(){
		final String key_fruit = "fruit";

		ListOperations<String, String> fruitListTable = redisTemplate.opsForList();
		fruitListTable.rightPush(key_fruit, "apple");
		fruitListTable.rightPush(key_fruit, "banana");
		fruitListTable.rightPush(key_fruit, "cherry");
		fruitListTable.rightPush(key_fruit, "diamond");
		fruitListTable.rightPush(key_fruit, "eeeee");

		List<String> list = fruitListTable
			.range(key_fruit, 0, Long.parseLong(String.valueOf(fruitListTable.size(key_fruit))));

		System.out.println("<"+ key_fruit + ", " + list + ">");

		System.out.println("\n####### iteration #1");
		for(String s : list){
			System.out.println(s);
		}

		System.out.println("\n####### after delete (" + key_fruit + ")");
		redisTemplate.delete(key_fruit);
		System.out.println(fruitListTable
			.range(key_fruit, 0, Long.parseLong(String.valueOf(fruitListTable.size(key_fruit)))));
	}

	@Test
	@DisplayName("opsForSet_테스트")
	void opsForSet_테스트(){
		String key_fruit = "fruit";
		SetOperations<String, String> fruitSet = redisTemplate.opsForSet();
		fruitSet.add(key_fruit, "apple");
		fruitSet.add(key_fruit, "banana");
		fruitSet.add(key_fruit, "cherry");

		System.out.println(fruitSet.members(key_fruit));

		System.out.println("\n####### after delete (" + key_fruit + ")");
		redisTemplate.delete(key_fruit);
		System.out.println(fruitSet.members(key_fruit));
	}

	@Test
	@DisplayName("opsForZSet_테스트")
	void opsForZSet_테스트(){
		String key_fruit = "fruit";
		ZSetOperations<String, String> fruitZset = redisTemplate.opsForZSet();
		fruitZset.add(key_fruit, "apple", 0.0);
		fruitZset.add(key_fruit, "banana", 0.1);
		fruitZset.add(key_fruit, "cherry", 0.2);

		System.out.println(fruitZset.range(key_fruit, 0, Long.parseLong(String.valueOf(fruitZset.size(key_fruit)))));

		System.out.println("\n####### after delete (" + key_fruit + ")");
		redisTemplate.delete(key_fruit);
		System.out.println(fruitZset.range(key_fruit, 0, Long.parseLong(String.valueOf(fruitZset.size(key_fruit)))));
	}

	@Test
	@DisplayName("opsForHash_테스트")
	void opsForHash_테스트(){
		String key_fruit = "fruit";
		HashOperations<String, Object, Object> priceHash = redisTemplate
			.opsForHash();
		priceHash.put(key_fruit, "apple", "1000");

		Object apple = priceHash.get(key_fruit, "apple");
		System.out.println(String.valueOf(apple));
	}
}
