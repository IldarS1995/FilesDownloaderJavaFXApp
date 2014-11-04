package ru.kpfu.ildar.fx.pojos;

import java.io.Serializable;

public class Statistics implements Serializable
{
    //Amount of MBs, downloaded in: last day, last week, all time
    //Amount of files, downloaded in: last day, last week, all time
    //Average size of files
    //Average speed
    private double mbsLastDay;
    private double mbsLastWeek;
    private double mbsAllTime;

    private int filesCountLastDay;
    private int filesCountLastWeek;
    private int filesCountAllTime;

    private double averageFilesSize;
    private double averageSpeed;

    public Statistics() { }
    public Statistics(double mbsLastDay, double mbsLastWeek, double mbsAllTime,
                      double averageFilesSize, double averageSpeed)
    {
        this.mbsLastDay = mbsLastDay;
        this.mbsLastWeek = mbsLastWeek;
        this.mbsAllTime = mbsAllTime;
        this.averageFilesSize = averageFilesSize;
        this.averageSpeed = averageSpeed;
    }

    public int getFilesCountLastDay()
    {
        return filesCountLastDay;
    }
    public void setFilesCountLastDay(int filesCountLastDay)
    {
        this.filesCountLastDay = filesCountLastDay;
    }

    public int getFilesCountLastWeek()
    {
        return filesCountLastWeek;
    }
    public void setFilesCountLastWeek(int filesCountLastWeek)
    {
        this.filesCountLastWeek = filesCountLastWeek;
    }

    public int getFilesCountAllTime()
    {
        return filesCountAllTime;
    }
    public void setFilesCountAllTime(int filesCountAllTime)
    {
        this.filesCountAllTime = filesCountAllTime;
    }

    public double getMbsLastDay()
    {
        return mbsLastDay;
    }
    public void setMbsLastDay(double mbsLastDay)
    {
        this.mbsLastDay = mbsLastDay;
    }

    public double getMbsLastWeek()
    {
        return mbsLastWeek;
    }
    public void setMbsLastWeek(double mbsLastWeek)
    {
        this.mbsLastWeek = mbsLastWeek;
    }

    public double getMbsAllTime()
    {
        return mbsAllTime;
    }
    public void setMbsAllTime(double mbsAllTime)
    {
        this.mbsAllTime = mbsAllTime;
    }

    public double getAverageFilesSize()
    {
        return averageFilesSize;
    }
    public void setAverageFilesSize(double averageFilesSize)
    {
        this.averageFilesSize = averageFilesSize;
    }

    public double getAverageSpeed()
    {
        return averageSpeed;
    }
    public void setAverageSpeed(double averageSpeed)
    {
        this.averageSpeed = averageSpeed;
    }
}
