package Bright.BeSafeProject.dto;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserInfoDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 255)
    private String email;
    @Column(nullable = false, length = 255)
    private String password;
}
