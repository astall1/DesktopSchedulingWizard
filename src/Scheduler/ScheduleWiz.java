/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Scheduler;

import com.mysql.jdbc.Connection;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 *
 * @author remin
 */
public class ScheduleWiz extends Application {

    public static Connection dbConnect;
    public static PreparedStatement pStmt;
    public static String user;
    static Stage stage;
    public static String loggedInUsername;

    @Override
    public void start(Stage stage) throws Exception {
//        Remove comment below to switch to French
//        Locale.setDefault(new Locale("fr", "FR")); 
//        Remove comment below to switch to Spanish
//        Locale.setDefault(new Locale ("es", "ES"));
        this.stage = stage;

        stage = loadStage("/ViewController/LogIN.fxml");
        stage.setTitle("C195 Scheduler");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        checkDBconn();
        launch(args);
    }
// tracks logins in loginLogs file in 
    public static void logInfo(String message) {
        String filename = "logins.txt";
        String path = "loginLogs/logins.txt";

        File logFilepath = new File(path, filename);
        File logPath = new File(path);

        if (!logPath.exists()) {
            logPath.mkdir();
        }
        try (FileWriter logWrite = new FileWriter(logFilepath, true)) {
            logWrite.write(LocalDateTime.now() + ": " + message + System.lineSeparator());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void checkDBconn() {
        DBaseConnect.init();
        dbConnect = DBaseConnect.getConnection();
    }

    public static Stage loadStage(String view) throws IOException {

        Parent root = FXMLLoader.load(ScheduleWiz.class.getResource(view));                     //getResource(view));        //Scheduler.class.getResource(view));
        Scene scene = new Scene(root);
        stage.setScene(scene);

        return stage;
    }

    public static Alert alert(String type, String title, String message) {
        Alert.AlertType aType = null;

        switch (type.toLowerCase()) {
            case "confirm":
                aType = Alert.AlertType.CONFIRMATION;
                break;

            case "error":
                aType = Alert.AlertType.ERROR;
                break;

            case "info":
                aType = Alert.AlertType.INFORMATION;
                break;

            default:
                aType = Alert.AlertType.INFORMATION;
        }

        Alert alert = new Alert(aType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert;
    }

    public static ResultSet RUNSQLCOMM(PreparedStatement statement, Function<PreparedStatement, ResultSet> action) {
        return action.apply(statement);
    }

    public static ResultSet query(PreparedStatement x) {
        try {
            return x.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(System.out);

            return null;
        }
    }

    public static ResultSet update(PreparedStatement x) {
        try {
            x.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static ZonedDateTime convertUTCtoLocale(Timestamp datetime) {
        ZoneId newzid = ZoneId.systemDefault();
        ZonedDateTime newzdtStart = datetime.toLocalDateTime().atZone(ZoneId.of("UTC"));
        ZonedDateTime newLocalStart = newzdtStart.withZoneSameInstant(newzid);
        return newLocalStart;
    }

    private static Timestamp dateConvertToUTCDate(LocalDateTime date) {
        ZoneId zid = ZoneId.systemDefault();
        ZonedDateTime zdtStart = date.atZone(zid);
        ZonedDateTime utcStart = zdtStart.withZoneSameInstant(ZoneId.of("UTC"));
        date = utcStart.toLocalDateTime();
        Timestamp startsqlts = Timestamp.valueOf(date);

        return startsqlts;
    }

    public static Timestamp convertToUTCDate(String date, String time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm");
        LocalDateTime ldtStart = LocalDateTime.parse(date + " " + time, df);

        return dateConvertToUTCDate(ldtStart);
    }

    public static Timestamp convertToUTCDate(LocalDateTime date) {
        return dateConvertToUTCDate(date);
    }
}
