package com.paic.nbm.analyticsservice.Helper;

import com.github.tsohr.JSONArray;
import com.github.tsohr.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Slf4j
@Component
public class OrderQueryBuilder {

    public String orderQuerys(String result) throws Exception {

        try{
            JSONArray array = new JSONArray(result);
            JSONObject firstobject = array.getJSONObject(0);

            if (!firstobject.has("frame.time_epoch") || firstobject.isNull("frame.time_epoch")) {
                return result;
            }
            JSONArray sortedJsonArray = new JSONArray();
            ArrayList<JSONObject> listdata = new ArrayList<JSONObject>();

            for (int i = 0; i < array.length(); i++){
                //Adding each element of JSON array into ArrayList
                listdata.add((JSONObject) array.get(i));
            }
            Collections.sort(listdata, new Comparator<>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    BigDecimal v1 = (BigDecimal) (o1.get("frame.time_epoch"));
                    BigDecimal v2 = (BigDecimal) (o2.get("frame.time_epoch"));
                    return v1.compareTo(v2);
                }
            });

            for (JSONObject obj : listdata) {
                sortedJsonArray.put(obj);
            }
            return sortedJsonArray.toString();
        }catch(Exception e){
            log.error("Error sorting.");
            e.printStackTrace();
            return null;
        }


    }
}
