package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.dto.RouteDTO;
import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.service.DatabaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    DatabaseService databaseService;

    @GetMapping(value = "/history")
    public String showHistory(HttpServletRequest request, Model model) throws ParseException {
        List<RouteDTO> log=databaseService
                .getMemberHistory((Member)request.getSession().getAttribute("loginMember"));
        model.addAttribute("log",log);
        return "historyView";
    }
}
