package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemMapperTest {
    @Test
    void toItemDto() {
        Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("itemDescription")
                .available(true)
                .build();
        ItemDto itemDto = ItemMapper.toItemDto(item);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertNull(itemDto.getRequestId());
    }

    @Test
    void toItem() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("itemDto")
                .description("descriptionDto")
                .available(false)
                .build();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();

        Item item = ItemMapper.toItem(itemDto, user);

        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
        assertEquals(item.getOwner(), user);
    }
}