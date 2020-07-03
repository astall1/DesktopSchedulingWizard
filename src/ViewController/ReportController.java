/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import com.mysql.jdbc.Connection;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Scheduler.ScheduleWiz;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * FXML Controller class
 *
 * @author remin
 */

public class ReportController {

    @FXML
    private AnchorPane reportScreenPane;

    @FXML
    void ConsultantReportScreen(ActionEvent event) throws IOException {
        System.out.println("Open Consultant Report");
        Stage main = ScheduleWiz.loadStage("/ViewController/ReportConsultant.fxml");
        main.show();
    }

    @FXML
    void CustomerReportScreen(ActionEvent event) throws IOException {
        System.out.println("Open Customer Report");
        Stage main = ScheduleWiz.loadStage("/ViewController/ReportCust.fxml");
        main.show();
    }

    @FXML
    void MonthlyReportScreen(ActionEvent event) throws IOException {
        System.out.println("Open Monthly Report");
        Stage main = ScheduleWiz.loadStage("/ViewController/ReportMonthly.fxml");
        main.show();
    }

    @FXML
    void RevertMain(ActionEvent event) throws IOException {
        System.out.println("Open Main");
        Stage main = ScheduleWiz.loadStage("/ViewController/Main.fxml");
        main.show();
    }
}
