package delta.games.lotro.tools.extraction.geo.maps.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.links.MapLink;

/**
 * Links storage.
 * @author DAM
 */
public class LinksStorage
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LinksStorage.class);

  private static final float THRESHOLD=0.0005f;
  private List<MapLink> _links;
  private Map<String,List<MapLink>> _sortedLinks;

  /**
   * Constructor.
   */
  public LinksStorage()
  {
    _links=new ArrayList<MapLink>();
    _sortedLinks=new HashMap<String,List<MapLink>>();
  }

  /**
   * Add a link.
   * @param link Link to add.
   */
  public void addLink(MapLink link)
  {
    String key=buildKey(link);
    List<MapLink> links=_sortedLinks.get(key);
    if (links==null)
    {
      links=new ArrayList<MapLink>();
      _sortedLinks.put(key,links);
    }
    addLink(links,link);
  }

  private void addLink(List<MapLink> links, MapLink linkToAdd)
  {
    boolean doAdd=true;
    if (!links.isEmpty())
    {
      for(MapLink link : links)
      {
        if (areNearEnough(link.getPosition(),linkToAdd.getPosition()))
        {
          doAdd=false;
          break;
        }
      }
    }
    if (doAdd)
    {
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Added link: "+linkToAdd);
      }
      _links.add(linkToAdd);
      links.add(linkToAdd);
    }
    else
    {
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Ignored: "+linkToAdd);
      }
    }
  }

  private boolean areNearEnough(GeoPoint p1, GeoPoint p2)
  {
    float deltaLat=p1.getLatitude()-p2.getLatitude();
    float deltaLon=p1.getLongitude()-p2.getLongitude();
    float d2=deltaLat*deltaLat+deltaLon*deltaLon;
    return d2<THRESHOLD;
  }

  private String buildKey(MapLink link)
  {
    int parentId=link.getParentId();
    int contentLayer=link.getContentLayerId();
    int targetId=link.getTargetMapKey();
    String key=parentId+"-"+contentLayer+"-"+targetId;
    return key;
  }

  /**
   * Get a list of all managed lists.
   * @return a list of links.
   */
  public List<MapLink> getLinks()
  {
    return _links;
  }
}
