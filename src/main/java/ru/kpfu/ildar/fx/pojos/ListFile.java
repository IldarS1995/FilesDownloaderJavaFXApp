package ru.kpfu.ildar.fx.pojos;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import ru.kpfu.ildar.LinkFile;

/** Represents convenient files object for linking with TableView in main window */
public class ListFile
{
    private SimpleStringProperty url;
    private SimpleStringProperty fileName;
    private SimpleDoubleProperty size;
    private SimpleDoubleProperty progress;
    private SimpleDoubleProperty speed;
    private SimpleStringProperty state;
    private SimpleStringProperty folderForFiles;

    public ListFile(String url, String fileName, String folderForFiles)
    {
        this.url = new SimpleStringProperty(url);
        this.fileName = new SimpleStringProperty(fileName);
        this.size = new SimpleDoubleProperty(Double.NaN);
        this.progress = new SimpleDoubleProperty(0.0);
        this.speed = new SimpleDoubleProperty(0.0);
        this.state = new SimpleStringProperty(LinkFile.State.NotStarted.toString());
        this.folderForFiles = new SimpleStringProperty(folderForFiles);
    }
    public ListFile(LinkFile file, String folderForFiles)
    {
        this(file.getUrl(), file.getSaveName(), folderForFiles);
    }

    public String getFolderForFiles()
    {
        return folderForFiles.get();
    }

    public void setFolderForFiles(String folderForFiles)
    {
        this.folderForFiles.set(folderForFiles);
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

    public double getSize()
    {
        return size.get();
    }

    public void setSize(double size)
    {
        this.size.set(size == Double.NaN ? -1.0 : size);
    }
}
