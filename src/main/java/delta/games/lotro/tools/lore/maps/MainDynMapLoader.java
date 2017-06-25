package delta.games.lotro.tools.lore.maps;

import java.io.File;
import java.util.List;

import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.games.lotro.maps.data.CategoriesManager;
import delta.games.lotro.maps.data.Category;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;
import delta.games.lotro.maps.data.io.xml.CategoriesXMLWriter;
import delta.games.lotro.maps.data.io.xml.MapXMLWriter;

/**
 * Download maps data from the site dynmap.
 * @author DAM
 */
public class MainDynMapLoader
{
  private File _outDir;

  /**
   * Constructor.
   */
  public MainDynMapLoader()
  {
    _outDir=new File("data/maps").getAbsoluteFile();
  }

  private void doIt()
  {
    DynMapSiteInterface dynMap=new DynMapSiteInterface();
    // Categories
    File localizationFile=dynMap.download(DynMapConstants.LOCALIZATION_PAGE,"localization.js");
    List<String> localizationPage=TextUtils.readAsLines(localizationFile);
    CategoriesParser categoriesParser=new CategoriesParser();
    CategoriesManager categories=categoriesParser.parse(localizationPage);
    // Maps
    File mapsFile=dynMap.download(DynMapConstants.MAP_PAGE,"map.js");
    List<String> mapsPage=TextUtils.readAsLines(mapsFile);
    MapsPageParser mapsParser=new MapsPageParser();
    List<MapBundle> maps=mapsParser.parse(mapsPage,categories);
    // Load all map data
    for(MapBundle map: maps)
    {
      String key=map.getKey();
      File mapDir=new File(new File(_outDir,"maps"),key);
      // JS
      String jsUrl=DynMapConstants.BASE_URL+"/data/"+key+".js";
      File js=dynMap.download(jsUrl,key+".js");
      loadMapData(map,js,categories);
      // Inspect
      MarkersManager markers=map.getData();
      for(Category category : categories.getAllSortedByCode())
      {
        List<Marker> markersList=markers.getByCategory(category);
        System.out.println(category);
        for(Marker marker : markersList)
        {
          System.out.println("\t"+marker);
        }
      }
      // Map images
      String[] locales={"en","de","fr"};
      for(String locale : locales)
      {
        String mapUrl=DynMapConstants.BASE_URL+"/images/maps/"+locale+'/'+key+".jpg";
        File mapImageFile=new File(mapDir,"map_"+locale+".jpg");
        dynMap.download(mapUrl,mapImageFile);
      }
      System.out.println(map.getMap());
      // Write file
      MapXMLWriter writer=new MapXMLWriter();
      File toFile=new File(mapDir,"markers.xml");
      writer.write(toFile,map,EncodingNames.UTF_8);
    }

    // Write categories file
    {
      CategoriesXMLWriter writer=new CategoriesXMLWriter();
      File toFile=new File(_outDir,"categories.xml");
      writer.write(toFile,categories,EncodingNames.UTF_8);
    }
    File imagesDir=new File(_outDir,"images");
    for(Category category : categories.getAllSortedByCode())
    {
      String key=category.getIcon();
      String iconUrl=DynMapConstants.BASE_URL+"/images/" + key + ".gif";
      File toFile=new File(imagesDir,key+".gif");
      dynMap.download(iconUrl,toFile);
      System.out.println(category);
    }
  }

  private void loadMapData(MapBundle map, File js, CategoriesManager categories)
  {
    MapPageParser parser=new MapPageParser(categories);
    parser.parse(map,js);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainDynMapLoader().doIt();
  }
}
