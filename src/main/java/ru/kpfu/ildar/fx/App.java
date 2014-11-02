package ru.kpfu.ildar.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Controller.stage = stage;
        stage.setTitle("Files Downloading Application");

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        stage.setScene(new Scene(root, 850, 500));
        stage.show();
    }
}
