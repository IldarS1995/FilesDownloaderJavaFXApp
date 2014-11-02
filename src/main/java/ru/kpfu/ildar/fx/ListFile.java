package ru.kpfu.ildar.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import ru.kpfu.ildar.LinkFile;

public class ListFile
{
    private SimpleStringProperty url;
    private SimpleStringProperty fileName;
    private SimpleDoubleProperty progress;
    private SimpleDoubleProperty speed;
    private SimpleStringProperty state;

    public ListFile(String url, String fileName)
    {
        this.url = new SimpleStringProperty(url);
        this.fileName = new SimpleStringProperty(fileName);
        this.progress = new SimpleDoubleProperty(0.0);
        this.speed = new SimpleDoubleProperty(0.0);
        this.state = new SimpleStringProperty(LinkFile.State.NotStarted.toString());
    }
    public ListFile(LinkFile file)
    {
        this(file.getUrl(), file.getSaveName());
    }

    public String getUrl()
    {
        return url.get();
    }

    public void setUrl(String url)
    {
        this.url.set(url);
    }

    public String getFileName()
    {
        return fileName.get();
    }

    public void setFileName(String fileName)
    {
        this.fileName.set(fileName);
    }

    public double getProgress()
    {
        return progress.get();
    }

    public void setProgress(double progress)
    {
        this.progress.set(progress);
    }

    public double getSpeed()
    {
        return speed.get();
    }

    public void setSpeed(double speed)
    {
        this.speed.set(speed);
    }

    public String getState()
    {
        return state.get();
    }

    public void setState(LinkFile.State state)
    {
        this.state.set(state.toString());
    }
}
