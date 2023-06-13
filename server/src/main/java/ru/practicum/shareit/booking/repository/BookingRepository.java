package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBooker_Id(long bookerId, Pageable page);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and ?2 between b.start and b.end " +
            "order by b.start desc")
    Page<Booking> findCurrentBooking(long bookerId, LocalDateTime now, Pageable page);

    Page<Booking> findByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime now, Pageable page);

    List<Booking> findByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime now);

    Page<Booking> findByBooker_IdAndStartIsAfter(long bookerId, LocalDateTime now, Pageable page);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    Page<Booking> findBookingByStatus(long bookerId, Status status, Pageable page);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "order by b.start desc")
    Page<Booking> findByOwner(long bookerId, Pageable page);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and ?2 between b.start and b.end " +
            "order by b.start desc")
    Page<Booking> findByOwnerCurrentBooking(long bookerId, LocalDateTime now, Pageable page);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.end < ?2 " +
            "order by b.start desc")
    Page<Booking> findByOwnerPastBooking(long bookerId, LocalDateTime now, Pageable page);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.start > ?2 " +
            "order by b.start desc")
    Page<Booking> findByOwnerFutureBooking(long bookerId, LocalDateTime now, Pageable page);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    Page<Booking> findByOwnerByStatus(long bookerId, Status status, Pageable page);

    List<Booking> findAllByItem_IdInAndStatus(List<Long> itemsId, Status status);

    List<Booking> findAllByItem_IdAndStatus(long itemId, Status status);
}