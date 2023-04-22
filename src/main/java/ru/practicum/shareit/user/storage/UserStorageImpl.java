package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1L;

    @Override
    public User create(User user) {
        boolean isNotValid = users.values().stream()
                .map(User::getEmail).anyMatch(user.getEmail()::equals);
        if (isNotValid) {
            throw new DuplicateEmailException(String.format("Пользователь %S уже существует.", user.getEmail()));
        }
        user.setId(idCounter);
        users.put(idCounter, user);
        idCounter++;
        log.info("Создан пользователь {}.", user);
        return users.get(user.getId());
    }

    @Override
    public Collection<User> getAll() {
        log.info("Текущее количество пользователей: {}.", users.size());
        return users.values();
    }

    @Override
    public User getById(long id) {
        foundUser(id);
        log.info("Получили пользователя с id {}: {}.", id, users.get(id));
        return users.get(id);
    }

    @Override
    public User update(User user, long id) {
        foundUser(id);
        if (user.getEmail() != null) {
            boolean isValid = users.values().stream()
                    .filter((u) -> u.getId() != id)
                    .map(User::getEmail).anyMatch(user.getEmail()::equals);
            if (isValid) {
                throw new DuplicateEmailException(String.format("Пользователь %S уже существует.", user.getEmail()));
            }
        }
        User updateUser = new User(id, user.getEmail(), user.getName());
        if (user.getEmail() == null) {
            updateUser.setEmail(users.get(id).getEmail());
        } else {
            updateUser.setEmail(user.getEmail());
        }
        if (user.getName() == null) {
            updateUser.setName(users.get(id).getName());
        }
        users.remove(id);
        users.put(id, updateUser);
        log.info("Обновили пользователя {}.", updateUser);
        return updateUser;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
        log.info("Удалили пользователя с id {}.", id);
    }

    public void foundUser(Long id) {
        if (!users.containsKey(id) || id == null) {
            log.info("Несуществующий или пустой id пользователя: {}.", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id = %d не найден.", id));
        }
    }
}