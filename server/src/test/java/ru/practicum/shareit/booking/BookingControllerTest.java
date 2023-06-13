package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @Mock
    private BookingService bookingService;
    @InjectMocks
    private BookingController bookingController;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);
    private final BookingDto bookingDto = new BookingDto(1L, start, end);
    private Booking booking;
    private Item item;
    private User user;
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private static final String REQUEST_BOOKINGS = "/bookings";
    private static final String REQUEST_BOOKING_WITH_ID = "/bookings/1";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        mapper.findAndRegisterModules();
        item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();
    }

    @SneakyThrows
    @Test
    void createBooking_whenAllRight_returnOkAndBooking() {
        when(bookingService.create(any(), anyLong()))
                .thenReturn(booking);

        mvc.perform(post(REQUEST_BOOKINGS)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.email", is(booking.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void createBooking_whenBookingDtoNotValid_returnException() {
        BookingDto pastStart = new BookingDto(1L, start.minusDays(50), end);
        BookingDto pastEnd = new BookingDto(1L, start, end.minusDays(50));

        mvc.perform(post(REQUEST_BOOKINGS)
                        .content(mapper.writeValueAsString(pastStart))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());
        mvc.perform(post(REQUEST_BOOKINGS)
                        .content(mapper.writeValueAsString(pastEnd))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(eq(bookingDto), anyLong());
    }

    @SneakyThrows
    @Test
    void update() {
        booking.setStatus(Status.APPROVED);

        when(bookingService.updateApprove(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(booking);

        mvc.perform(patch(REQUEST_BOOKING_WITH_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(booking.getStatus().name())));

        verify(bookingService, times(1)).updateApprove(anyLong(), anyBoolean(), anyLong());
    }

    @SneakyThrows
    @Test
    void getById() {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(booking);

        mvc.perform(get(REQUEST_BOOKING_WITH_ID)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.email", is(booking.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getAllByState_whenStateIsAll_returnAll() {
        Booking booking2 = Booking.builder()
                .id(2L)
                .start(start.plusDays(1))
                .end(end.plusDays(2))
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();
        when(bookingService.getAllByState(any(), anyInt(), anyInt()))
                .thenReturn(List.of(booking, booking2));

        mvc.perform(get(REQUEST_BOOKINGS)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", is(hasSize(2))))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.email", is(booking.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().name())))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(booking2.getItem().getName())))
                .andExpect(jsonPath("$[1].item.description", is(booking2.getItem().getDescription())))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.email", is(booking2.getBooker().getEmail())))
                .andExpect(jsonPath("$[1].booker.name", is(booking2.getBooker().getName())))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().name())));
    }

    @SneakyThrows
    @Test
    void getAllByState_whenStateUnknown_returnException() {
        mvc.perform(get(REQUEST_BOOKINGS)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("state", "UNKNOWN_STATE")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getAllByOwner() {
        Booking booking2 = Booking.builder()
                .id(2L)
                .start(start.plusDays(1))
                .end(end.plusDays(2))
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();
        when(bookingService.getAllByOwner(any(), anyInt(), anyInt()))
                .thenReturn(List.of(booking, booking2));

        mvc.perform(get(REQUEST_BOOKINGS + "/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", is(hasSize(2))))
                .andExpect(jsonPath("$[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(booking.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(booking.getItem().getDescription())))
                .andExpect(jsonPath("$[0].booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.email", is(booking.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].booker.name", is(booking.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(booking.getStatus().name())))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(booking2.getItem().getName())))
                .andExpect(jsonPath("$[1].item.description", is(booking2.getItem().getDescription())))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.email", is(booking2.getBooker().getEmail())))
                .andExpect(jsonPath("$[1].booker.name", is(booking2.getBooker().getName())))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().name())));
    }
}