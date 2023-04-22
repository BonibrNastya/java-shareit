package ru.practicum.shareit.request.storage;

import ru.practicum.shareit.request.ItemRequest;

public interface ItemRequestStorage {
    ItemRequest getById(long id);

}
