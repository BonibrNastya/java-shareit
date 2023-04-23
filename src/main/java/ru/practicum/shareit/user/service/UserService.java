package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    User create(User user);

    Collection<User> getAll();

    User getById(long id);

    User update(User user, long id);

    void delete(long id);
}