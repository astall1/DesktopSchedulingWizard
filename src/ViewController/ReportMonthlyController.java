/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import Model.ReportMonthly;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.mysql.jdbc.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class ReportMonthlyController implements Initializable {

    @FXML
    private TableView<ReportMonthly> MonthlyReportTable;
    @FXML
    private TableColumn<ReportMonthly, String> MonthlyReportTableDate;
    @FXML
    private TableColumn<ReportMonthly, String> MonthlyReportTableType;
    @FXML
    private TableColumn<ReportMonthly, Integer> MonthlyReportTableTotal;
    @FXML
    private AnchorPane MonthlyReportScreenPane;

    private ObservableList<ReportMonthly> records = FXCollections.observableArrayList();

    @FXML
    void BackToReportScreen(ActionEvent event) throws IOException {
        System.out.println("Open Reports");
        Stage main = ScheduleWiz.loadStage("/ViewController/Report.fxml");
        main.show();
    }

    /**
     * Initializes the controller class.
     *
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MonthlyReportTableDate.setCellValueFactory(cellData -> cellData.getValue().monthYearProperty());
        MonthlyReportTableType.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        MonthlyReportTableTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());

        try {
            updateMonthlyTableView();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateMonthlyTableView() throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT Count(appointmentId) AS total, Month(start) as theMonth, Year(start) AS theYear, title "
                + "FROM appointment "
                + "GROUP BY Year(start), Month(start), title "
                + "ORDER BY Year(start) ASC, Month(start) ASC, title ASC;");

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        ObservableList<ReportMonthly> records = FXCollections.observableArrayList();

        while (rs.next()) {
            LocalDate month = LocalDate.of(rs.getInt("theYear"), rs.getInt("theMonth"), 1);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy");

            records.add(
                    new ReportMonthly(
                            month.format(dtf),
                            rs.getString("title"),
                            rs.getInt("total")
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        this.MonthlyReportTable.setItems(records);
    }

}
