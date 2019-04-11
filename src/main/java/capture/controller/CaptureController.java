package capture.controller;

import capture.entity.ChangeInfo;
import capture.service.CaptureService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CaptureController {
    @Autowired
    private CaptureService captureService;

    @ResponseBody
    @RequestMapping(value = "getChangeInfoByName/{companyName}")
    public JSONPObject getChangeInfoByName(@PathVariable("companyName") String companyName) {
        List<ChangeInfo> changeInfos = captureService.getByName(companyName);
        JSONPObject jsonpObject = new JSONPObject("ChangeInfo", changeInfos);
        return jsonpObject;
    }
}
