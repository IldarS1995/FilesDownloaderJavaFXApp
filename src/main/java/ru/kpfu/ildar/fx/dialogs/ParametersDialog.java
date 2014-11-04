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

/** Dialog where user can change some downloading parameters */
public class ParametersDialog extends Dialog
{
    /** Submit type button action */
    private Action submitAction;
    /** New Parameters instance will be constructed with this dialog */
    private Parameters parameters;

    public Action getSubmitAction() { return submitAction; }
    public Parameters getParameters() { return parameters; }

    public ParametersDialog(Object owner, String title, Parameters parameters)
    {
        super(owner, title);
        this.parameters = parameters;
    }

    /** Field that shows max threads amount parameter */
    private TextField threadsField;
    /** Field that shows speed limit parameter */
    private TextField speedField;
    /** Field that shows a folder path where downloaded files will be stored */
    private TextField saveFolderField;
    /** Combo box that has three values - bytes, KBs, and MBs. User chooses them to change speed limit */
    private ComboBox<String> speedMeasureBox;

    public Action showDialog()
    {
        GridPane root = new GridPane();
        root.setHgap(10); root.setVgap(10);

        Label threadsLabel = new Label("Maximum threads amount:");
        Label speedLabel = new Label("Download speed limit for application:");
        Label saveFolderLabel = new Label("Folder where to save downloaded files:");

        threadsField = new TextField(String.valueOf(parameters.getMaxThreadsAmount()));
        speedField = new TextField();
        saveFolderField = new TextField(String.valueOf(parameters.getFolderForFiles()));

        saveFolderField.setPrefWidth(200);

        Button browseBtn = new Button("...");
        browseBtn.setOnAction((evt) ->
        {
            /** User can select file save folder with this browsing window */
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(this.getWindow());
            if(dir != null)
                saveFolderField.setText(dir.getAbsolutePath());
        });

        speedMeasureBox = new ComboBox<>();
        speedMeasureBox.setItems(FXCollections.observableArrayList("bytes", "KBs", "MBs"));
        int speed = parameters.getBytesPerSec();
        if(speed % 1024 == 0)
        {
            if((speed / 1024) % 1024 == 0)
            {
                speedMeasureBox.getSelectionModel().select("MBs");
                speedField.setText(String.valueOf(speed / 1024 / 1024));
            }
            else
            {
                speedMeasureBox.getSelectionModel().select("KBs");
                speedField.setText(String.valueOf(speed / 1024));
            }
        }
        else
        {
            speedMeasureBox.getSelectionModel().select("bytes");
            speedField.setText(String.valueOf(speed));
        }

        root.add(threadsLabel, 0, 0);
        root.add(threadsField, 1, 0);
        root.add(speedLabel, 0, 1);
        root.add(speedField, 1, 1);
        root.add(speedMeasureBox, 2, 1);
        root.add(saveFolderLabel, 0, 2);
        root.add(saveFolderField, 1, 2);
        root.add(browseBtn, 2, 2);

        submitAction = new AbstractAction("Save")
        {
            @Override
            public void handle(ActionEvent evt)
            {

                parameters = new Parameters(getThreadsCount(), getBytesPerSec(), getFolderForFiles());
                Dialog dial = (Dialog)evt.getSource();
                dial.hide();
            }
        };

        /** Enable submit action button only when threads field and speed field values have
         * correct format */
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

    /** Returns correctly formatted folder where files are saved */
    private String getFolderForFiles()
    {
        String res = saveFolderField.getText().replace("/", File.separator)
                .replace("\\", File.separator);
        return res.charAt(res.length() - 1) == File.separator.charAt(0) ? res : res.concat(File.separator);
}

    /** Returns speed limit in bytes per second */
    private int getBytesPerSec()
    {
        String val = speedMeasureBox.getSelectionModel().getSelectedItem();
        int koeff = koeff = val.equals("bytes") ? 1 : (val.equals("KBs") ? 1024 : 1024 * 1024);

        int speed = Integer.parseInt(speedField.getText()) * koeff;
        if(speed <= 0)
            throw new NumberFormatException();
        return speed;
    }

    /** Returns max threads count */
    private int getThreadsCount()
    {
        int threadsCount = Integer.parseInt(threadsField.getText());
        if(threadsCount <= 0)
            throw new NumberFormatException();
        return threadsCount;
    }
}
