package ru.kpfu.ildar.fx.dialogs;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import ru.kpfu.ildar.ConsoleApp;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/** Dialog where user can select a file with multiple links.
 * The application will download them all. */
public class MultipleFilesDialog extends Dialog
{
    /** Files that will be returned to calling code */
    private Map<String, String> files;
    /** Submit button action - user clicks it when he submits the downloading of selected files */
    private Action submitAction;

    /** Internal list where parsed data is stored. Will be linked to the TableView instance. */
    private ObservableList<FileUrlName> list = FXCollections.observableArrayList();

    /** Get parsed files - keys are URLs, and values are files save names */
    public Map<String, String> getFiles() { return files; }
    /** Get 'Submit' type button action */
    public Action getSubmitAction() { return submitAction; }

    public MultipleFilesDialog(Object owner, String title)
    {
        super(owner, title);
    }

    /** Internal file for linking with TableView */
    public static class FileUrlName
    {
        private SimpleStringProperty url;
        private SimpleStringProperty name;

        public FileUrlName() { }
        public FileUrlName(String url, String name)
        {
            this.url = new SimpleStringProperty(url);
            this.name = new SimpleStringProperty(name);
        }

        public String getUrl() { return url.get(); }
        public String getName() { return name.get(); }
    }

    @SuppressWarnings("unchecked")
    public Action showDialog()
    {
        GridPane pane = new GridPane();
        pane.setHgap(10); pane.setVgap(10);

        TextField filePathField = new TextField();
        filePathField.setPromptText("Enter the path to the file");

        Button browseBtn = new Button("...");
        //File browsing window opens when clicked; the selected file is a file to parse.
        browseBtn.setOnAction((evt) ->
        {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(this.getWindow());
            if(file != null)
                filePathField.setText(file.getAbsolutePath());
        });

        Button parseBtn = new Button("Parse file");
        //Make at first disabled so the user can't click it without specifying a file at first
        parseBtn.setDisable(true);

        TableView<FileUrlName> view = new TableView<>();
        TableColumn urlCol = new TableColumn("URL");
        TableColumn nameCol = new TableColumn("File name");
        urlCol.setMinWidth(250);
        nameCol.setMinWidth(150);
        view.setPrefHeight(300);

        urlCol.setCellValueFactory(new PropertyValueFactory<FileUrlName, String>("url"));
        nameCol.setCellValueFactory(new PropertyValueFactory<FileUrlName, String>("name"));

        view.getColumns().addAll(urlCol, nameCol);

        pane.add(filePathField, 0, 0);
        pane.add(browseBtn, 1, 0);
        pane.add(parseBtn, 2, 0);
        pane.add(view, 0, 1, 3, 1);

        view.setItems(list);

        //Enable Parse button only when file path field has some text
        filePathField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            parseBtn.setDisable(newVal.length() == 0);
        });

        parseBtn.setOnAction((evt) ->
        {
            Map<String, String> parsedList;
            try
            {
                //Parse this file - each line in the file must have the following format:
                //<URL><space><Filename>
                parsedList = ConsoleApp.parseLinksFilePath(filePathField.getText());
            }
            catch(IOException exc)
            {
                Dialogs.create().title("Error while trying to access file")
                        .message("Error has happened while trying to access file. " +
                                "Make sure that it exists and is accessible.").showError();
                return;
            }
            catch(Exception exc)
            {
                Dialogs.create().title("Error in parsing file")
                        .message("The format of the specified file isn't correct.").showError();
                return;
            }
            this.files = parsedList;
            list.clear();
            parsedList.entrySet().stream().forEach((pair) ->
                    list.add(new FileUrlName(pair.getKey(), pair.getValue())));
        });

        submitAction = new AbstractAction("Download files")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
                if(files == null)
                {
                    Dialogs.create().title("Submitting")
                            .message("Please choose some file and parse it at first.").showError();
                    return;
                }
                
                Dialog d = (Dialog)actionEvent.getSource();
                d.hide();
            }
        };

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);

        this.setResizable(false);
        this.setContent(pane);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setGraphic(new ImageView(getClass()
                .getClassLoader().getResource("images/downloads.png").toString()));

        return this.show();
    }
}
