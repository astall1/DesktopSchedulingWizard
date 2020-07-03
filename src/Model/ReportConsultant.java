/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author remin
 */
public class ReportConsultant {

    private final StringProperty customerName;
    private final StringProperty title;
    private final StringProperty date;
    private final StringProperty start;
    private final StringProperty end;
    private final StringProperty location;

    public ReportConsultant(String customerName, String title, String date, String start, String end, String location) {
        this.customerName = new SimpleStringProperty(customerName);
        this.title = new SimpleStringProperty(title);
        this.date = new SimpleStringProperty(date);
        this.start = new SimpleStringProperty(start);
        this.end = new SimpleStringProperty(end);
        this.location = new SimpleStringProperty(location);
    }

    public StringProperty customerNameProperty() {
        return customerName;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty startProperty() {
        return start;
    }

    public StringProperty endProperty() {
        return end;
    }

    public StringProperty locationProperty() {
        return location;
    }

}
