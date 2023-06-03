package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequest create(@RequestHeader(REQUEST_HEADER) Long userId,
                              @Valid @RequestBody ItemRequest itemRequest) {
        return itemRequestService.create(itemRequest, userId);
    }

    @GetMapping("{id}")
    public ItemRequestDto getById(@RequestHeader(REQUEST_HEADER) Long userId,
                                  @PathVariable("id") long requestId) {
        return itemRequestService.getById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequestor(@RequestHeader(REQUEST_HEADER) Long userId) {
        return itemRequestService.getAllByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(REQUEST_HEADER) Long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAll(userId, from, size);
    }
}