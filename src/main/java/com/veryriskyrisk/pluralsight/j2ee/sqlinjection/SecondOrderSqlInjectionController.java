package com.veryriskyrisk.pluralsight.j2ee.sqlinjection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */

/**
 * Second order SQL injection attack vector:
 * - `Walter Eugene "Radar" O' UNION SELECT username, '1-1-1917', password FROM users -- Reilly`
 * Entire query, after attack vector is appended may look like that:
 * - `SELECT name, timestamp, ip FROM visits WHERE name = 'Walter Eugene "Radar" O' UNION SELECT username, '1-1-1917', password FROM users -- Reilly' ORDER BY timestamp desc LIMIT 5`
 *
 * In order to run exploit go to second order page, submit attack vector via name field.
 * New entry will appear on the right, click on the name that looks exactly as attack vector entered.
 * You'll see passwords from `users` table displayed on the right
 */
@Controller
public class SecondOrderSqlInjectionController {

    private static final Logger logger = Logger.getLogger(SecondOrderSqlInjectionController.class.getName());

    @Value("${connectionstring}")
    String connectionString;

    @PostMapping("/second-order")
    public String persistVisit(@RequestParam(name = "name", required = false) String name, ModelMap model) {


        name = (name == null || name.equals("")) ? "Anonymous" : name;
        model.addAttribute("name", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);

            String insertVisitQuery = "INSERT INTO visits(timestamp, name) VALUES(?, ?);";
            model.addAttribute("query", insertVisitQuery);

            PreparedStatement insertVisitStatement = connection.prepareStatement(
                    insertVisitQuery);

            insertVisitStatement.setString(1, timestamp);
            insertVisitStatement.setString(2, name);

            insertVisitStatement.execute();

            Collection<Visit> visitList = getLatestVisits(connection);
            model.addAttribute("latestVisits", visitList);

        } catch (SQLException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            model.addAttribute("exception", sw.toString());
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

        return "second-order";
    }

    private Collection<Visit> getLatestVisits(Connection connection) throws SQLException {
        PreparedStatement selectVisitsStatement = connection.prepareStatement(
                "SELECT visit_id, name, timestamp FROM visits ORDER BY timestamp desc LIMIT 5"
        );

        selectVisitsStatement.execute();
        ResultSet selectVisitsResultset = selectVisitsStatement.getResultSet();

        Collection<Visit> visitList = new LinkedList<>();
        while (selectVisitsResultset.next()) {
            visitList.add(new Visit(
                    selectVisitsResultset.getLong("visit_id"),
                    selectVisitsResultset.getString("name"),
                    selectVisitsResultset.getDate("timestamp")

            ));

        }
        return visitList;
    }

    @GetMapping("/second-order")
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

        return "second-order";
    }

    @GetMapping("/second-order/visit")
    public String visitDetails(@RequestParam(name = "id", required = true) Long id, ModelMap model) {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(connectionString);

            String selectVisit = "SELECT * FROM visits WHERE visit_id = ?";

            PreparedStatement selectVisitStatement = connection.prepareStatement(
                    selectVisit);

            selectVisitStatement.setLong(1, id);
            selectVisitStatement.execute();
            ResultSet visitDetailsResultset = selectVisitStatement.getResultSet();
            visitDetailsResultset.next();

            String visitorName = visitDetailsResultset.getString("name");
            Date timestamp = visitDetailsResultset.getDate("timestamp");
            String ip = visitDetailsResultset.getString("ip");

            model.addAttribute("visitDetails", new Visit(visitorName, timestamp, ip));

            String selectVisitsWithTheSameName = "SELECT name, timestamp, ip FROM visits WHERE name = '" + visitorName + "' ORDER BY timestamp desc LIMIT 5";
            model.addAttribute("query", selectVisitsWithTheSameName);
            PreparedStatement selectVisitsStatement = connection.prepareStatement(
                    selectVisitsWithTheSameName
            );

            selectVisitsStatement.execute();
            ResultSet selectVisitsResultset = selectVisitsStatement.getResultSet();

            Collection<Visit> visitList = new LinkedList<>();
            while (selectVisitsResultset.next()) {
                visitList.add(new Visit(
                        selectVisitsResultset.getString("name"),
                        selectVisitsResultset.getDate("timestamp"),
                        selectVisitsResultset.getString("ip")
                ));

            }
            model.addAttribute("latestVisits", visitList);

        } catch (SQLException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            model.addAttribute("exception", sw.toString());
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
        return "second-order-details";
    }
}


