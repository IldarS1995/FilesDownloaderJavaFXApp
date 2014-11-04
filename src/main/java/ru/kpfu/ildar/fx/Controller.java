package ru.kpfu.ildar.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
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

/** Controller for this application main window */
public class Controller implements Initializable, Observer
{
    public static Stage stage;
    /** URL column in TableView */
    @FXML
    private TableColumn urlCol;
    /** File save name column in TableView */
    @FXML
    private TableColumn fileNameCol;
    /** File size column in TableView */
    @FXML
    private TableColumn sizeCol;
    /** File downloading progress column in TableView */
    @FXML
    private TableColumn progressCol;
    /** File downloading speed column in TableView */
    @FXML
    private TableColumn speedCol;
    /** File downloading state column in TableView */
    @FXML
    public TableColumn stateCol;

    /** Files are shown in this table */
    @FXML
    private TableView<ListFile> filesView;

    /** Remove a file from the list button */
    @FXML
    private Button removeFromListBtn;
    /** Open a file from the list button */
    @FXML
    private Button openFilePathBtn;

    /** Downloading and downloaded files that are shown in the table */
    private ObservableList<ListFile> files =
            FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    /** Downloading files that need to be interrupted */
    private List<ListFile> filesToStop = new ArrayList<>();

    private LinksDownloader downloader = new LinksDownloader();
    private Statistics statistics;
    private Factory factory = new Factory();


    /** 'Add new file for download' action clicked */
    @FXML
    private void addToListClicked(ActionEvent actionEvent)
    {
        DownloadFileDialog dialog = new DownloadFileDialog(stage, "Download new file",
                downloader.getParameters().getFolderForFiles());
        Action result = dialog.showDialog();
        if(result == dialog.getSubmitAction())
        {
            ListFile file = new ListFile(dialog.getFile(), downloader.getParameters().getFolderForFiles());
            //If there's no already such file in the table and in the downloader object list, then download it
            if(!containsFile(files, file) && downloader.addNewFile(dialog.getFile()))
            {
                if(!downloader.startedDownloading())
                    downloader.startDownloading();

                this.files.add(file);
            }
        }

        saveConfigs();
    }

    /** Returns true, if a collection contains such file.
     * Fields URL and Name are checked, not references */
    private boolean containsFile(ObservableList<ListFile> files, ListFile file)
    {
        return files.stream().filter((f) -> f.getUrl().equals(file.getUrl())
                && f.getFileName().equals(file.getFileName())).findAny().isPresent();
    }

    /** 'Add multiple files for download' action clicked */
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

                //Add each file, if it isn't already contained in the list
                LinkFile linkFl = new LinkFile(file.getKey(), file.getValue());
                ListFile listFl = new ListFile(linkFl, downloader.getParameters().getFolderForFiles());
                if(!containsFile(this.files, listFl) && downloader.addNewFile(linkFl))
                    this.files.add(listFl);
            }
        }

        saveConfigs();
    }

    /** 'Remove a file from list' action clicked */
    @FXML
    public void removeFromListClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();
        files.remove(file);

        //If the file has been fully downloaded, no need to trace it in order to interrupt further
        if(file.getProgress() != 100.0)
            synchronized (filesToStop) { filesToStop.add(file); }
    }

    /** 'Open a folder where file saved' action clicked */
    @FXML
    public void openFilePathClicked(ActionEvent actionEvent)
    {
        ListFile file = filesView.getSelectionModel().getSelectedItem();
        try
        {
            String filePath = file.getFolderForFiles() + file.getFileName();
            System.out.println(filePath);
            //Open file folder and select the file
            Process p = new ProcessBuilder("explorer.exe", "/select," + filePath).start();
        }
        catch(IOException exc) { exc.printStackTrace(); }
    }

    /** 'Open parameters window' action clicked */
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
                //Save new threads value, but apply changes only in the next program start.
                //Such behaviour is made because thread pool is once created with given number
                //of threads and amount can't be changed dynamically in the pool afterwards
                Dialogs.create().title("Warning").message("You've changed the maximum threads" +
                        " amount property. You will have to reload the program so " +
                        "the changes could take effect.").showInformation();
            }

            downloader.setParameters(newPs);
        }
    }

    /** 'Open statistics window' action clicked */
    @FXML
    public void statisticsClicked(ActionEvent actionEvent)
    {

        StatisticsDialog dialog = new StatisticsDialog(stage, "Program statistics", statistics);
        dialog.showDialog();
    }

    /** Mouse was clicked on the table */
    @FXML
    private void viewMouseClicked(MouseEvent evt)
    {
        //If it's a double click with the left mouse key
        if(evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 2)
        {
            if(filesView.getSelectionModel().getSelectedItem() != null)
                openFilePathClicked(null);
        }
    }

    /** 'About' action clicked */
    @FXML
    private void aboutClicked(ActionEvent actionEvent)
    {

    }

    /** 'Exit the application' action clicked */
    @FXML
    public void exitClicked(ActionEvent actionEvent) { onClosing(actionEvent); }

    /** This method starts when the program begins its work. Initialize some things. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        loadParamsAndStatistics();  //Load parameters and statistics objects from database

        Dialog.Actions.CANCEL.textProperty().set("Cancel");
        Dialog.Actions.YES.textProperty().set("Yes");
        Dialog.Actions.NO.textProperty().set("No");

        linksColumnsToFields();  //Links created columns to the fields of the ListFile class

        filesView.setItems(files);

        //Disable buttons removeFromListBtn and openFilePathBtn, if there's no selected item in the table
        filesView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            removeFromListBtn.setDisable(newVal == null);
            openFilePathBtn.setDisable(newVal == null);
        });

        setCellFactoryForProgressCol();  //Set progress bar and progress indicator for progress column

        downloader.setObserver(this);

        stage.setOnCloseRequest(this::onClosing);
        Platform.setImplicitExit(false);
    }

    /** Set progress bar and progress indicator for progress column */
    private void setCellFactoryForProgressCol()
    {
        Callback<TableColumn<ListFile, Double>, TableCell<ListFile, Double>> cellFactory =
                new Callback<TableColumn<ListFile, Double>, TableCell<ListFile, Double>>()
                {
                    @Override
                    public TableCell<ListFile, Double> call(TableColumn<ListFile, Double> listFileDoubleTableColumn)
                    {
                        return new TableCell<ListFile, Double>()
                        {
                            @Override
                            protected void updateItem(Double val, boolean empty)
                            {
                                super.updateItem(val, empty);
                                if(val == null || empty)
                                {
                                    setGraphic(null);
                                    return;
                                }
                                HBox box = new HBox(5);
                                ProgressBar bar = new ProgressBar(val / 100);
                                bar.setTooltip(new Tooltip(String.valueOf(val) + "%"));
                                ProgressIndicator indicator = new ProgressIndicator(val / 100);
                                box.getChildren().addAll(bar, indicator);
                                setGraphic(box);
                            }
                        };
                    }
                };

        progressCol.setCellFactory(cellFactory);
    }

    /** Load parameters and statistics objects from database */
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
        LinkFile linkFl = thread.getFile(); //File that caused an event
        if(removedFile(linkFl))  //If file is to remove, interrupt the download
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
                //File was downloaded
                if(arg instanceof FileDownloadedEvent)
                {
                    System.out.println("FileDownloadedEvent: " + listFile.getFileName());
                    listFile.setState(linkFl.getLoadState());
                    listFile.setProgress(100.0);
                    listFile.setSpeed(0.0);

                    incrementDownloadedFilesCount(listFile.getSize());  //For statistics
                    saveConfigs();
                }
                //There is some progress in file download
                else if(arg instanceof FileProgressEvent)
                {
                    System.out.println("FileProgressEvent: " + listFile.getFileName());
                    listFile.setState(LinkFile.State.Loading);

                    FileProgressEvent evt = (FileProgressEvent)arg;
                    double speedKBs = floor((double) evt.getBytesDownloadedInLastSec() / 1024);
                    listFile.setSpeed(speedKBs);
                    listFile.setProgress(floor(evt.getPercentProgress()));

                    statistics.setSpeedMeasuresCount(statistics.getSpeedMeasuresCount() + 1);
                    statistics.setDownloadSpeedSum(statistics.getDownloadSpeedSum() + speedKBs);
                }
                //Connection was established with remote server. Program starts to download the file
                else if(arg instanceof ConnectionEstablishedEvent)
                {
                    System.out.println("ConnectionEstablishedEvent");
                    long size = ((ConnectionEstablishedEvent)arg).getContentLength();
                    double szMB = size == -1 ? -1 :
                            floor((double) thread.getContentLength() / 1024 / 1024);
                    listFile.setSize(szMB);
                }
                //Due to exception file downloading was interrupted
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
                    //Update files collection so the changes are visible to the user
                    int idx = filesView.getSelectionModel().getSelectedIndex();
                    ObservableList<ListFile> lst = FXCollections.observableArrayList(files);
                    files.clear();
                    files.addAll(lst);
                    filesView.getSelectionModel().select(idx);
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

    /** Alter some values. Statistics staff. */
    private void incrementDownloadedFilesCount(double size)
    {
        Statistics s = statistics;
        synchronized (statistics)
        {
            s.setFilesCountLastDay(s.getFilesCountLastDay() + 1);
            s.setFilesCountLastWeek(s.getFilesCountLastWeek() + 1);
            s.setFilesCountAllTime(s.getFilesCountAllTime() + 1);

            s.setMbsLastDay(s.getMbsLastDay() + size);
            s.setMbsLastWeek(s.getMbsLastWeek() + size);
            s.setMbsAllTime(s.getMbsAllTime() + size);
        }
    }

    /** Returns true if the specified file is for deletion from the list and download abortion. */
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

    /** Program closing event */
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

    /** Save parameters and statistics objects to the database */
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
