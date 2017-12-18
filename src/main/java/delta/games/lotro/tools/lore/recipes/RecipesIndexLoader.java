package delta.games.lotro.tools.lore.recipes;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.crafting.recipes.index.RecipeSummary;
import delta.games.lotro.lore.crafting.recipes.index.RecipesIndex;
import delta.games.lotro.lore.crafting.recipes.index.io.xml.RecipesIndexXMLWriter;
import delta.games.lotro.utils.LotroLoggers;

/**
 * Recipes index loader.
 * @author DAM
 */
public class RecipesIndexLoader
{
  private static final Logger _logger=LotroLoggers.getWebInputLogger();

  /**
   * Load recipes index for my.lotro.com and write it to a file.
   * @param indexFile Target file.
   * @return <code>true</code> if it was done, <code>false</code> otherwise.
   */
  public boolean doIt(File indexFile)
  {
    boolean ret=false;
    RecipesIndexJSONParser parser=new RecipesIndexJSONParser();
    RecipesIndex index=parser.parseRecipesIndex();
    if (index!=null)
    {
      RecipesIndexXMLWriter writer=new RecipesIndexXMLWriter();
      ret=writer.write(indexFile,index,"UTF-8");
      if (!ret)
      {
        _logger.error("Cannot write recipes index file ["+indexFile+"]");
      }
      String[] keys=index.getKeys();
      for(String key : keys)
      {
        RecipeSummary summary=index.getRecipe(key);
        System.out.println(summary);
      }
      ret=true;
    }
    else
    {
      _logger.error("Recipes index is null");
    }
    return ret;
  }
}
