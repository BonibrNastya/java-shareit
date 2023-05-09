package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String text;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Item item;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User author;
    private LocalDateTime created;

    public Comment() {
    }
}
