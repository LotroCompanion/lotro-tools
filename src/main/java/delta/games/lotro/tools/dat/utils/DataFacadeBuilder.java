package delta.games.lotro.tools.dat.utils;

import delta.games.lotro.dat.archive.DATL10nSupport;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;

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
    cfg.setLocale(DATL10nSupport.EN);
    DataFacade facade=new DataFacade(cfg);
    return facade;
  }
}
