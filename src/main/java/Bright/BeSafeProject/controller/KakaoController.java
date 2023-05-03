package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.service.KakaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class KakaoController {

    private Member currentMember;

    @Autowired
    KakaoService kakaoService;

    @GetMapping(value = "/member/kakao")
    public String kakaoCode(@RequestParam String code,HttpServletRequest request) throws ParseException {
        String accessToken = kakaoService.callAccessToken(code);
        currentMember = kakaoService.callUserInfo(accessToken);
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(0);
        session.setAttribute("loginMember", currentMember);
        return "redirect:../search";
    }

    @PostMapping(value = "/logout")
    public String kakaoLogout(HttpSession session, HttpServletRequest request, HttpServletResponse response){
        Member member = (Member) session.getAttribute("loginMember");
        kakaoService.returnAccessToken(member.getToken());
        session.removeAttribute("loginMember");
        session.invalidate();
        return "redirect:/";
    }
}
