# RedisTemplate 사용 예제

# 참고자료

## 메인 자료

> 메인으로 참고한 자료

- [사바라다는 차곡차곡](https://sabarada.tistory.com)
  - 메인으로 참고한 자료. 기본적인 컨셉을 참고할 수 있어서 유용하게 참고했다.
  - 이분 블로그를 자주 보다보면 공식 문서에서 필요한 부분을 어떻게 찾아서 적용하는지 조금씩 적응이 되는 것 같음
  - 블로그 글을 먼저 보지 않았다면 공식 문서를 처음부터 끝까지 정독하다가 지쳤을 듯.

- [사바라다는 차곡차곡 - RedisTemplate](https://sabarada.tistory.com/105)
- [사바라다는 차곡차곡 - RedisRepository](https://sabarada.tistory.com/106)



## 공식문서

> Spring Data Redis 의 공식문서 버전은 2.4.6 을 참고했다. spring boot starter parent 의 내부동작으로 잡아주는 spring data redis 의 버전은 한단계 아래이지 않을까 싶기는 하다. 

- Spring Data Redis
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6 GA)](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#reference)
- RedisTemplate 
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6 GA #Working with Objects through RedisTemplate)](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#redis:template)
- Redis Repository
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6.GA) #Redis Repositories](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#reference)
- RedisTemplate 클래스 명세 
  - [Spring Data Redis DOCS - API Guide](https://docs.spring.io/spring-data/redis/docs/2.4.6/api/org/springframework/data/redis/core/RedisTemplate.html)



## Redis 개념 관련된 자료들

- [Redis Sorted Set](https://jupiny.com/2020/03/28/redis-sorted-set/)
  - ZSet 의 개념에 대해서 설명해주고 있다.



# 라이브러리 추가 (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
	  // ...
}
```



# Bean, Configuration

## xml 설정

> 참고 : [docs.spring.io/spring-data/redis/docs/2.4.6](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#redis:string)

공식문서(2.4.6)에서는 아래와 같이 설정을 하고 있다. 예전에 다니던 회사에서는 xml 설정을 하는 프로젝트가 다수 있었는데, 그 당시에는 또 java config 가 유행이라, xml 설정을 하는데에 애를 먹었던 기억이 있다. 요즘에는 공식문서에서 직접 xml 설정을 하는 경우가 많은걸 보니 "또 트렌드가 변하는 가 보다" 하는 생각이 들기는 했다.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:p="http://www.springframework.org/schema/p"
  xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory" p:use-pool="true"/>
  <!-- redis template definition -->
  <bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate" p:connection-factory-ref="jedisConnectionFactory"/>
  ...

</beans>
```



위의 xml 기반 bean 설정을 보면 아래와 같은 구문이 있다. bean 의 property 중 `connection-factory-ref`  를 `jedisConnectionFactory` 로 지정하고 있다. 

```xml
<bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate" p:connection-factory-ref="jedisConnectionFactory"/>
```

p:connection-factory-ref 의미는 이렇다. 

- RedisTemplate 클래스 내에는 connection-factory 라는 필드가 있다.
- connection-factory 라는 필드명 뒤에 `-ref` 를 붙여서 `connection-factory-ref` 라는 프로퍼티를 지정하고 있다.
- 이 connection-factory-ref 필드는 기본자료형이 아닌 참조자료형(타입자료형)을 찾마한다는 의미이다. 보통 참조자료형의 필드에 `-ref` 를 붙이는 것이 관습.
- `connection-factory-ref` 에 지정하고 있는 값은 `jedisConnectionFactory`  인데 이것은 xml 설정내에서 생성한 bean 인 `jedisConnectionFactory` 이다. 



## Java Config

이제 이 설정을 java configuration 으로 바꿔보면 아래와 같다. 

```java
package io.study.redisboot.redis_template.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Bean
	public RedisConnectionFactory redisConnectionFactory(){
		LettuceConnectionFactory redisFactory = new LettuceConnectionFactory(host, port);
		return redisFactory;
	}

	@Bean
	public RedisTemplate<?,?> redisTemplate(){
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		return redisTemplate;
	}
}
```

  

RedisConnectionFactory 는 인터페이스이다. 이것을 구현하고 있는 클래스는 여러가지가 있는데 위의 예제에서는 이중 LettuceConnectionFactory 를 사용했다.  

```java
package org.springframework.data.redis.connection;
import org.springframework.dao.support.PersistenceExceptionTranslator;
public interface RedisConnectionFactory extends PersistenceExceptionTranslator {
  // ...
}
```



# application.yml

yaml 파일에는 spring.redis.host, spring.redis.port 를 애플리케이션 환경변수로 추가해두었다.

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```



# Test

>  참고자료 : RedisTemplate 클래스 명세 
>
> - [Spring Data Redis DOCS - API Guide](https://docs.spring.io/spring-data/redis/docs/2.4.6/api/org/springframework/data/redis/core/RedisTemplate.html)  

  

RedisTemplate 클래스에는 여러가지 함수가 있지만, 그중에서 간단한 몇가지 자료구조들에 대한 함수들을 정리해보면 아래와 같다. (아래 함수 외에도 종류가 굉장히 많다.)

- opsForValue
  - 단순 키,밸류 형테의 값을 저장할 때 사용한다.
- opsForList
  - 원하는 특정 키에 대한 밸류를 리스트 형태로 지정하고자 할 때 사용한다.
- opsForSet
  - 원하는 특정 키에 대한 밸류를 Set 형태로 지정하고자 할 때 사용한다. 
- opsForZSet
  - Redis 에서는 SortedSet 이라는 자료구조를 제공하는데 이것을  ZSet이라고도 부르는 것 같다.
  - 참고자료 : [Redis Sorted Set](https://jupiny.com/2020/03/28/redis-sorted-set/)



## opsForValue

> ValueOperations\<K,V\> 를 이용한 연산을 한다.

RedisTemplate 내의 **opsForValue()** 함수는 **ValueOperations\<String, String\>** 를 생성해서 넘겨준다.  

그리고 get(), set() 을 통해서 키/밸류 연산을 하면 된다.  

자세한 설명은 더 이상 필요할 것 같지 않아서 예제로 정리해보면 아래와 같다.  

### 예제

```java
package io.study.redisboot.redis_template;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
  
}
```



### 출력결과

```plain
<fruit, [apple, banana, cherry, diamond, eeeee]>

####### iteration #1
apple
banana
cherry
diamond
eeeee

####### after delete (fruit)
[]
```



## opsForList

> ListOperations\<K,V\> 를 이용한 연산을 한다.

RedisTemplate 내의 **opsForList()** 함수는 **ListOperations\<String, String\>** 를 생성해서 넘겨준다.  

그리고 get(), set() 을 통해서 키/밸류 연산을 하면 된다.  

자세한 설명은 더 이상 필요할 것 같지 않아서 예제로 정리해보면 아래와 같다.  

  

### 예제

```java
package io.study.redisboot.redis_template;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class StringRedisTemplateTest {

	@Autowired
	StringRedisTemplate redisTemplate;

  // ...
  
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
}
```



### 출력결과

```plain
<fruit, [apple, banana, cherry, diamond, eeeee]>

####### iteration #1
apple
banana
cherry
diamond
eeeee

####### after delete (fruit)
[]
```



## opsForSet

> SetOperations\<K,V\> 를 이용한 연산을 한다.  

RedisTemplate 내의 **opsForSet()** 함수는 **SetOperations\<String, String\>** 를 생성해서 넘겨준다.  

그리고 get(), set() 을 통해서 키/밸류 연산을 하면 된다.  

자세한 설명은 더 이상 필요할 것 같지 않아서 예제로 정리해보면 아래와 같다.  

​    

### 예제

```java
package io.study.redisboot.redis_template;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class StringRedisTemplateTest {

	@Autowired
	StringRedisTemplate redisTemplate;

  // ... 

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
}
```



### 출력결과

```plain
[cherry, banana, apple]

####### after delete (fruit)
[]
```



## opsForZSet

> ZSetOperations\<K,V\> 를 이용한 연산을 한다.  



**참고한 자료** : [Redis Sorted Set](https://jupiny.com/2020/03/28/redis-sorted-set/)

  

RedisTemplate 내의 **opsForZSet()** 함수는 **ZSetOperations\<String, String\>** 를 생성해서 넘겨준다.  

그리고 get(), set() 을 통해서 키/밸류 연산을 하면 된다.  

자세한 설명은 더 이상 필요할 것 같지 않아서 예제로 정리해보면 아래와 같다.  



### 예제

```java
package io.study.redisboot.redis_template;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

@SpringBootTest
public class StringRedisTemplateTest {

	@Autowired
	StringRedisTemplate redisTemplate;

  // ... 

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
}
```



### 출력결과

```plain
[apple, banana, cherry]

####### after delete (fruit)
[]
```



## opsForHash

> ZSetOperations\<K,V\> 를 이용한 연산을 한다.  
>
> 예제를 더 자세히 할까 했는데, 여기까지 정리하다보니 힘이 딸려서 굉장히 간단한 예제로 바꿨다.

  

RedisTemplate 내의 **opsForHash()** 함수는 **HashOperations\<String, Object, Object\>** 를 생성해서 넘겨준다.  

그리고 get(), set() 을 통해서 키/밸류 연산을 하면 된다.  

자세한 설명은 더 이상 필요할 것 같지 않아서 예제로 정리해보면 아래와 같다.  

  

### 예제

```java
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
```



### 출력결과

```plain
1000
```

