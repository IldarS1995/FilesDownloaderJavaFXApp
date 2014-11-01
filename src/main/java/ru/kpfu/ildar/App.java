package ru.kpfu.ildar;

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
        stage.setTitle("Files Downloading Application");

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }
}
