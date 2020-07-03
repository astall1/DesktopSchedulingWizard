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
public class Appt {

    private final IntegerProperty id; //l
    private final StringProperty customer;
    private final StringProperty title;
    private final StringProperty date;
    private final StringProperty start;
    private final StringProperty end;
    private final StringProperty consultant;
    private final StringProperty location;

    public Appt(int id, String customer, String title, String date, String start, String end, String consultant, String location) {
        this.id = new SimpleIntegerProperty(id);
        this.customer = new SimpleStringProperty(customer);
        this.title = new SimpleStringProperty(title);
        this.date = new SimpleStringProperty(date);
        this.start = new SimpleStringProperty(start);
        this.end = new SimpleStringProperty(end);
        this.consultant = new SimpleStringProperty(consultant);
        this.location = new SimpleStringProperty(location);
    }

    public IntegerProperty appointmentID() {
        return id;
    }

    public StringProperty customerProperty() {
        return customer;
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

    public StringProperty consultantProperty() {
        return consultant;
    }

    public StringProperty locationProperty() {
        return location;
    }

    public int getappointmentID() {
        return this.id.get();
    }

}
