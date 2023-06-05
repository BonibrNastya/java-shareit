package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    private final User user = new User();
    private final Item item = new Item();
    private final Comment comment = new Comment();

    @BeforeEach
    void setUp() {
        user.setName("user2");
        user.setEmail("user2@mail.ru");
        userRepository.save(user);
        item.setName("itemName");
        item.setDescription("itemDescription");
        item.setAvailable(true);
        item.setOwner(user);
        itemRepository.save(item);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setText("text");
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Test
    void contentLoad() {
        assertNotNull(em);
    }

    @Test
    void findCommentsByItem_Id() {
        long itemId = item.getId();
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);

        assertEquals(1, comments.size());
        assertTrue(comments.contains(comment));
    }
}