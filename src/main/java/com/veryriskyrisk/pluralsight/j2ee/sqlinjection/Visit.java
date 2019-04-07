package com.veryriskyrisk.pluralsight.j2ee.sqlinjection;

import java.sql.Date;

public class Visit {
    private Long visitId;
    private Date timestamp;
    private String name;
    private String ip;


    public Visit(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Visit(Long visitId, String name, Date timestamp) {
        this.visitId = visitId;
        this.timestamp = timestamp;
        this.name = name;
    }

    public Visit(String name, Date timestamp) {
        this.timestamp = timestamp;
        this.name = name;
    }

    public Visit(String name, Date timestamp, String ip) {
        this.timestamp = timestamp;
        this.name = name;
        this.ip = ip;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public Long getVisitId() {
        return visitId;
    }
}
