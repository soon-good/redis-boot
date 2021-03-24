# RedisRepository 사용 예제

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

## 예제

```java
package io.study.redisboot.redis_repository.employee;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmployeeRedisTest {

	@Autowired
	private EmployeeRepository repository;

	@Test
	void testEmployeeSave(){
		Employee e1 = Employee.builder().id(100L).name("소방관#1").salary(1000D).build();
		Employee e2 = Employee.builder().id(200L).name("소방관#2").salary(2000D).build();
		Employee e3 = Employee.builder().id(300L).name("소방관#3").salary(3000D).build();

		Employee save = repository.save(e1);

		Optional<Employee> optEmployee = repository.findById(e1.getId());
		System.out.println("optEmployee = " + optEmployee);

		repository.save(e2);
		repository.save(e3);

		System.out.println("\nAll Employee List =======");
		Iterable<Employee> all = repository.findAll();

		for (Employee employee : all) {
			System.out.println(employee);
		}

//		repository.deleteAll();
	}
}
```



## 출력결과

```plain
optEmployee = Optional[Employee{id=100, name='소방관#1', salary=1000.0}]

All Employee List =======
Employee{id=100, name='소방관#1', salary=1000.0}
Employee{id=200, name='소방관#2', salary=2000.0}
Employee{id=300, name='소방관#3', salary=3000.0}
```



## redis 에 접속해서 데이터 확인해보기

[github.com/soongujung/docker-scripts](https://github.com/soongujung/docker-scripts/tree/develop/docker-redis) 에 미리 도커 스크립트들을 작성해 두었는데, 이 스크립트들을 이용하여 아래와 같이 Redis 컨테이너를 구동시키고, 인스턴스에 직접 접속해보자.

```bash
source docker-redis-start.sh
source docker-redis-repl.sh
```

  

그리고 접속한 컨테이너 내에서 아래와 같은 명령어를 통해 Redis 에서 지원하는 CLI 를 실행시키자.

```bash
# redis-cli
```



`keys * ` 명령어를 통해서 등록된 키의 목록들을 확인해보면 아래와 같은 결과가 나타난다. 

```plain
127.0.0.1:6379> keys *
1) "employees:300"
2) "employees:100"
3) "employees:200"
4) "employees"
```

