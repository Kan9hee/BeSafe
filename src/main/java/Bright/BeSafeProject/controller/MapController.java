package Bright.BeSafeProject.controller;

import Bright.BeSafeProject.service.PublicAPIService;
import Bright.BeSafeProject.service.TmapAPIService;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.*;

@Controller
public class MapController {

    private final PublicAPIService publicAPIService;
    private final TmapAPIService tmapAPIService;

    public MapController(PublicAPIService publicAPIService, TmapAPIService tmapAPIService) {
        this.publicAPIService = publicAPIService;
        this.tmapAPIService = tmapAPIService;
    }

    @GetMapping(value = "/map")
    public String Map() throws IOException, InterruptedException, URISyntaxException, ParseException {

        tmapAPIService.callTmapRoute();
        publicAPIService.callStreetLight();
        return "tmapExam";
    }

    //@PostMapping(value = "/map")
    public String result(){return "tmapExam";}
}
