package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.model.StreetLight;
import Bright.BeSafeProject.service.PublicAPIService;
import Bright.BeSafeProject.service.TmapAPIService;
import com.google.gson.Gson;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class MapController {

    private final PublicAPIService publicAPIService;
    private final TmapAPIService tmapAPIService;

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/map")
    public String Map(Model model) throws IOException, InterruptedException, ParseException {

        StreetLight streetLight=new StreetLight();
        tmapAPIService.callTmapRoute();
        publicAPIService.callStreetLight(streetLight);
        System.out.println(streetLight.getLatitudeList().size());
        model.addAttribute("streetLightLatitude",new Gson().toJson(streetLight.getLatitudeList()));
        model.addAttribute("streetLightLongitude",new Gson().toJson(streetLight.getLongitudeList()));
        return "tmapExam";
    }

    //@PostMapping(value = "/map")
    public String result(){return "tmapExam";}
}
