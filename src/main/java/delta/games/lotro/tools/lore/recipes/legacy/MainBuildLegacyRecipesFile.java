package delta.games.lotro.tools.lore.recipes.legacy;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import delta.common.utils.files.filter.ExtensionPredicate;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeUtils;
import delta.games.lotro.lore.crafting.recipes.io.xml.RecipeXMLParser;
import delta.games.lotro.lore.crafting.recipes.io.xml.RecipeXMLWriter;

/**
 * Build a single file from all the legacy recipes files.
 * @author DAM
 */
public class MainBuildLegacyRecipesFile
{
  private static final File RECIPES_DIR=new File("data/recipes/in/legacy").getAbsoluteFile();

  private void doIt()
  {
    List<Recipe> recipes=loadRecipes();
    RecipeUtils.sort(recipes);
    System.out.println("Nb recipes: "+recipes.size());
    RecipeXMLWriter writer=new RecipeXMLWriter();
    writer.write(new File("data/recipes/in/recipesLegacy.xml").getAbsoluteFile(),recipes,EncodingNames.UTF_8);
  }

  private List<Recipe> loadRecipes()
  {
    File[] recipeFiles=getRecipeFiles();
    List<Recipe> recipes=new ArrayList<Recipe>();
    for(File recipeFile : recipeFiles)
    {
      RecipeXMLParser parser=new RecipeXMLParser();
      Recipe recipe=parser.parseXML(recipeFile);
      recipes.add(recipe);
    }
    return recipes;
  }

  private File[] getRecipeFiles()
  {
    FileFilter fileFilter=new ExtensionPredicate("xml");
    File[] recipeFiles=RECIPES_DIR.listFiles(fileFilter);
    return recipeFiles;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainBuildLegacyRecipesFile().doIt();
  }
}
