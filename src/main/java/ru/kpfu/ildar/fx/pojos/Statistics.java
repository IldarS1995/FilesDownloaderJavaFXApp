package ru.kpfu.ildar.fx.pojos;

import java.io.Serializable;
import java.util.Calendar;

/** Class for storing some statistical variables */
public class Statistics implements Serializable
{
    //Amount of MBs, downloaded in: last day, last week, all time
    //Amount of files, downloaded in: last day, last week, all time
    //Average size of files
    //Average speed
    private Calendar lastDay;
    private Calendar lastWeek;

    private double mbsLastDay;
    private double mbsLastWeek;
    private double mbsAllTime;

    private int filesCountLastDay;
    private int filesCountLastWeek;
    private int filesCountAllTime;

    private double downloadSpeedSum;
    private int speedMeasuresCount;

    public Statistics()
    {
        Calendar today = Calendar.getInstance();
        if(lastDay == null)
            lastDay = today;
        if(lastWeek == null)
            lastWeek = today;
        if(!equalsDates(today, lastDay)) //If day has changed, set new day and reset variables
        {
            lastDay = today;
            mbsLastDay = 0.0;
            filesCountLastDay = 0;
        }
        if(!equalWeeks(today, lastWeek))//If week has changed, set new week and reset variables
        {
            lastWeek = today;
            mbsLastWeek = 0.0;
            filesCountLastWeek = 0;
        }
    }

    private boolean equalWeeks(Calendar today, Calendar thatDay)
    {
        return today.get(Calendar.YEAR) == thatDay.get(Calendar.YEAR)
                && today.get(Calendar.WEEK_OF_YEAR) == thatDay.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean equalsDates(Calendar today, Calendar lastDay)
    {
        return today.get(Calendar.YEAR) == lastDay.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == lastDay.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == lastDay.get(Calendar.DAY_OF_MONTH);
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

    public double getDownloadSpeedSum()
    {
        return downloadSpeedSum;
    }
    public void setDownloadSpeedSum(double downloadSpeedSum)
    {
        this.downloadSpeedSum = downloadSpeedSum;
    }

    public int getSpeedMeasuresCount()
    {
        return speedMeasuresCount;
    }
    public void setSpeedMeasuresCount(int speedMeasuresCount)
    {
        this.speedMeasuresCount = speedMeasuresCount;
    }
}
