package delta.games.lotro.tools.utils;

import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.Locales;

/**
 * Builder for the data facade to use for data-building tools.
 * @author DAM
 */
public class DataFacadeBuilder
{
  /**
   * Build a data facade for data-building tools.
   * @return A data facade.
   */
  public static DataFacade buildFacadeForTools()
  {
    DatConfiguration cfg=new DatConfiguration();
    cfg.setLocale(Locales.EN);
    DataFacade facade=new DataFacade(cfg);
    return facade;
  }
}
