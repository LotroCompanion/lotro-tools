package delta.games.lotro.tools.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobsManager;
import delta.games.lotro.lore.agents.npcs.NPCsManager;
import delta.games.lotro.lore.agents.npcs.NpcDescription;
import delta.games.lotro.lore.geo.landmarks.LandmarkDescription;
import delta.games.lotro.lore.geo.landmarks.LandmarksManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.GlobalMarkersManager;
import delta.games.lotro.maps.data.markers.LandblockMarkersManager;

/**
 * Computes statistics about the markers.
 * @author DAM
 */
public class MainMarkersStatistics
{
  private List<Marker> loadMarkers()
  {
    List<Marker> ret=new ArrayList<Marker>();
    GlobalMarkersManager markersMgr=initMarkersMgr();
    List<LandblockMarkersManager> lbMarkersMgrs=markersMgr.getAllManagers();
    for(LandblockMarkersManager lbMarkersMgr : lbMarkersMgrs)
    {
      for(Marker marker : lbMarkersMgr.getMarkers())
      {
        if (useMarker(marker))
        {
          ret.add(marker);
        }
      }
    }
    return ret;
  }

  private boolean useMarker(Marker marker)
  {
    int categoryCode=marker.getCategoryCode();
    if (categoryCode==50) return false;
    if (categoryCode==77) return false;
    if (categoryCode==78) return false;
    return true;
  }

  private Map<Integer,List<Marker>> sortMarkers(List<Marker> markers)
  {
    Map<Integer,List<Marker>> ret=new HashMap<Integer,List<Marker>>();
    for(Marker marker : markers)
    {
      Integer did=Integer.valueOf(marker.getDid());
      List<Marker> list=ret.get(did);
      if (list==null)
      {
        list=new ArrayList<Marker>();
        ret.put(did,list);
      }
      list.add(marker);
    }
    return ret;
  }

  private GlobalMarkersManager initMarkersMgr()
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir,false);
    GlobalMarkersManager markersMgr=mapsManager.getMarkersManager();
    int[] regions=new int[]{1,2,3,4,5,14};
    //int[] regions=new int[]{14};
    for(int region : regions)
    {
      for(int x=0;x<16;x++)
      {
        for(int y=0;y<16;y++)
        {
          markersMgr.getBlockManager(region,x,y);
        }
      }
    }
    return markersMgr;
  }

  private void showStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    showGlobalStats(sortedMarkers,markers);
    showItemStats(sortedMarkers,markers);
    showNpcStats(sortedMarkers,markers);
    showMobStats(sortedMarkers,markers);
    showLandmarksStats(sortedMarkers,markers);
    showMarkersLeft(sortedMarkers);
  }

  private void showGlobalStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    int nbDID=sortedMarkers.size();
    System.out.println("Nb markers: "+markers.size());
    System.out.println("Nb DIDs: "+nbDID);
    int minNb=Integer.MAX_VALUE;
    int maxNb=0;
    int totalBigs=0;
    for(List<Marker> list : sortedMarkers.values())
    {
      int nb=list.size();
      if (nb<minNb) minNb=nb;
      if (nb>maxNb) maxNb=nb;
      if (nb>1000)
      {
        System.out.println("Nb="+nb+" => "+list.get(0).getLabel());
        totalBigs+=nb;
      }
    }
    System.out.println("Total bigs: "+totalBigs);
    System.out.println("Min nb markers: "+minNb);
    System.out.println("Max nb markers: "+maxNb);
  }

  private void showItemStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    int totalItemMarkers=0;
    int totalItemsWithMarkers=0;
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      Integer id=Integer.valueOf(item.getIdentifier());
      List<Marker> itemMarkers=sortedMarkers.get(id);
      if (itemMarkers!=null)
      {
        totalItemMarkers+=itemMarkers.size();
        totalItemsWithMarkers++;
        sortedMarkers.remove(id);
      }
    }
    System.out.println("Total item markers: "+totalItemMarkers);
    System.out.println("Total item with markers: "+totalItemsWithMarkers);
  }

  private void showNpcStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    int totalMarkers=0;
    int totalElementsWithMarkers=0;
    int maxMarkerPerElement=0;
    NpcDescription maxNpc=null;
    for(NpcDescription npc : NPCsManager.getInstance().getNPCs())
    {
      Integer id=Integer.valueOf(npc.getIdentifier());
      List<Marker> elementMarkers=sortedMarkers.get(id);
      if (elementMarkers!=null)
      {
        totalMarkers+=elementMarkers.size();
        totalElementsWithMarkers++;
        if (elementMarkers.size()>1000)
        {
          System.out.println("Big NPC: "+npc+" => "+elementMarkers.size());
        }
        if (elementMarkers.size()>maxMarkerPerElement)
        {
          maxMarkerPerElement=elementMarkers.size();
          maxNpc=npc;
        }
        sortedMarkers.remove(id);
      }
    }
    System.out.println("Total NPC markers: "+totalMarkers);
    System.out.println("Total NPCs with markers: "+totalElementsWithMarkers);
    System.out.println("Max markers per NPC: "+maxMarkerPerElement+" ("+maxNpc+")");
  }

  private void showMobStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    int totalMarkers=0;
    int totalElementsWithMarkers=0;
    int maxMarkerPerElement=0;
    MobDescription maxMob=null;
    for(MobDescription mob : MobsManager.getInstance().getMobs())
    {
      Integer id=Integer.valueOf(mob.getIdentifier());
      List<Marker> elementMarkers=sortedMarkers.get(id);
      if (elementMarkers!=null)
      {
        totalMarkers+=elementMarkers.size();
        totalElementsWithMarkers++;
        if (elementMarkers.size()>1000)
        {
          System.out.println("Big mob: "+mob+" => "+elementMarkers.size());
        }
        if (elementMarkers.size()>maxMarkerPerElement)
        {
          maxMarkerPerElement=elementMarkers.size();
          maxMob=mob;
        }
        sortedMarkers.remove(id);
      }
    }
    System.out.println("Total mob markers: "+totalMarkers);
    System.out.println("Total Mobs with markers: "+totalElementsWithMarkers);
    System.out.println("Max markers per mob: "+maxMarkerPerElement+" ("+maxMob+")");
  }

  private void showLandmarksStats(Map<Integer,List<Marker>> sortedMarkers, List<Marker> markers)
  {
    int totalMarkers=0;
    int totalElementsWithMarkers=0;
    int maxMarkerPerElement=0;
    LandmarkDescription max=null;
    for(LandmarkDescription landmark : LandmarksManager.getInstance().getLandmarks())
    {
      Integer id=Integer.valueOf(landmark.getIdentifier());
      List<Marker> elementMarkers=sortedMarkers.get(id);
      if (elementMarkers!=null)
      {
        totalMarkers+=elementMarkers.size();
        totalElementsWithMarkers++;
        if (elementMarkers.size()>1000)
        {
          System.out.println("Big landmark: "+landmark+" => "+elementMarkers.size());
        }
        if (elementMarkers.size()>maxMarkerPerElement)
        {
          maxMarkerPerElement=elementMarkers.size();
          max=landmark;
        }
      }
      sortedMarkers.remove(id);
    }
    System.out.println("Total landmark markers: "+totalMarkers);
    System.out.println("Total landmarks with markers: "+totalElementsWithMarkers);
    System.out.println("Max markers per landmark: "+maxMarkerPerElement+" ("+max+")");
  }

  private void showMarkersLeft(Map<Integer,List<Marker>> sortedMarkers)
  {
    for(List<Marker> markers : sortedMarkers.values())
    {
      Marker marker=markers.get(0);
      System.out.println(markers.size()+" => "+marker);
    }
  }

  private void doIt()
  {
    List<Marker> markers=loadMarkers();
    Map<Integer,List<Marker>> sortedMarkers=sortMarkers(markers);
    showStats(sortedMarkers,markers);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMarkersStatistics().doIt();
  }
}
