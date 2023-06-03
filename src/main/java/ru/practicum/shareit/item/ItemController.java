package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(REQUEST_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("{id}")
    public Item update(@RequestHeader(REQUEST_HEADER) Long userId,
                       @RequestBody Item item,
                       @PathVariable("id") long itemId) {
        return itemService.update(item, userId, itemId);
    }

    @GetMapping
    public List<ItemWithDateDto> getAll(@RequestHeader(REQUEST_HEADER) Long userId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return itemService.getAll(userId, from, size);
    }

    @GetMapping("{id}")
    public ItemWithDateDto getById(@RequestHeader(REQUEST_HEADER) Long userId,
                                   @PathVariable("id") long itemId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{id}/comment")
    public CommentDto createComment(@RequestHeader(REQUEST_HEADER) Long userId,
                                    @PathVariable("id") long itemId,
                                    @Valid @RequestBody CommentFromRequestDto comment) {
        return itemService.createComment(userId, itemId, comment);
    }
}