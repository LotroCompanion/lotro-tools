package delta.games.lotro.tools.lore.maps.dynmap;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

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
  private static final Logger LOGGER=Logger.getLogger(MainDynMapLoader.class);

  private File _outDir;

  /**
   * Constructor.
   */
  public MainDynMapLoader()
  {
    File tmpDir=new File("data");
    File mapsDir=new File(tmpDir,"maps");
    File outputDir=new File(mapsDir,"output");
    _outDir=outputDir.getAbsoluteFile();
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
    File mapsDir=new File(_outDir,"maps");
    MapsPageParser mapsParser=new MapsPageParser(mapsDir);
    List<MapBundle> maps=mapsParser.parse(mapsPage,categories);
    // Load all map data
    for(MapBundle map: maps)
    {
      String key=map.getKey();
      map.getData().clear();
      // JS
      String jsUrl=DynMapConstants.BASE_URL+"/data/"+key+".js";
      File js=dynMap.download(jsUrl,key+".js");
      if (useJson(key))
      {
        File jsonFile=new File("../lotro-maps/data/"+key+".json");
        if (jsonFile.exists())
        {
          loadMapDataFromJson(map,jsonFile);
        }
        else
        {
          LOGGER.warn("Could not find file: "+jsonFile);
        }
      }
      else
      {
        loadMapDataFromJavascript(map,js);
      }
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
        File mapRootDir=map.getRootDir();
        File mapImageFile=new File(mapRootDir,"map_"+locale+".jpg");
        if (!mapImageFile.exists())
        {
          dynMap.download(mapUrl,mapImageFile);
        }
      }
      System.out.println(map.getMap());
      // Write file
      MapXMLWriter.writeMapFiles(map,EncodingNames.UTF_8);
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

  private boolean useJson(String key)
  {
    if ("wells_of_langflood".equals(key))
    {
      return true;
    }
    return false;
  }

  private void loadMapDataFromJavascript(MapBundle map, File js)
  {
    MapPageParser parser=new MapPageParser();
    parser.parse(map,js);
  }

  private void loadMapDataFromJson(MapBundle map, File jsonFile)
  {
    MapJsonParser parser=new MapJsonParser();
    parser.parse(map,jsonFile);
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
