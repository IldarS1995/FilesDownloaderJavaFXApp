package ru.kpfu.ildar.fx.database;

import ru.kpfu.ildar.download.Parameters;
import ru.kpfu.ildar.fx.pojos.Statistics;

import java.io.*;

/** Implementation that uses serialization into files as a database option */
public class ConfigDAOImpl implements ConfigDAO
{
    /** Path to the file where statistics object is stored */
    private static final String statPath = "stat.db";
    /** Path to the file where parameters object is stored */
    private static final String paramsPath = "params.db";

    @Override
    public void saveStatistics(Statistics stat) throws IOException
    {
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(statPath)))
        {
            os.writeObject(stat);
        }
    }

    @Override
    public Statistics getStatistics()
    {
        File fl = new File(statPath);
        if(!fl.exists())
            return new Statistics();

        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(statPath)))
        {
            return (Statistics)is.readObject();
        }
        catch(IOException | ClassNotFoundException exc)
        {
            //Some problem happened while trying to fetch an object from the file.
            //Remove corrupted file.
            fl.delete();
            return new Statistics();
        }
    }

    @Override
    public void saveParameters(Parameters params) throws IOException
    {
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(paramsPath)))
        {
            os.writeObject(params);
        }
    }

    @Override
    public Parameters getParameters()
    {
        File fl = new File(paramsPath);
        if(!fl.exists())
            return new Parameters();

        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(paramsPath)))
        {
            return (Parameters)is.readObject();
        }
        catch(IOException | ClassNotFoundException exc)
        {
            //Some problem happened while trying to fetch an object from the file.
            //Remove corrupted file.
            fl.delete();
            return new Parameters();
        }
    }
}
