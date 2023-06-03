package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.request.repository.RequestRepository;
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
    private final RequestRepository requestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, long userId) {
        User user = getUserOrException(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        Long requestId = itemDto.getRequestId();
        if (nonNull(requestId)) {
            item.setRequest(requestRepository.findById(requestId).get());
        }
        item = itemRepository.save(item);
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
    public List<ItemWithDateDto> getAll(long id, int from, int size) {
        if (from >= 0 && size > 0) {
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
            List<ItemWithDateDto> items = itemRepository.findByOwnerId(id, page)
                    .map(ItemMapper::toItemWithDate)
                    .getContent();
            List<Long> itemsId = items.stream().map(ItemWithDateDto::getId).collect(Collectors.toList());
            List<Booking> bookings = bookingRepository.findAllByItem_IdInAndStatus(itemsId, Status.APPROVED);
            for (ItemWithDateDto item : items) {
                setDateToItem(bookings, item);
                Set<Comment> comments = commentRepository.findCommentsByItem_Id(item.getId());
                if (!comments.isEmpty()) {
                    item.setComments(comments.stream().map(CommentMapper::commentDto).collect(Collectors.toSet()));
                }
            }
            log.info("Получение списка вещей пользователя с id = {}.", id);
            return items;
        } else {
            throw new ArithmeticException("Неверный индекс или количество элементов.");
        }
    }

    @Override
    public ItemWithDateDto getById(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена."));
        ItemWithDateDto itemWithDate = ItemMapper.toItemWithDate(item);
        List<Booking> bookings = bookingRepository.findAllByItem_IdAndStatus(itemId, Status.APPROVED);
        if (item.getOwner().getId() == userId) {
            setDateToItem(bookings, itemWithDate);
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
    public List<ItemDto> searchItem(String query, int from, int size) {
        if (from >= 0 && size > 0) {
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
            log.info("Поиск вещи, содержащей {}.", query);
            if (query.isEmpty()) {
                return new ArrayList<>();
            }
            return ItemMapper.toItemDto(itemRepository
                    .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(query,
                            query, page).getContent());
        } else {
            throw new ArithmeticException("Неверный индекс или количество элементов.");
        }
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

    private User getUserOrException(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден. " +
                        "Добавление/обновление вещи невозможно."));
    }

    private void setDateToItem(List<Booking> bookings, ItemWithDateDto item) {
        Booking lastBooking = bookings.stream()
                .filter(b -> Objects.equals(b.getItem().getId(), item.getId()))
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .max(Comparator.comparing(Booking::getEnd)).orElse(null);
        if (nonNull(lastBooking)) {
            item.setLastBooking(BookingMapper.bookingItemDto(lastBooking));
        }
        Booking nextBooking = bookings.stream()
                .filter(b -> Objects.equals(b.getItem().getId(), item.getId()))
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        if (nonNull(nextBooking)) {
            item.setNextBooking(BookingMapper.bookingItemDto(nextBooking));
        }
    }
}