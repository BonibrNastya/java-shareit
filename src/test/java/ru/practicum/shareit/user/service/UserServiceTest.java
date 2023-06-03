package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {
    private UserService userService;
    private final UserRepository userRepository;
    private final User user1 = new User();
    private final User updateUser = new User();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
        user1.setName("name1");
        user1.setEmail("name1@mail.ru");
    }

    @Test
    void create() {
        User actualUser = userService.create(user1);

        assertEquals(user1.getId(), actualUser.getId());
        assertEquals("name1", actualUser.getName());
        assertEquals("name1@mail.ru", actualUser.getEmail());
    }

    @Test
    void getAll() {
        userService.create(user1);
        updateUser.setName("updateName");
        updateUser.setEmail("update@mail.ru");
        userService.create(updateUser);
        Collection<User> users = userService.getAll();

        assertEquals(2, users.size());
        assertTrue(users.contains(updateUser));
        assertTrue(users.contains(user1));
    }

    @Test
    void getById() {
        userService.create(user1);
        long userId = user1.getId();
        User actualUser = userService.getById(userId);

        assertEquals(userId, actualUser.getId());
        assertEquals("name1", actualUser.getName());
        assertEquals("name1@mail.ru", actualUser.getEmail());
    }

    @Test
    void update() {
        userService.create(user1);
        long userId = user1.getId();
        updateUser.setName("updateName");
        updateUser.setEmail("update@mail.ru");
        User actualUser = userService.update(updateUser, userId);

        assertEquals(userId, actualUser.getId());
        assertEquals("updateName", actualUser.getName());
        assertEquals("update@mail.ru", actualUser.getEmail());
    }

    @Test
    void delete() {
        userService.create(user1);
        long userId = user1.getId();
        updateUser.setName("updateName");
        updateUser.setEmail("update@mail.ru");
        userService.create(updateUser);
        userService.delete(userId);
        Collection<User> users = userService.getAll();

        assertEquals(1, users.size());
        assertTrue(users.contains(updateUser));
        assertFalse(users.contains(user1));
    }
}