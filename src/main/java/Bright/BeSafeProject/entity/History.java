package Bright.BeSafeProject.entity;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("history")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class History {
    @Id
    private Long id;

    @Column("start_x")
    @NotNull
    private Float startX;
    @NotNull
    @Column("start_y")
    private Float startY;
    @NotNull
    @Column("end_x")
    private Float endX;
    @Column("end_y")
    @NotNull
    private Float endY;
    @Column("used_at")
    @NotNull
    private LocalDateTime usedAt;
    @Column("account_id")
    @NotNull
    private Long accountId;
}
