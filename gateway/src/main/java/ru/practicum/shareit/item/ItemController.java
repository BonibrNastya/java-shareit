package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> update(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable("id") long itemId) {
        return itemClient.update(itemDto, userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.getAll(userId, from, size);
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getById(@RequestHeader(REQUEST_HEADER) Long userId,
                                          @PathVariable("id") long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @RequestParam String text,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemClient.search(text, from, size, userId);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(REQUEST_HEADER) Long userId,
                                                @PathVariable("id") long itemId,
                                                @Valid @RequestBody CommentFromRequestDto comment) {
        return itemClient.createComment(userId, itemId, comment);
    }
}
