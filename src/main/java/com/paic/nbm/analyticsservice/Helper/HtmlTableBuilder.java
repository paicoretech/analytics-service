package com.paic.nbm.analyticsservice.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HtmlTableBuilder {
    HashMap<String, List<String>> eventsMap;
    String tableClassName;
    String thClassName;
    String trClassName;
    String tdClassName;

    public HtmlTableBuilder() {
        this.eventsMap = new HashMap<>();
    }

    public void addValue(String name, String value){
        if(!eventsMap.containsKey(name))
            eventsMap.put(name, new ArrayList<>());

        eventsMap.get(name).add(value);
    }

    //TODO: Complete table builder implementation
/*
    public List<String> buildTable(){
        List<String> result = new ArrayList<>();
        StringBuilder headers = new StringBuilder();
        StringBuilder body = new StringBuilder();
        int valuesAmt = 0;
        StringBuilder html = new StringBuilder("<table class='table table-striped'>");
        html.append("<tbody>");

        for(String event: eventsMap.keySet()){
            headers.append("<th>" + event + "</th>");

            if(valuesAmt == 0)
                valuesAmt = eventsMap.get(event).size();

            for(List<String> value : eventsMap.values()){
                html.append("<td>" + value.get(i) + "</td>");
            }

            if(!event.equalsIgnoreCase("json")){
                html.append("<tr>");
                html.append("<td>" + event + "</td>");
                html.append("<td>" + eventsMap.get(event).get(i) + "</td>");
                html.append("</tr>");
            }


            html.append("</tbody>");
            html.append("</table>");
            result.add(html.toString());
        }

        for(int i=0; i<valuesAmt; i++){

            for(List<String> value : eventsMap.values()){
                html.append("<td>" + value.get(i) + "</td>");
            }

        }

        return result;
    }*/
}
