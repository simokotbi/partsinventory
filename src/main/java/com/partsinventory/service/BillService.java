package com.partsinventory.service;

import static com.partsinventory.helper.AlertHandler.handleDatabaseError;
import static com.partsinventory.helper.AlertHandler.handleInvalidInput;
import static com.partsinventory.helper.AlertHandler.handleSuccessfulEdit;

import com.partsinventory.helper.DbConnection;
import com.partsinventory.model.Bill;
import com.partsinventory.model.Command;
import com.partsinventory.model.Part;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn.CellEditEvent;
import org.sqlite.SQLiteConfig;
import org.sqlite.date.FastDateFormat;

public class BillService {

    private static int currentBillId;
    public static BillService instance = new BillService();

    private BillService() {
        currentBillId = -1;
    }

    public int getCurrentBillId() {
        return currentBillId;
    }

    public void setCurrentBillId(int currentBillId) {
        BillService.currentBillId = currentBillId;
    }

    private static ObservableList<Bill> getAllBillsFromResultset(ResultSet resultSet)
            throws SQLException {
        ObservableList<Bill> billslist = FXCollections.observableArrayList();
        while (resultSet.next()) {
            Bill bill = new Bill();
            bill.setId(resultSet.getInt("id"));
            bill.setClientName(resultSet.getString("clientName"));
            bill.setClientPhone(resultSet.getString("clientPhone"));
            bill.setTotalPrice(resultSet.getFloat("totalPrice"));
            bill.setDate(
                    LocalDate.parse(
                            resultSet.getString("date"),
                            DateTimeFormatter.ofPattern(SQLiteConfig.DEFAULT_DATE_STRING_FORMAT)));
            billslist.add(bill);
        }
        return billslist;
    }

    public static ObservableList<Bill> getAllBills() throws SQLException {
        String statement = DbConnection.load("ALL_BILLS");
        ResultSet resultSet = DbConnection.DbqueryExecute(statement);
        ObservableList<Bill> billsList = getAllBillsFromResultset(resultSet);
        return billsList;
    }

    public static ObservableList<Part> getPartsOfBill(int billId) throws SQLException {
        if (billId == -1) throw new SQLException("No bill Selected");
        Connection connection = DbConnection.getConnection();
        PreparedStatement statement =
                connection.prepareStatement(DbConnection.load("PART_IN_BILL"));
        statement.setInt(1, billId);
        ResultSet resultSet = statement.executeQuery();
        ObservableList<Part> partslist = FXCollections.observableArrayList();
        while (resultSet.next()) {
            Part part = new Part();
            part.setId(resultSet.getInt("id"));
            part.setName(resultSet.getString("name"));
            part.setMaker(resultSet.getString("maker"));
            part.setDescription(resultSet.getString("description"));
            part.setPrice(resultSet.getFloat("priceconsidered"));
            part.setQuantity(resultSet.getInt("quantity"));
            part.setCategory(PartService.getCategoryById(resultSet.getInt("catid")));
            partslist.add(part);
        }
        return partslist;
    }

    public static void onEditCommit(CellEditEvent<Bill, ?> event, String propertyName) {
        try {
            Bill editedBill = event.getRowValue();
            String newValue = event.getNewValue().toString();

            switch (propertyName) {
                case "clientName":
                    editedBill.setClientName(newValue);
                    break;
                case "clientPhone":
                    editedBill.setClientPhone(newValue);
                    break;
                case "date":
                    editedBill.setDate(LocalDate.parse(newValue));
                    break;
                case "price":
                    editedBill.setTotalPrice(Float.parseFloat(newValue));
                    break;
            }
            updateBill(
                    editedBill.getId(),
                    editedBill.getClientName(),
                    editedBill.getClientPhone(),
                    editedBill.getTotalPrice());
            handleSuccessfulEdit();
        } catch (NumberFormatException e) {
            handleInvalidInput();
        }
    }

    public static void addBill(Bill bill) {
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("ADD_BILL"))) {
            statement.setFloat(1, 0);
            statement.setString(2, bill.getClientName());
            statement.setString(3, bill.getClientPhone());
            statement.setString(
                    4,
                    FastDateFormat.getInstance(SQLiteConfig.DEFAULT_DATE_STRING_FORMAT)
                            .format(new Date()));
            statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    public static long addBill(String clientName, String clientPhone) {
        Bill bill = new Bill();
        bill.setClientName(clientName);
        bill.setClientPhone(clientPhone);
        long result = -1L;
        try {
            result =
                    DbConnection.getLastInsertedRowId(
                            (Bill b) -> {
                                addBill(b);
                            },
                            bill);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return result;
    }

    private static void deleteBill(int id) {
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(DbConnection.load("DELETE_BILL"))) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    public static Boolean updateBill(
            int id, String clientName, String clientPhone, float totalPrice) {
        int result = 0;
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("UPDATE_BILL"))) {
            statement.setString(1, clientName);
            statement.setString(2, clientPhone);
            statement.setFloat(3, totalPrice);
            statement.setInt(4, id);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return result == 1;
    }

    public static boolean addToChart(int partId, int billId, int quantity, float consideredPrice) {
        int result = 0;
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("ADD_PART_TO_CHART"))) {
            statement.setInt(1, partId);
            statement.setInt(2, billId);
            statement.setInt(3, quantity);
            statement.setFloat(4, consideredPrice);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
            if (result != -1L) deleteBill(billId);
        }
        return result == 1;
    }

    public static Boolean updateChart(Command command) {
        int result = 0;
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("UPDATE_COMMAND"))) {
            statement.setInt(1, command.getQuantity());
            statement.setFloat(2, command.getConsideredPrice());
            statement.setInt(3, command.getPartId());
            statement.setInt(4, command.getBillId());
            result = statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return result == 1;
    }
}
