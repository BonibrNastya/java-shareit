package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    ItemRequestServiceImpl requestService;
    private User user1;
    private Item item;
    private ItemRequest itemRequest;
    private static final LocalDateTime CREATE = LocalDateTime.now();
    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .email("name1@email.ru")
                .name("name1")
                .build();
        User user2 = User.builder()
                .id(2L)
                .email("name2@email.ru")
                .name("name2")
                .build();
        item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user2)
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("requestDescription")
                .requestor(user1)
                .created(CREATE)
                .build();
    }

    @Test
    void createRequest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.save(any()))
                .thenReturn(itemRequest);

        ItemRequest actualRequest = requestService.create(itemRequest, 1L);
        verify(requestRepository).save(itemRequestArgumentCaptor.capture());
        ItemRequest savedRequest = itemRequestArgumentCaptor.getValue();

        assertEquals(itemRequest, actualRequest);
        assertEquals(itemRequest.getId(), savedRequest.getId());
        assertEquals("requestDescription", savedRequest.getDescription());
        assertEquals(user1, savedRequest.getRequestor());
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    void getById_whenItemRequestDtoIsValid_returnItemRequestDto() {
        ItemRequestDto expectedRequest = RequestMapper.requestToDto(itemRequest);
        expectedRequest.setItems(new HashSet<>());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong()))
                .thenReturn(new ArrayList<>());

        ItemRequestDto actualRequest = requestService.getById(1L, 1L);

        assertEquals(expectedRequest, actualRequest);
    }

    @Test
    void getById_whenItemRequestDtoNitFound_returnException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        String message = assertThrows(ResponseStatusException.class,
                () -> requestService.getById(1L, 1L)).getMessage();

        assertEquals("404 NOT_FOUND \"Несуществующий запрос: id = 1\"", message);
    }

    @Test
    void getAllByRequestor() {
        ItemRequestDto expectedRequest = RequestMapper.requestToDto(itemRequest);
        expectedRequest.setItems(new HashSet<>());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.findByRequestorId(anyLong()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong()))
                .thenReturn(List.of(item));

        List<ItemRequestDto> actualRequest = requestService.getAllByRequestor(1L);

        assertEquals(expectedRequest.getId(), actualRequest.get(0).getId());
        assertEquals(expectedRequest.getDescription(), actualRequest.get(0).getDescription());
        assertEquals(expectedRequest.getCreated(), actualRequest.get(0).getCreated());
        assertEquals(expectedRequest.getItems(), actualRequest.get(0).getItems());
    }

    @Test
    void getAll() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(requestRepository.findAll((Pageable) any()))
                .thenReturn(Page.empty());
        when(itemRepository.findAll())
                .thenReturn(List.of(item));

        List<ItemRequestDto> actualRequest = requestService.getAll(1L, 1, 5);

        assertEquals(0, actualRequest.size());
    }

    @Test
    void getAll_whenPageNotValid() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        String message = assertThrows(ArithmeticException.class,
                () -> requestService.getAll(1L, -5, -5)).getMessage();

        assertEquals("Неверный индекс или количество элементов.", message);
    }
}