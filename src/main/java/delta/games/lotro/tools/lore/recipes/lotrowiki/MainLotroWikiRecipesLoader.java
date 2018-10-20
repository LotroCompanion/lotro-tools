package delta.games.lotro.tools.lore.recipes.lotrowiki;

import java.io.File;
import java.util.List;

import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.tools.utils.lotrowiki.LotroWikiSiteInterface;

/**
 * Download recipes data from the site lotro-wiki.
 * @author DAM
 */
public class MainLotroWikiRecipesLoader
{
  // Professions... Forester and Prospector not managed
  private static final String[] PROFESSIONS={"Cook","Farmer","Forester","Jeweller","Metalsmith","Prospector","Scholar","Tailor","Weaponsmith","Woodworker"};
  private static final String[] TIERS={"Apprentice","Journeyman","Expert","Artisan","Master","Supreme","Westfold","Eastemnet","Westemnet","Anórien","Doomfold","Ironfold"};

  private File _tmpFilesDir;

  /**
   * Constructor.
   */
  public MainLotroWikiRecipesLoader()
  {
    _tmpFilesDir=new File("data/recipes/tmp/lotrowiki").getAbsoluteFile();
  }

  private void doIt()
  {
    LotroWikiSiteInterface lotroWiki=new LotroWikiSiteInterface(_tmpFilesDir);

    // Recipe index parser
    LotroWikiRecipeIndexPageParser parser=new LotroWikiRecipeIndexPageParser(lotroWiki);

    RecipesManager recipesManager=new RecipesManager();
    for(String profession : PROFESSIONS)
    {
      int tierIndex=1;
      for(String tier : TIERS)
      {
        String indexId=profession+"_"+tier+"_Recipe_Index";
        List<Recipe> recipes=parser.doRecipesIndex(indexId,tierIndex);
        for(Recipe recipe : recipes)
        {
          recipe.setProfession(profession);
          recipe.setTier(tierIndex);
          recipesManager.registerRecipe(recipe);
        }
        tierIndex++;
      }
      int nbRecipes=recipesManager.getRecipesCount();
      System.out.println("Found: "+nbRecipes+" recipes.");
    }
    File out=new File("../lotro-companion/data/lore/recipes_wiki.xml");
    recipesManager.writeToFile(out);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainLotroWikiRecipesLoader().doIt();
  }
}
