package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.List;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.maps.data.categories.CategoriesConstants;
import delta.games.lotro.maps.data.categories.CategoriesManager;
import delta.games.lotro.maps.data.categories.Category;
import delta.games.lotro.utils.StringUtils;

/**
 * Builder for map categories.
 * @author DAM
 */
public class MapCategoriesBuilder
{
  private DataFacade _facade;
  private MapIconsLoader _iconsLoader;
  private EnumMapper _mapNoteType;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapCategoriesBuilder(DataFacade facade)
  {
    _facade=facade;
    _iconsLoader=new MapIconsLoader(facade);
    _mapNoteType=_facade.getEnumsManager().getEnumMapper(587202775);
  }

  /**
   * Do it.
   * @param categoriesManager Categories manager to use.
   */
  public void doIt(CategoriesManager categoriesManager)
  {
    File categoriesDir=categoriesManager.getCategoriesDir();
    _iconsLoader.doIt(categoriesDir);
    List<Integer> tokens=_mapNoteType.getTokens();
    for(Integer token : tokens)
    {
      String meaning=_mapNoteType.getString(token.intValue());
      meaning=StringUtils.fixName(meaning);
      Category category=new Category(token.intValue());
      category.setIcon(token.toString());
      category.setName(meaning);
      categoriesManager.addCategory(category);
    }
    // Additional categories
    categoriesManager.addCategory(buildCategory(CategoriesConstants.NPC,"NPC"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.MONSTER,"Monster"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.CONTAINER,"Container"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.DOOR,"Door"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.ITEM,"Item"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.LANDMARK,"Landmark"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.HOTSPOT,"Hotspot"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.CROP,"Crop"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.CRITTER,"Critter"));
    categoriesManager.addCategory(buildCategory(CategoriesConstants.OTHER,"Other"));
  }

  private Category buildCategory(int id, String name)
  {
    Category category=new Category(id);
    category.setIcon(String.valueOf(id));
    category.setName(name);
    return category;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapCategoriesBuilder loader=new MapCategoriesBuilder(facade);
    File rootDir=MapConstants.getRootDir();
    File categoriesDir=new File(rootDir,"categories");
    CategoriesManager categoriesManager=new CategoriesManager(categoriesDir);
    loader.doIt(categoriesManager);
    categoriesManager.save();
  }
}
