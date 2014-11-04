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
import ru.kpfu.ildar.events.ConnectionEstablishedEvent;
import ru.kpfu.ildar.events.DownloadInterruptedEvent;
import ru.kpfu.ildar.events.FileDownloadedEvent;
import ru.kpfu.ildar.events.FileProgressEvent;
import ru.kpfu.ildar.fx.database.ConfigDAO;
import ru.kpfu.ildar.fx.database.Factory;
import ru.kpfu.ildar.fx.dialogs.DownloadFileDialog;
import ru.kpfu.ildar.download.LinksDownloader;
import ru.kpfu.ildar.fx.dialogs.MultipleFilesDialog;
import ru.kpfu.ildar.fx.dialogs.ParametersDialog;
import ru.kpfu.ildar.fx.dialogs.StatisticsDialog;
import ru.kpfu.ildar.fx.pojos.ListFile;
import ru.kpfu.ildar.fx.pojos.Statistics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

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

    private ObservableList<ListFile> files =
            FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private List<ListFile> filesToStop = new ArrayList<>();

    private LinksDownloader downloader = new LinksDownloader();
    private Statistics statistics;
    private Factory factory = new Factory();


    @FXML
    private void addToListClicked(ActionEvent actionEvent)
    {
        DownloadFileDialog dialog = new DownloadFileDialog(stage, "Download new file",
                downloader.getParameters().getFolderForFiles());
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction() && downloader.addNewFile(dialog.getFile()))
        {
            ListFile file = new ListFile(dialog.getFile(), downloader.getParameters().getFolderForFiles());
            if(!containsFile(files, file))
            {
                if(!downloader.startedDownloading())
                    downloader.startDownloading();

                this.files.add(file);
            }
        }

        saveConfigs();
    }

    private boolean containsFile(ObservableList<ListFile> files, ListFile file)
    {
        return files.stream().filter((f) -> f.getUrl().equals(file.getUrl())
                && f.getFileName().equals(file.getFileName())).findAny().isPresent();
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
                    this.files.add(new ListFile(linkFl, downloader.getParameters().getFolderForFiles()));
            }
        }

        saveConfigs();
    }

    @FXML
    public void removeFromListClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();
        files.remove(file);

        //If the file has been fully downloaded, no need to trace it in order to interrupt further
        if(file.getProgress() != 100.0)
            synchronized (filesToStop) { filesToStop.add(file); }
    }

    @FXML
    public void openFilePathClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();
        try
        {
            String filePath = file.getFolderForFiles() + file.getFileName();
            Process p = new ProcessBuilder("explorer.exe", "/select," + filePath).start();
        }
        catch(IOException exc) { exc.printStackTrace(); }
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
            if(downloader.startedDownloading() &&
                    newPs.getMaxThreadsAmount() != prevPs.getMaxThreadsAmount())
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

        StatisticsDialog dialog = new StatisticsDialog(stage, "Program statistics", statistics);
        dialog.showDialog();
        //...
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
        loadParamsAndStatistics();

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

    private void loadParamsAndStatistics()
    {
        try
        {
            Parameters params = factory.getConfigDAO().getParameters();
            Statistics stats = factory.getConfigDAO().getStatistics();
            this.downloader.setParameters(params);
            this.statistics = stats;
        }
        catch(Exception exc) { exc.printStackTrace(); }
    }

    /** An event came from the downloader library */
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
            ListFile listFile = files.stream().filter((f) -> f.getUrl().equals(linkFl.getUrl()) &&
                    f.getFileName().equals(linkFl.getSaveName())).findAny().get();
            Platform.runLater(() ->
            {
                if(arg instanceof FileDownloadedEvent)
                {
                    System.out.println("FileDownloadedEvent: " + listFile.getFileName());
                    listFile.setState(linkFl.getLoadState());
                    listFile.setProgress(100.0);
                    listFile.setSpeed(0.0);
                    saveConfigs();
                }
                else if(arg instanceof FileProgressEvent)
                {
                    System.out.println("FileProgressEvent: " + listFile.getFileName());
                    listFile.setState(LinkFile.State.Loading);

                    FileProgressEvent evt = (FileProgressEvent)arg;
                    listFile.setSpeed(floor((double) evt.getBytesDownloadedInLastSec() / 1024));
                    listFile.setProgress(floor(evt.getPercentProgress()));
                }
                else if(arg instanceof ConnectionEstablishedEvent)
                {
                    System.out.println("ConnectionEstablishedEvent");
                    long size = ((ConnectionEstablishedEvent)arg).getContentLength();
                    double szMB = size == -1 ? -1 :
                            floor((double) thread.getContentLength() / 1024 / 1024);
                    listFile.setSize(szMB);
                }
                else if(arg instanceof DownloadInterruptedEvent)
                {
                    DownloadInterruptedEvent evt = (DownloadInterruptedEvent)arg;
                    LinkFile fl = evt.getFile();
                    Exception exc = evt.getException();
                    String message = null;
                    if(exc instanceof MalformedURLException)
                    {
                        message = "The specified URL " + fl.getUrl() + " has incorrect format. " +
                                "Make sure that you've entered the right address.";
                    }
                    else if(exc instanceof UnknownHostException)
                    {
                        message = "Host " + fl.getUrl() +
                                " is unavailable; please check Internet connection " +
                                "and verify that this host exists.";
                    }
                    else if(exc instanceof IOException)
                    {
                        message = "There was some error while downloading file " + fl.getUrl()
                                + " : " + exc.getMessage();
                    }
                    else
                        message = exc.getMessage();

                    files.remove(listFile);
                    Dialogs.create().title("Downloading error").message(message).showError();
                }

                //synchronized (Controller.class)
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
        {
            saveConfigs();
            System.exit(0);
        }
        else if(evt instanceof WindowEvent)
        {
            WindowEvent event = (WindowEvent)evt;
            event.consume();
        }
    }

    private void saveConfigs()
    {
        ConfigDAO configDAO = factory.getConfigDAO();
        synchronized (factory.getConfigDAO())
        {
            try
            {
                configDAO.saveParameters(downloader.getParameters());
                configDAO.saveStatistics(statistics);
            }
            catch(Exception exc) { exc.printStackTrace(); }
        }
    }
}
