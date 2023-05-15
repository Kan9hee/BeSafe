package Bright.BeSafeProject.service;

import Bright.BeSafeProject.dto.MemberDTO;
import Bright.BeSafeProject.dto.RouteDTO;
import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.repository.MemberRepository;
import Bright.BeSafeProject.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RouteRepository routeRepository;

    public DatabaseService() {
    }

    public void saveMemberData(MemberDTO memberDTO) {
        try {
            memberRepository.save(memberDTO.toEntity());
        }catch (DataIntegrityViolationException e){
            System.out.println("가입되어 있는 회원입니다.");
        }
    }

    public void saveRouteData(RouteDTO routeDTO) {
        try {
            routeRepository.saveIfNotDuplicate(routeDTO.toEntity());
        }catch (DataIntegrityViolationException e){
            System.out.println("이미 찾은 경로입니다.");
        }
    }

    public void loadMemberHistory(Member member){
        System.out.println(routeRepository.findByEmail(member.getEmail()));
    }
}
