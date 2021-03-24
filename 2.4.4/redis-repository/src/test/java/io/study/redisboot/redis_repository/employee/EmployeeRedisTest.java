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

		repository.deleteAll();
	}
}
