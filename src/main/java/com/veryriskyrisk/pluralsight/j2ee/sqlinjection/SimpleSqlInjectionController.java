package com.veryriskyrisk.pluralsight.j2ee.sqlinjection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple SQL injection attack vector:
 * - `Hacker'), ('2019-04-05', (select password from users where username = 'admin')) -- `
 * Entire query, after attack vector is appended may look like that:
 * - `INSERT INTO visits VALUES('2019-04-05', 'Hacker'), ('2019-04-05', (SELECT password FROM users WHERE username = 'admin')) -- `
 */
@Controller
public class SimpleSqlInjectionController {

    private static final Logger logger = Logger.getLogger(SimpleSqlInjectionController.class.getName());

    @Value("${connectionstring}")
    String connectionString;

    @PostMapping("/simple")
    public String persistVisit(@RequestParam(name = "name", required = false) String name, ModelMap model) {


        name = (name == null || name.equals("")) ? "Anonymous" : name;
        model.addAttribute("name", name);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(connectionString);

            String insertVisitQuery = "INSERT INTO visits(timestamp, name) VALUES('" + timestamp + "', '" + name + "');";
            model.addAttribute("query", insertVisitQuery);

            PreparedStatement insertVisitStatement = connection.prepareStatement(
                    insertVisitQuery);
            insertVisitStatement.execute();

            Collection<Visit> visitorList = getLatestVisits(connection);
            model.addAttribute("latestVisits", visitorList);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException("We cannot add this visit", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "simple";
    }

    private Collection<Visit> getLatestVisits(Connection connection) throws SQLException {
        PreparedStatement selectVisitorsStatement = connection.prepareStatement(
                "SELECT name, timestamp FROM visits ORDER BY timestamp desc LIMIT 5"
        );

        selectVisitorsStatement.execute();
        ResultSet selectVisitorsResultset = selectVisitorsStatement.getResultSet();

        Collection<Visit> visitsList = new LinkedList<>();
        while (selectVisitorsResultset.next()) {
            visitsList.add(new Visit(
                    selectVisitorsResultset.getString("name"),
                    selectVisitorsResultset.getDate("timestamp")

            ));

        }
        return visitsList;
    }

    @GetMapping("/simple")
    public String index(ModelMap model) {
        Connection connection = null;


        try {
            connection = DriverManager.getConnection(connectionString);
            Collection<Visit> visitsList = getLatestVisits(connection);
            model.addAttribute("latestVisits", visitsList);

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

        return "simple";
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        logger.log(Level.SEVERE, "Request: " + req.getRequestURL() + " raised " + ex, ex);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");
        return mav;
    }
}


