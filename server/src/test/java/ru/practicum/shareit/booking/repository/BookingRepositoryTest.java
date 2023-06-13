package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private final Booking booking = new Booking();
    private final Booking booking1 = new Booking();
    private final Item item1 = new Item();
    private final Item item2 = new Item();
    private final User user1 = new User();
    private final User user2 = new User();
    private final Pageable page = PageRequest.of(0, 5);

    @BeforeEach
    public void setUp() {
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        userRepository.save(user1);
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        userRepository.save(user2);
        item1.setName("itemName");
        item1.setDescription("itemDescription");
        item1.setAvailable(true);
        item1.setOwner(user1);
        itemRepository.save(item1);
        item2.setName("itemName2");
        item2.setDescription("itemDescription2");
        item2.setAvailable(true);
        item2.setOwner(user2);
        itemRepository.save(item2);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(5));
        booking.setItem(item1);
        booking.setBooker(user2);
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
        booking1.setStart(LocalDateTime.now().plusHours(2));
        booking1.setEnd(LocalDateTime.now().plusHours(6));
        booking1.setItem(item2);
        booking1.setBooker(user1);
        booking1.setStatus(Status.WAITING);
        bookingRepository.save(booking1);
    }

    @Test
    void contentLoad() {
        assertNotNull(em);
    }

    @Test
    void findByBooker_Id() {
        List<Booking> bookings = bookingRepository
                .findByBooker_Id(user1.getId(), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @Test
    void findCurrentBooking() {
        List<Booking> bookings = bookingRepository.findCurrentBooking(
                user2.getId(), LocalDateTime.now(), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findByBooker_IdAndEndIsBefore() {
        List<Booking> bookings = bookingRepository
                .findByBooker_IdAndEndIsBefore(user1.getId(),
                        LocalDateTime.now().plusDays(3), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @Test
    void findListByBooker_IdAndEndIsBefore() {
        List<Booking> bookings = bookingRepository
                .findByBooker_IdAndEndIsBefore(
                        user1.getId(), LocalDateTime.now().plusDays(3));

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @Test
    void findByBooker_IdAndStartIsAfter() {
        List<Booking> bookings = bookingRepository
                .findByBooker_IdAndStartIsAfter(user1.getId(),
                        LocalDateTime.now().minusDays(3), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @Test
    void findBookingByStatus() {
        List<Booking> bookings = bookingRepository
                .findBookingByStatus(
                        user2.getId(), Status.APPROVED, page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findByOwner() {
        List<Booking> bookings = bookingRepository
                .findByOwner(user2.getId(), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @Test
    void findByOwnerCurrentBooking() {
        List<Booking> bookings = bookingRepository
                .findByOwnerCurrentBooking(user1.getId(),
                        LocalDateTime.now(), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findByOwnerPastBooking() {
        List<Booking> bookings = bookingRepository
                .findByOwnerPastBooking(user1.getId(),
                        LocalDateTime.now().plusDays(3), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findByOwnerFutureBooking() {
        List<Booking> bookings = bookingRepository
                .findByOwnerFutureBooking(user1.getId(),
                        LocalDateTime.now().minusDays(3), page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findByOwnerByStatus() {
        List<Booking> bookings = bookingRepository
                .findByOwnerByStatus(
                        user1.getId(), Status.APPROVED, page).getContent();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findAllByItem_IdInAndStatus() {
        long itemId = item1.getId();
        List<Booking> bookings = bookingRepository.findAllByItem_IdInAndStatus(
                List.of(itemId), Status.APPROVED);

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking);
    }

    @Test
    void findAllByItem_IdAndStatus() {
        long itemId = item2.getId();
        List<Booking> bookings = bookingRepository.findAllByItem_IdAndStatus(
                itemId, Status.WAITING);

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0), booking1);
    }

    @AfterEach
    void deleteDB() {
        bookingRepository.deleteAll();
    }
}