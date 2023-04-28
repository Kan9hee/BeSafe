package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.service.KakaoLoginService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/member")
public class LoginController {

    @Autowired
    KakaoLoginService kakaoLoginService;

    @GetMapping(value = "/kakaoLogin")
    public String kakaoLoginPage(){
        return "kakaoLogin";
    }

    @ResponseBody
    @GetMapping(value = "/kakao")
    public void kakaoCode(@RequestParam String code) throws ParseException {
        System.out.println(code);
        String accessToken = kakaoLoginService.callAccessToken(code);
        kakaoLoginService.callUserInfo(accessToken);
    }
}
