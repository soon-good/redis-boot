package io.study.redisboot.redis_repository.employee;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Getter
@RedisHash("employees")
public class Employee {

	@Id
	private Long id;

	private String name;

	private Double salary;

	@Builder
	public Employee(Long id, String name, Double salary){
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	@Override
	public String toString() {
		return "Employee{" +
			"id=" + id +
			", name='" + name + '\'' +
			", salary=" + salary +
			'}';
	}
}
