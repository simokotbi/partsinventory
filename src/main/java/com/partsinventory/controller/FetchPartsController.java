package com.partsinventory.controller;

import java.io.IOException;

import com.partsinventory.model.Part;
import com.partsinventory.service.PartService;
import com.partsinventory.service.ReportsService;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class FetchPartsController {

    @FXML
    private SplitPane rootSplitPane;

    @FXML
    private StackPane resultsStackPane;

    @FXML
    private Group searchGroup;

    @FXML
    private SplitMenuButton searchOptionPick;

    @FXML
    private MenuItem searchByDescription;

    @FXML
    private MenuItem searchByName;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    @FXML
    private Button printReportButton;

    @FXML
    private Button deleteButton;

    public SplitPane getRootSplitPane() {
        return rootSplitPane;
    }

    public StackPane getResultsStackPane() {
        return resultsStackPane;
    }

    @FXML
    void initialize() {
        rootSplitPane.setDividerPosition(0, 0.2);
        searchByName.setOnAction(e -> searchOptionPick.setText(searchByName.getText()));
        searchByDescription.setOnAction(e -> searchOptionPick.setText(searchByDescription.getText()));
        FXMLLoader tableViewLoader = new FXMLLoader(getClass().getResource("/views/parts-table-component.fxml"));
        resultsStackPane.getChildren().clear();
        try {
            Parent tableViewRoot = tableViewLoader.load();
            PartController productTableView = tableViewLoader.getController();
            StackPane.setMargin(productTableView.getPartsListTableView(), new Insets(10, 10, 10, 10));
            StackPane.setAlignment(resultsStackPane, Pos.CENTER);
            StackPane.setMargin(searchGroup, new Insets(10, 10, 10, 10));
            StackPane.setAlignment(searchGroup, Pos.CENTER);
            resultsStackPane.getChildren().add(tableViewRoot);
            ObservableList<Part> selectedItems = productTableView.getPartsListTableView().getSelectionModel().getSelectedItems();
            deleteButton.setOnAction(e -> {
                if(selectedItems.toArray().length!=0 && PartService.handleDelete()){
                    for(Part part : selectedItems) {
                        PartService.deletePart(part.getId());
                    }
                    productTableView.getPartsListTableView().getItems().removeAll(selectedItems);
                }
            });

            printReportButton.setOnAction(e -> {
                ReportsService.getInstance().resetParameters();
                ReportsService.getInstance().setParameter("partIds", String.join(",", selectedItems.stream().map(Part::getId).map(id -> id.toString()).toArray(String[]::new)));
                ReportsService.getInstance().generatePartReport("part-report.jrxml", "all-parts-report.pdf");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSearch(ActionEvent event) {
        FXMLLoader tableViewLoader = new FXMLLoader(getClass().getResource("/views/parts-table-component.fxml"));
        resultsStackPane.getChildren().clear();
        try {
            Parent tableViewRoot = tableViewLoader.load();
            PartController productTableView = tableViewLoader.getController();
            if (searchTextField.getText() != null && !searchTextField.getText().isBlank()
                    && !searchOptionPick.getText().contains("Search by")) {
                StackPane.setMargin(productTableView.getPartsListByCriteriaTableView(
                        searchOptionPick.getText() + " like", searchTextField.getText()), new Insets(10, 10, 10, 10));
                StackPane.setAlignment(resultsStackPane, Pos.CENTER);
                resultsStackPane.getChildren().add(tableViewRoot);
            } else if (searchOptionPick.getText().contains("Search by")) {
                Label warningMessage = new Label("Please select a search criteria");
                warningMessage.setMinHeight(0);
                resultsStackPane.getChildren().add(warningMessage);
            } else {
                StackPane.setMargin(productTableView.getPartsListTableView(), new Insets(10, 10, 10, 10));
                StackPane.setAlignment(resultsStackPane, Pos.CENTER);
                resultsStackPane.getChildren().add(tableViewRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
