package com.library.management;

import com.library.management.model.User;
import com.library.management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    public DataSeeder(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public void run(String... args) throws Exception {
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); 
            admin.setFullName("System Administrator");
            admin.setEmail("admin@library.com");
            admin.setRole("ADMIN");
            admin.setActive(true);
            userRepo.save(admin);
            System.out.println("✅ Đã tạo tài khoản Admin mặc định: admin / admin123");
        }
    }
}