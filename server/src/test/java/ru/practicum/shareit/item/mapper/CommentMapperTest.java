package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentMapperTest {
    @Test
    void commentDto() {
        Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("itemDescription")
                .available(true)
                .build();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("text")
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        CommentDto commentDto = CommentMapper.commentDto(comment);

        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals("User", commentDto.getAuthorName());
    }
}