/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import Model.ReportConsultant;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.mysql.jdbc.Connection;

/**
 * FXML Controller class
 *
 * @author remin
 */

public class ReportConsultantController implements Initializable {

    @FXML
    private TableView<ReportConsultant> ConsultTable;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableCustomer;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableTitle;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableDate;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableStart;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableEnd;
    @FXML
    private TableColumn<ReportConsultant, String> ConsultTableLoc;
    @FXML
    private ComboBox<String> consultCbox;
    @FXML
    private Label ShowRecords;

    private String selectedUser = "";
    private ObservableList<String> consultantsList = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     *
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ConsultTableCustomer.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        ConsultTableTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        ConsultTableDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        ConsultTableStart.setCellValueFactory(cellData -> cellData.getValue().startProperty());
        ConsultTableEnd.setCellValueFactory(cellData -> cellData.getValue().endProperty());
        ConsultTableLoc.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        try {
            populateConsultants();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void BackToReportScreen(ActionEvent event) throws IOException {
        System.out.println("Open Reports");
        Stage main = ScheduleWiz.loadStage("/ViewController/Report.fxml");
        main.show();
    }

    @FXML
    void RetrieveSchedule(ActionEvent event) throws SQLException {
        String selectedConsultant = this.consultCbox.getSelectionModel().getSelectedItem();

        if (selectedConsultant != "Select Consultant" && selectedConsultant != selectedUser) {
            selectedUser = selectedConsultant;
            updateTableArea();
        }
    }

    private void populateConsultants() throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT userName FROM user ORDER BY userName ASC;");

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        consultantsList.add("Select Consultant");

        while (rs.next()) {
            consultantsList.add(rs.getString("userName"));
        }

        this.consultCbox.setItems(consultantsList);
        this.consultCbox.getSelectionModel().selectFirst();//default to first value
    }

    private void updateTableArea() throws SQLException {
        Integer recordTotal = 0;
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT a.location, a.title, a.start, a.end, c.customerName "
                + "FROM appointment a "
                + "INNER JOIN customer c ON c.customerId = a.customerId "
                + "WHERE a.createdBy = ? "
                + "ORDER BY a.start ASC");

        ScheduleWiz.pStmt.setString(1, selectedUser);

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        ObservableList<ReportConsultant> records = FXCollections.observableArrayList();

        while (rs.next()) {
            recordTotal += 1;
            ZonedDateTime zStart = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("start"));
            ZonedDateTime zEnd = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("end"));

            records.add(
                    new ReportConsultant(
                            rs.getString("customerName"),
                            rs.getString("title"),
                            zStart.toLocalDate().toString(),
                            zStart.toLocalTime().toString(),
                            zEnd.toLocalTime().toString(),
                            rs.getString("location")
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        this.ConsultTable.setItems(records);
        this.ShowRecords.setText(recordTotal.toString());
    }

}
