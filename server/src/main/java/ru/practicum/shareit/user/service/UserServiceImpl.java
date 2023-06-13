package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        log.info("Создан пользователь {}.", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение списка пользователей.");
        return userRepository.findAll();
    }

    @Override
    public User getById(long id) {
        log.info("Получение пользователя по id = {}.", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден."));
    }

    @Override
    public User update(User user, long id) {
        User updateUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден."));
        if (nonNull(user.getEmail())) {
            updateUser.setEmail(user.getEmail());
        }
        if (nonNull(user.getName())) {
            updateUser.setName(user.getName());
        }
        log.info("Обновление пользователя {}.", updateUser);
        return userRepository.save(updateUser);
    }

    @Override
    public void delete(long id) {
        userRepository.deleteById(id);
        log.info("Удаление пользователя с id = {}.", id);
    }
}