package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final User user1 = new User();
    private final User user2 = new User();
    private final ItemRequest itemRequest = new ItemRequest();
    private final ItemDto itemDto = new ItemDto();
    private final Item updateItem = new Item();
    private final Booking lastBooking = new Booking();
    private final Booking nextBooking = new Booking();
    private final Comment comment = new Comment();
    private ItemService itemService;
    private final CommentFromRequestDto commentFromRequest = new CommentFromRequestDto();

    @BeforeEach
    public void setUp() {
        itemService = new ItemServiceImpl(userRepository, itemRepository,
                bookingRepository, commentRepository, requestRepository);
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
        itemDto.setName("itemDtoName");
        itemDto.setDescription("itemDtoDescription");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());
        lastBooking.setStart(LocalDateTime.now().minusDays(5));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setBooker(user1);
        lastBooking.setStatus(Status.APPROVED);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(5));
        nextBooking.setBooker(user1);
        nextBooking.setStatus(Status.APPROVED);
        comment.setText("text");
        comment.setAuthor(user1);
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    void create() {
        ItemDto actualItem = itemService.create(itemDto, user1.getId());
        assertEquals("itemDtoName", actualItem.getName());
        assertEquals("itemDtoDescription", actualItem.getDescription());
        assertTrue(actualItem.getAvailable());
        assertEquals(itemRequest.getId(), actualItem.getRequestId());
    }

    @Test
    void update() {
        long itemId = itemService.create(itemDto, user2.getId()).getId();
        updateItem.setName("updateItem");
        updateItem.setDescription("updateDescription");
        Item actualItem = itemService.update(updateItem, user2.getId(), itemId);

        assertEquals(itemId, actualItem.getId());
        assertEquals("updateItem", actualItem.getName());
        assertEquals("updateDescription", actualItem.getDescription());
    }

    @Test
    void getAll() {
        ItemDto createdItem = itemService.create(itemDto, user1.getId());
        Item item = ItemMapper.toItem(createdItem, user1);
        lastBooking.setItem(item);
        nextBooking.setItem(item);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);
        comment.setItem(item);
        commentRepository.save(comment);
        List<ItemWithDateDto> listItems = itemService.getAll(user1.getId(), 0, 5);

        assertEquals(1, listItems.size());
        assertEquals(listItems.get(0).getId(), item.getId());
        assertEquals(listItems.get(0).getName(), item.getName());
        assertEquals(listItems.get(0).getDescription(), item.getDescription());
        assertEquals(listItems.get(0).getAvailable(), item.getAvailable());
        assertNotNull(listItems.get(0).getLastBooking());
        assertNotNull(listItems.get(0).getNextBooking());
        assertNotNull(listItems.get(0).getComments());
    }

    @Test
    void getById() {
        ItemDto createdItem = itemService.create(itemDto, user1.getId());
        Item item = ItemMapper.toItem(createdItem, user1);
        lastBooking.setItem(item);
        nextBooking.setItem(item);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);
        comment.setItem(item);
        commentRepository.save(comment);
        ItemWithDateDto listItems = itemService
                .getById(item.getId(), user1.getId());

        assertNotNull(listItems);
        assertEquals(listItems.getId(), item.getId());
        assertEquals(listItems.getName(), item.getName());
        assertEquals(listItems.getDescription(), item.getDescription());
        assertEquals(listItems.getAvailable(), item.getAvailable());
        assertNotNull(listItems.getLastBooking());
        assertNotNull(listItems.getNextBooking());
        assertNotNull(listItems.getComments());
    }

    @Test
    void searchItem() {
        itemService.create(itemDto, user2.getId());
        String text = "itemDtoName";
        List<ItemDto> items = itemService.searchItem(text, 0, 5);

        assertEquals(1, items.size());
        assertEquals(items.get(0).getName(), itemDto.getName());
        assertEquals(items.get(0).getDescription(), itemDto.getDescription());
        assertEquals(items.get(0).getAvailable(), itemDto.getAvailable());
        assertEquals(items.get(0).getRequestId(), itemDto.getRequestId());
    }

    @Test
    void createComment() {
        ItemDto createItem = itemService.create(itemDto, user2.getId());
        Item item = ItemMapper.toItem(createItem, user2);
        lastBooking.setItem(item);
        bookingRepository.save(lastBooking);
        commentFromRequest.setText("commentRequest");

        CommentDto commentDto = itemService
                .createComment(user1.getId(), item.getId(), commentFromRequest);

        assertNotNull(commentDto);
        assertEquals("commentRequest", commentDto.getText());
        assertEquals("user1", commentDto.getAuthorName());
    }
}