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
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class MapController {

    private final PublicAPIService publicAPIService;
    private final TmapAPIService tmapAPIService;
    private StreetLight streetLight;
    private Route route;
    private Gson gson;

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/map")
    public String Map(Model model){
        model.addAttribute("startLocation",gson.toJson(new ArrayList<>(Arrays.asList(route.getStartLocation()))));
        model.addAttribute("endLocation",gson.toJson(new ArrayList<>(Arrays.asList(route.getEndLocation()))));
        model.addAttribute("streetLightLatitude",gson.toJson(streetLight.getLatitudeList()));
        model.addAttribute("streetLightLongitude",gson.toJson(streetLight.getLongitudeList()));
        return "resultView";
    }

    @GetMapping(value = "/search")
    public String routeSetup(){
        streetLight=new StreetLight();
        route=new Route();
        gson=new Gson();
        return "routeSetupView";
    }

    @PostMapping(value = "/search")
    public String routeResult(@RequestParam("start") String startJSON,@RequestParam("end") String endJSON)
            throws IOException, InterruptedException, ParseException {
        route.setStartLocation(gson.fromJson(startJSON, Double[].class));
        route.setEndLocation(gson.fromJson(endJSON, Double[].class));
        route.setStartAddress(tmapAPIService.findAddress(route.getStartLocation()));
        route.setEndAddress(tmapAPIService.findAddress(route.getEndLocation()));
        tmapAPIService.callTmapRoute(route);
        route.setShowRange();
        publicAPIService.callSecurityLight(streetLight,route.getStartAddress());
        if(!route.sameAddressCheck())
            publicAPIService.callSecurityLight(streetLight,route.getEndAddress());
        publicAPIService.callStreetLamp(streetLight,route.getShowRange());
        streetLight.setRangeWithRoute(route.getShowRange());
        return "redirect:/map";
    }
}
