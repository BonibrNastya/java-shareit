package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private User user1;
    private User user2;
    private Item item;
    private Booking booking;
    private Booking booking2;
    private static final LocalDateTime START = LocalDateTime.now();
    private static final LocalDateTime END = LocalDateTime.now().plusHours(5);
    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .email("name1@email.ru")
                .name("name1")
                .build();
        user2 = User.builder()
                .id(2L)
                .email("name2@email.ru")
                .name("name2")
                .build();
        item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user2)
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(START)
                .end(END)
                .item(item)
                .booker(user1)
                .status(Status.APPROVED)
                .build();
        booking2 = Booking.builder()
                .id(1L)
                .start(START)
                .end(END)
                .item(item)
                .booker(user1)
                .status(Status.REJECTED)
                .build();
    }

    @Test
    void create_whenValidBookingDto_returnBooking() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(START);
        bookingDto.setEnd(END);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        Booking actualBooking = bookingService.create(bookingDto, user1.getId());
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();

        assertEquals(booking, actualBooking);
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(Status.WAITING, savedBooking.getStatus());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void create_whenBookingDtoNotValidDate_returnException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(START);
        bookingDto.setEnd(START);

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.create(bookingDto, user1.getId())).getMessage();

        assertEquals("400 BAD_REQUEST \"Неверные даты бронирования.\"", message);
    }

    @Test
    void create_whenUserEqualsBooker_returnException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(START);
        bookingDto.setEnd(END);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.create(bookingDto, user2.getId())).getMessage();

        assertEquals("404 NOT_FOUND \"Нельзя бронировать свои вещи.\"", message);
    }

    @Test
    void create_whenItemNotAvailable_returnException() {
        item.setAvailable(false);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(START);
        bookingDto.setEnd(END);
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when((userRepository.findById(anyLong())))
                .thenReturn(Optional.of(user1));

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.create(bookingDto, user1.getId())).getMessage();

        assertEquals("400 BAD_REQUEST \"Вещь недоступна для бронирования. Id = 1\"", message);
    }

    @Test
    void updateApprove_whenStatusRejected_returnApprovedBooking() {
        Booking updateBooking = booking;
        updateBooking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking2));
        when(bookingRepository.save(any()))
                .thenReturn(updateBooking);

        Booking actualBooking = bookingService
                .updateApprove(1L, true, 2L);
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();

        assertEquals(Status.APPROVED, actualBooking.getStatus());
        assertEquals(updateBooking.getStart(), savedBooking.getStart());
        assertEquals(updateBooking.getEnd(), savedBooking.getEnd());
        assertEquals(updateBooking.getBooker(), savedBooking.getBooker());
        assertEquals(updateBooking.getItem(), savedBooking.getItem());
    }

    @Test
    void updateApprove_whenStatusApproved_returnRejectedBooking() {
        Booking updateBooking = booking;
        updateBooking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any()))
                .thenReturn(updateBooking);

        Booking actualBooking = bookingService
                .updateApprove(1L, false, 2L);
        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();

        assertEquals(Status.REJECTED, actualBooking.getStatus());
        assertEquals(updateBooking.getStart(), savedBooking.getStart());
        assertEquals(updateBooking.getEnd(), savedBooking.getEnd());
        assertEquals(updateBooking.getBooker(), savedBooking.getBooker());
        assertEquals(updateBooking.getItem(), savedBooking.getItem());
    }

    @Test
    void updateApproved_whenStatusAlreadyRejected_returnException() {
        Booking updateBooking = booking;
        updateBooking.setStatus(Status.REJECTED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.updateApprove(1L, false, 2L)).getMessage();
        assertEquals("400 BAD_REQUEST \"Текущий статус уже REJECTED.\"", message);
    }

    @Test
    void updateApproved_whenStatusAlreadyApproved_returnException() {
        Booking updateBooking = booking;
        updateBooking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.updateApprove(1L, true, 2L)).getMessage();
        assertEquals("400 BAD_REQUEST \"Текущий статус уже APPROVED.\"", message);
    }

    @Test
    void updateApproved_whenNotOwner_returnException() {
        Booking updateBooking = booking;
        updateBooking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.updateApprove(1L, true, 1L)).getMessage();
        assertEquals("404 NOT_FOUND \"Изменят статус может только собственник вещи.\"", message);
    }

    @Test
    void getById_whenRightUserId_returnBooking() {
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Booking actualBooking = bookingService.getById(booking.getId(), user1.getId());

        assertEquals(booking, actualBooking);
    }

    @Test
    void getById_whenWrongUserId_returnException() {
        booking2.setBooker(user2);
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking2));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        String message = assertThrows(ResponseStatusException.class,
                () -> bookingService.getById(booking2.getId(), user1.getId())).getMessage();

        assertEquals("404 NOT_FOUND " +
                "\"Данные о бронировании доступны только авторам бронирования и владельцам вещи.\"", message);
    }

    @Test
    void getAllByState_whenStateAll() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.ALL);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBooker_Id(anyLong(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBooker_Id(anyLong(), any());
    }

    @Test
    void getAllByState_whenStateCurrent() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.CURRENT);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findCurrentBooking(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findCurrentBooking(anyLong(), any(), any());
    }

    @Test
    void getAllByState_whenStatePast() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.PAST);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBooker_IdAndEndIsBefore(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBooker_IdAndEndIsBefore(anyLong(), any(), any());
    }

    @Test
    void getAllByState_whenStateFuture() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.FUTURE);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBooker_IdAndStartIsAfter(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBooker_IdAndStartIsAfter(anyLong(), any(), any());
    }

    @Test
    void getAllByState_whenStateApproved() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.APPROVED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findBookingByStatus(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findBookingByStatus(anyLong(), any(), any());
    }

    @Test
    void getAllByState_whenStateRejected() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findBookingByStatus(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByState(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findBookingByStatus(anyLong(), any(), any());
    }

    @Test
    void getAllByState_whenPageNotValid_returnException() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));

        String message = assertThrows(ArithmeticException.class,
                () -> bookingService.getAllByState(bookingStateDto, -1, -5)).getMessage();

        assertEquals("Неверный индекс или количество элементов.", message);
        verify(bookingRepository, never()).findBookingByStatus(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenStateAll() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.ALL);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwner(anyLong(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwner(anyLong(), any());
    }

    @Test
    void getAllByOwner_whenStateCurrent() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.CURRENT);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwnerCurrentBooking(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwnerCurrentBooking(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenStatePast() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.PAST);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwnerPastBooking(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwnerPastBooking(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenStateFuture() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.FUTURE);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwnerFutureBooking(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwnerFutureBooking(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenStateApproved() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.APPROVED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwnerByStatus(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwnerByStatus(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenStateRejected() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByOwnerByStatus(anyLong(), any(), any()))
                .thenReturn(Page.empty());

        List<Booking> bookings = bookingService.getAllByOwner(bookingStateDto, 0, 5);

        assertNotNull(bookings);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByOwnerByStatus(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_whenPageNotValid_returnException() {
        BookingStateDto bookingStateDto = new BookingStateDto(user1.getId(),
                State.REJECTED);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user1));

        String message = assertThrows(ArithmeticException.class,
                () -> bookingService.getAllByOwner(bookingStateDto, -1, -5)).getMessage();

        assertEquals("Неверный индекс или количество элементов.", message);
        verify(bookingRepository, never()).findByOwnerByStatus(anyLong(), any(), any());
    }
}