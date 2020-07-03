/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author remin
 */
public class ReportCustomer {

    private final StringProperty customerName;
    private final StringProperty date;
    private final IntegerProperty totalAppt;
    private final DoubleProperty totalTime;
    private final DoubleProperty avgTime;

    public ReportCustomer(String customerName, String date, int totalAppt, double totalTime, double avgTime) {
        this.customerName = new SimpleStringProperty(customerName);
        this.date = new SimpleStringProperty(date);
        this.totalAppt = new SimpleIntegerProperty(totalAppt);
        this.totalTime = new SimpleDoubleProperty(totalTime);
        this.avgTime = new SimpleDoubleProperty(avgTime);
    }

    public StringProperty customerNameProperty() {
        return customerName;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public IntegerProperty totalApptProperty() {
        return totalAppt;
    }

    public DoubleProperty totalTimeProperty() {
        return totalTime;
    }

    public DoubleProperty avgTimeProperty() {
        return avgTime;
    }

}
