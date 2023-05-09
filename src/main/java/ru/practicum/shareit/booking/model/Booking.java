package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NonNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;
    @NonNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;
    @ManyToOne(cascade = CascadeType.ALL)
    private Item item;
    @ManyToOne(cascade = CascadeType.ALL)
    private User booker;
    @Enumerated(value = EnumType.STRING)
    private Status status;

    public Booking() {
    }
}
