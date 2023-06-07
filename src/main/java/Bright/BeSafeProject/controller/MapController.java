package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.dto.MemberDTO;
import Bright.BeSafeProject.dto.RouteDTO;
import Bright.BeSafeProject.model.Member;
import Bright.BeSafeProject.model.Route;
import Bright.BeSafeProject.model.StreetLight;
import Bright.BeSafeProject.service.DatabaseService;
import Bright.BeSafeProject.service.PublicAPIService;
import Bright.BeSafeProject.service.TmapAPIService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class MapController {

    @Autowired
    PublicAPIService publicAPIService;
    @Autowired
    TmapAPIService tmapAPIService;
    @Autowired
    DatabaseService databaseService;
    private StreetLight streetLight;
    private Member member;
    private Route route;
    private Gson gson;

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/search")
    public String routeSetup(HttpServletRequest request){
        route=new Route();
        gson=new Gson();
        member=(Member)request.getSession().getAttribute("loginMember");
        return "routeSetupView";
    }

    @PostMapping(value = "/search")
    public String routeResult(@RequestParam("start") String startJSON,@RequestParam("end") String endJSON)
            throws IOException, InterruptedException, ParseException {
        route.setStartLocation(gson.fromJson(startJSON, Double[].class));
        route.setEndLocation(gson.fromJson(endJSON, Double[].class));
        String[] startAd=tmapAPIService.findAddress(route.getStartLocation()).split(",");
        String[] endAd=tmapAPIService.findAddress(route.getEndLocation()).split(",");
        route.setStartAddress(startAd[0]);
        route.setEndAddress(endAd[0]);
        databaseService.saveRouteData(new RouteDTO(member.getEmail(),
                startAd[1], route.getStartLocation(),
                endAd[1], route.getEndLocation(),null));
        searchRouteAndLights();
        return "redirect:/map";
    }

    @GetMapping(value = "/map")
    public String Map(Model model){
        model.addAttribute("startLocation",gson.toJson(new ArrayList<>(Arrays.asList(route.getStartLocation()))));
        model.addAttribute("endLocation",gson.toJson(new ArrayList<>(Arrays.asList(route.getEndLocation()))));
        model.addAttribute("streetLightLatitude",gson.toJson(streetLight.getLatitudeList()));
        model.addAttribute("streetLightLongitude",gson.toJson(streetLight.getLongitudeList()));
        model.addAttribute("waypointLatitude",gson.toJson(route.getWaypointLatitudes()));
        model.addAttribute("waypointLongitude",gson.toJson(route.getWaypointLongitudes()));
        return "resultView";
    }

    @PostMapping(value = "/map")
    public String routeReset(@RequestParam("marker_x") String markerXJSON,@RequestParam("marker_y") String markerYJSON)
            throws IOException, InterruptedException, ParseException {
        route.setPassLocation(new Double[]{Double.valueOf(markerXJSON), Double.valueOf(markerYJSON)});
        searchRouteAndLights();
        return "redirect:/map";
    }

    private void searchRouteAndLights() throws IOException, ParseException, InterruptedException {
        streetLight=new StreetLight();
        tmapAPIService.callTmapRoute(route);
        route.setShowMaxRange();
        publicAPIService.callSecurityLight(streetLight,route.getShowRange(),route.getStartAddress());
        if(!route.sameAddressCheck())
            publicAPIService.callSecurityLight(streetLight,route.getShowRange(),route.getEndAddress());
        publicAPIService.callStreetLamp(streetLight,route);
    }
}
