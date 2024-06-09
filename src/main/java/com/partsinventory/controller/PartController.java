package com.partsinventory.controller;

import static com.partsinventory.helper.AlertHandler.handleDatabaseError;
import static com.partsinventory.helper.AlertHandler.handleStockShortage;

import com.partsinventory.helper.AlertHandler;
import com.partsinventory.helper.DefaultFloatConvertor;
import com.partsinventory.helper.DefaultIntegerConvertor;
import com.partsinventory.model.Category;
import com.partsinventory.model.Command;
import com.partsinventory.model.Part;
import com.partsinventory.service.BillService;
import com.partsinventory.service.PartService;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class PartController {

    @FXML private TableView<Part> partsListTableView;

    @FXML private TableColumn<Part, Integer> partIdColumn;

    @FXML private TableColumn<Part, String> partMakerColumn;

    @FXML private TableColumn<Part, String> partNameColumn;

    @FXML private TableColumn<Part, String> partDescriptionColumn;

    @FXML private TableColumn<Part, Float> partPriceColumn;

    @FXML private TableColumn<Part, Integer> partQuantityColumn;

    @FXML private TableColumn<Part, String> partCategoryColumn;

    public static String loader = "part";

    public TableView<Part> getPartsListTableView() {
        return partsListTableView;
    }

    public TableColumn<Part, Integer> getPartQuantityColumn() {
        return partQuantityColumn;
    }

    public TableColumn<Part, Float> getPartPriceColumn() {
        return partPriceColumn;
    }

    public TableView<Part> getPartsListByCriteriaTableView(String criteria, String identifier) {
        ObservableList<Part> parts = PartService.getPartByCriteria(criteria, identifier);
        partsListTableView.setItems(parts);
        return partsListTableView;
    }

    public TableView<Part> getPartsListInChart(int billId) {
        ObservableList<Part> parts = FXCollections.observableArrayList();
        try {
            parts = BillService.getPartsOfBill(billId);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        partsListTableView.setItems(parts);
        return partsListTableView;
    }

    @FXML
    private void initialize() throws SQLException {
        partIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        partNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        partNameColumn.setOnEditCommit(event -> PartService.onEditCommit(event, "name"));

        partMakerColumn.setCellValueFactory(new PropertyValueFactory<>("maker"));
        partMakerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        partMakerColumn.setOnEditCommit(event -> PartService.onEditCommit(event, "maker"));

        partDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        partDescriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        partDescriptionColumn.setOnEditCommit(
                event -> PartService.onEditCommit(event, "description"));

        partPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        partPriceColumn.setCellFactory(
                TextFieldTableCell.forTableColumn(new DefaultFloatConvertor()));

        partQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        partQuantityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn(new DefaultIntegerConvertor()));

        if (loader.equals("part")) {
            partPriceColumn.setOnEditCommit(event -> PartService.onEditCommit(event, "price"));
            partQuantityColumn.setOnEditCommit(
                    event -> PartService.onEditCommit(event, "quantity"));
        } else if (loader.equals("chart")) {
            partQuantityColumn.setOnEditCommit(
                    event -> {
                        Command command = new Command();
                        command.setBillId(BillService.instance.getCurrentBillId());
                        command.setPartId(event.getRowValue().getId());
                        command.setQuantity(event.getNewValue().intValue());
                        command.setConsideredPrice(event.getRowValue().getPrice());
                        Part part = PartService.getPartById(event.getRowValue().getId());
                        if (part.getQuantity() < event.getNewValue().intValue()) {
                            handleStockShortage(part.getName());
                            return;
                        }
                        part.setQuantity(part.getQuantity() - event.getNewValue().intValue());
                        try {
                            PartService.updatePart(part);
                        } catch (SQLException e) {
                            handleDatabaseError(e);
                        }
                        BillService.updateChart(command);
                    });
            partPriceColumn.setOnEditCommit(
                    event -> {
                        Command command = new Command();
                        command.setBillId(BillService.instance.getCurrentBillId());
                        command.setPartId(event.getRowValue().getId());
                        command.setConsideredPrice(event.getNewValue().floatValue());
                        command.setQuantity(event.getRowValue().getQuantity());
                        Part part = PartService.getPartById(event.getRowValue().getId());
                        if (part.getPrice() < event.getNewValue().floatValue()) {
                            handleStockShortage(part.getName());
                            return;
                        }
                        part.setPrice(part.getPrice() - event.getNewValue().floatValue());
                        try {
                            PartService.updatePart(part);
                        } catch (SQLException e) {
                            handleDatabaseError(e);
                        }
                        BillService.updateChart(command);
                    });
        }

        partCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        partCategoryColumn.setCellFactory(ComboBoxTableCell.forTableColumn());

        ObservableList<Category> categoriesList = PartService.getAllCategories();
        ObservableList<String> categoryNamesList =
                FXCollections.observableArrayList(
                        categoriesList.stream().map(Category::getName).toList());

        partCategoryColumn.setCellFactory(ComboBoxTableCell.forTableColumn(categoryNamesList));
        partCategoryColumn.setOnEditCommit(
                event -> {
                    event.getTableView()
                            .getItems()
                            .get(event.getTablePosition().getRow())
                            .setCategory(
                                    categoriesList.stream()
                                            .filter(
                                                    category ->
                                                            category.getName()
                                                                    .equals(event.getNewValue()))
                                            .findAny()
                                            .orElse(event.getRowValue().getCategory()));
                    try {
                        PartService.updatePart(event.getRowValue());
                    } catch (SQLException e) {
                        AlertHandler.handleDatabaseError(e);
                    }
                });

        ObservableList<Part> parts = PartService.getAllParts();
        partsListTableView.setItems(parts);
        partsListTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
