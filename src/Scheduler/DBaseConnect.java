/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Scheduler;

import com.mysql.jdbc.Connection;
import java.sql.SQLException;
//import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author remin
 */
public class DBaseConnect {

    private static Connection connectDBase;

    public DBaseConnect() {

    }

    public static void init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connectDBase = (Connection) DriverManager.getConnection("jdbc:mysql://52.206.157.109:3306/U05TmQ?allowMultiQueries=true", "U05TmQ", "53688600810");
        } catch (ClassNotFoundException cnf) {           // :3306/U05TmQ?allowMultiQueries=true"  useServerPrepStmts to true. Set SQL_MODE to NO_BACKSLASH_ESCAPES. 
            cnf.printStackTrace();
        } catch (SQLException se) {
//            se.getCause().printStackTrace();
            System.out.println("Database unavailable");
            throw new RuntimeException(se);
        } catch (Exception ex) {
            System.out.println("Unhandled exception occurred");
            throw new RuntimeException(ex);
        }
    }

    public static Connection getConnection() {
        return connectDBase;
    }

    public static void closeConnection() {
        try {
            connectDBase.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection closed.");
        }
    }
}
