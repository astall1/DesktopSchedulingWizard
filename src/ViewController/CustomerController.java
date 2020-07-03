/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ViewController;

import java.util.Scanner;
import Model.Customer;
import Scheduler.DBaseConnect;
import Scheduler.ScheduleWiz;
import com.mysql.jdbc.Connection;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
//import javafx.util.StringConverter; //if need for deletions
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * FXML Controller class
 *
 * @author remin
 */
public class CustomerController implements Initializable {

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> customerTableId;
    @FXML
    private TableColumn<Customer, String> customerTableName;
    @FXML
    private TableColumn<Customer, String> customerTableAddress;
    @FXML
    private TableColumn<Customer, String> customerTableActive;
    @FXML
    private TextField customerId;
    @FXML
    private TextField addressId;
    @FXML
    private TextField textCustField;
    @FXML
    private Button handleDelCustomer;
    @FXML
    private AnchorPane customerPane;
    @FXML
    private TextField customerName;
    @FXML
    private TextField customerAddress;
    @FXML
    private TextField customerAddress2;
    @FXML
    private TextField customerPostal;
    @FXML
    private TextField customerCity;
    @FXML
    private TextField customerCountry;
    @FXML
    private TextField customerPhone;
    @FXML
    private CheckBox customerActive;
    @FXML
    private TextField customerSearch;

//    private ObservableList<Customer> customerData = FXCollections.observableArrayList();
    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        NewCustomer();
        customerTableId.setCellValueFactory(cellData -> cellData.getValue().customerIDProperty().asObject());
        customerTableName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        customerTableAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        customerTableActive.setCellValueFactory(cellData -> cellData.getValue().activeProperty());

        try { 
            updateTableArea();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void NewCustomer() {
        this.customerId.setText("");
        this.addressId.setText("");
        this.customerName.setText("");
        this.customerAddress.setText("");
        this.customerAddress2.setText("");
        this.customerCity.setText("");
        this.customerPostal.setText("");
        this.customerCountry.setText("");
        this.customerPhone.setText("");
        this.customerActive.setSelected(true);

        this.customerName.requestFocus();
    }

    @FXML
    void SaveCustomer(ActionEvent event) throws SQLException {

        if (isValid(this.customerName.getText(),
                this.customerAddress.getText(),
                this.customerCity.getText(),
                this.customerPostal.getText(),
                this.customerCountry.getText(),
                this.customerPhone.getText(),
                this.customerActive.isSelected()
        )) {
            //cursor hourglass
            customerPane.getScene().setCursor(Cursor.WAIT);

            int countryId = getNewCountryId(this.customerCountry.getText());
            int cityId = getNewCityId(this.customerCity.getText(), countryId);
            if (this.customerId.getText().isEmpty()) {
                Integer addressIdNew = addAddress(this.customerAddress.getText(), this.customerAddress2.getText(), this.customerPostal.getText(), this.customerPhone.getText(), cityId);
                addCustomer(this.customerName.getText(), addressIdNew, this.customerActive.isSelected());
                Alert alert = ScheduleWiz.alert("info", "Customer Action", "New customer '" + this.customerName.getText() + "' successfully added");
                alert.showAndWait();
            } else {
                updateAddress(Integer.parseInt(this.addressId.getText()), this.customerAddress.getText(), this.customerAddress2.getText(), this.customerPostal.getText(), this.customerPhone.getText(), cityId);
                updateCustInfo(Integer.parseInt(this.customerId.getText()), this.customerName.getText(), this.customerActive.isSelected());
                Alert alert = ScheduleWiz.alert("info", "Customer Action", "Customer '" + this.customerName.getText() + "' successfully updated");
                alert.showAndWait();
            }
            updateTableArea();
            //Set cursor back 
            customerPane.getScene().setCursor(Cursor.DEFAULT);
            NewCustomer();
        }
    }

    @FXML
    void EditCustomer(ActionEvent event) throws SQLException {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer != null) {
            IntegerProperty id = selectedCustomer.customerIDProperty();

            ScheduleWiz.dbConnect = DBaseConnect.getConnection();
            ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT c.customerId, c.customerName, c.active, c.addressId, a.address, a.address2, a.postalCode, a.phone, ci.city, co.country "
                    + "FROM customer c "
                    + "LEFT OUTER JOIN address a ON a.addressId = c.addressId "
                    + "LEFT OUTER JOIN city ci ON ci.cityId = a.cityId "
                    + "LEFT OUTER JOIN country co ON co.countryId = ci.countryId "
                    + "WHERE c.customerId = ?");
            ScheduleWiz.pStmt.setInt(1, id.getValue());

            System.out.println(ScheduleWiz.pStmt.toString());

            ResultSet rs = ScheduleWiz.pStmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                Alert alert = ScheduleWiz.alert("error", "No record exists", "Customer record could not be found");
                alert.showAndWait();
            } else {
                while (rs.next()) {
                    System.out.println("Process selected customer");

                    this.customerId.setText(rs.getString("customerId"));
                    this.addressId.setText(rs.getString("addressId"));
                    this.customerName.setText(rs.getString("customerName"));
                    this.customerAddress.setText(rs.getString("address"));
                    this.customerAddress2.setText(rs.getString("address2"));
                    this.customerPostal.setText(rs.getString("postalCode"));
                    this.customerCity.setText(rs.getString("city"));
                    this.customerCountry.setText(rs.getString("country"));
                    this.customerPhone.setText(rs.getString("phone"));
                    this.customerActive.setSelected(rs.getInt("active") == 1 ? true : false);
                }
            }

            ScheduleWiz.pStmt.close();
            rs.close();
        } else {
            Alert alert = ScheduleWiz.alert("error", "No record selected", "Please select a customer record to edit");
            alert.showAndWait();
        }
    }

    @FXML
    void RevertMain(ActionEvent event) throws IOException {
        System.out.println("Open Main");
        Stage main = ScheduleWiz.loadStage("/ViewController/Main.fxml");
        main.show();
    }

    @FXML
    void CancelCustomer(ActionEvent event) throws IOException {
        Alert alert = ScheduleWiz.alert("confirm", "Confirm Cancel", "Warning: Any unsaved changes will be lost.");
        //lambda alert for quick confirmation
        alert.showAndWait().ifPresent((response -> {
            if (response == ButtonType.OK) {
                NewCustomer();
            }
        }));
    }

    @FXML
    void SearchCustomer(ActionEvent event) throws SQLException {
        ObservableList customersLocated = getCustomersList();

        if (customersLocated.isEmpty()) {
            System.out.println("No results found");
            Alert alert = ScheduleWiz.alert("error", "No Results found", "No records match '" + this.customerSearch.getText() + "'");
            alert.showAndWait();
        } else {
            System.out.println("Results found");
            customerTable.setItems(customersLocated);
        }
    }

    private Boolean isValid(String name, String address1, String city, String postal, String country, String phone, Boolean active) {
        String errorAlertMsg = "";
        Boolean valid = false;

        if (name == null || name.length() == 0) {
            errorAlertMsg += ("Name cannot be empty\n");
        }

        if (address1 == null || address1.length() == 0) {
            errorAlertMsg += ("Address cannot be empty\n");
        }

        if (city == null || city.length() == 0) {
            errorAlertMsg += ("Please enter city\n");
        }

        if (postal == null || postal.length() == 0) {
            errorAlertMsg += "Postal cannot be empty\n";
        } else if (postal.length() > 10 || postal.length() < 5) {
            errorAlertMsg += "Enter valid Zip Code\n";
        }

        if (country == null || country.length() == 0) {
            errorAlertMsg += ("Country cannot be empty\n");
        }

        if (phone == null || phone.length() == 0) {
            errorAlertMsg += ("Phone cannot be empty\n");
        } else if (phone.length() < 10 || phone.length() > 15) {
            errorAlertMsg += "Please enter a valid phone number with including Area Code.\n";
        }

        if (!active) {
            Alert alert = ScheduleWiz.alert("info", "Customer not active", "Warning: Customer cannot schedule appointments until active");
            //lambda for quick warning   
            alert.showAndWait().ifPresent((response -> {
                if (response == ButtonType.OK) {
                }
            }));
        }

        if (errorAlertMsg.length() > 0) {
            errorAlertMsg += ("\nPlease correct errors and save again");

            Alert alert = ScheduleWiz.alert("error", "Validation Error", errorAlertMsg);
            alert.showAndWait();
        } else {
            valid = true;
        }

        return valid;
    }

    //create records if do not exist
    private static Integer getNewCountryId(String country) throws SQLException {
        Integer countryId = 0;

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT countryId FROM country WHERE LOWER(country) = ?");
        ScheduleWiz.pStmt.setString(1, country.toLowerCase());
        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        if (!rs.isBeforeFirst()) {
            System.out.println("New country");
            ScheduleWiz.pStmt.close();
            rs.close();

            ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT @Id := IFNULL(MAX(countryId)+1, 1) FROM country; "
                    + "INSERT INTO country (countryId, country, createDate, createdBy, lastUpdateBy) VALUES (@Id, ?, CURRENT_TIMESTAMP, ?, ?); "
                    + "SELECT @Id"); //:= IFNULL(MAX(countryId)+1, 1) FROM country; ");
            ScheduleWiz.pStmt.setString(1, country);
            ScheduleWiz.pStmt.setString(2, ScheduleWiz.user);
            ScheduleWiz.pStmt.setString(3, ScheduleWiz.user);
//            ScheduleWiz.pStmt.setNull(3, java.sql.Types.INTEGER

            System.out.println(ScheduleWiz.pStmt.toString());

            rs = ScheduleWiz.pStmt.executeQuery();
//            rs = ScheduleWiz.pStmt.executeUpdate();
//          ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.query(x));

        } else {
            System.out.println("Country exists");
//                    ScheduleWiz.pStmt.close();
//        rs.close();
        }

        while (rs.next()) {
            countryId = rs.getInt(1);
            System.out.println(countryId);
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        return countryId;
    }

    //create records if do not exist
    private Integer getNewCityId(String city, Integer countryId) throws SQLException {
        Integer cityId = 0;

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT cityId FROM city WHERE LOWER(city) = ?");
        ScheduleWiz.pStmt.setString(1, city.toLowerCase());
        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        if (!rs.isBeforeFirst()) {
            ScheduleWiz.pStmt.close();
            rs.close();

            ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT @Id := IFNULL(MAX(cityId)+1, 1) FROM city; "
                    + "INSERT INTO city (cityId, city, countryId, createDate, createdBy, lastUpdateBy) VALUES (@Id, ?, ?, CURRENT_TIMESTAMP, ?, ?);"
                    + "SELECT @Id");
            ScheduleWiz.pStmt.setString(1, city);
            ScheduleWiz.pStmt.setInt(2, countryId);
            ScheduleWiz.pStmt.setString(3, ScheduleWiz.user);
            ScheduleWiz.pStmt.setString(4, ScheduleWiz.user);

            System.out.println(ScheduleWiz.pStmt.toString());

            rs = ScheduleWiz.pStmt.executeQuery();
        }

        while (rs.next()) {
            cityId = rs.getInt(1);
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        return cityId;
    }

    private int addAddress(String address1, String address2, String postal, String phone, Integer cityId) throws SQLException {
        Integer newAddressMark = 0;

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT @Id := IFNULL(MAX(addressId)+1, 1) FROM address; "
                + "INSERT INTO address (addressId, address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdateBy) VALUES (@Id, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?);"
                + "SELECT @Id");
        ScheduleWiz.pStmt.setString(1, address1);
        ScheduleWiz.pStmt.setString(2, address2);
        ScheduleWiz.pStmt.setInt(3, cityId);
        ScheduleWiz.pStmt.setString(4, postal);
        ScheduleWiz.pStmt.setString(5, phone);
        ScheduleWiz.pStmt.setString(6, ScheduleWiz.user);
        ScheduleWiz.pStmt.setString(7, ScheduleWiz.user);

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        while (rs.next()) {
            newAddressMark = rs.getInt(1);
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        return newAddressMark;
    }

    private void updateAddress(Integer addressId, String address1, String address2, String postal, String phone, Integer cityId) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("UPDATE address SET "
                + "address = ?, "
                + "address2 = ?, "
                + "cityId = ?, "
                + "postalCode = ?, "
                + "phone = ?, "
                + "lastUpdateBy = ? "
                + "WHERE addressId = ?;");
        ScheduleWiz.pStmt.setString(1, address1);
        ScheduleWiz.pStmt.setString(2, address2);
        ScheduleWiz.pStmt.setInt(3, cityId);
        ScheduleWiz.pStmt.setString(4, postal);
        ScheduleWiz.pStmt.setString(5, phone);
        ScheduleWiz.pStmt.setString(6, ScheduleWiz.user);
        ScheduleWiz.pStmt.setInt(7, addressId);

        System.out.println(ScheduleWiz.pStmt.toString());

        ScheduleWiz.pStmt.executeUpdate();

        ScheduleWiz.pStmt.close();
    }

    private void addCustomer(String customerName, Integer addressId, Boolean selected) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT @Id := IFNULL(MAX(customerId)+1, 1) FROM customer; "
                + "INSERT INTO customer (customerId, customerName, addressId, active, createDate, createdBy, lastUpdateBy) VALUES (@Id, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?);"
                + "SELECT @Id");
        ScheduleWiz.pStmt.setString(1, customerName);
        ScheduleWiz.pStmt.setInt(2, addressId);
        ScheduleWiz.pStmt.setInt(3, (selected == true) ? 1 : 0);
        ScheduleWiz.pStmt.setString(4, ScheduleWiz.user); //Scheduler.user);
        ScheduleWiz.pStmt.setString(5, ScheduleWiz.user);

        System.out.println(ScheduleWiz.pStmt.toString());

        ScheduleWiz.pStmt.executeQuery();

        ScheduleWiz.pStmt.close();
    }

    private void updateCustInfo(Integer customerId, String customerName, Boolean selected) throws SQLException {
        ScheduleWiz.dbConnect = DBaseConnect.getConnection();
        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("UPDATE customer SET "
                + "customerName = ?, "
                + "active = ?, "
                + "lastUpdateBy = ? "
                + "WHERE customerId = ?;");
        ScheduleWiz.pStmt.setString(1, customerName);
        ScheduleWiz.pStmt.setInt(2, selected ? 1 : 0);
        ScheduleWiz.pStmt.setString(3, ScheduleWiz.user);
        ScheduleWiz.pStmt.setInt(4, customerId);

        System.out.println(ScheduleWiz.pStmt.toString());

        ScheduleWiz.pStmt.executeUpdate();

        ScheduleWiz.pStmt.close();
    }

    private void updateTableArea() throws SQLException {
        ObservableList records = getCustomersList();
        customerTable.setItems(records);
    }

    private ObservableList getCustomersList() throws SQLException {
        String searchTerms = this.customerSearch.getText();

        ScheduleWiz.dbConnect = DBaseConnect.getConnection();

        ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("SELECT c.customerId, c.customerName, c.active, a.address FROM customer c INNER JOIN address a ON a.addressId = c.addressId WHERE LOWER(c.customerName) LIKE ?");
        ScheduleWiz.pStmt.setString(1, searchTerms.toLowerCase() + "%");

        System.out.println(ScheduleWiz.pStmt.toString());

        ResultSet rs = ScheduleWiz.pStmt.executeQuery();

        ObservableList<Customer> customers = FXCollections.observableArrayList();

        while (rs.next()) {
            customers.add(
                    new Customer(
                            rs.getInt("customerId"),
                            rs.getString("customerName"),
                            rs.getString("address"),
                            rs.getInt("active")
                    )
            );
        }

        ScheduleWiz.pStmt.close();
        rs.close();

        return customers;
    }

    private void backToCust() {
        System.out.println("Open Customer");
        Stage main;
        try {
            main = ScheduleWiz.loadStage("/ViewController/Customer.fxml");
            main.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void handleDeleteCustomer(ActionEvent event) throws SQLException {
        Customer custToDelete = customerTable.getSelectionModel().getSelectedItem();

        if (custToDelete != null) {
            IntegerProperty id = custToDelete.customerIDProperty();

            ScheduleWiz.dbConnect = DBaseConnect.getConnection();
            ScheduleWiz.pStmt = ScheduleWiz.dbConnect.prepareStatement("DELETE FROM U05TmQ.customer WHERE customerId= ? ");

            ScheduleWiz.pStmt.setInt(1, id.getValue());
            System.out.println(ScheduleWiz.pStmt.toString());
            //lambda sql deletion and cust data
            ScheduleWiz.RUNSQLCOMM(ScheduleWiz.pStmt, x -> ScheduleWiz.update(x));
            ScheduleWiz.pStmt.close();
            Alert alert = ScheduleWiz.alert("info", "Customer Alert", "Customer successfully deleted");
            //lambda alert
            alert.showAndWait().ifPresent((response -> {
                if (response == ButtonType.OK) {
                    backToCust();
                }
            }));

        } else {
            Alert alert = ScheduleWiz.alert("error", "No record selected", "Please select a customer to delete");
            alert.showAndWait();
        }

    }
}
