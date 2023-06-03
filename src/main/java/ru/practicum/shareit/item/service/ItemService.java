package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, long userId);

    Item update(Item item, long userId, long itemId);

    List<ItemWithDateDto> getAll(long id, int from, int size);

    ItemWithDateDto getById(long itemId, long userId);

    List<ItemDto> searchItem(String query, int from, int size);

    CommentDto createComment(long userId, long itemId, CommentFromRequestDto comment);
}
