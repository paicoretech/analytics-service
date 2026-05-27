package com.paic.nbm.analyticsservice.Helper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlowDiagramBuilder {
    StringBuilder events;
    String firstNode;
    String from;
    String to;
    String timestamp;
    String type;
    String separator;
    String title;

    public FlowDiagramBuilder(String title) {
        this.events = new StringBuilder("Title: " + title + "\n");
        this.firstNode = "";
    }

    public FlowDiagramBuilder setSeparator(Boolean request) {
        if (request) {
            this.separator = "->";
        } else {
            this.separator = "-->";
        }

        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFrom(String from) {
        this.from = from;

        if (firstNode.isEmpty())
            this.firstNode = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public FlowDiagramBuilder setType(String type) {
        this.type = type;

        return this;
    }

    public void buildEvent() {
        String line = "Note left of " + firstNode + ": " + timestamp + "\n " + from + separator + to + ":" + type + "\n";

        if (firstNode == null || firstNode.isEmpty() ||
                timestamp == null || timestamp.isEmpty() ||
                from == null || from.isEmpty() ||
                separator == null || separator.isEmpty() ||
                to == null || to.isEmpty() ||
                type == null || type.isEmpty()) {
        } else {
            events.append(line);
        }

    }

    public String buildSequenceDiagram() {
        return events.toString().replace("[", "").replace("]", "").replace("\"", "");
    }

    public String getType() {
        return type;
    }

    public void setParticipant(String participant) {
        events.append("participant  " + participant + "\n");
    }

    public String getFirstNode() {
        return this.firstNode;
    }

    public void setFirstNode(String firstNode) {
        this.firstNode = firstNode;
    }
}
