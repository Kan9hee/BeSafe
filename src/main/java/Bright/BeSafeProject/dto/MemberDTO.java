package Bright.BeSafeProject.dto;

import Bright.BeSafeProject.entity.MemberEntity;

public class MemberDTO {
    private String nickname;
    private String email;

    public MemberDTO(String nickname, String email){
        this.nickname=nickname;
        this.email=email;
    }

    @Override
    public String toString(){
        return "{nickname="+nickname+", email="+email+"}";
    }

    public MemberEntity toEntity(){return new MemberEntity(nickname,email);}
}
