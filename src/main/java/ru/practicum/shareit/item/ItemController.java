package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(REQUEST_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("{id}")
    public ItemDto update(@RequestHeader(REQUEST_HEADER) Long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable("id") long itemId) {
        return itemService.update(itemDto, userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(REQUEST_HEADER) Long userId) {
        return itemService.getAll(userId);
    }

    @GetMapping("{id}")
    public Item getById(@PathVariable("id") long id) {
        return itemService.getById(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.searchItem(text);
    }
}
