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

public class MultipleFilesDialog extends Dialog
{
    private Map<String, String> files;
    private Action submitAction;

    private ObservableList<FileUrlName> list = FXCollections.observableArrayList();

    public Map<String, String> getFiles() { return files; }
    public Action getSubmitAction() { return submitAction; }

    public MultipleFilesDialog(Object owner, String title)
    {
        super(owner, title);
    }

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
        browseBtn.setOnAction((evt) ->
        {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(this.getWindow());
            if(file != null)
                filePathField.setText(file.getAbsolutePath());
        });

        Button parseBtn = new Button("Parse file");

        TableView<FileUrlName> view = new TableView<>();
        TableColumn urlCol = new TableColumn("URL");
        TableColumn nameCol = new TableColumn("File name");
        urlCol.setMinWidth(150);
        nameCol.setMinWidth(150);

        urlCol.setCellValueFactory(new PropertyValueFactory<FileUrlName, String>("url"));
        nameCol.setCellValueFactory(new PropertyValueFactory<FileUrlName, String>("name"));

        view.getColumns().addAll(urlCol, nameCol);

        pane.add(filePathField, 0, 0);
        pane.add(browseBtn, 1, 0);
        pane.add(parseBtn, 2, 0);
        pane.add(view, 0, 1, 3, 1);

        view.setItems(list);

        parseBtn.setOnAction((evt) ->
        {
            try
            {
                Map<String, String> parsedList = ConsoleApp.parseLinksFilePath(filePathField.getText());
                this.files = parsedList;
                list.clear();
                parsedList.entrySet().stream().forEach((pair) ->
                        list.add(new FileUrlName(pair.getKey(), pair.getValue())));
            }
            catch(IOException exc)
            {
                exc.printStackTrace();
                Dialogs.create().title("Error while trying to parse file")
                        .message("Some error happened: " + exc.getMessage()).showError();
            }
        });

        submitAction = new AbstractAction("Download files")
        {
            @Override
            public void handle(ActionEvent actionEvent)
            {
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
