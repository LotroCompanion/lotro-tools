package delta.games.lotro.tools.extraction.geo.markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.LandblockMarkersManager;
import delta.games.lotro.maps.data.markers.comparators.MarkerPositionComparator;

/**
 * Removes duplicate markers.
 * @author DAM
 */
public class MarkerDuplicatesRemover
{
  private static final String REPLACE="\tReplace ";

  private static final Logger LOGGER=LoggerFactory.getLogger(MarkerDuplicatesRemover.class);

  private int _removedMarkers=0;
  private int _totalMarkers=0;

  /**
   * Handle a landblock.
   * @param markersMgr Markers manager.
   */
  public void handleLandblock(LandblockMarkersManager markersMgr)
  {
    List<Marker> markers=markersMgr.getMarkers();
    _totalMarkers+=markers.size();
    Map<Integer,List<Marker>> groupedByDid=groupByDid(markers);
    for(Map.Entry<Integer,List<Marker>> entry : groupedByDid.entrySet())
    {
      List<Marker> markersForDid=entry.getValue();
      if (markersForDid.size()>1)
      {
        handleDidList(markersMgr,markersForDid);
      }
    }
  }

  private Map<Integer,List<Marker>> groupByDid(List<Marker> markers)
  {
    Map<Integer,List<Marker>> groupsMap=new HashMap<Integer,List<Marker>>();
    for(Marker marker : markers)
    {
      Integer did=Integer.valueOf(marker.getDid());
      List<Marker> markersForDid=groupsMap.get(did);
      if (markersForDid==null)
      {
        markersForDid=new ArrayList<Marker>();
        groupsMap.put(did,markersForDid);
      }
      markersForDid.add(marker);
    }
    return groupsMap;
  }

  private void handleDidList(LandblockMarkersManager markersMgr, List<Marker> markers)
  {
    // Sort by position
    Collections.sort(markers,new MarkerPositionComparator());
    Marker firstMarker=markers.get(0);
    if (LOGGER.isDebugEnabled())
    {
      int did=firstMarker.getDid();
      LOGGER.debug("DID="+did+" => "+markers.size());
    }
    int nbMarkers=markers.size();
    GeoPoint position=firstMarker.getPosition();
    Marker previous=firstMarker;
    for(int i=1;i<nbMarkers;i++)
    {
      Marker currentMarker=markers.get(i);
      GeoPoint newPosition=currentMarker.getPosition();
      if ((position.getLatitude()==newPosition.getLatitude())
          && (position.getLongitude()==newPosition.getLongitude()))
      {
        mergeMarkers(markersMgr, previous,currentMarker);
      }
      else
      {
        position=newPosition;
        previous=currentMarker;
      }
    }
  }

  private void mergeMarkers(LandblockMarkersManager mgr, Marker marker1, Marker marker2)
  {
    String label1=marker1.getLabel();
    String label2=marker2.getLabel();
    if (!label1.equals(label2))
    {
      LOGGER.debug("Found same DID and position: \n\t"+marker1+"\n\t"+marker2);
      LOGGER.debug("\tLabels differ: "+label1+" / "+label2);
      resolveLabel(marker1,marker2);
    }
    int category1=marker1.getCategoryCode();
    int category2=marker2.getCategoryCode();
    if (category1!=category2)
    {
      Integer resolvedCategory=resolveCategory(category1,category2);
      if (resolvedCategory!=null)
      {
        marker1.setCategoryCode(resolvedCategory.intValue());
        marker2.setCategoryCode(resolvedCategory.intValue());
      }
      else
      {
        LOGGER.warn("Found same DID and position: \n\t"+marker1+"\n\t"+marker2);
        LOGGER.warn("\tCategories differ: "+category1+" / "+category2);
      }
    }
    mgr.removeMarker(marker2);
    marker2.setId(marker1.getId());
    _removedMarkers++;
  }

  private boolean resolveLabel(Marker aMarker, Marker anotherMarker)
  {
    boolean ret=resolveLabelOneWay(aMarker,anotherMarker);
    if (!ret)
    {
      ret=resolveLabelOneWay(anotherMarker,aMarker);
    }
    if (!ret)
    {
      LOGGER.warn("Unresolved label diff: \n\t"+aMarker+"\n\t"+anotherMarker);
    }
    return ret;
  }

  private boolean resolveLabelOneWay(Marker marker1, Marker marker2)
  {
    if (marker2.getLabel().contains(marker1.getLabel()))
    {
      LOGGER.debug(REPLACE+marker1.getLabel()+" by "+marker2.getLabel());
      marker1.setLabel(marker2.getLabel());
      return true;
    }
    int category1=marker1.getCategoryCode();
    if (category1==74)
    {
      LOGGER.debug(REPLACE+marker1.getLabel()+" by "+marker2.getLabel());
      marker1.setLabel(marker2.getLabel());
      return true;
    }
    int category2=marker2.getCategoryCode();
    if (category1==70)
    {
      String newLabel;
      if (category2==27)
      {
        newLabel=marker2.getLabel().trim();
      }
      else
      {
        if (marker2.getLabel().trim().length()>0)
        {
          newLabel=marker1.getLabel().trim()+"\n"+marker2.getLabel().trim();
        }
        else
        {
          newLabel=marker1.getLabel().trim();
        }
      }
      LOGGER.debug(REPLACE+marker1.getLabel()+" and "+marker2.getLabel()+" by "+newLabel);
      marker1.setLabel(newLabel);
      marker2.setLabel(newLabel);
      return true;
    }
    if ((category1==57) && (category2==55))
    {
      LOGGER.debug(REPLACE+marker1.getLabel()+" by "+marker2.getLabel());
      marker1.setLabel(marker2.getLabel());
      return true;
    }
    return false;
  }

  private Integer resolveCategory(int aCategory, int anotherCategory)
  {
    Integer ret=resolveCategoryOneWay(aCategory,anotherCategory);
    if (ret==null)
    {
      ret=resolveCategoryOneWay(anotherCategory,aCategory);
    }
    return ret;
  }

  private Integer resolveCategoryOneWay(int category1, int category2)
  {
    if ((category1==70) &&
        ((category2==22) || (category2==23) || (category2==24)
          || (category2==27) || (category2==29) || (category2==31)
          || (category2==33) || (category2==38) || (category2==40)
          || (category2==42) || (category2==51) || (category2==52) || (category2==53)
          || (category2==54) || (category2==58) || (category2==60)
          || (category2==61) || (category2==63)))
    {
      return Integer.valueOf(category2);
    }
    if ((category1==75) && (category2==45)) return Integer.valueOf(45);
    if ((category1==73) && (category2==34)) return Integer.valueOf(34);
    if ((category1==73) && (category2==45)) return Integer.valueOf(45);
    if ((category1==74) && (category2==21)) return Integer.valueOf(21);
    if ((category1==74) && (category2==30)) return Integer.valueOf(30);
    if ((category1==74) && (category2==31)) return Integer.valueOf(31);
    if ((category1==74) && (category2==39)) return Integer.valueOf(43);
    if ((category1==74) && (category2==41)) return Integer.valueOf(41);
    if ((category1==74) && (category2==43)) return Integer.valueOf(43);
    if ((category1==74) && (category2==51)) return Integer.valueOf(51);
    if ((category1==74) && (category2==56)) return Integer.valueOf(56);
    if ((category1==74) && (category2==57)) return Integer.valueOf(57);
    if ((category1==74) && (category2==58)) return Integer.valueOf(74); // Trader/Landmark=>Landmark (once at MT Skirmish Camp)
    if ((category1==43) && (category2==39)) return Integer.valueOf(43);
    if ((category1==55) && (category2==57)) return Integer.valueOf(57);
    if ((category1==27) && (category2==73)) return Integer.valueOf(27);
    return null;
  }

  /*
   * Some mappings:
   * NPCs: 70
   * 22: Dock-master
   * 23: Far-ranging Stable-master
   * 24: Eagle
   * 27: Task
   * 29: Item-master
   * 31: Town-crier
   * 33: Crafting Vendor
   * 38: Vendor
   * 40: Vault-keeper
   * 42: Trainer
   * 51: Stable-master
   * 53: Auctioneer
   * 54: Bard
   * 58: Trader
   * 60: Barber
   * 61: Faction Representative
   *
   * 55 / 57:
   * Milestone / Camp Site
   *
   * Item: 73
   * 45: Crafting Facility
   * Hotspot: 75
   * 45: Crafting Facility
   *
   * Landmark: 74
   * 30: Homestead
   * 31: Town Crier
   * 39: No Icon
   * 41: Settlement
   * 43: Point of Interest
   */

  /**
   * Get the number of removed markers.
   * @return a markers count.
   */
  public int getRemovedMarkers()
  {
    return _removedMarkers;
  }

  /**
   * Get the total number of markers.
   * @return a markers count.
   */
  public int getTotalMarkers()
  {
    return _totalMarkers;
  }
}
