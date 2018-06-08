package delta.games.lotro.tools.lore.maps.dynmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.text.TextTools;
import delta.games.lotro.maps.data.CategoriesManager;
import delta.games.lotro.maps.data.Category;
import delta.games.lotro.maps.data.Labels;
import delta.games.lotro.maps.data.MapBundle;

/**
 * Parser for the maps page.
 * @author DAM
 */
public class MapsPageParser
{
  private static final Logger _logger=Logger.getLogger(MapsPageParser.class);

  private static final String MAP_MARKER="obj.push(new Map(";
  private static final String CATEGORY_LINE=": type =";

  private File _rootDir;

  /**
   * Constructor.
   * @param rootDir Root directory.
   */
  public MapsPageParser(File rootDir)
  {
    _rootDir=rootDir;
  }

  /**
   * Parse maps index data.
   * @param lines Lines to read.
   * @param categories Categories to use.
   * @return A list of map data bundles.
   */
  public List<MapBundle> parse(List<String> lines, CategoriesManager categories)
  {
    List<MapBundle> ret=new ArrayList<MapBundle>();
    if (lines==null)
    {
      return ret;
    }
    for(String line : lines)
    {
      // Looking for lines like:
      // obj.push(new Map("angmar", { en: "Angmar", de: "Angmar", fr: "Angmar" }));
      int index=line.indexOf(MAP_MARKER);
      if (index!=-1)
      {
        String data=line.substring(index+MAP_MARKER.length());
        MapBundle map=parseMapLine(data);
        if (map!=null)
        {
          ret.add(map);
        }
      }
      index=line.indexOf(CATEGORY_LINE);
      if (index!=-1)
      {
        String caseStr=line.substring(0,index).trim();
        Integer categoryCode=NumericTools.parseInteger(TextTools.findAfter(caseStr," "));
        if (categoryCode!=null)
        {
          String valueStr=line.substring(index+CATEGORY_LINE.length()).trim();
          String categoryKey=TextTools.findBetween(valueStr,"\"","\"");
          Category category=categories.getByCode(categoryCode.intValue());
          if (category!=null)
          {
            category.setIcon(categoryKey);
          }
        }
      }
    }
    return ret;
  }

  private MapBundle parseMapLine(String data)
  {
    MapBundle map=null;
    // "angmar", { en: "Angmar", de: "Angmar", fr: "Angmar" }));
    try
    {
      int commaIndex=data.indexOf(',');
      if (commaIndex!=-1)
      {
        String mapKey=data.substring(0,commaIndex);
        if (mapKey.startsWith("\"")) mapKey=mapKey.substring(1);
        if (mapKey.endsWith("\"")) mapKey=mapKey.substring(0,mapKey.length()-1);
        File mapDir=new File(_rootDir,mapKey);
        map=new MapBundle(mapKey,mapDir);
        String labels=data.substring(commaIndex+1).trim();
        if (labels.endsWith("));"))
        {
          labels=labels.substring(0,labels.length()-3).trim();
        }
        Labels labelsManager=map.getMap().getLabels();
        ParsingUtils.parseLabels(labelsManager,labels);
      }
    }
    catch(Exception e)
    {
      _logger.warn("Failed to parse map data ["+data+"]",e);
    }
    return map;
  }
}
