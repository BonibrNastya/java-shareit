package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping
    public Collection<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("{id}")
    public User getById(@PathVariable("id") long userId) {
        return userService.getById(userId);
    }

    @PatchMapping("/{userId}")
    public User update(@RequestBody User user,
                       @PathVariable long userId) {
        return userService.update(user, userId);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable("id") long userId) {
        userService.delete(userId);
    }
}