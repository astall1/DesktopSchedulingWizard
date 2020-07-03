/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 *
 * @author remin
 */
public class Customer {

    private final IntegerProperty customerID;
    private final StringProperty name;
    private final StringProperty address;
    private final StringProperty active;

    public Customer(int id, String name, String address, int active) {
        this.customerID = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.active = new SimpleStringProperty((active == 1) ? "Yes" : "No");
    }

    public Customer(int id, String name) {
        this.customerID = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty("");
        this.active = new SimpleStringProperty("Yes");
    }

    public IntegerProperty customerIDProperty() {
        return customerID;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty activeProperty() {
        return active;
    }

    public int getCustomerID() {
        return this.customerID.get();
    }

}
