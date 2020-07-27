package delta.games.lotro.tools.dat.maps;

import java.util.List;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.maps.data.CategoriesManager;
import delta.games.lotro.maps.data.Category;
import delta.games.lotro.maps.data.MapsManager;
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
   * @param mapsManager Maps managee to use.
   */
  public void doIt(MapsManager mapsManager)
  {
    _iconsLoader.doIt(mapsManager.getRootDir());
    CategoriesManager categoriesMgr=mapsManager.getCategories();
    List<Integer> tokens=_mapNoteType.getTokens();
    for(Integer token : tokens)
    {
      String meaning=_mapNoteType.getString(token.intValue());
      meaning=StringUtils.fixName(meaning);
      Category category=new Category(token.intValue());
      category.setIcon(token.toString());
      category.setName(meaning);
      categoriesMgr.addCategory(category);
    }
  }
}
