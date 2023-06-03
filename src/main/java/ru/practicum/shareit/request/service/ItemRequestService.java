package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest create(ItemRequest itemRequest, long userId);

    ItemRequestDto getById(long requestId, long userId);

    List<ItemRequestDto> getAllByRequestor(long userId);

    List<ItemRequestDto> getAll(long userId, int from, int size);
}