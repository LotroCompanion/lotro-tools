package delta.games.lotro.tools.lore.deeds.geo;

import java.awt.geom.Point2D;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.geo.AchievableGeoData;
import delta.games.lotro.dat.data.geo.AchievableGeoDataItem;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.maps.AbstractMap;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.geo.AchievableGeoPoint;
import delta.games.lotro.lore.quests.objectives.InventoryItemCondition;
import delta.games.lotro.lore.quests.objectives.MonsterDiedCondition;
import delta.games.lotro.lore.quests.objectives.NpcTalkCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.maps.MapUtils;
import delta.games.lotro.tools.dat.maps.landblocks.LandblocksManager;

/**
 * Injector for deed geo data.
 * @author DAM
 */
public class MainGeoDataInjector
{
  private static final Logger LOGGER=Logger.getLogger(MainGeoDataInjector.class);

  private DataFacade _facade;
  private LandblocksManager _landblocksManager;

  private MainGeoDataInjector()
  {
    _facade=new DataFacade();
    _landblocksManager=LandblocksManager.getInstance();
  }

  private void handleAchievable(Achievable achievable, GeoData data)
  {
    int achievableId=achievable.getIdentifier();
    AchievableGeoData achievableGeoData=data.getGeoDataForAchievable(achievableId);
    if (achievableGeoData!=null)
    {
      //System.out.println("Achievable: "+achievable);
      List<Objective> objectives=achievable.getObjectives().getObjectives();
      List<Integer> objectiveIndexes=achievableGeoData.getObjectiveIndexes();
      for(Integer objectiveIndex : objectiveIndexes)
      {
        Objective objective=objectives.get(objectiveIndex.intValue()-1);
        handleAchievableObjective(achievable,objective,achievableGeoData);
      }
    }
  }

  private void handleAchievableObjective(Achievable achievable, Objective objective, AchievableGeoData geoData)
  {
    int objectiveIndex=objective.getIndex();
    //System.out.println("\tObjective #"+objectiveIndex);
    List<Integer> conditionIndexes=geoData.getConditionIndexes(objectiveIndex);
    List<ObjectiveCondition> conditions=objective.getConditions();
    for(Integer conditionIndex : conditionIndexes)
    {
      ObjectiveCondition condition=conditions.get(conditionIndex.intValue());
      condition.removeAllPoints();
      if (!useCondition(condition))
      {
        continue;
      }
      //System.out.println("\t\tCondition #"+conditionIndex);
      List<AchievableGeoDataItem> items=geoData.getConditionData(objectiveIndex,conditionIndex.intValue());
      for(AchievableGeoDataItem geoItem : items)
      {
        int did=geoItem.getDid();
        String key1=geoItem.getKey();
        String key2=geoItem.getKey2();
        DatPosition position=geoItem.getPosition();
        //DataIdentification dataId=(did!=0)?DataIdentificationTools.identify(_facade,did):null;
        //String positionLabel=(position!=null)?position.asLatLon():"?";
        //System.out.println("\t\t\t"+dataId+", key1="+key1+", key2="+key2+", position="+positionLabel);
        String key=null;
        if (key1.length()>0)
        {
          key=key1;
        }
        if (key2.length()>0)
        {
          if (key!=null)
          {
            LOGGER.warn("Both key are defined: key1="+key1+", key2="+key2);
          }
          key=key2;
        }
        float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
        Integer mapId=getMap(position);
        if (mapId==null)
        {
          LOGGER.warn("Deed: "+achievable.getName()+": No map for point: "+position);
          mapId=Integer.valueOf(0);
        }
        AchievableGeoPoint item=new AchievableGeoPoint(did,key,mapId.intValue(),new Point2D.Float(lonLat[0],lonLat[1]));
        condition.addPoint(item);
      }
    }
  }

  private boolean useCondition(ObjectiveCondition condition)
  {
    if (condition instanceof NpcTalkCondition)
    {
      return false;
    }
    if (condition instanceof MonsterDiedCondition)
    {
      return false;
    }
    if (condition instanceof InventoryItemCondition)
    {
      return false;
    }
    return true;
  }

  private Integer getMap(DatPosition position)
  {
    Integer ret=null;
    Integer parentZoneId=_landblocksManager.getParentZone(position);
    if (parentZoneId!=null)
    {
      AbstractMap map=MapUtils.findMapForZone(parentZoneId.intValue());
      if (map!=null)
      {
        ret=Integer.valueOf(map.getIdentifier());
      }
      else
      {
        Area area=GeoAreasManager.getInstance().getAreaById(parentZoneId.intValue());
        LOGGER.warn("No map for zone: "+parentZoneId+" => "+area);
      }
    }
    return ret;
  }

  private void doIt()
  {
    GeoData data=QuestEventTargetLocationLoader.loadGeoData(_facade);
    DeedsManager deedsMgr=DeedsManager.getInstance();
    for(DeedDescription deed : deedsMgr.getAll())
    {
      handleAchievable(deed,data);
    }
    DeedXMLWriter writer=new DeedXMLWriter();
    boolean ok=writer.writeDeeds(GeneratedFiles.DEEDS,deedsMgr.getAll(),EncodingNames.UTF_8);
    if (ok)
    {
      System.out.println("Updated the deeds file: "+GeneratedFiles.DEEDS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainGeoDataInjector().doIt();
  }
}
