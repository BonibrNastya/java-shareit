package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(ItemDto itemDto, long userId) {
        userStorage.getById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        return itemStorage.create(item);
    }

    @Override
    public ItemDto update(ItemDto itemDto, long userId, long itemId) {
        if (userStorage.getById(userId) == null || itemStorage.getById(itemId).getOwner() != userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Невозможно обновить вещь невалидному пользователю.");
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        return itemStorage.update(item, itemId);
    }

    @Override
    public List<ItemDto> getAll(long id) {
        return itemStorage.getAll(id);
    }

    @Override
    public Item getById(long id) {
        return itemStorage.getById(id);
    }

    @Override
    public List<ItemDto> searchItem(String query) {
        if (query.isEmpty()) {
            return new ArrayList<>();
        }
        return itemStorage.searchItem(query);
    }
}