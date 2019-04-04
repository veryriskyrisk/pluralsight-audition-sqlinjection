package com.veryriskyrisk.pluralsight.j2ee.sqlinjection;

import java.sql.Date;

public class Visitor {
    private Date timestamp;
    private String name;

    public Visitor(String name, Date timestamp) {
        this.timestamp = timestamp;
        this.name = name;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }
}
