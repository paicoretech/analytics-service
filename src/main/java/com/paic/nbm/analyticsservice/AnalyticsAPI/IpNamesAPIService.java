package com.paic.nbm.analyticsservice.AnalyticsAPI;

import com.paic.nbm.analyticsservice.Entities.IpNamesData;
import com.paic.nbm.analyticsservice.Service.IpNamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class IpNamesAPIService {
    @Autowired
    IpNamesService ipNamesService;

    @CrossOrigin(origins = "*")
    @PostMapping("SaveIpName/")
    public IpNamesData save(@RequestBody Object fields) throws Exception {
        HashMap<String, String> parameters = (HashMap<String, String>) fields;

        IpNamesData newIpName = new IpNamesData(0L,
                parameters.get("friendly_name").toUpperCase(),
                parameters.get("ip_addr"),
        Integer.parseInt(parameters.get("type")));

        return ipNamesService.save(newIpName);
    }


    @CrossOrigin(origins = "*")
    @PostMapping("SaveIpNameList/")
    public Boolean saveList(@RequestBody List<IpNamesData> ipNamesData) throws Exception {
        return ipNamesService.saveList(ipNamesData);
    }
}
