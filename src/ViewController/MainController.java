/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import Model.Appt;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import com.mysql.jdbc.Connection;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class MainController implements Initializable {

    @FXML
    private TableView<Appt> apptTable;
    @FXML
    private TableColumn<Appt, String> apptTableDate;
    @FXML
    private TableColumn<Appt, String> apptTableStart;
    @FXML
    private TableColumn<Appt, String> apptTableEnd;
    @FXML
    private TableColumn<Appt, String> apptTableCust;
    @FXML
    private TableColumn<Appt, String> apptTableConsul;
    @FXML
    private TableColumn<Appt, String> apptTableTitle;
    @FXML
    private TableColumn<Appt, String> apptTableLoc;
    @FXML
    private Label dateLabel;
    @FXML
    private ToggleButton calendarToggleButt;
    @FXML
    private AnchorPane primaryPanel;

    private OffsetDateTime offsetTime = OffsetDateTime.now();
    private Integer mode = 0;

    /**
     * Initializes the controller class.
     *
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        apptTableDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        apptTableStart.setCellValueFactory(cellData -> cellData.getValue().startProperty());
        apptTableEnd.setCellValueFactory(cellData -> cellData.getValue().endProperty());
        apptTableCust.setCellValueFactory(cellData -> cellData.getValue().customerProperty());
        apptTableConsul.setCellValueFactory(cellData -> cellData.getValue().consultantProperty());
        apptTableTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        apptTableLoc.setCellValueFactory(cellData -> cellData.getValue().locationProperty());

        try {
            showCalendarformats();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void openCustomerScreen(ActionEvent event) throws IOException {
        System.out.println("Open Customer");
        Stage cust = ScheduleWiz.loadStage("/ViewController/Customer.fxml");
        cust.show();
    }

    @FXML
    void openApptScreen(ActionEvent event) throws IOException {
        System.out.println("Open Appointment");
        Stage appt = ScheduleWiz.loadStage("/ViewController/Appt.fxml");
        appt.show();
    }

    @FXML
    void OpenLogFile(ActionEvent event) throws IOException {
        Runtime.getRuntime().exec("explorer.exe /select,C:\\NetBeansProjects\\C195\\log.txt"); //\\C195/Scheduler\\log.txt");"C://Java//"
    }

    @FXML
    void openReportsScreen(ActionEvent event) throws IOException {
        System.out.println("Open Reports");
        Stage appt = ScheduleWiz.loadStage("/ViewController/Report.fxml");
        appt.show();
    }

    @FXML
    void EditAppointment(ActionEvent event) throws IOException {
        Appt selectedApt = apptTable.getSelectionModel().getSelectedItem();

        if (selectedApt != null) {
            System.out.println("Open Appointment to edit");

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ViewController/Appt.fxml"));
            Parent apptParent = loader.load();

            Scene apptScene = new Scene(apptParent);
            Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            ApptController controller = loader.getController();
            try {
                controller.loadAppointment(selectedApt.getappointmentID());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            app_stage.setScene(apptScene);
            app_stage.show();
        } else {
            Alert alert = ScheduleWiz.alert("error", "Nothing selected", "Please select an appointment");
            alert.showAndWait();
        }
    }

    @FXML
    void ExitSchedulingApp(ActionEvent event) {
        Alert alert = ScheduleWiz.alert("confirm", "Confirm Exit", "Warning: Any unsaved changes will be lost.");
        //lambda for quick alert
        alert.showAndWait().ifPresent((response -> {
            if (response == ButtonType.OK) {
                DBaseConnect.closeConnection();
                System.exit(0);
            }
        }));
    }

    @FXML
    void dateAhead(ActionEvent event) throws SQLException {
        if (this.mode == 0) {
            this.offsetTime = this.offsetTime.plusMonths(1);
        } else {
            this.offsetTime = this.offsetTime.plusWeeks(1);
        }

        showCalendarformats();
    }

    @FXML
    void dateBehind(ActionEvent event) throws SQLException {
        if (this.mode == 0) {
            this.offsetTime = this.offsetTime.minusMonths(1);
        } else {
            this.offsetTime = this.offsetTime.minusWeeks(1);
        }

        showCalendarformats();
    }

    @FXML
    void ToggleCalendarView(ActionEvent event) throws SQLException {
        mode = (mode == 1) ? 0 : 1;//if 1 set to 0, if 1 then set to 0
        this.calendarToggleButt.setText(mode == 1 ? "Month" : "Week");
        if (mode == 1) {
            //default is 1st week
            this.offsetTime = this.offsetTime.with(TemporalAdjusters.firstDayOfMonth());
        }
        showCalendarformats();
    }

    private ObservableList getAppts() throws SQLException {
        String whereClause;
        Integer dateMetric;

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        if (this.mode == 0) {
            whereClause = "WHERE MONTH(a.start) = ? ";
            dateMetric = this.offsetTime.getMonthValue();
        } else {
            whereClause = "WHERE WEEK(a.start) = ? ";
            TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            dateMetric = this.offsetTime.get(woy) - 1;
        }

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT a.appointmentId, a.location, a.title, a.start, a.end, c.customerName, a.createdBy "
                + "FROM appointment a "
                + "INNER JOIN customer c ON c.customerId = a.customerId "
                + whereClause
                + "ORDER BY a.start ASC");

        ScheduleWiz.pStmt.setInt(1, dateMetric);

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        ObservableList<Appt> appointments = FXCollections.observableArrayList();

        while (rs.next()) {
            ZonedDateTime zStart = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("start"));
            ZonedDateTime zEnd = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("end"));

            appointments.add(
                    new Appt(
                            rs.getInt("appointmentId"),
                            rs.getString("customerName"),
                            rs.getString("title"),
                            zStart.toLocalDate().toString(),
                            zStart.toLocalTime().toString(),
                            zEnd.toLocalTime().toString(),
                            rs.getString("createdBy"),
                            rs.getString("location")
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        return appointments;
    }

    private void showCalendarformats() throws SQLException {
        if (mode == 1) {
            DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
            DayOfWeek lastDayOfWeek = DayOfWeek.of(((firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length) + 1);;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String start = this.offsetTime.with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).format(dtf);
            String end = this.offsetTime.with(TemporalAdjusters.nextOrSame(lastDayOfWeek)).format(dtf);

            this.dateLabel.setText(start + " - " + end);
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy");
            this.dateLabel.setText(this.offsetTime.format(dtf));
        }

        ObservableList apptFiles = getAppts();
        apptTable.setItems(apptFiles);
    }

}
