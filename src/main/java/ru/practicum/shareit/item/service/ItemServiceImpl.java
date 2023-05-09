package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentFromRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(ItemDto itemDto, long userId) {
        User user = getUserOrException(userId);
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, user));
        log.info("Создана вещь {}.", item.getName());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Item update(Item item, long userId, long itemId) {
        getUserOrException(userId);
        if (itemRepository.getReferenceById(itemId).getOwner().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Невозможно обновить вещь невалидному пользователю.");
        }
        Item updateItem = itemRepository.findById(itemId).orElseThrow();
        if (nonNull(item.getName())) {
            updateItem.setName(item.getName());
        }
        if (nonNull(item.getDescription())) {
            updateItem.setDescription(item.getDescription());
        }
        if (nonNull(item.getAvailable())) {
            updateItem.setAvailable(item.getAvailable());
        }
        log.info("Обновление вещи {}.", updateItem.getName());
        return itemRepository.save(updateItem);
    }

    @Override
    public List<ItemWithDateDto> getAll(long id) {
        List<Item> userItems = itemRepository.findByOwnerId(id);
        userItems.sort(Comparator.comparing(Item::getId));
        List<ItemWithDateDto> items = userItems.stream().map(ItemMapper::toItemWithDate)
                .collect(Collectors.toList());
        for (ItemWithDateDto item : items) {
            setDateToItem(item);
            Set<Comment> comments = commentRepository.findCommentsByItem_Id(item.getId());
            if (!comments.isEmpty()) {
                item.setComments(comments.stream().map(CommentMapper::commentDto).collect(Collectors.toSet()));
            }
        }
        log.info("Получение списка вещей пользователя с id = {}.", id);
        return items;
    }

    @Override
    public ItemWithDateDto getById(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена."));
        ItemWithDateDto itemWithDate = ItemMapper.toItemWithDate(item);
        if (item.getOwner().getId() == userId) {
            setDateToItem(itemWithDate);
        }
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        if (!comments.isEmpty()) {
            itemWithDate.setComments(comments.stream().map(CommentMapper::commentDto).collect(Collectors.toSet()));
        } else {
            itemWithDate.setComments(new HashSet<>());
        }
        log.info("Получение вещи с id = {}.", itemId);
        return itemWithDate;
    }

    @Override
    public List<ItemDto> searchItem(String query) {
        log.info("Поиск вещи, содержащей {}.", query);
        if (query.isEmpty()) {
            return new ArrayList<>();
        }
        return ItemMapper.toItemDto(itemRepository
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(query, query));
    }

    @Override
    public CommentDto createComment(long userId, long itemId, CommentFromRequestDto comment) {
        List<Booking> bookings = bookingRepository
                .findByBooker_IdAndEndIsBefore(userId, LocalDateTime.now());
        if (bookings.stream().anyMatch(b -> b.getItem().getId() == itemId)) {
            Comment newComment = Comment.builder()
                    .text(comment.getText())
                    .item(itemRepository.findById(itemId).get())
                    .author(userRepository.findById(userId).get())
                    .created(LocalDateTime.now())
                    .build();
            log.info("Создан коммент {}.", comment);
            commentRepository.save(newComment);
            return CommentMapper.commentDto(newComment);
        } else {
            throw new ResponseStatusException(HttpStatus
                    .BAD_REQUEST, "Вещь не была в аренде у пользователя с id = " + userId);
        }
    }

    public User getUserOrException(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден. " +
                        "Добавление/обновление вещи невозможно."));
    }

    private void setDateToItem(ItemWithDateDto item) {
        Booking lastBooking = bookingRepository
                .findAllByItem_IdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now())
                .stream().max(Comparator.comparing(Booking::getEnd)).orElse(null);
        if (nonNull(lastBooking)) {
            item.setLastBooking(BookingMapper.bookingItemDto(lastBooking));
        }
        Booking nextBooking = bookingRepository
                .findAllByItem_IdAndStartAfterOrderByEnd(item.getId(), LocalDateTime.now())
                .stream().filter(b -> b.getStatus().equals(Status.APPROVED)).limit(1)
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        if (nonNull(nextBooking)) {
            item.setNextBooking(BookingMapper.bookingItemDto(nextBooking));
        }
    }
}