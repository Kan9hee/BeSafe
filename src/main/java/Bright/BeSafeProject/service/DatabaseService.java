package Bright.BeSafeProject.service;

import Bright.BeSafeProject.dto.MemberDTO;
import Bright.BeSafeProject.dto.RouteDTO;
import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.repository.MemberRepository;
import Bright.BeSafeProject.repository.RouteRepository;
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<RouteDTO> getMemberHistory(Member member) throws ParseException {
        List<RouteDTO> resultAddressList=new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        JSONArray memberLog = (JSONArray) jsonParser.parse(loadRouteData(member));
        for(Object object:memberLog){
            JSONObject resultAddress=(JSONObject)object;
            resultAddressList.add(new RouteDTO(resultAddress));
        }
        return resultAddressList;
    }

    private String loadRouteData(Member member){
        return new Gson().toJson(routeRepository.findByEmail(member.getEmail()));
    }
}
