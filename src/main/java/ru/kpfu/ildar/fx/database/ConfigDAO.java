package ru.kpfu.ildar.fx.database;

import ru.kpfu.ildar.download.Parameters;
import ru.kpfu.ildar.fx.pojos.Statistics;

public interface ConfigDAO
{
    void saveStatistics(Statistics stat) throws Exception;
    Statistics getStatistics() throws Exception;

    void saveParameters(Parameters param) throws Exception;
    Parameters getParameters() throws Exception;
}
