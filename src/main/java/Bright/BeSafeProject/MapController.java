package Bright.BeSafeProject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    @GetMapping(value = "/map")
    public String exam(){
        return "tmapExam";
    }

    //@PostMapping(value = "/map")
    public String result(){return "tmapExam";}
}
