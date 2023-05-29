package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class RequestRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    private final ItemRequest itemRequest = new ItemRequest();
    private final User user = new User();
    final Pageable page = PageRequest.of(0, 5);

    @BeforeEach
    public void setUp() {
        user.setName("user1");
        user.setEmail("user1@mail.ru");
        userRepository.save(user);
        itemRequest.setDescription("requestDescription");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        requestRepository.save(itemRequest);
    }

    @Test
    void contentLoad() {
        assertNotNull(em);
    }

    @Test
    void findByRequestorId() {
        List<ItemRequest> requests = requestRepository.findByRequestorId(user.getId());

        assertEquals(1, requests.size());
        assertEquals(requests.get(0), itemRequest);
    }

    @Test
    void findAll() {
        List<ItemRequest> requests = requestRepository.findAll(page).getContent();

        assertEquals(1, requests.size());
        assertEquals(requests.get(0), itemRequest);
    }

    @AfterEach
    void deleteDB() {
        requestRepository.deleteAll();
    }
}