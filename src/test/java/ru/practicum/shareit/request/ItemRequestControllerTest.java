package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController itemRequestController;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private static final String REQUEST_REQUESTS = "/requests";
    private static final String REQUEST_REQUEST_WITH_ID = "/requests/1";
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemRequestController).build();
        mapper.findAndRegisterModules();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("descriptionDto")
                .created(LocalDateTime.now())
                .items(Collections.emptySet())
                .build();
    }

    @SneakyThrows
    @Test
    void createRequest_whenValid_returnOkAndItemRequest() {
        when(itemRequestService.create(any(), anyLong()))
                .thenReturn(itemRequest);

        mvc.perform(post(REQUEST_REQUESTS)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.requestor.email", is(itemRequest.getRequestor().getEmail())))
                .andExpect(jsonPath("$.requestor.name", is(itemRequest.getRequestor().getName())));

        verify(itemRequestService, times(1)).create(eq(itemRequest), anyLong());
    }

    @SneakyThrows
    @Test
    void getById() {
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);

        mvc.perform(get(REQUEST_REQUEST_WITH_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.items.size()", is(0)));
    }

    @SneakyThrows
    @Test
    void getAllByRequestor() {
        when(itemRequestService.getAllByRequestor(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get(REQUEST_REQUESTS)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].items.size()", is(0)));
    }

    @SneakyThrows
    @Test
    void getAll() {
        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(REQUEST_REQUESTS + "/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}