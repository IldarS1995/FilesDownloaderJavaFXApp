package ru.kpfu.ildar.fx.dialogs;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.ildar.download.Parameters;

import java.io.File;

public class ParametersDialog extends Dialog
{
    private Action submitAction;
    private Parameters parameters;

    public Action getSubmitAction() { return submitAction; }

    public ParametersDialog(Object owner, String title, Parameters parameters)
    {
        super(owner, title);
        this.parameters = parameters;
    }

    private TextField threadsField;
    private TextField speedField;
    private TextField saveFolderField;
    private ComboBox<String> box;

    public Action showDialog()
    {
        GridPane root = new GridPane();
        root.setHgap(10); root.setVgap(10);

        Label threadsLabel = new Label("Maximum threads amount:");
        Label speedLabel = new Label("Download speed limit for application:");
        Label saveFolderLabel = new Label("Folder where to save downloaded files:");

        threadsField = new TextField(String.valueOf(parameters.getMaxThreadsAmount()));
        speedField = new TextField(String.valueOf(parameters.getBytesPerSec()));
        saveFolderField = new TextField(String.valueOf(parameters.getFolderForFiles()));

        saveFolderField.setPrefWidth(200);

        Button browseBtn = new Button("...");
        browseBtn.setOnAction((evt) ->
        {
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(this.getWindow());
            if(dir != null)
                saveFolderField.setText(dir.getAbsolutePath());
        });

        box = new ComboBox<>();
        box.setItems(FXCollections.observableArrayList("bytes", "KBs", "MBs"));
        box.getSelectionModel().select("bytes");

        root.add(threadsLabel, 0, 0);
        root.add(threadsField, 1, 0);
        root.add(speedLabel, 0, 1);
        root.add(speedField, 1, 1);
        root.add(box, 2, 1);
        root.add(saveFolderLabel, 0, 2);
        root.add(saveFolderField, 1, 2);
        root.add(browseBtn, 2, 2);

        submitAction = new AbstractAction("Save")
        {
            @Override
            public void handle(ActionEvent evt)
            {
                parameters.setMaxThreadsAmount(getThreadsCount());
                parameters.setBytesPerSec(getBytesPerSec());
                parameters.setFolderForFiles(getFolderForFiles());

                Dialog dial = (Dialog)evt.getSource();
                dial.hide();
            }
        };

        submitAction.disabledProperty().bind(new BooleanBinding()
        {
            { super.bind(threadsField.textProperty(), speedField.textProperty()); }

            @Override
            protected boolean computeValue()
            {
                try
                {
                    getThreadsCount();
                    getBytesPerSec();

                    return false;
                }
                catch(IllegalArgumentException exc) { return true; }
                catch(Exception exc) { exc.printStackTrace(); return true; }
            }
        });

        ButtonBar.setType(submitAction, ButtonBar.ButtonType.OK_DONE);

        this.setContent(root);
        this.setResizable(false);
        this.getActions().addAll(Actions.CANCEL, submitAction);
        this.setGraphic(new ImageView(getClass().getClassLoader()
                .getResource("images/params.png").toString()));

        return this.show();
    }

    private String getFolderForFiles()
    {
        return saveFolderField.getText();
    }

    private int getBytesPerSec()
    {
        String val = box.getSelectionModel().getSelectedItem();
        int koeff = koeff = val.equals("bytes") ? 1 : (val.equals("KBs") ? 1024 : 1024 * 1024);

        int speed = Integer.parseInt(speedField.getText()) * koeff;
        if(speed <= 0)
            throw new NumberFormatException();
        return speed;
    }

    private int getThreadsCount()
    {
        int threadsCount = Integer.parseInt(threadsField.getText());
        if(threadsCount <= 0)
            throw new NumberFormatException();
        return threadsCount;
    }
}
