package ru.kpfu.ildar.fx.dialogs;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.controlsfx.dialog.Dialog;
import ru.kpfu.ildar.fx.pojos.Statistics;

public class StatisticsDialog extends Dialog
{
    private boolean resetStat = false;
    private Statistics statistics;

    public boolean isResetStatistics() { return resetStat; }

    public StatisticsDialog(Object owner, String title, Statistics statistics)
    {
        super(owner, title);
        this.statistics = statistics;
    }

    public void showDialog()
    {
        //Amount of MBs, downloaded in: last day, last week, all time
        //Amount of files, downloaded in: last day, last week, all time
        //Average size of files in MBs
        //Average speed in KBs/sec

        GridPane pane = new GridPane();
        pane.setHgap(10); pane.setVgap(10);

        Label amountOfMbsDownloadedLabel = new Label("Amount of MBs, downloaded in");
        Label amountOfFilesDownloadedLabel = new Label("Amount of files, downloaded in");
        Label averSizeLabel = new Label("Average size of files in MBs:");
        Label speedLabel = new Label("Average downloading speed in KBs/sec:");

        Text mbsDownloadedText = new Text(String.valueOf(statistics.getMbsLastDay()));
        Text filesDownloadedText = new Text(String.valueOf(statistics.getFilesCountLastDay()));
        Text averSizeText = new Text(String.valueOf(statistics.getAverageFilesSize()));
        Text averSpeedText = new Text(String.valueOf(statistics.getAverageSpeed()));

        String[] strs = { "Last day", "Last week", "All time" };
        ComboBox<String> mbsBox = new ComboBox<>(FXCollections.observableArrayList(strs));
        ComboBox<String> filesBox = new ComboBox<>(FXCollections.observableArrayList(strs));

        mbsBox.getSelectionModel().select(strs[0]);
        filesBox.getSelectionModel().select(strs[0]);

        pane.add(amountOfMbsDownloadedLabel, 0, 0);
        pane.add(mbsDownloadedText, 1, 0);
        pane.add(mbsBox, 2, 0);
        pane.add(amountOfFilesDownloadedLabel, 0, 1);
        pane.add(filesDownloadedText, 1, 1);
        pane.add(filesBox, 2, 1);
        pane.add(averSizeLabel, 0, 2);
        pane.add(averSizeText, 1, 2);
        pane.add(speedLabel, 0, 3);
        pane.add(averSpeedText, 1, 3);

        mbsBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
        {
            switch (newVal)
            {
                case "Last day": mbsDownloadedText.setText(String.valueOf(statistics.getMbsLastDay()));
                    break;
                case "Last week": mbsDownloadedText.setText(String.valueOf(statistics.getMbsLastWeek()));
                    break;
                case "All time": mbsDownloadedText.setText(String.valueOf(statistics.getMbsAllTime()));
                    break;
            }
        });

        filesBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
        {
            switch (newVal)
            {
                case "Last day": filesDownloadedText.setText
                        (String.valueOf(statistics.getFilesCountLastDay()));
                    break;
                case "Last week": filesDownloadedText.setText
                        (String.valueOf(statistics.getFilesCountLastWeek()));
                    break;
                case "All time": filesDownloadedText.setText
                        (String.valueOf(statistics.getFilesCountAllTime()));
                    break;
            }
        });

        this.setResizable(false);
        this.setContent(pane);
        this.getActions().addAll(Dialog.Actions.OK);
        this.show();
    }
}
