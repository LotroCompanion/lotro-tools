package delta.games.lotro.tools.lore.maps.dynmap;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import delta.common.utils.NumericTools;
import delta.common.utils.text.EncodingNames;
import delta.common.utils.text.TextUtils;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.Map;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * Parser for a map described in JSON.
 * @author DAM
 */
public class MapJsonParser
{
  private static final Logger LOGGER=Logger.getLogger(MapJsonParser.class);

  /**
   * Parse map data.
   * @param mapBundle Storage for extracted data.
   * @param input JSON input file.
   */
  public void parse(MapBundle mapBundle, File input)
  {
    try
    {
      String jsonStr=TextUtils.loadTextFile(input,EncodingNames.UTF_8);
      JSONObject o=new JSONObject(jsonStr);
      double startX=o.getDouble("StartX");
      double startY=o.getDouble("StartY");
      double scale=o.getDouble("ScaleToMap");
      String dateStr=o.getString("Modified");
      Date date=getDate(dateStr);

      // Markers
      MarkersManager markers=mapBundle.getData();
      JSONArray markersArray=o.getJSONArray("Markers");
      int nbMarkers=markersArray.length();
      int markerId=1;
      for(int i=0;i<nbMarkers;i++)
      {
        JSONObject markerJson=(JSONObject)markersArray.get(i);
        Marker marker=parseJsonMarker(markerJson);
        marker.setId(markerId);
        markerId++;
        markers.addMarker(marker);
      }
      // Setup map
      Map map=mapBundle.getMap();
      GeoPoint start=new GeoPoint((float)startX,(float)startY);
      GeoReference reference=new GeoReference(start,(float)scale);
      map.setGeoReference(reference);
      map.setLastUpdate(date);

      JSONObject nameJson=o.getJSONObject("Name");
      String mapName=ParsingUtils.parseLabel(nameJson);
      map.setName(mapName);
    }
    catch(Exception e)
    {
      LOGGER.error("Caught error when loading a map from JSON file: "+input,e);
    }
  }

  private Date getDate(String dateStr)
  {
    Date ret=null;
    String[] dateComponents=dateStr.split("/");
    if (dateComponents.length==3)
    {
      Integer day=NumericTools.parseInteger(dateComponents[1]);
      Integer month=NumericTools.parseInteger(dateComponents[0]);
      Integer year=NumericTools.parseInteger(dateComponents[2]);
      if ((day!=null) && (month!=null) && (year!=null))
      {
        Calendar c=GregorianCalendar.getInstance();
        c.set(year.intValue(),month.intValue()-1,day.intValue(),0,0);
        c.set(GregorianCalendar.SECOND,0);
        c.set(GregorianCalendar.MILLISECOND,0);
        ret=c.getTime();
      }
    }
    return ret;
  }

  private Marker parseJsonMarker(JSONObject line)
  {
    Marker marker=new Marker();
    try
    {
      JSONObject nameJson=line.getJSONObject("Name");
      String label=ParsingUtils.parseLabel(nameJson);
      marker.setLabel(label);
      double latitude=line.getDouble("Y");
      double longitude=line.getDouble("X");
      int categoryCode=line.getInt("Type");
      GeoPoint position=new GeoPoint((float)longitude,(float)latitude);
      marker.setPosition(position);
      marker.setCategoryCode(categoryCode);
    }
    catch(Exception e)
    {
      LOGGER.warn("Bad JSON for marker!",e);
      marker=null;
    }
    return marker;
  }

  /*
  private void doIt()
  {
    File input=new File("../lotro-maps/data/wells_of_langflood.json");
    File mapDir=new File("../lotro-maps-db/maps/wells_of_langflood");
    String key=mapDir.getName();
    MapBundle mapBundle=new MapBundle(key,mapDir);
    parse(mapBundle,input);
    // Write file
    MapXMLWriter writer=new MapXMLWriter();
    writer.writeMapFiles(mapBundle,EncodingNames.UTF_8);
  }

  public static void main(String[] args)
  {
    new MapJsonParser().doIt();
  }
  */
}
