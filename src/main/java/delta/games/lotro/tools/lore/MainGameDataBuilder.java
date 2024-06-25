package delta.games.lotro.tools.lore;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.parameters.GameParameters;
import delta.games.lotro.lore.parameters.io.xml.GameXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Builder for the game data file.
 * @author DAM
 */
public class MainGameDataBuilder
{
  private static final Logger LOGGER=Logger.getLogger(MainGameDataBuilder.class);

  private GameParameters buildGameParameters()
  {
    GameParameters ret=new GameParameters();
    ret.setMaxCharacterLevel(150);
    ret.setMaxLegendaryItemLevel(520);
    ret.setMaxVirtueRank(92);
    return ret;
  }

  /**
   * Build game data file.
   */
  public void doIt()
  {
    GameParameters parameters=buildGameParameters();
    File toFile=GeneratedFiles.GAME_DATA;
    boolean ok=GameXMLWriter.write(toFile,parameters);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build game file: "+toFile);
    }
  }


  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    MainGameDataBuilder builder=new MainGameDataBuilder();
    builder.doIt();
  }
}
