package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private RequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private final Comment comment = new Comment();
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .email("name1@email.ru")
                .name("name1")
                .build();
        user2 = User.builder()
                .id(2L)
                .email("name2@email.ru")
                .name("name2")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("requestDescription")
                .requestor(user2)
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user1)
                .request(itemRequest)
                .build();
        item2 = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user2)
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("itemDtoDescription")
                .available(true)
                .requestId(1L)
                .build();
    }

    @Test
    void create_whenItemAndUserValidAndRequestorNonNull_returnOkAndItemDto() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item1))
                .thenReturn(item1);

        ItemDto actualItem = itemService.create(ItemMapper.toItemDto(item1), user1.getId());
        ItemDto expectedItem = ItemMapper.toItemDto(item1);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(actualItem, expectedItem);
        assertEquals(item1.getId(), savedItem.getRequest().getId());
        assertEquals(itemRequest, savedItem.getRequest());
        verify(itemRepository).save(item1);
    }

    @Test
    void create_whenItemAndUserValidAndRequestorNull_returnOkAndItemDto() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.save(item2))
                .thenReturn(item2);

        ItemDto actualItem = itemService.create(ItemMapper.toItemDto(item2), user2.getId());
        ItemDto expectedItem = ItemMapper.toItemDto(item2);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(actualItem, expectedItem);
        assertNull(savedItem.getRequest());
        verify(itemRepository).save(item2);
    }

    @Test
    void createOrUpdate_whenUserNotFound_returnException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        String message = assertThrows(ResponseStatusException.class,
                () -> itemService.create(itemDto, 0L)).getMessage();

        assertEquals("404 NOT_FOUND \"Пользователь не найден. " +
                "Добавление/обновление вещи невозможно.\"", message);
    }

    @Test
    void update_whenItemIsValidAndWithNewName_returnOkAndItem() {
        Item updateItem = Item.builder()
                .name("updateName")
                .owner(user1)
                .build();
        Item expectedItem = Item.builder()
                .id(1L)
                .name("updateName")
                .description("itemDescription")
                .available(true)
                .owner(user1)
                .request(itemRequest)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(itemRepository.getReferenceById(anyLong()))
                .thenReturn(item1);
        when(itemRepository.save(any()))
                .thenReturn(expectedItem);

        itemService.update(updateItem, 1L, 1L);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(item1.getId(), savedItem.getId());
        assertEquals("updateName", savedItem.getName());
        assertEquals(item1.getDescription(), savedItem.getDescription());
        assertEquals(item1.getAvailable(), savedItem.getAvailable());
        assertEquals(item1.getOwner(), savedItem.getOwner());
        assertEquals(item1.getRequest(), savedItem.getRequest());
    }

    @Test
    void update_whenItemIsValidAndWithNewDescription_returnOkAndItem() {
        Item updateItem = Item.builder()
                .description("updateDescription")
                .owner(user1)
                .build();
        Item expectedItem = Item.builder()
                .id(1L)
                .name("itemName")
                .description("updateDescription")
                .available(true)
                .owner(user1)
                .request(itemRequest)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(itemRepository.getReferenceById(anyLong()))
                .thenReturn(item1);
        when(itemRepository.save(any()))
                .thenReturn(expectedItem);

        itemService.update(updateItem, 1L, 1L);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(item1.getId(), savedItem.getId());
        assertEquals(item1.getName(), savedItem.getName());
        assertEquals("updateDescription", savedItem.getDescription());
        assertEquals(item1.getAvailable(), savedItem.getAvailable());
        assertEquals(item1.getOwner(), savedItem.getOwner());
        assertEquals(item1.getRequest(), savedItem.getRequest());
    }

    @Test
    void update_whenItemIsValidAndWithNewAvailable_returnOkAndItem() {
        Item updateItem = Item.builder()
                .available(false)
                .owner(user1)
                .build();
        Item expectedItem = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(false)
                .owner(user1)
                .request(itemRequest)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(itemRepository.getReferenceById(anyLong()))
                .thenReturn(item1);
        when(itemRepository.save(any()))
                .thenReturn(expectedItem);

        itemService.update(updateItem, 1L, 1L);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals(item1.getId(), savedItem.getId());
        assertEquals(item1.getName(), savedItem.getName());
        assertEquals(item1.getDescription(), savedItem.getDescription());
        assertEquals(false, savedItem.getAvailable());
        assertEquals(item1.getOwner(), savedItem.getOwner());
        assertEquals(item1.getRequest(), savedItem.getRequest());
    }

    @Test
    void update_whenOwnerIdNotEqualsUserId_returnExceptionAndRightMessage() {
        when(itemRepository.getReferenceById(anyLong()))
                .thenReturn(item1);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));

        String message = assertThrows(ResponseStatusException.class,
                () -> itemService.update(item1, 2L, 1L)).getMessage();

        assertEquals("404 NOT_FOUND \"Невозможно обновить вещь невалидному пользователю.\"", message);
    }

    @Test
    void getAll_whenPageValid_returnListItemWithDateDto() {
        when(itemRepository.findByOwnerId(anyLong(), any()))
                .thenReturn(Page.empty());
        when(bookingRepository.findAllByItem_IdInAndStatus(any(), any()))
                .thenReturn(Collections.emptyList());

        List<ItemWithDateDto> listItems = itemService.getAll(1L, 0, 1);

        assertTrue(listItems.isEmpty());
        assertNotNull(listItems);
    }

    @Test
    void getAll_whenPageNotValid_returnException() {
        String nullablePage = assertThrows(ArithmeticException.class,
                () -> itemService.getAll(1L, 0, 0)).getMessage();
        String negativeFrom = assertThrows(ArithmeticException.class,
                () -> itemService.getAll(1L, -1, 1)).getMessage();
        String negativeSize = assertThrows(ArithmeticException.class,
                () -> itemService.getAll(1L, 1, -1)).getMessage();
        String allNegative = assertThrows(ArithmeticException.class,
                () -> itemService.getAll(1L, -1, -1)).getMessage();

        assertEquals("Неверный индекс или количество элементов.", nullablePage);
        assertEquals("Неверный индекс или количество элементов.", negativeSize);
        assertEquals("Неверный индекс или количество элементов.", negativeFrom);
        assertEquals("Неверный индекс или количество элементов.", allNegative);
    }

    @Test
    void getById_whenValidItemToNotOwnerWithComment_returnItemWithDateDtoWithoutBooking() {
        comment.setId(1L);
        comment.setText("comment");
        comment.setItem(item1);
        comment.setAuthor(user2);

        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(commentRepository.findCommentsByItem_Id(anyLong()))
                .thenReturn(Set.of(comment));
        ItemWithDateDto actualItem = itemService.getById(1L, 1L);

        assertEquals(item1.getId(), actualItem.getId());
        assertEquals(item1.getName(), actualItem.getName());
        assertEquals(item1.getDescription(), actualItem.getDescription());
        assertEquals(item1.getAvailable(), actualItem.getAvailable());
        assertNull(actualItem.getLastBooking());
        assertNull(actualItem.getNextBooking());
        assertEquals(Set.of(CommentMapper.commentDto(comment)),
                actualItem.getComments());
    }

    @Test
    void getById_whenValidItemToNotOwnerWithoutComment_returnItemWithDateDtoWithoutBooking() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(commentRepository.findCommentsByItem_Id(anyLong()))
                .thenReturn(Collections.emptySet());
        ItemWithDateDto actualItem = itemService.getById(1L, 1L);

        assertEquals(item1.getId(), actualItem.getId());
        assertEquals(item1.getName(), actualItem.getName());
        assertEquals(item1.getDescription(), actualItem.getDescription());
        assertEquals(item1.getAvailable(), actualItem.getAvailable());
        assertNull(actualItem.getLastBooking());
        assertNull(actualItem.getNextBooking());
        assertTrue(actualItem.getComments().isEmpty());
    }

    @Test
    void getById_whenItemNotFound_returnException() {
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        String message = assertThrows(ResponseStatusException.class,
                () -> itemService.getById(1L, 1L)).getMessage();

        assertEquals("404 NOT_FOUND \"Вещь не найдена.\"", message);

    }

    @Test
    void searchItem() {
        when(itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(
                anyString(), anyString(), any()))
                .thenReturn(Page.empty());

        List<ItemDto> list = itemService.searchItem("text", 1, 2);

        assertTrue(list.isEmpty());
        assertNotNull(list);
    }

    @Test
    void searchItem_whenPageNotValid_returnException() {
        String nullablePage = assertThrows(ArithmeticException.class,
                () -> itemService.searchItem("t", 0, 0)).getMessage();
        String negativeFrom = assertThrows(ArithmeticException.class,
                () -> itemService.searchItem("t", -1, 1)).getMessage();
        String negativeSize = assertThrows(ArithmeticException.class,
                () -> itemService.searchItem("L", 1, -1)).getMessage();
        String allNegative = assertThrows(ArithmeticException.class,
                () -> itemService.searchItem("L", -1, -1)).getMessage();

        assertEquals("Неверный индекс или количество элементов.", nullablePage);
        assertEquals("Неверный индекс или количество элементов.", negativeSize);
        assertEquals("Неверный индекс или количество элементов.", negativeFrom);
        assertEquals("Неверный индекс или количество элементов.", allNegative);
    }

    @Test
    void createComment_whenUserGetItem_returnCommentDto() {
        CommentFromRequestDto createComment = new CommentFromRequestDto();
        createComment.setText("comment");
        Comment expectedComment = Comment.builder()
                .id(1L)
                .text("comment")
                .item(item1)
                .author(user2)
                .created(LocalDateTime.now())
                .build();
        Booking newBooking = Booking.builder()
                .id(2L)
                .item(item1)
                .booker(user2)
                .status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        when(bookingRepository.findByBooker_IdAndEndIsBefore(anyLong(), any()))
                .thenReturn(List.of(newBooking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item1));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user2));
        when(commentRepository.save(any()))
                .thenReturn(expectedComment);

        CommentDto actualComment = itemService
                .createComment(user2.getId(), item1.getId(), createComment);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        CommentDto savedComment = CommentMapper.commentDto(commentArgumentCaptor.getValue());

        assertEquals(savedComment, actualComment);
    }

    @Test
    void createComment_whenUserNotBooker_returnException() {
        CommentFromRequestDto createComment = new CommentFromRequestDto();
        createComment.setText("comment");
        Booking newBooking = Booking.builder()
                .id(2L)
                .item(item2)
                .booker(user2)
                .status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        when(bookingRepository.findByBooker_IdAndEndIsBefore(anyLong(), any()))
                .thenReturn(List.of(newBooking));

        String message = assertThrows(ResponseStatusException.class,
                () -> itemService.createComment(
                        1L, 10L, createComment))
                .getMessage();

        assertEquals("400 BAD_REQUEST \"Вещь не была в аренде у пользователя с id = 1\"", message);
    }
}