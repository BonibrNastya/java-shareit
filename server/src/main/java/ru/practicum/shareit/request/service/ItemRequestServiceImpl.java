package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequest create(ItemRequest itemRequest, long userId) {
        User user = getUserOrException(userId);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Создан запрос: {}", itemRequest.getDescription());
        return requestRepository.save(itemRequest);
    }

    @Override
    public ItemRequestDto getById(long requestId, long userId) {
        getUserOrException(userId);
        ItemRequestDto itemRequestDto = RequestMapper.requestToDto(requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Несуществующий запрос: id = " + requestId)));
        List<Item> itemsByRequestId = itemRepository.findByRequestId(requestId);
        setItemsToRequest(itemRequestDto, itemsByRequestId);
        log.info("Поиск запроса по id = " + requestId);
        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getAllByRequestor(long userId) {
        getUserOrException(userId);
        List<ItemRequestDto> requests = requestRepository.findByRequestorId(userId).stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(RequestMapper::requestToDto)
                .collect(Collectors.toList());
        List<Item> itemsByRequestId = itemRepository.findByRequestId(userId);
        for (ItemRequestDto request : requests) {
            setItemsToRequest(request, itemsByRequestId);
        }
        log.info("Запрос запросов от пользователя с id = {}", userId);
        return requests;
    }

    @Override
    public List<ItemRequestDto> getAll(long userId, int from, int size) {
        getUserOrException(userId);
        if (from >= 0 && size > 0) {
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0,
                    size, Sort.by("created").descending());
            List<ItemRequestDto> requests = requestRepository.findAll(page).get()
                    .filter(r -> r.getRequestor().getId() != userId)
                    .map(RequestMapper::requestToDto)
                    .collect(Collectors.toList());
            List<Item> items = itemRepository.findAll();
            for (ItemRequestDto request : requests) {
                setItemsToRequest(request, items);
            }
            log.info("Запрос всех запросов с {} элемента по {}шт на странице.", from, size);
            return requests;
        } else {
            throw new ArithmeticException("Неверный индекс или количество элементов.");
        }
    }

    private User getUserOrException(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден. " +
                        "Добавление/обновление запроса невозможно."));
    }

    private void setItemsToRequest(ItemRequestDto request, List<Item> items) {
        request.setItems(items.stream()
                .map(ItemMapper::toItemDto)
                .filter(i -> Objects.nonNull(i.getRequestId()))
                .filter(i -> i.getRequestId() == request.getId())
                .collect(Collectors.toSet()));
    }
}