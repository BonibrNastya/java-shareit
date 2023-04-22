package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exception.NotFoundItemException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1L;

    @Override
    public ItemDto create(Item item) {
        item.setId(idCounter);
        items.put(idCounter, item);
        idCounter++;
        log.info("Добавлена вещь: {}.", item);
        return ItemMapper.toItemDto(items.get(item.getId()));
    }

    @Override
    public ItemDto update(Item item, long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundItemException(String.format("Вещь с id %d не найдена.", itemId));
        }
        item.setId(itemId);
        Item oldItem = items.get(itemId);
        if (item.getName() == null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(oldItem.getAvailable());
        }
        items.remove(itemId);
        items.put(itemId, item);
        log.info("Обновлена вещь: {}.", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAll(long id) {
        return items.values().stream().filter((u) -> u.getOwner() == id)
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public Item getById(long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundItemException(String.format("Вещь с id = %d не найдена.", id));
        }
        return items.get(id);
    }

    @Override
    public List<ItemDto> searchItem(String query) {
        return items.values().stream()
                .filter((i) -> i.getDescription().toLowerCase().contains(query.toLowerCase())
                        && (i.getAvailable()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
