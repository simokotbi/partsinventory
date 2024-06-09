package com.partsinventory.controller;

import com.partsinventory.helper.DefaultFloatConvertor;
import com.partsinventory.helper.LocalDateTableCell;
import com.partsinventory.model.Bill;
import com.partsinventory.service.BillService;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class BillsController {

    @FXML private TableView<Bill> billsListTableView;

    @FXML private TableColumn<Bill, Integer> billId;

    @FXML private TableColumn<Bill, String> clientName;

    @FXML private TableColumn<Bill, String> clientPhoneNumber;

    @FXML private TableColumn<Bill, LocalDate> date;

    @FXML private TableColumn<Bill, Float> totalPrice;

    public TableView<Bill> getBillsListTableView() {
        return billsListTableView;
    }

    @FXML
    private void initialize() throws SQLException {
        billId.setCellValueFactory(new PropertyValueFactory<>("id"));

        clientName.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientName.setCellFactory(TextFieldTableCell.forTableColumn());
        clientName.setOnEditCommit(event -> BillService.onEditCommit(event, "clientName"));

        clientPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("clientPhone"));
        clientPhoneNumber.setCellFactory(TextFieldTableCell.forTableColumn());
        clientPhoneNumber.setOnEditCommit(event -> BillService.onEditCommit(event, "clientPhone"));

        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        date.setCellFactory(LocalDateTableCell::new);
        date.setOnEditCommit(event -> BillService.onEditCommit(event, "date"));

        totalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultFloatConvertor()));

        ObservableList<Bill> bills = BillService.getAllBills();
        billsListTableView.setItems(bills);
        billsListTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
