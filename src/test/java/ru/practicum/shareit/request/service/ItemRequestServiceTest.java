package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceTest {
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequest itemRequest = new ItemRequest();
    private ItemRequestService itemRequestService;
    private final User user1 = new User();
    private final User user2 = new User();
    private final Item item = new Item();

    @BeforeEach
    public void setUp() {
        itemRequestService = new ItemRequestServiceImpl(userRepository,
                requestRepository, itemRepository);
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        userRepository.save(user1);
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        userRepository.save(user2);
        itemRequest.setDescription("requestDescription");
        itemRequest.setRequestor(user1);
        requestRepository.save(itemRequest);
        item.setName("itemName");
        item.setDescription("itemDescription");
        item.setAvailable(true);
        item.setOwner(user1);
        item.setRequest(itemRequest);
        itemRepository.save(item);
    }

    @Test
    void create() {
        ItemRequest createRequest = itemRequestService
                .create(itemRequest, user1.getId());

        assertEquals(1L, createRequest.getId());
        assertEquals(itemRequest.getDescription(), createRequest.getDescription());
        assertEquals(itemRequest.getRequestor(), createRequest.getRequestor());
    }

    @Test
    void getById() {
        long requestId = itemRequestService
                .create(itemRequest, user1.getId()).getId();
        long userId = user1.getId();
        ItemRequestDto getingRequest = itemRequestService
                .getById(requestId, userId);

        assertEquals(requestId, getingRequest.getId());
        assertEquals(itemRequest.getDescription(), getingRequest.getDescription());
    }

    @Test
    void getAllByRequestor() {
        itemRequestService.create(itemRequest, user1.getId());
        List<ItemRequestDto> requestDtos = itemRequestService
                .getAllByRequestor(user1.getId());

        assertEquals(1, requestDtos.size());
        assertEquals(itemRequest.getDescription(), requestDtos.get(0).getDescription());
    }

    @Test
    void getAll() {
        itemRequestService.create(itemRequest, user2.getId());
        List<ItemRequestDto> requestDtos = itemRequestService
                .getAll(user1.getId(), 0, 5);

        assertEquals(1, requestDtos.size());
        assertEquals(itemRequest.getDescription(), requestDtos.get(0).getDescription());
    }
}