package Bright.BeSafeProject.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false, unique = true)
    private String email;

    public MemberEntity(String nickname,String email){
        this.nickname=nickname;
        this.email=email;
    }
}
