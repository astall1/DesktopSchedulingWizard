/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import com.mysql.jdbc.Connection;
//import java.sql.Connection;
import Model.Customer;
import javafx.util.Callback;
import javafx.util.StringConverter;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class ApptController implements Initializable {

    @FXML
    private AnchorPane apptPanel;
    @FXML
    private TextField apptTitle;
    @FXML
    private ComboBox<Customer> apptCustomer;
    @FXML
    private TextField apptLocation;
    @FXML
    private TextField apptContact;
    @FXML
    private TextField apptURL;
    @FXML
    private DatePicker apptDate;
    @FXML
    private ComboBox<String> apptStart;
    @FXML
    private ComboBox<String> apptEnd;
    @FXML
    private TextArea apptDescript;
    @FXML
    private TextField fieldApptId;
    @FXML
    private Button deleteButt;

    private ObservableList<String> appTimes = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     *
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        populateTimes();

        try {
            populateCustDropMenu();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        final Callback<DatePicker, DateCell> dayCellFactory
                = new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item.isBefore(LocalDate.now())
                                || item.getDayOfWeek().equals(item.getDayOfWeek().SUNDAY)
                                || item.getDayOfWeek().equals(item.getDayOfWeek().SATURDAY)) {
                                    setDisable(true);

                                }
                            }
                        };
                    }
                };
        apptDate.setDayCellFactory(dayCellFactory);
    }

    @FXML
    void SaveAppt(ActionEvent event) throws SQLException {
        if (isValid(
                this.apptTitle.getText(),
                this.apptCustomer.getSelectionModel().getSelectedItem(),
                this.apptDate.getValue(),
                this.apptStart.getSelectionModel().getSelectedItem(),
                this.apptEnd.getSelectionModel().getSelectedItem()
        )) {
            Timestamp startUTC = ScheduleWiz.convertToUTCDate(this.apptDate.getValue().toString(), this.apptStart.getSelectionModel().getSelectedItem());
            Timestamp endUTC = ScheduleWiz.convertToUTCDate(this.apptDate.getValue().toString(), this.apptEnd.getSelectionModel().getSelectedItem());

            if (this.fieldApptId.getText().isEmpty()) {
                addAppointment(
                        this.apptTitle.getText(),
                        this.apptCustomer.getSelectionModel().getSelectedItem(),
                        this.apptLocation.getText(),
                        this.apptContact.getText(),
                        this.apptURL.getText(),
                        startUTC,
                        endUTC,
                        this.apptDescript.getText()
                );
//lambda alert
                Alert alert = ScheduleWiz.alert("info", "Appointment Alert", "New appointment successfully added");
                alert.showAndWait().ifPresent((response -> {
                    if (response == ButtonType.OK) {
                        backToMainApptScreen();
                    }
                }));
            } else {
                updateAppointment(
                        Integer.parseInt(this.fieldApptId.getText()),
                        this.apptTitle.getText(),
                        this.apptCustomer.getSelectionModel().getSelectedItem(),
                        this.apptLocation.getText(),
                        this.apptContact.getText(),
                        this.apptURL.getText(),
                        startUTC,
                        endUTC,
                        this.apptDescript.getText()
                );
//lambda for alert
                Alert alert = ScheduleWiz.alert("info", "Appointment Alert", "Appointment successfully updated");
                alert.showAndWait().ifPresent((response -> {
                    if (response == ButtonType.OK) {
                        backToMainApptScreen();
                    }
                }));
            }
        }
    }

    // lists only active customers
    private void populateCustDropMenu() throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT customerId, customerName FROM customer WHERE active = 1 ORDER BY customerName ASC;");

        System.out.println(ScheduleWiz.pStmt.toString());
//lamba to process resultset
        ResultSet rs = ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.query(x));

        ObservableList<Customer> customers = FXCollections.observableArrayList(); //ll

        while (rs.next()) {
            customers.add(
                    new Customer(
                            rs.getInt("customerId"),
                            rs.getString("customerName"), "", 1
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        this.apptCustomer.setItems(customers);

        StringConverter<Customer> converter = new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer.nameProperty().get();
            }

            @Override
            public Customer fromString(String id) {
                //lambda tp process cust info 
                return customers.stream()
                        .filter(item -> item.customerIDProperty().toString().equals(id))
                        .collect(Collectors.toList()).get(0);
            }
        };

        this.apptCustomer.setConverter(converter);
    }

    //only appTimes between 9 and 5 in 24 hour military time!! can only add appointments within allowable times m-f 9am to 5pm in military/24 hour time 
    private void populateTimes() {
        LocalTime start = LocalTime.of(0, 0);

        do {
            this.appTimes.add(start.toString());
            start = start.plusMinutes(15);
        } while (start.isBefore(LocalTime.of(23, 15)));

        this.apptStart.setItems(this.appTimes);
        this.apptEnd.setItems(this.appTimes);
    }

    private boolean isValid(String title, Customer customer, LocalDate localDateTime, String startTime, String endTime) throws SQLException {
        String errorAlertmsg = "";
        Boolean valid = false;
        Boolean dateOk = false, timeOk = false;

        if (title == null || title.isEmpty()) {
            errorAlertmsg += ("Title cannot be empty\n");
        }

        if (!(customer instanceof Customer)) {
            errorAlertmsg += ("Must choose customer\n");
        }

        if (localDateTime == null) {
            errorAlertmsg += ("Must select date\n");
        } else {
            //ensure date is in future and not on weekend
            if (localDateTime.isBefore(LocalDate.now())) {
                errorAlertmsg += ("Date must be in future\n");
            } else if (localDateTime.getDayOfWeek().equals(localDateTime.getDayOfWeek().SUNDAY) || localDateTime.getDayOfWeek().equals(localDateTime.getDayOfWeek().SATURDAY)) {
                errorAlertmsg += ("No appointments allowed on weekends\n");
            } else {
                dateOk = true;
            }
        }

        if (startTime == null) {
            errorAlertmsg += ("Must choose start time\n");
        }

        if (endTime == null) {
            errorAlertmsg += ("Must choose end time\n");
        }
        //ensure start is before end
        if (startTime != null && endTime != null) {
            if (LocalTime.parse(startTime).isAfter(LocalTime.parse(endTime))) {
                errorAlertmsg += ("Start time must be earlier than End time\n");
            } else {
                timeOk = true;
            }
        }

        if (timeOk) {
            //appointments only during business hours 9am-5pm
            if ((LocalTime.parse(startTime).getHour() < 9 || LocalTime.parse(startTime).getHour() >= 17)
                    || (LocalTime.parse(endTime).getHour() < 9 || LocalTime.parse(endTime).getHour() > 17)) {
                errorAlertmsg += ("Appointments only allowed during business hours. (9am-5pm or 09:00-17:00)\n");
                timeOk = false;
            }
        }

        if (dateOk && timeOk) {
            Timestamp startUTC = ScheduleWiz.convertToUTCDate(localDateTime.toString(), startTime);
            Timestamp endUTC = ScheduleWiz.convertToUTCDate(localDateTime.toString(), endTime);
            ScheduleWiz.dbConnect = DBaseConnect.getConnection();
            String whereClause = "";

            if (!this.fieldApptId.getText().isEmpty()) {
                whereClause = "AND appointmentId <> ?";
            }

            ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT appointmentId "
                    + "FROM appointment "
                    + "WHERE (? < end AND ? > start) "
                    + whereClause + ";");

            ScheduleWiz.pStmt.setTimestamp(1, startUTC);
            ScheduleWiz.pStmt.setTimestamp(2, endUTC);

            if (!this.fieldApptId.getText().isEmpty()) {
                ScheduleWiz.pStmt.setInt(3, Integer.parseInt(this.fieldApptId.getText()));
            }

            System.out.println(ScheduleWiz.pStmt.toString());

            //lambda to streamline process resultSEts
            ResultSet rs = ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.query(x));

            System.out.println(rs);

            if (!rs.isBeforeFirst()) {

            } else {
                errorAlertmsg += ("\nWarning: Appointment times must not overlap");
            }

            ScheduleWiz.pStmt.close();
            rs.close();
        }

        if (errorAlertmsg.length() > 0) {
            errorAlertmsg += ("\nFix any errors and save again");

            Alert alert = ScheduleWiz.alert("error", "Validation Error", errorAlertmsg);
            alert.showAndWait();
        } else {
            valid = true;
        }

        return valid;
    }

    private void addAppointment(String title, Customer customer, String location, String contact, String url, Timestamp start, Timestamp end, String description) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT @Id := IFNULL(MAX(appointmentId)+1, 1) FROM appointment; "
                + "INSERT INTO appointment (appointmentId, customerId, title, description, location, contact, url, start, end, createDate, createdBy, lastUpdateBy) "
                + "VALUES (@Id, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?);"
                + "SELECT @Id");
        ScheduleWiz.pStmt.setInt(1, customer.getCustomerID());
        ScheduleWiz.pStmt.setString(2, title);
        ScheduleWiz.pStmt.setString(3, description);
        ScheduleWiz.pStmt.setString(4, location);
        ScheduleWiz.pStmt.setString(5, contact);
        ScheduleWiz.pStmt.setString(6, url);
        ScheduleWiz.pStmt.setTimestamp(7, start);
        ScheduleWiz.pStmt.setTimestamp(8, end);
        ScheduleWiz.pStmt.setString(9, ScheduleWiz.user);
        ScheduleWiz.pStmt.setString(10, ScheduleWiz.user);

        System.out.println(ScheduleWiz.pStmt.toString());
//        ScheduleWiz.pStmt.executeQuery(); lambda for sql queries to process appt times
        ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.query(x));

        ScheduleWiz.pStmt.close();
    }

    private void updateAppointment(Integer id, String title, Customer customer, String location, String contact, String url, Timestamp start, Timestamp end, String description) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("UPDATE appointment SET "
                + "customerId = ?, "
                + "title = ?, "
                + "description = ?, "
                + "location = ?, "
                + "contact = ?, "
                + "url = ?, "
                + "start = ?, "
                + "end = ?, "
                + "lastUpdateBy = ? "
                + "WHERE appointmentId = ?;");

        ScheduleWiz.pStmt.setInt(1, customer.getCustomerID());
        ScheduleWiz.pStmt.setString(2, title);
        ScheduleWiz.pStmt.setString(3, description);
        ScheduleWiz.pStmt.setString(4, location);
        ScheduleWiz.pStmt.setString(5, contact);
        ScheduleWiz.pStmt.setString(6, url);
        ScheduleWiz.pStmt.setTimestamp(7, start);
        ScheduleWiz.pStmt.setTimestamp(8, end);
        ScheduleWiz.pStmt.setString(9, ScheduleWiz.user);
        ScheduleWiz.pStmt.setInt(10, id);

        System.out.println(ScheduleWiz.pStmt.toString());
//lambda for sql queries
        ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.update(x));

        ScheduleWiz.pStmt.close();
    }

    private void backToMainApptScreen() {
        System.out.println("Open Main");
        Stage main;
        try {
            main = ScheduleWiz.loadStage("/ViewController/Main.fxml");
            main.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadAppointment(Integer id) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT a.customerId, title, description, contact, url, start, end, location, customerName "
                + "FROM appointment a "
                + "INNER JOIN customer c ON c.customerId = a.customerId "
                + "WHERE appointmentId = ?");
        ScheduleWiz.pStmt.setInt(1, id);

        System.out.println(ScheduleWiz.pStmt.toString());
//lamba to process resultset
        ResultSet rs = ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.query(x));

        if (!rs.isBeforeFirst()) {
            Alert alert = ScheduleWiz.alert("error", "No record available", "The apointment record was not found");
            alert.showAndWait();
        } else {
            while (rs.next()) {
                System.out.println("process appointment record");
                Customer selCust = new Customer(rs.getInt("customerId"), rs.getString("customerName"), "", 1);

                this.fieldApptId.setText(id.toString());

                this.apptCustomer.setValue(selCust);

                this.apptTitle.setText(rs.getString("title"));
                this.apptLocation.setText(rs.getString("location"));
                this.apptURL.setText(rs.getString("url"));
                this.apptContact.setText(rs.getString("contact"));
                this.apptDescript.setText(rs.getString("description"));

                ZonedDateTime zStart = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("start"));
                ZonedDateTime zEnd = ScheduleWiz.convertUTCtoLocale(rs.getTimestamp("end"));

                this.apptDate.setValue(zStart.toLocalDate());
                this.apptStart.getSelectionModel().select(zStart.toLocalTime().toString());
                this.apptEnd.getSelectionModel().select(zEnd.toLocalTime().toString());

                this.deleteButt.setDisable(false);
            }
        }

        ScheduleWiz.pStmt.close();
        rs.close();
    }

    @FXML
    void CancelAppt(ActionEvent event) {
        Alert alert = ScheduleWiz.alert("confirm", "Confirm Cancel", "Would you like to cancel? Any unsaved changes will be lost.");
//lambda for alert
        alert.showAndWait().ifPresent((response -> {
            if (response == ButtonType.OK) {
                backToMainApptScreen();
            }
        }));
    }

    @FXML
    void DeleteAppt(ActionEvent event) {
        Alert alert = ScheduleWiz.alert("confirm", "Confirm Delete", "Warning: Remove this appointment?");
//lambda for alert
        alert.showAndWait().ifPresent((response -> {
            if (response == ButtonType.OK) {
                try {
                    deleteAppointment(this.fieldApptId.getText());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                backToMainApptScreen();
            }
        }));
    }

    private void deleteAppointment(String id) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("DELETE FROM appointment WHERE appointmentId = ?;");
        ScheduleWiz.pStmt.setInt(1, Integer.parseInt(id));

        System.out.println(ScheduleWiz.pStmt.toString());
//lambda for queriesupdate
        ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.update(x));
        ScheduleWiz.pStmt.close();
    }

}
