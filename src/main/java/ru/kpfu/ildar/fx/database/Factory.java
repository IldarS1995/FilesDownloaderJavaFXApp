package ru.kpfu.ildar.fx.database;

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
