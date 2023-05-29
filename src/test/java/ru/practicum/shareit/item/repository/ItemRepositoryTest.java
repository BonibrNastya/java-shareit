package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RequestRepository requestRepository;
    private final User user1 = new User();
    private final User user2 = new User();
    private final Item item1 = new Item();
    private final Item item2 = new Item();
    private final ItemRequest itemRequest = new ItemRequest();
    final Pageable page = PageRequest.of(0, 5);

    @BeforeEach
    public void setUp() {
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        userRepository.save(user1);
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        userRepository.save(user2);
        itemRequest.setDescription("requestDescription");
        itemRequest.setRequestor(user1);
        itemRequest.setCreated(LocalDateTime.now());
        requestRepository.save(itemRequest);
        item1.setName("itemName");
        item1.setDescription("itemDescription");
        item1.setAvailable(true);
        item1.setOwner(user2);
        itemRepository.save(item1);
        item2.setName("itemName2");
        item2.setDescription("itemDescription2");
        item2.setAvailable(true);
        item2.setOwner(user1);
        item2.setRequest(itemRequest);
        itemRepository.save(item2);
    }

    @Test
    void contentLoad() {
        assertNotNull(em);
    }

    @Test
    void findByOwnerId() {
        long userId = user2.getId();

        List<Item> items = itemRepository.findByOwnerId(userId, page)
                .getContent();

        assertEquals(1, items.size());
        assertEquals(items.get(0), item1);
    }

    @Test
    void findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue() {
        String text = "2";

        List<Item> relevantItem = itemRepository
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(
                        text, text, page).getContent();

        assertEquals(1, relevantItem.size());
        assertEquals(relevantItem.get(0), item2);
    }

    @Test
    void findByRequestId() {
        long requestId = itemRequest.getId();

        List<Item> items = itemRepository.findByRequestId(requestId);

        assertEquals(1, items.size());
        assertEquals(items.get(0), item2);
    }

    @AfterEach
    void deleteDB() {
        itemRepository.deleteAll();
    }
}