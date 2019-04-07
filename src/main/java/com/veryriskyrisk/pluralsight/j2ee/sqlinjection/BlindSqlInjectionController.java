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

/**
 * Blind SQL injection attack vector:
 * - Hacker'), ('2019-04-05', (select password from users where username = 'admin' AND (password LIKE 'z%' OR SLEEP(3)))) --
 * Entire query, after attack vector is appended may look like that:
 * - INSERT INTO visitors(timestamp, name) VALUES('2019-04-07T17:02:41.1557991', 'Hacker'),
 * ('2019-04-05', (select password from users where username = 'admin' AND (password LIKE 'z%' OR SLEEP(3)))) -- ');
 */
@Controller
public class BlindSqlInjectionController {

    private static final Logger logger = Logger.getLogger(BlindSqlInjectionController.class.getName());

    @Value("${connectionstring}")
    String connectionString;

    @PostMapping("/blind")
    public String persistVisit(@RequestParam(name = "name", required = false) String name, ModelMap model) {


        name = (name == null || name.equals("")) ? "Anonymous" : name;
        model.addAttribute("name", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);

            Collection<Visit> visitList = getLatestVisits(connection);
            model.addAttribute("latestVisits", visitList);

            String insertVisitQuery = "INSERT INTO visits(timestamp, name) VALUES('" + timestamp + "', '" + name + "');";
            model.addAttribute("query", insertVisitQuery);

            PreparedStatement insertVisitStatement = connection.prepareStatement(
                    insertVisitQuery);
            insertVisitStatement.execute();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "blind";
    }

    private Collection<Visit> getLatestVisits(Connection connection) throws SQLException {
        PreparedStatement selectVisitsStatement = connection.prepareStatement(
                "SELECT name, timestamp FROM visits ORDER BY timestamp desc LIMIT 5"
        );

        selectVisitsStatement.execute();
        ResultSet selectVisitsResultset = selectVisitsStatement.getResultSet();

        Collection<Visit> visitList = new LinkedList<>();
        while (selectVisitsResultset.next()) {
            visitList.add(new Visit(
                    selectVisitsResultset.getString("name"),
                    selectVisitsResultset.getDate("timestamp")

            ));

        }
        return visitList;
    }

    @GetMapping("/blind")
    public String index(ModelMap model) {
        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);
            Collection<Visit> visitList = getLatestVisits(connection);
            model.addAttribute("latestVisits", visitList);

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

        return "blind";
    }
}


