package ru.kpfu.ildar.fx.database;

/** Provides implementation for DAO objects */
public class Factory
{
    private ConfigDAO configDAO;

    public ConfigDAO getConfigDAO()
    {
        if(configDAO == null)
            configDAO = new ConfigDAOImpl();
        return configDAO;
    }
}
