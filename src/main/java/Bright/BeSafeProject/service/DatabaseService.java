package Bright.BeSafeProject.service;

import Bright.BeSafeProject.dto.MemberDTO;
import Bright.BeSafeProject.entity.MemberEntity;
import Bright.BeSafeProject.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private MemberRepository memberRepository;

    public void saveMemberData(MemberDTO memberDTO) {
        try {
            memberRepository.save(memberDTO.toEntity());
        }catch (DataIntegrityViolationException e){
            System.out.println("가입되어 있는 회원입니다.");
        }
    }
}
