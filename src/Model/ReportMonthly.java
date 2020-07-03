/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;//

/**
 *
 * @author remin
 */
public class ReportMonthly {

    private final StringProperty repMonthYear;
    private final StringProperty repTitle;
    private final IntegerProperty repTotal;

    public ReportMonthly(String monthYear, String title, int total) {
        this.repMonthYear = new SimpleStringProperty(monthYear);
        this.repTitle = new SimpleStringProperty(title);
        this.repTotal = new SimpleIntegerProperty(total);
    }

    public StringProperty monthYearProperty() {
        return repMonthYear;
    }

    public StringProperty titleProperty() {
        return repTitle;
    }

    public IntegerProperty totalProperty() {
        return repTotal;
    }

}
