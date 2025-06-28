package Bright.BeSafeProject.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/besafe")
public class ViewController {
    @GetMapping(value="")
    public String titlePage(){
        return "loginView";
    }

    @GetMapping(value = "/join")
    public String join() { return "joinView"; }

    @GetMapping(value = "/servicePage")
    public String mapPage(){
        return "serviceView";
    }
}
