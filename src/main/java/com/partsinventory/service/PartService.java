package com.partsinventory.service;

import static com.partsinventory.helper.AlertHandler.handleDatabaseError;
import static com.partsinventory.helper.AlertHandler.handleInvalidInput;
import static com.partsinventory.helper.AlertHandler.handleSuccessfulEdit;

import com.partsinventory.helper.DbConnection;
import com.partsinventory.helper.Settings;
import com.partsinventory.model.Category;
import com.partsinventory.model.Part;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableColumn;

public class PartService {

    public static Part getPartById(int id) {
        Part part = new Part();
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("PART_BY_ID"))) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                part.setId(resultSet.getInt("id"));
                part.setName(resultSet.getString("name"));
                part.setMaker(resultSet.getString("maker"));
                part.setDescription(resultSet.getString("description"));
                part.setPrice(resultSet.getFloat("price"));
                part.setQuantity(resultSet.getInt("quantity"));
                part.setCategory(getCategoryById(resultSet.getInt("catid")));
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return part;
    }

    private static ObservableList<Part> getAllPartsFromResultset(ResultSet resultSet)
            throws SQLException {
        ObservableList<Part> partslist = FXCollections.observableArrayList();
        while (resultSet.next()) {
            Part part = new Part();
            part.setId(resultSet.getInt("id"));
            part.setName(resultSet.getString("name"));
            part.setMaker(resultSet.getString("maker"));
            part.setDescription(resultSet.getString("description"));
            part.setPrice(resultSet.getFloat("price"));
            part.setQuantity(resultSet.getInt("quantity"));
            part.setCategory(getCategoryById(resultSet.getInt("catid")));
            partslist.add(part);
        }
        return partslist;
    }

    public static ObservableList<Part> getAllParts() throws SQLException {
        String statement = DbConnection.load("ALL_PARTS");
        ResultSet resultSet = DbConnection.DbqueryExecute(statement);
        ObservableList<Part> partsList = getAllPartsFromResultset(resultSet);
        return partsList;
    }

    public static Series<String, Number> getAllPartsQunatityDESC() throws SQLException {
        String statement = DbConnection.load("PARTS_QUANTITY_DESC");
        ResultSet rs = DbConnection.DbqueryExecute(statement);
        ObservableList<Part> partsList = getAllPartsFromResultset(rs);
        Series<String, Number> charList = new Series<>();
        for (Part part : partsList) {
            charList.getData().add(new Data<String, Number>(part.getName(), part.getPrice()));
        }
        return charList;
    }

    public static Series<String, Number> getAllPartsPriceDESC() throws SQLException {
        String statement = DbConnection.load("PARTS_PRICE_DESC");
        ResultSet rs = DbConnection.DbqueryExecute(statement);
        ObservableList<Part> partsList = getAllPartsFromResultset(rs);
        Series<String, Number> charList = new Series<>();
        for (Part part : partsList) {
            charList.getData().add(new Data<String, Number>(part.getName(), part.getQuantity()));
        }
        return charList;
    }

    public static ObservableList<Part> getPartByCriteria(String criteria, String target) {
        ObservableList<Part> partsList = FXCollections.observableArrayList();
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                String.format(
                                        "%s %s '%%%s%%'",
                                        DbConnection.load("PART_BY_CRITERIA"), criteria, target))) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                partsList.add(extractPartFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return partsList;
    }

    public static void updatePart(Part part) throws SQLException {
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("UPDATE_PART"))) {
            statement.setString(1, part.getName());
            statement.setString(2, part.getMaker());
            statement.setString(3, part.getDescription());
            statement.setFloat(4, part.getPrice());
            statement.setInt(5, part.getQuantity());
            statement.setInt(6, part.getCategory().getId());
            statement.setInt(7, part.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private static Part extractPartFromResultSet(ResultSet resultSet) throws SQLException {
        Part part = new Part();
        part.setId(resultSet.getInt("id"));
        part.setName(resultSet.getString("name"));
        part.setMaker(resultSet.getString("maker"));
        part.setDescription(resultSet.getString("description"));
        part.setPrice(resultSet.getFloat("price"));
        part.setQuantity(resultSet.getInt("quantity"));
        part.setCategory(getCategoryById(resultSet.getInt("catid")));
        return part;
    }

    public static boolean addPart(Part part) {
        int result = 0;
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("ADD_PART"))) {
            statement.setString(1, part.getName());
            statement.setString(2, part.getMaker());
            statement.setString(3, part.getDescription());
            statement.setFloat(4, part.getPrice());
            statement.setInt(5, part.getQuantity());
            statement.setInt(6, part.getCategory().getId());
            result = statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return result == 1;
    }

    public static void onEditCommit(TableColumn.CellEditEvent<Part, ?> event, String propertyName) {
        try {
            Part editedPart = event.getRowValue();
            String newValue = event.getNewValue().toString();

            switch (propertyName) {
                case "name":
                    editedPart.setName(newValue);
                    break;
                case "maker":
                    editedPart.setMaker(newValue);
                    break;
                case "description":
                    editedPart.setDescription(newValue);
                    break;
                case "price":
                    editedPart.setPrice(Float.parseFloat(newValue));
                    break;
                case "quantity":
                    editedPart.setQuantity(Integer.parseInt(newValue));
                    break;
            }
            PartService.updatePart(editedPart);
            handleSuccessfulEdit();
        } catch (NumberFormatException e) {
            handleInvalidInput();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    public static void deletePart(int id) {
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(DbConnection.load("DELETE_PART"))) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    public static ObservableList<String> populateMakerCombobox() {
        ObservableList<String> collection =
                FXCollections.observableArrayList("...", "Mahle", "Bosch", "Dayco", "Contitech");
        return collection;
    }

    private static ObservableList<Category> getAllCategoriesFromResultset(ResultSet rs)
            throws SQLException {
        ObservableList<Category> categorieslist = FXCollections.observableArrayList();
        while (rs.next()) {
            Category category = new Category();
            category.setId(rs.getInt("id"));
            category.setName(rs.getString("name"));
            category.setDescription(rs.getString("description"));
            category.setImage(Settings.loadPath("images.path", rs.getString("image")));
            categorieslist.add(category);
        }
        return categorieslist;
    }

    private static Category getCategorieFromResultset(ResultSet rs) throws SQLException {

        Category category = null;
        while (rs.next()) {
            category = new Category();
            category.setId(rs.getInt("id"));
            category.setName(rs.getString("name"));
            category.setDescription(rs.getString("description"));
            category.setImage(Settings.loadPath("images.path", rs.getString("image")));
        }
        return category;
    }

    public static ObservableList<Category> getAllCategories() throws SQLException {
        String statement = DbConnection.load("ALL_CATEGORIES");
        ResultSet rs = DbConnection.DbqueryExecute(statement);
        ObservableList<Category> categoriessList = getAllCategoriesFromResultset(rs);
        return categoriessList;
    }

    public static Category getCategoryById(int catId) throws SQLException {
        Optional<Category> category = Optional.ofNullable(null);
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("GET_CATEGORY_BY_ID"))) {
            statement.setInt(1, catId);
            statement.execute();
            category = Optional.ofNullable(getCategorieFromResultset(statement.getResultSet()));
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return category.or(
                        () -> {
                            Category defaultCategory = new Category();
                            defaultCategory.setName("NONE");
                            return Optional.of(defaultCategory);
                        })
                .get();
    }

    public static boolean addCategory(Category category) {
        int result = 0;
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DbConnection.load("ADD_CATEGORY"))) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setString(3, category.getImageName());
            result = statement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return result == 1;
    }

    public static void deleteCategory(int catId) {
        try (Connection connection = DbConnection.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(DbConnection.load("DELETE_CATEGORY"))) {
            preparedStatement.setInt(1, catId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }
}
