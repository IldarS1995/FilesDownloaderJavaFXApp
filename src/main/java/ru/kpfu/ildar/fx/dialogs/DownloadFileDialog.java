package ru.kpfu.ildar.fx.dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.ildar.LinkFile;

public class DownloadFileDialog extends Dialog
{
    private LinkFile file;
    private String savePath;
    private Action submitAction;

    public Action getSubmitAction() { return submitAction; }

    public LinkFile getFile() { return file; }

    public DownloadFileDialog(Object owner, String title, String savePath)
    {
        super(owner, title);
        this.savePath = savePath;
    }

    public Action showDialog()
    {
        GridPane root = new GridPane();
        root.setHgap(10); root.setVgap(10);

        Label urlLabel = new Label("Enter file URL:");
        Label nameLabel = new Label("Enter file save name:");

        TextField urlField = new TextField();
        TextField nameField = new TextField();

        Text folderText = new Text("Your file will be downloaded to the folder: ");
        folderText.setFont(Font.font("Arial Narrow", 14));
        Text folderValText = new Text(savePath);
        folderValText.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
        TextFlow folderFlow = new TextFlow(folderText, folderValText);

        root.add(urlLabel, 0, 0);
        root.add(urlField, 1, 0);
        root.add(nameLabel, 0, 1);
        root.add(nameField, 1, 1);
        root.add(folderFlow, 0, 2, 2, 1);

        submitAction = new AbstractAction("Download")
        {
            @Override
            public void handle(ActionEvent evt)
            {
                file = new LinkFile(urlField.getText(), nameField.getText());
                Dialog d = (Dialog)evt.getSource();
                d.hide();
            }
        };

        nameField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            submitAction.disabledProperty().set(newVal.length() == 0);
        });
        urlField.textProperty().addListener((obs, oldVal, newVal) ->
        {
            submitAction.disabledProperty().set(newVal.length() == 0);
        });

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setResizable(false);
        this.setContent(root);
        this.setGraphic(new ImageView(getClass()
                .getClassLoader().getResource("images/download.png").toString()));

        Platform.runLater(() -> {
            urlField.requestFocus();
        });

        return this.show();
    }
}
