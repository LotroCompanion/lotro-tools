package delta.games.lotro.tools.lore.maps.dynmap;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.NumericTools;
import delta.common.utils.text.TextTools;
import delta.common.utils.text.TextUtils;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.Map;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * Parser for a map page.
 * @author DAM
 */
public class MapPageParser
{
  private static final Logger _logger=Logger.getLogger(MapPageParser.class);

  private static final String STARTX_SEED="map.StartX = ";
  private static final String STARTY_SEED="map.StartY = ";
  private static final String SCALE2MAP_SEED="map.ScaleToMap = ";
  private static final String MODIFIED_SEED="map.Modified = ";

  /**
   * Parse map data.
   * @param mapBundle Storage for extracted data.
   * @param js Input file.
   */
  public void parse(MapBundle mapBundle, File js)
  {
    List<String> lines=TextUtils.readAsLines(js);
    Float startX=null;
    Float startY=null;
    Float scale=null;
    Date date=null;
    MarkersManager markers=mapBundle.getData();
    int markerId=1;
    for(String line : lines)
    {
      if (line.startsWith(STARTX_SEED))
      {
        //map.StartX = -42.8;
        startX=getValue(STARTX_SEED,line);
      }
      else if (line.startsWith(STARTY_SEED))
      {
        //map.StartY = 14.7;
        startY=getValue(STARTY_SEED,line);
      }
      else if (line.startsWith(SCALE2MAP_SEED))
      {
        //map.ScaleToMap = 3.498879;
        scale=getValue(SCALE2MAP_SEED,line);
      }
      //map.ScaleToIg = 0.285807;
      else if (line.startsWith(MODIFIED_SEED))
      {
        //map.Modified = "07/12/2015";
        date=getDate(MODIFIED_SEED,line);
      }
      else if (line.startsWith("["))
      {
        Marker marker=parseItemLine(line);
        if (marker!=null)
        {
          marker.setId(markerId);
          markerId++;
          markers.addMarker(marker);
        }
      }
    }
    Map map=mapBundle.getMap();
    if ((startX!=null) && (startY!=null) && (scale!=null))
    {
      GeoPoint start=new GeoPoint(startX.floatValue(),startY.floatValue());
      GeoReference reference=new GeoReference(start,scale.floatValue());
      map.setGeoReference(reference);
    }
    map.setLastUpdate(date);
  }

  private Float getValue(String seed, String line)
  {
    line=line.substring(seed.length()).trim();
    if (line.endsWith(";")) line=line.substring(0,line.length()-1);
    return NumericTools.parseFloat(line);
  }

  private Date getDate(String seed, String line)
  {
    Date ret=null;
    line=line.substring(seed.length()).trim();
    if (line.endsWith(";")) line=line.substring(0,line.length()-1);
    if (line.endsWith("\"")) line=line.substring(0,line.length()-1);
    if (line.startsWith("\"")) line=line.substring(1);
    String[] dateComponents=line.split("/");
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

  private Marker parseItemLine(String line)
  {
    // [-77.4,-24.6,{en:"Auctioneer",de:"TBD",fr:"TBD"},13,"Auctioneer"],
    line=line.trim();
    if (line.endsWith(",")) line=line.substring(0,line.length()-1);
    line=TextTools.findBetween(line,"[", "]");
    Marker marker=new Marker();
    String part1=findBefore(line,"{");
    String labels=TextTools.findBetween(line,"{","}");
    String part3=TextTools.findAfter(line,"}");
    ParsingUtils.parseLabels(marker.getLabels(),"{"+labels+"}");
    Float latitude=null;
    Float longitude=null;
    String[] posItems=part1.split(",");
    if (posItems.length==2)
    {
      latitude=NumericTools.parseFloat(posItems[0]);
      longitude=NumericTools.parseFloat(posItems[1]);
    }
    Integer categoryCode=null;
    String[] categoryItems=part3.split(",");
    if (categoryItems.length>=3)
    {
      categoryCode=NumericTools.parseInteger(categoryItems[1]);
    }
    String comment=null;
    if (categoryItems.length>=4)
    {
      comment=TextTools.findBetween(categoryItems[3],"\"","\"");
    }
    marker.setComment(comment);
    if ((latitude!=null) && (longitude!=null) && (categoryCode!=null))
    {
      GeoPoint position=new GeoPoint(longitude.floatValue(),latitude.floatValue());
      marker.setPosition(position);
      marker.setCategoryCode(categoryCode.intValue());
    }
    else
    {
      _logger.warn("Bad line: "+line);
    }
    return marker;
  }

  /**
   * In a given line, get the string item before the <code>after</code> string key.
   * @param line Line to parse.
   * @param after A string key.
   * @return A string item or <code>null</code> if <code>line</code> does not contain <code>before</code>.
   */
  public static String findBefore(String line, String after)
  {
    String ret=null;
    int index=line.indexOf(after);
    if (index!=-1)
    {
      ret=line.substring(0,index);
    }
    return ret;
  }
}
