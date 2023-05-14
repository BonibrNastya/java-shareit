package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_Id(long bookerId);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and ?2 between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findCurrentBooking(long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartIsAfter(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByStatus(long bookerId, Status status);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findByOwner(long bookerId);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and ?2 between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findByOwnerCurrentBooking(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerPastBooking(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerFutureBooking(long bookerId, LocalDateTime now);

    @Query(value = "select b from Booking b " +
            "left join b.item i " +
            "where i.owner.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerByStatus(long bookerId, Status status);

    List<Booking> findAllByItem_IdInAndStatus(List<Long> itemsId, Status status);

    List<Booking> findAllByItem_IdAndStatus(long itemId, Status status);
}