package com.partsinventory.model;

import java.time.LocalDate;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Bill {

    private IntegerProperty id;
    private StringProperty clientName;
    private StringProperty clientPhone;
    private FloatProperty totalPrice;
    private ObjectProperty<LocalDate> date;

    public Bill(
            Integer id, String clientName, String clientPhone, Float totalPrice, LocalDate date) {
        this.id = new SimpleIntegerProperty();
        this.clientName = new SimpleStringProperty();
        this.clientPhone = new SimpleStringProperty();
        this.totalPrice = new SimpleFloatProperty();
        this.date = new SimpleObjectProperty<LocalDate>();
        this.id.set(id);
        this.clientName.set(clientName);
        this.clientPhone.set(clientPhone);
        this.totalPrice.set(totalPrice);
        this.date.set(date);
    }

    public Bill() {
        this.id = new SimpleIntegerProperty();
        this.clientName = new SimpleStringProperty();
        this.clientPhone = new SimpleStringProperty();
        this.totalPrice = new SimpleFloatProperty();
        this.date = new SimpleObjectProperty<LocalDate>();
    }

    public Integer getId() {
        return id.getValue();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public String getClientName() {
        return clientName.getValue();
    }

    public void setClientName(String clientName) {
        this.clientName.set(clientName);
    }

    public String getClientPhone() {
        return clientPhone.getValue();
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone.set(clientPhone);
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public Float getTotalPrice() {
        return totalPrice.getValue();
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice.set(totalPrice);
    }
}
