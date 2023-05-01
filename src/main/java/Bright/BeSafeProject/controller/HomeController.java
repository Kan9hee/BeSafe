package Bright.BeSafeProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = "/welcome")
    public String permission(){
        return "loginView";
    }

    @GetMapping(value = "test")
    public String test(){return "testView";}
}
