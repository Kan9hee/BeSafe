package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.model.Route;
import Bright.BeSafeProject.model.StreetLight;
import Bright.BeSafeProject.service.PublicAPIService;
import Bright.BeSafeProject.service.TmapAPIService;
import com.google.gson.Gson;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class MapController {

    private final PublicAPIService publicAPIService;
    private final TmapAPIService tmapAPIService;
    private StreetLight streetLight=new StreetLight();
    private Route route=new Route();
    private Gson gson=new Gson();

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/map")
    public String Map(Model model){
        model.addAttribute("streetLightLatitude",gson.toJson(streetLight.getLatitudeList()));
        model.addAttribute("streetLightLongitude",gson.toJson(streetLight.getLongitudeList()));
        return "resultView";
    }

    @GetMapping(value = "/search")
    public String routeSetup(){
        return "routeSetupView";
    }

    @PostMapping(value = "/search")
    public String routeResult(@RequestParam("start") String startJSON,@RequestParam("end") String endJSON)
            throws IOException, InterruptedException, ParseException {
        route.setStartLocation(gson.fromJson(startJSON,Double[].class));
        route.setEndLocation(gson.fromJson(endJSON,Double[].class));
        route.setStartAddress(tmapAPIService.findAddress(route.getStartLocation()));
        route.setEndAddress(tmapAPIService.findAddress(route.getEndLocation()));
        tmapAPIService.callTmapRoute(route);
        publicAPIService.callStreetLight(streetLight);
        return "redirect:/map";
    }
}
