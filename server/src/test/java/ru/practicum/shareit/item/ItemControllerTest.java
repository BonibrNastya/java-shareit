package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private Item item;
    private ItemDto itemDto;
    private ItemWithDateDto itemWithDateDto;
    private CommentDto comment;
    private final CommentFromRequestDto commentFromRequestDto = new CommentFromRequestDto();
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private static final String REQUEST_ITEMS = "/items";
    private static final String REQUEST_ITEM_WITH_ID = "/items/1";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(itemController).build();
        item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();
        itemWithDateDto = ItemWithDateDto.builder()
                .id(2L)
                .name("nameWithDate")
                .description("description2")
                .available(true)
                .build();
        comment = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("name")
                .build();
    }

    @SneakyThrows
    @Test
    void createItem_whenAllRight_returnOkAndItemDto() {
        when(itemService.create(any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(post(REQUEST_ITEMS)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).create(eq(itemDto), anyLong());
    }

    @SneakyThrows
    @Test
    void createItem_whenItemNotValid_returnException() {
        ItemDto nameWithSpace = ItemDto.builder()
                .id(2L)
                .name(" ")
                .description("description")
                .available(true)
                .build();
        ItemDto nameIsAvailable = ItemDto.builder()
                .id(3L)
                .description("description")
                .available(true)
                .build();
        ItemDto descriptionIsAvailable = ItemDto.builder()
                .id(4L)
                .name("name")
                .available(true)
                .build();

        mvc.perform(post(REQUEST_ITEMS)
                        .content(mapper.writeValueAsString(nameWithSpace))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());

        mvc.perform(post(REQUEST_ITEMS)
                        .content(mapper.writeValueAsString(nameIsAvailable))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());

        mvc.perform(post(REQUEST_ITEMS)
                        .content(mapper.writeValueAsString(descriptionIsAvailable))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).create(eq(itemDto), anyLong());
    }

    @SneakyThrows
    @Test
    void update() {
        Item updateItem = Item.builder()
                .id(1L)
                .name("update")
                .description("update")
                .available(true)
                .build();

        when(itemService.update(any(Item.class), anyLong(), anyLong()))
                .thenReturn(updateItem);

        mvc.perform(patch(REQUEST_ITEM_WITH_ID)
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateItem.getName())))
                .andExpect(jsonPath("$.description", is(updateItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updateItem.getAvailable())));
        verify(itemService, times(1)).update(eq(item), anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getAll_whenListItemIsNull_returnOk() {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());

        mvc.perform(get(REQUEST_ITEMS)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void getAll_whenListItemIsNotNull_returnOkAndListItems() {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemWithDateDto));

        mvc.perform(get(REQUEST_ITEMS)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemWithDateDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithDateDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithDateDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemWithDateDto.getAvailable())));
    }

    @SneakyThrows
    @Test
    void getById() {
        when(itemService.getById(anyLong(), anyLong()))
                .thenReturn(itemWithDateDto);

        mvc.perform(get(REQUEST_ITEM_WITH_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithDateDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithDateDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithDateDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithDateDto.getAvailable())));
    }

    @SneakyThrows
    @Test
    void search() {
        String text = "text";

        when(itemService.searchItem(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));

        mvc.perform(get(REQUEST_ITEMS + "/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param(text, text))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @SneakyThrows
    @Test
    void createComment_whenCommentIsValid_returnOkAndCommentDto() {
        commentFromRequestDto.setText("text");

        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(comment);

        mvc.perform(post(REQUEST_ITEM_WITH_ID + "/comment")
                        .content(mapper.writeValueAsString(commentFromRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())));

        verify(itemService, times(1))
                .createComment(anyLong(), anyLong(), eq(commentFromRequestDto));
    }

    @SneakyThrows
    @Test
    void createComment_whenCommentIsNotValid_returnException() {
        mvc.perform(post(REQUEST_ITEM_WITH_ID + "/comment")
                        .content(mapper.writeValueAsString(commentFromRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemService, never())
                .createComment(anyLong(), anyLong(), eq(commentFromRequestDto));
    }
}