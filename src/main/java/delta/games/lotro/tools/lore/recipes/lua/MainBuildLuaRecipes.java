package delta.games.lotro.tools.lore.recipes.lua;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.plugins.PluginConstants;
import delta.games.lotro.plugins.lotrocompanion.RecipesParser;

/**
 * Build a single file from all the recipes extracted from local LUA data.
 * @author DAM
 */
public class MainBuildLuaRecipes
{
  private static final Logger LOGGER=Logger.getLogger(MainBuildLuaRecipes.class);

  private void doIt()
  {
    RecipesManager manager=loadRecipes();
    File toFile=new File("data/recipes/in/luaRecipes.xml");
    manager.writeToFile(toFile);
  }

  private RecipesManager loadRecipes()
  {
    RecipesManager manager=new RecipesManager();
    String account="glorfindel666";
    String server="Landroval";
    RecipesParser parser=new RecipesParser();
    List<String> characters=PluginConstants.getCharacters(account,server,false);
    for(String character : characters)
    {
      File dataDir=PluginConstants.getCharacterDir(account,server,character);
      File dataFile=new File(dataDir,"LotroCompanionRecipes.plugindata");
      if (dataFile.exists())
      {
        try
        {
          System.out.println("Doing: " + character);
          List<Recipe> toonRecipes=parser.doIt(dataFile);
          for(Recipe recipe : toonRecipes)
          {
            manager.registerRecipe(recipe);
          }
        }
        catch(Exception e)
        {
          LOGGER.error("Error when loading recipes from file "+dataFile, e);
        }
      }
      else
      {
        System.out.println("No recipes for: " + character);
      }
    }
    return manager;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainBuildLuaRecipes().doIt();
  }
}
