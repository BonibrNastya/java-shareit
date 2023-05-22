package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("name@email.ru")
                .name("name")
                .build();
    }

    @Test
    void createUser_whenValid_returnUser() {
        when(userRepository.save(user)).thenReturn(user);

        User actualUser = userService.create(user);

        assertEquals(user, actualUser);
        verify(userRepository).save(user);
    }

    @Test
    void getAll_whenListUsersNotNull_returnListUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<User> actualListUsers = userService.getAll();

        assertEquals(List.of(user), actualListUsers);
    }

    @Test
    void getByIdOrUpdate_whenUserFound_returnUser() {
        User expextedUser = new User();
        when(userRepository.findById(0L)).thenReturn(Optional.of(expextedUser));

        User actualUser = userService.getById(0L);

        assertEquals(expextedUser, actualUser);
    }

    @Test
    void getById_whenUserNotFound_returnNotFoundExceptionAndRightMessage() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        String message = assertThrows(ResponseStatusException.class,
                () -> userService.getById(0L)).getMessage();
        assertEquals("404 NOT_FOUND \"Пользователь не найден.\"", message);
    }

    @Test
    void updateUserEmail_whenFound_returnUpdateUser() {
        User userWithNewEmail = User.builder()
                .email("update@mail.ru")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.update(userWithNewEmail, 1L);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("update@mail.ru", savedUser.getEmail());
        assertEquals("name", savedUser.getName());
    }

    @Test
    void updateUserName_whenFound_returnUpdateUser() {
        User userWithNewName = User.builder()
                .name("updateName")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.update(userWithNewName, 1L);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("name@email.ru", savedUser.getEmail());
        assertEquals("updateName", savedUser.getName());
    }

    @Test
    void updateUser_whenFound_returnUpdateUser() {
        User newUser = User.builder()
                .email("newEmail@mail.ru")
                .name("newName")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.update(newUser, 1L);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("newEmail@mail.ru", savedUser.getEmail());
        assertEquals("newName", savedUser.getName());
    }

    @Test
    void delete() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }
}