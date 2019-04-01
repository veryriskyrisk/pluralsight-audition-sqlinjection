package com.veryriskyrisk.pluralsight.j2ee.sqlinjection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class HelloSqlInjectionController {

    private static final Logger logger = Logger.getLogger(HelloSqlInjectionController.class.getName());

    @Value("${connectionstring}")
    String connectionString;

    @PostMapping("/")
    public String persistVisitor(@RequestParam(name = "name", required = false) String name, ModelMap model) {


        name = (name == null || name.equals("")) ? "Anonymous" : name;
        model.addAttribute("name", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);

            String insertVisitorQuery = "INSERT INTO visitors(timestamp, name) VALUES('" + timestamp + "', '" + name + "');";
            model.addAttribute("query", insertVisitorQuery);

            PreparedStatement insertVisitorStatement = connection.prepareStatement(
                    insertVisitorQuery);
            insertVisitorStatement.execute();

            Collection<Visitor> visitorList = getLatestVisitors(connection);
            model.addAttribute("latestVisitors", visitorList);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "welcome";
    }

    private Collection<Visitor> getLatestVisitors(Connection connection) throws SQLException {
        PreparedStatement selectVisitorsStatement = connection.prepareStatement(
                "SELECT name, timestamp FROM visitors ORDER BY timestamp desc LIMIT 10"
        );

        selectVisitorsStatement.execute();
        ResultSet selectVisitorsResultset = selectVisitorsStatement.getResultSet();

        Collection<Visitor> visitorList = new LinkedList<Visitor>();
        while (selectVisitorsResultset.next()) {
            visitorList.add(new Visitor(
                    selectVisitorsResultset.getString("name"),
                    selectVisitorsResultset.getDate("timestamp")

            ));

        }
        return visitorList;
    }

    @GetMapping("/")
    public String index(ModelMap model) {
        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);
            Collection<Visitor> visitorList = getLatestVisitors(connection);
            model.addAttribute("latestVisitors", visitorList);

            model.addAttribute("name", "Anonymous");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "welcome";
    }
}


class Visitor {
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