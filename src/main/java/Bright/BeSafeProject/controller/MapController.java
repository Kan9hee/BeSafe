package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.model.Route;
import Bright.BeSafeProject.model.StreetLight;
import Bright.BeSafeProject.service.PublicAPIService;
import Bright.BeSafeProject.service.TmapAPIService;
import com.google.gson.Gson;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class MapController {

    private final PublicAPIService publicAPIService;
    private final TmapAPIService tmapAPIService;
    private StreetLight streetLight=new StreetLight();
    private Route route=new Route();

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/map")
    public String Map(Model model) throws IOException, InterruptedException, ParseException {

        tmapAPIService.callTmapRoute();
        publicAPIService.callStreetLight(streetLight);
        model.addAttribute("streetLightLatitude",new Gson().toJson(streetLight.getLatitudeList()));
        model.addAttribute("streetLightLongitude",new Gson().toJson(streetLight.getLongitudeList()));
        return "resultView";
    }

    @RequestMapping(value = "/search")
    public String result(Double[] start,Double[] end){
        route.setStartLocation(start);
        route.setEndLocation(end);
        return "routeSetupView";
    }
}
