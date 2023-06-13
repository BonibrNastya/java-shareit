package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class UserDto {
    private long id;
    @Email
    @NonNull
    private String email;
    private String name;
}
