package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    ItemDto create(Item item);

    ItemDto update(Item itemDto, long itemId);

    List<ItemDto> getAll(long id);

    Item getById(long id);

    List<ItemDto> searchItem(String query);
}
