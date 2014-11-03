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
import ru.kpfu.ildar.LinkFile;
import ru.kpfu.ildar.download.LoadThread;
import ru.kpfu.ildar.download.Parameters;
import ru.kpfu.ildar.events.FileDownloadedEvent;
import ru.kpfu.ildar.events.FileProgressEvent;
import ru.kpfu.ildar.fx.dialogs.DownloadFileDialog;
import ru.kpfu.ildar.download.LinksDownloader;
import ru.kpfu.ildar.fx.dialogs.MultipleFilesDialog;
import ru.kpfu.ildar.fx.dialogs.ParametersDialog;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable, Observer
{
    public static Stage stage;
    @FXML
    private TableColumn urlCol;
    @FXML
    private TableColumn fileNameCol;
    @FXML
    private TableColumn sizeCol;
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
    private List<ListFile> filesToStop = new ArrayList<>();

    private LinksDownloader downloader = new LinksDownloader();

    @FXML
    private void addToListClicked(ActionEvent actionEvent)
    {
        DownloadFileDialog dialog = new DownloadFileDialog(stage, "Download new file",
                downloader.getParameters().getFolderForFiles());
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction())
        {
            downloader.addNewFile(dialog.getFile());
            if(!downloader.startedDownloading())
                downloader.startDownloading();

            files.add(new ListFile(dialog.getFile()));
        }
    }

    @FXML
    private void addMultipleClicked(ActionEvent actionEvent)
    {
        MultipleFilesDialog dialog = new MultipleFilesDialog(stage, "Download multiple files");
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction())
        {
            Map<String, String> files = dialog.getFiles();
            for(Map.Entry<String, String> file : files.entrySet())
            {
                if(!downloader.startedDownloading())
                    downloader.startDownloading();

                LinkFile linkFl = new LinkFile(file.getKey(), file.getValue());
                if(downloader.addNewFile(linkFl))
                    this.files.add(new ListFile(linkFl));
            }
        }
    }

    @FXML
    public void removeFromListClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();
        files.remove(file);
        synchronized (filesToStop) { filesToStop.add(file); }
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
        if(result == dialog.getSubmitAction())
        {
            Parameters newPs = dialog.getParameters();
            Parameters prevPs = downloader.getParameters();
            if(newPs.getMaxThreadsAmount() != prevPs.getMaxThreadsAmount())
            {
                Dialogs.create().title("Warning").message("You've changed the maximum threads" +
                        " amount property. You will have to reload the program so " +
                        "the changes could take effect.").showInformation();
            }

            downloader.setParameters(newPs);
        }
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
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Dialog.Actions.CANCEL.textProperty().set("Cancel");
        Dialog.Actions.YES.textProperty().set("Yes");
        Dialog.Actions.NO.textProperty().set("No");

        linksColumnsToFields();

        filesView.setItems(files);

        filesView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            removeFromListBtn.setDisable(newVal == null);
            openFilePathBtn.setDisable(newVal == null);
        });

        downloader.setObserver(this);

        stage.setOnCloseRequest(this::onClosing);
        Platform.setImplicitExit(false);
    }

    /** An event from the downloader project - either a file was downloaded,
     * or download progress is made */
    @Override
    public void update(Observable o, Object arg)
    {
        LoadThread thread = (LoadThread)o;
        LinkFile linkFl = thread.getFile();
        if(removedFile(linkFl))
        {
            thread.interruptDownload();
            thread.deleteObserver(this);
            return;
        }

        try
        {
            ListFile file = files.stream().filter((f) -> f.getUrl().equals(linkFl.getUrl()) &&
                    f.getFileName().equals(linkFl.getSaveName())).findAny().get();
            Platform.runLater(() ->
            {
                if(arg instanceof FileDownloadedEvent)
                {
                    System.out.println("FileDownloadedEvent");
                    file.setState(linkFl.getLoadState());
                    file.setProgress(100.0);
                    file.setSpeed(0.0);
                }
                else if(arg instanceof FileProgressEvent)
                {
                    System.out.println("FileProgressEvent");
                    file.setState(LinkFile.State.Loading);

                    long length = thread.getContentLength();
                    double size = length == -1 ? -1 :
                            floor((double) thread.getContentLength() / 1024 / 1024);
                    file.setSize(size);

                    file.setSpeed(floor((double)thread.getBytesDownloadedInLastSec() / 1024));
                    file.setProgress(floor(((FileProgressEvent)arg).getPercentProgress()));
                }

                synchronized (Controller.class)
                {
                    ObservableList<ListFile> lst = FXCollections.observableArrayList(files);
                    files.clear();
                    files.addAll(lst);
                }
            });
        }
        catch(NoSuchElementException exc)
        {
            //This exception may happen when a file was deleted from the files list, but
            //it still hasn't been interrupted and an event came from it. The program tries
            //to find the file in the list, but fails to do so.
        }
    }

    private boolean removedFile(LinkFile linkFl)
    {
        Optional<ListFile> file = filesToStop.stream().filter((f) -> f.getFileName()
                .equals(linkFl.getSaveName()) && f.getUrl().equals(linkFl.getUrl())).findAny();
        if(file.isPresent())
        {
            filesToStop.remove(file.get());
            return true;
        }
        return false;
    }

    private double floor(double a)
    {
        return (int)(a * 100) / 100.0;
    }

    @SuppressWarnings("unchecked")
    private void linksColumnsToFields()
    {
        urlCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("url"));
        fileNameCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("fileName"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<ListFile, Double>("size"));
        progressCol.setCellValueFactory(new PropertyValueFactory<ListFile, Double>("progress"));
        speedCol.setCellValueFactory(new PropertyValueFactory<ListFile, Double>("speed"));
        stateCol.setCellValueFactory(new PropertyValueFactory<ListFile, String>("state"));
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
