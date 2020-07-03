/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import com.mysql.jdbc.Connection;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import static Scheduler.ScheduleWiz.logInfo;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class LogINController implements Initializable {

    @FXML
    private PasswordField loginPwInput;

    @FXML
    private TextField loginUserInput;

    @FXML
    private Label loginErrorMessage;

    @FXML
    private Button loginSubmitButt;

    @FXML
    private Label loginUserLabel;

    @FXML
    private Label loginPwLabel;

    @FXML
    private Label loginLabel;

    //practice with login error alerts
//    private Boolean isValid(String loginUserInput, String loginPasswordInput) {
//        String errorMessage = "";
//        Boolean valid = false;
//
//        if (loginUserInput == null || loginUserInput.length() == 0) {
//            errorMessage += ("Username cannot be empty\n");
//        }
//
//        if (loginPasswordInput == null || loginPasswordInput.length() == 0) {
//            errorMessage += ("Password cannot be empty\n");
//        }
//        if (errorMessage.length() > 0) {
//            errorMessage += ("\nPlease correct any errors and login again");
//
//            Alert alert = ScheduleWiz.alert("error", "Validation Error", errorMessage);
//            alert.showAndWait();
//        } else {
//            valid = true;
//        }
//
//        return valid;
//    }
    
    @FXML
    void handleSubmitLogin(ActionEvent event) throws SQLException {//, IOException {

        //tester login
        if (loginUserInput.getText().equals("test") && loginPwInput.getText().equals("test")) {
            ScheduleWiz.user = this.loginUserInput.getText();
            try {apptAlertNotify();
                logInfo("tester logged in");
                Stage main = ScheduleWiz.loadStage("/ViewController/Main.fxml");
                main.show();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT userId FROM user WHERE LOWER (userName) = ? AND password = ? AND active = 1");
        ScheduleWiz.pStmt.setString(1, this.loginUserInput.getText().toLowerCase());
        ScheduleWiz.pStmt.setString(2, this.loginPwInput.getText());
        ResultSet rs = ScheduleWiz.pStmt.executeQuery();
//        ScheduleWiz.user = loginUserInput.getText();

        if (!rs.first()) {
            loginErrorMessage.setVisible(true);
            logInfo("attempted login for user '" + this.loginUserInput.getText() + "'");
            ScheduleWiz.pStmt.close();
            rs.close();
        } else {
            ScheduleWiz.user = this.loginUserInput.getText();
            apptAlertNotify();
            logInfo("Successfule login for user '" + this.loginUserInput.getText() + "'");
            ScheduleWiz.pStmt.close();
            rs.close();
            try {
                apptAlertNotify();
                System.out.println("Open MainView");
                Stage main = ScheduleWiz.loadStage("/ViewController/Main.fxml");
                main.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
     
        setLanguage();
        loginErrorMessage.setVisible(false);
    }

    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.LogIN", Locale.getDefault());
        loginLabel.setText(rb.getString("loginLabel"));
        loginUserLabel.setText(rb.getString("loginUserLabel"));
        loginPwLabel.setText(rb.getString("loginPasswordLabel"));
        loginSubmitButt.setText(rb.getString("loginSubmitBtn"));
        loginErrorMessage.setText(rb.getString("loginErrorMessage"));
    }

// alert if upcoming appointments within 15 minutes
    private void apptAlertNotify() throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT title, description, contact, url, start, end, customerName  "
                + "FROM appointment a "
                + "INNER JOIN customer c ON c.customerId = a.customerId "
                + "WHERE start BETWEEN ? AND ? "
                + "AND a.createdBy = ? "
                + "ORDER BY a.start ASC");

        Timestamp tNowA = ScheduleWiz.convertToUTCDate(LocalDateTime.now());

        Timestamp tNowB = ScheduleWiz.convertToUTCDate(LocalDateTime.now().plusMinutes(15));
        ScheduleWiz.pStmt.setTimestamp(1, tNowA);
        ScheduleWiz.pStmt.setTimestamp(2, tNowB);
        ScheduleWiz.pStmt.setString(3, ScheduleWiz.user);

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        if (rs.isBeforeFirst()) {
            String apptErrorAlerttMsg = "";
            apptErrorAlerttMsg += ("ALERT! You have appointments soon!");

            while (rs.next()) {
                apptErrorAlerttMsg += ("\n\n");
                ZonedDateTime zStart = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("start"));
                ZonedDateTime zEnd = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("end"));

                apptErrorAlerttMsg += (rs.getString("title") + "\n");
                apptErrorAlerttMsg += ("From " + zStart.toLocalTime().toString() + " to " + zEnd.toLocalTime().toString() + "\n");
                apptErrorAlerttMsg += ("With " + rs.getString("customerName"));
            }

            Alert alert = ScheduleWiz.alert("info", "Appointment Coming Up Soon", apptErrorAlerttMsg);

            //lambda for quick alert
            alert.showAndWait().ifPresent((response -> {
                if (response == ButtonType.OK) {
                }
            }));
        }

    }
}
