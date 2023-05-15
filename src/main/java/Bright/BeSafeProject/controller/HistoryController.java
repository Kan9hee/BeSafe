package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.dto.MemberDTO;
import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.service.DatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HistoryController {

    @Autowired
    DatabaseService databaseService;

    @GetMapping(value = "/history")
    public String showHistory(HttpServletRequest request){
        databaseService.loadMemberHistory((Member)request.getSession().getAttribute("loginMember"));
        return "historyView";
    }

    @PostMapping(value = "/history")
    public String postHistory(){
        return "redirect:/map";
    }
}
