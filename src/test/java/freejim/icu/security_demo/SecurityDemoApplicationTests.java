package freejim.icu.security_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SecurityDemoApplicationTests {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
    }
    @Test
    void insertUser(){
        User user = new User();
        user.setName("user");
        user.setPassword("123456");
        user.setRole("USER");
        userService.insert(user);
    }

    @Test
    void findByName() {
        User admin = userRepository.findByName("user");
        System.out.println(admin);
    }

}
