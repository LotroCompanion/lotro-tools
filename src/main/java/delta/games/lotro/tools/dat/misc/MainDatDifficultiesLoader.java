package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.difficulty.Difficulty;
import delta.games.lotro.common.difficulty.io.xml.DifficultyXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get difficulties from DAT files.
 * @author DAM
 */
public class MainDatDifficultiesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatDifficultiesLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatDifficultiesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load difficulties.
   */
  public void doIt()
  {
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(587203292);
    if (enumMapper!=null)
    {
      List<Difficulty> difficulties=new ArrayList<Difficulty>();
      for(Integer code : enumMapper.getTokens())
      {
        if (code.intValue()==0)
        {
          continue;
        }
        String label=enumMapper.getLabel(code.intValue());
        Difficulty difficulty=new Difficulty(code.intValue(),label);
        difficulties.add(difficulty);
      }
      DifficultyXMLWriter.writeDifficultiesFile(GeneratedFiles.DIFFICULTIES,difficulties);
    }
    else
    {
      LOGGER.warn("Could not load difficulties enum");
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatDifficultiesLoader(facade).doIt();
    facade.dispose();
  }
}
