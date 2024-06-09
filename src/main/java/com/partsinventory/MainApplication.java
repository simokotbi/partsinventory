package com.partsinventory;

import com.partsinventory.helper.DbConnection;
import com.partsinventory.helper.ViewManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.UNDECORATED);
        ViewManager.getInstance().setPrimaryStage(stage);
        ViewManager.getInstance().loadView("loading-view.fxml", "Loading", 546, 365);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        DbConnection.closeConnection();
    }

    public static void main(String[] args) throws Exception {
        if (!DbConnection.checkDrivers()) {
            throw new Exception("Unexpected error in database configuration.");
        }
        launch(args);
    }
}
