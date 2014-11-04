package ru.kpfu.ildar.fx.database;

import ru.kpfu.ildar.download.Parameters;
import ru.kpfu.ildar.fx.pojos.Statistics;

/** Provides methods for saving and getting statistics and parameters objects */
public interface ConfigDAO
{
    /** Save statistics object to the database */
    void saveStatistics(Statistics stat) throws Exception;
    /** Get statistics object from the database */
    Statistics getStatistics() throws Exception;

    /** Save parameters object to the database */
    void saveParameters(Parameters param) throws Exception;
    /** Get parameters object from the database */
    Parameters getParameters() throws Exception;
}
