package Bright.BeSafeProject.entity;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("account")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    @Id
    private Long id;

    @Column("platform")
    @NotNull
    private String platform;
    @Column("name")
    @NotNull
    private String name;
    @Column("email")
    @NotNull
    private String email;
    @Column("password")
    private String password;
    @Column("authority")
    @NotNull
    private String authority;
    @Column("created_at")
    @NotNull
    private LocalDateTime createdAt;

    @Transient
    @Builder.Default
    private List<History> accountHistoryList = new ArrayList<>();
}
