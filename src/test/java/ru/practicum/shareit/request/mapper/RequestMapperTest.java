package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestMapperTest {

    @Test
    void requestToDto() {
        LocalDateTime date = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requestor(user)
                .created(date)
                .build();

        ItemRequestDto itemRequestDto = RequestMapper.requestToDto(itemRequest);
        assertEquals(itemRequest.getId(), itemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestDto.getDescription());
        assertEquals(date, itemRequestDto.getCreated());
        assertNull(itemRequestDto.getItems());
    }
}