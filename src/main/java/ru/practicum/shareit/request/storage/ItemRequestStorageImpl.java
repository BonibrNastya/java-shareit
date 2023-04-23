package ru.practicum.shareit.request.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ItemRequestStorageImpl implements ItemRequestStorage {
    private final Map<Long, ItemRequest> itemRequests = new HashMap<>();

    @Override
    public ItemRequest getById(long id) {
        ItemRequest itemRequestRaw = ItemRequest.builder().id(id).build();
        itemRequests.put(id, itemRequestRaw);
        return itemRequests.get(id);
    }
}
