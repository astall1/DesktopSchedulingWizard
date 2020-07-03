/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import com.mysql.jdbc.Connection;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import Model.Customer;
import Model.ReportCustomer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class ReportCustController implements Initializable {

    @FXML
    private ComboBox<Customer> customerCbox;
    @FXML
    private TableView<ReportCustomer> reportTable;
    @FXML
    private TableColumn<ReportCustomer, String> reportTableCust;
    @FXML
    private TableColumn<ReportCustomer, String> reportTableDate;
    @FXML
    private TableColumn<ReportCustomer, Integer> reportTableApptTotal;
    @FXML
    private TableColumn<ReportCustomer, Double> reportTableHourTotal;
    @FXML
    private TableColumn<ReportCustomer, Double> reportTableAvgHour;

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reportTableCust.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        reportTableDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        reportTableApptTotal.setCellValueFactory(cellData -> cellData.getValue().totalApptProperty().asObject());
        reportTableHourTotal.setCellValueFactory(cellData -> cellData.getValue().totalTimeProperty().asObject());
        reportTableAvgHour.setCellValueFactory(cellData -> cellData.getValue().avgTimeProperty().asObject());

        try {
            populateCustomers();
            reportPush();//will default to running the report on all customers
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
    void SelectCustomer(ActionEvent event) throws SQLException {
        reportPush();
    }

    //Populate with business hours     
    private void populateCustomers() throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT customerName, customerId FROM customer WHERE active = 1 ORDER BY customerName ASC;");

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        customers.add(new Customer(0, "All"));

        while (rs.next()) {
            customers.add(
                    new Customer(rs.getInt("customerId"), rs.getString("customerName"))
            );
        }

        this.customerCbox.setItems(customers);
        this.customerCbox.getSelectionModel().selectFirst();//default to first value

        StringConverter<Customer> converter = new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer.nameProperty().get();
            }

            //lambda to process collection of customer records
            @Override
            public Customer fromString(String id) {
                return customers.stream()
                        .filter(item -> item.customerIDProperty().toString().equals(id))
                        .collect(Collectors.toList()).get(0);
            }
        };

        this.customerCbox.setConverter(converter);
    }

    private Double hoursStyle(Double value) {
        Double hour = Math.floor(value / 60);
        BigDecimal minuteInt = new BigDecimal((value % 60) / 60);
        minuteInt = minuteInt.setScale(1, RoundingMode.HALF_UP);

        return hour + minuteInt.doubleValue();
    }

    private void reportPush() throws SQLException {
        Integer customerId = this.customerCbox.getSelectionModel().getSelectedItem().getCustomerID();
        String whereClauseString = "WHERE 1 = 1 ";

        if (customerId != 0) {
            whereClauseString = "WHERE c.customerId = ? ";
        }

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT c.customerName, Count(a.appointmentID) AS totalAppt, Month(start) AS theMonth, Year(start) AS theYear, SUM(TIMESTAMPDIFF(MINUTE, start, end)) AS totalTime "
                + "FROM appointment a "
                + "INNER JOIN customer c ON a.customerId = c.customerId "
                + whereClauseString
                + "GROUP BY c.customerName, Year(start), Month(start) "
                + "ORDER BY c.customerName ASC, Year(start), Month(start);");

        if (customerId != 0) {
            ScheduleWiz.pStmt.setInt(1, customerId);
        }
        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        ObservableList<ReportCustomer> records = FXCollections.observableArrayList();

        while (rs.next()) {
            LocalDate month = LocalDate.of(rs.getInt("theYear"), rs.getInt("theMonth"), 1);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy");
            //time conversion
            Double totalHours = hoursStyle(rs.getDouble("totalTime"));
            Double avgTime = hoursStyle(rs.getDouble("totalTime") / rs.getInt("totalAppt"));

            records.add(
                    new ReportCustomer(
                            rs.getString("customerName"),
                            month.format(dtf),
                            rs.getInt("totalAppt"),
                            totalHours,
                            avgTime
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        this.reportTable.setItems(records);
    }

}
