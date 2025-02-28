package delta.games.lotro.tools.extraction.global;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.lore.parameters.GameParameters;
import delta.games.lotro.lore.parameters.io.xml.GameXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Builder for the game data file.
 * @author DAM
 */
public class MainGameDataBuilder
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainGameDataBuilder.class);

  private GameParameters buildGameParameters()
  {
    GameParameters ret=new GameParameters();
    ret.setMaxCharacterLevel(150);
    ret.setMaxLegendaryItemLevel(530);
    ret.setMaxVirtueRank(94);
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
