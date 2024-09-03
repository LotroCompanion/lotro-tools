package delta.games.lotro.tools.datDisplay;

import java.io.File;

import delta.common.utils.NumericTools;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;

/**
 * Tool to display some DAT data.
 * @author DAM
 */
public class MainDatDisplay
{
  private static final long OFFSET=DATConstants.DBPROPERTIES_OFFSET;

  private static final String ID_SEED="--id=";
  private static final String LANGUAGE_SEED="--language=";
  private DataFacade _facade;
  private String _language;
  private int _identifier;

  private MainDatDisplay(String[] args)
  {
    _language="fr";
    parseArgs(args);
  }

  private void parseArgs(String[] args)
  {
    for(String arg : args)
    {
      if (arg.startsWith(ID_SEED))
      {
        String idStr=arg.substring(ID_SEED.length());
        _identifier=NumericTools.parseInt(idStr,0);
      }
      else if (arg.startsWith(LANGUAGE_SEED))
      {
        _language=arg.substring(LANGUAGE_SEED.length());
      }
    }
  }

  private void doIt()
  {
    Context.init();
    DatConfiguration cfg=new DatConfiguration();
    cfg.setLocale(_language);
    File datFiles=cfg.getRootPath();
    System.out.println("LOTRO install directory: "+datFiles);
    System.out.println("Language: "+_language);
    _facade=new DataFacade(cfg);
    showProperties(_facade,_identifier+OFFSET);
  }

  PropertiesSet showProperties(DataFacade facade, long id)
  {
    PropertiesSet properties=facade.loadProperties(id);
    if (properties!=null)
    {
      System.out.println("******** Properties: "+(id-OFFSET));
      System.out.println(properties.dump());
    }
    else
    {
      System.out.println("Properties "+id+" not found!");
    }
    return properties;
  }

  /**
   * Main method for this tool
   * @param args Not used
   */
  public static void main(String[] args)
  {
    new MainDatDisplay(args).doIt();
  }
}
