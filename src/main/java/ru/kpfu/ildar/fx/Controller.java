package ru.kpfu.ildar.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import ru.kpfu.ildar.fx.dialogs.DownloadFileDialog;
import ru.kpfu.ildar.download.LinksDownloader;
import ru.kpfu.ildar.fx.dialogs.ParametersDialog;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable
{
    public static Stage stage;
    @FXML
    private TableColumn urlCol;
    @FXML
    private TableColumn fileNameCol;
    @FXML
    private TableColumn progressCol;
    @FXML
    private TableColumn speedCol;
    @FXML
    public TableColumn stateCol;

    @FXML
    private TableView<ListFile> filesView;

    @FXML
    private Button removeFromListBtn;
    @FXML
    private Button openFilePathBtn;


    private ObservableList<ListFile> files = FXCollections.observableArrayList();

    private LinksDownloader downloader = new LinksDownloader();


    @FXML
    private void addToListClicked(ActionEvent actionEvent)
    {
        DownloadFileDialog dialog = new DownloadFileDialog(stage, "Download new file",
                downloader.getParameters().getFolderForFiles());
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction())
        {
            //downloader.addNewFile(dialog.getFile());
            files.add(new ListFile(dialog.getFile()));
        }
    }

    @FXML
    public void removeFromListClicked(ActionEvent actionEvent)
    {

    }

    @FXML
    public void openFilePathClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();


    }

    @FXML
    public void paramsClicked(ActionEvent actionEvent)
    {
        ParametersDialog dialog = new ParametersDialog(stage,
                "Downloading parameters", downloader.getParameters());
        Action result = dialog.showDialog();
//        if(result == dialog.getSubmitAction())
//        {
//
//        }
    }

    @FXML
    public void statisticsClicked(ActionEvent actionEvent)
    {

    }

    @FXML
    public void aboutClicked(ActionEvent actionEvent)
    {

    }

    @FXML
    public void exitClicked(ActionEvent actionEvent) { onClosing(actionEvent); }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        urlCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("url"));
        fileNameCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("fileName"));
        progressCol.setCellValueFactory(new PropertyValueFactory<ListFile, Double>("progress"));
        speedCol.setCellValueFactory(new PropertyValueFactory<ListFile, Double>("speed"));
        stateCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("state"));

        filesView.setItems(files);

        filesView.selectionModelProperty().addListener((obs, oldVal, newVal) ->
        {
            removeFromListBtn.setDisable(newVal == null);
            openFilePathBtn.setDisable(newVal == null);
        });

        stage.setOnCloseRequest(this::onClosing);
        Platform.setImplicitExit(false);
    }

    private<T> void onClosing(T evt)
    {
        Action result = Dialogs.create().message("Do you really want to exit?")
                .title("Exit").showConfirm();
        if(result == Dialog.Actions.YES)
            System.exit(0);
        else if(evt instanceof WindowEvent)
        {
            WindowEvent event = (WindowEvent)evt;
            event.consume();
        }
    }
}
