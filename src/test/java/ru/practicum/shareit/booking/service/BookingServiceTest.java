package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final User user1 = new User();
    private final User user2 = new User();
    private final Item item = new Item();
    private final BookingDto bookingDto = new BookingDto();
    private BookingService bookingService;
    private BookingStateDto bookingStateDto;
    private BookingStateDto bookingStateDto2;
    private static final LocalDateTime start = LocalDateTime.now();
    private static final LocalDateTime end = LocalDateTime.now().plusHours(5);
    private final int from = 0;
    private final int size = 5;


    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(itemRepository,
                bookingRepository, userRepository);
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        userRepository.save(user1);
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        userRepository.save(user2);
        item.setName("itemName");
        item.setDescription("itemDescription");
        item.setAvailable(true);
        item.setOwner(user2);
        itemRepository.save(item);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(item.getId());
        bookingStateDto = new BookingStateDto(user1.getId(), State.ALL);
        bookingStateDto2 = new BookingStateDto(user2.getId(), State.ALL);
    }

    @Test
    void create() {
        Booking booking = bookingService.create(bookingDto, user1.getId());

        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(user1, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void updateApprove() {
        Booking booking = bookingService.create(bookingDto, user1.getId());
        bookingService.updateApprove(booking.getId(), true, user2.getId());

        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(user1, booking.getBooker());
        assertEquals(booking.getStatus(), Status.APPROVED);
    }

    @Test
    void getById() {
        Booking booking = bookingService.create(bookingDto, user1.getId());
        Booking getingBooking = bookingService.getById(booking.getId(), user1.getId());

        assertEquals(start, getingBooking.getStart());
        assertEquals(end, getingBooking.getEnd());
        assertEquals(item, getingBooking.getItem());
        assertEquals(user1, getingBooking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void getAllByState() {
        Booking booking = bookingService.create(bookingDto, user1.getId());

        List<Booking> bookings = bookingService
                .getAllByState(bookingStateDto, from, size);

        assertEquals(1, bookings.size());
        assertEquals(start, bookings.get(0).getStart());
        assertEquals(end, bookings.get(0).getEnd());
        assertEquals(item, bookings.get(0).getItem());
        assertEquals(user1, bookings.get(0).getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void getAllByOwner() {
        Booking booking = bookingService.create(bookingDto, user1.getId());

        List<Booking> bookings = bookingService
                .getAllByOwner(bookingStateDto2, from, size);

        assertEquals(1, bookings.size());
        assertEquals(start, bookings.get(0).getStart());
        assertEquals(end, bookings.get(0).getEnd());
        assertEquals(item, bookings.get(0).getItem());
        assertEquals(user1, bookings.get(0).getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }
}