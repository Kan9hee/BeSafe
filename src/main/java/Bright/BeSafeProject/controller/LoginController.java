package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.service.KakaoLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private Member currentMember;

    @Autowired
    KakaoLoginService kakaoLoginService;

    @GetMapping(value = "/member/kakao")
    public String kakaoCode(@RequestParam String code,HttpServletRequest request) throws ParseException {
        System.out.println(code);
        String accessToken = kakaoLoginService.callAccessToken(code);
        currentMember = kakaoLoginService.callUserInfo(accessToken);
        HttpSession session = request.getSession();
        session.setAttribute("loginMember", currentMember);
        return "redirect:../search";
    }
}
