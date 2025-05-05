package delta.games.lotro.tools.extraction.achievables.geo;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.Console;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.geo.AchievableGeoData;
import delta.games.lotro.dat.data.geo.AchievableGeoDataItem;
import delta.games.lotro.dat.data.geo.GeoData;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.loaders.wstate.QuestEventTargetLocationLoader;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.geo.GeoBoundingBox;
import delta.games.lotro.lore.maps.MapDescription;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.quests.geo.AchievableGeoPoint;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.lore.quests.objectives.InventoryItemCondition;
import delta.games.lotro.lore.quests.objectives.MonsterDiedCondition;
import delta.games.lotro.lore.quests.objectives.NpcTalkCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.geo.maps.finder.MapsFinder;
import delta.games.lotro.tools.extraction.instances.BlockGroupsBuilder;

/**
 * Injector for deed geo data.
 * @author DAM
 */
public class MainGeoDataInjector
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainGeoDataInjector.class);

  private DataFacade _facade;
  private MapsFinder _mapsFinder;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainGeoDataInjector(DataFacade facade)
  {
    _facade=facade;
    _mapsFinder=new MapsFinder();
  }

  private void handleAchievable(Achievable achievable, GeoData data)
  {
    AchievableGeoPointsManager pointsMgr=new AchievableGeoPointsManager();
    int achievableId=achievable.getIdentifier();
    AchievableGeoData achievableGeoData=data.getGeoDataForAchievable(achievableId);
    if (achievableGeoData!=null)
    {
      LOGGER.debug("Achievable: {}",achievable);
      List<Objective> objectives=achievable.getObjectives().getObjectives();
      List<Integer> objectiveIndexes=achievableGeoData.getObjectiveIndexes();
      for(Integer objectiveIndex : objectiveIndexes)
      {
        Objective objective=objectives.get(objectiveIndex.intValue()-1);
        handleAchievableObjective(pointsMgr,achievable,objective,achievableGeoData);
      }
      List<MapDescription> maps=buildMaps(pointsMgr);
      for(MapDescription map : maps)
      {
        achievable.addMap(map);
      }
    }
  }

  private void handleAchievableObjective(AchievableGeoPointsManager pointsMgr, Achievable achievable, Objective objective, AchievableGeoData geoData)
  {
    int objectiveIndex=objective.getIndex();
    LOGGER.debug("\tObjective #{}",Integer.valueOf(objectiveIndex));
    List<Integer> conditionIndexes=geoData.getConditionIndexes(objectiveIndex);
    List<ObjectiveCondition> conditions=objective.getConditions();
    for(Integer conditionIndex : conditionIndexes)
    {
      int nbConditions=conditions.size();
      if (conditionIndex.intValue()>=nbConditions)
      {
        LOGGER.warn("Invalid condition index for achievable: {}, objective #{}, condition #{}",achievable,Integer.valueOf(objectiveIndex),conditionIndex);
        continue;
      }
      ObjectiveCondition condition=conditions.get(conditionIndex.intValue());
      condition.removeAllPoints();
      if (!useCondition(achievable,condition))
      {
        continue;
      }
      LOGGER.debug("\t\tCondition #{}",conditionIndex);
      List<AchievableGeoDataItem> items=geoData.getConditionData(objectiveIndex,conditionIndex.intValue());
      for(AchievableGeoDataItem geoItem : items)
      {
        AchievableGeoPoint point=handleGeoDataItem(pointsMgr,achievable,objective,conditionIndex,geoItem);
        if (point!=null)
        {
          condition.addPoint(point);
        }
      }
    }
  }

  private AchievableGeoPoint handleGeoDataItem(AchievableGeoPointsManager pointsMgr, Achievable achievable, Objective objective,Integer conditionIndex, AchievableGeoDataItem geoItem)
  {
    int did=geoItem.getDid();
    String key1=geoItem.getKey();
    String key2=geoItem.getKey2();
    DatPosition position=geoItem.getPosition();
    if (position==null)
    {
      return null;
    }
    if (LOGGER.isDebugEnabled())
    {
      DataIdentification dataId=(did!=0)?DataIdentificationTools.identify(_facade,did):null;
      String positionLabel=position.asLatLon();
      LOGGER.debug("\t\t\t{}, key1={}, key2={}, position={}",dataId,key1,key2,positionLabel);
    }
    String key=null;
    if (!key1.isEmpty())
    {
      key=key1;
    }
    if (!key2.isEmpty())
    {
      if (key!=null)
      {
        DataIdentification dataId=(did!=0)?DataIdentificationTools.identify(_facade,did):null;
        String positionLabel=position.asLatLon();
        int objectiveIndex=objective.getIndex();
        LOGGER.warn("Achievable {}, objective #{}, condition #{}:",achievable,Integer.valueOf(objectiveIndex),conditionIndex);
        LOGGER.warn("\tBoth key are defined: DataID={}, position={}, key1={}, key2={}",dataId,positionLabel,key1,key2);
      }
      key=key2;
    }
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    Integer mapId=_mapsFinder.getMap(position);
    AchievableGeoPoint item=new AchievableGeoPoint(did,key,0,new Point2D.Float(lonLat[0],lonLat[1]));
    if (mapId!=null)
    {
      pointsMgr.addPointForMap(mapId.intValue(),item);
    }
    else
    {
      BlockReference block=new BlockReference(position.getRegion(),position.getBlockX(),position.getBlockY());
      pointsMgr.addPointForBlock(block,item);
    }
    fixPoint(achievable.getIdentifier(),item);
    return item;
  }

  private void fixPoint(int deedId, AchievableGeoPoint point)
  {
    // Rare Gorgoroth Chests of Lhingris
    if (deedId==1879354855)
    {
      if ("cache_1".equals(point.getKey()))
      {
        point.setPosition(10.7f,-64.2f);
      }
    }
  }

  private List<MapDescription> buildMaps(AchievableGeoPointsManager pointsMgr)
  {
    List<MapDescription> maps=new ArrayList<MapDescription>();
    ParchmentMapsManager mgr=ParchmentMapsManager.getInstance();
    // Map IDs
    List<Integer> mapIds=pointsMgr.getMapIds();
    for(Integer mapId : mapIds)
    {
      MapDescription map=new MapDescription();
      map.setMapId(mapId);
      ParchmentMap parchmentMap=mgr.getMapById(mapId.intValue());
      if (parchmentMap!=null)
      {
        map.setRegion(parchmentMap.getRegion());
      }
      int mapIndex=maps.size();
      for(AchievableGeoPoint point : pointsMgr.getPointsForMap(mapId.intValue()))
      {
        point.setMapIndex(mapIndex);
      }
      maps.add(map);
    }
    // Blocks
    List<BlockReference> blocks=pointsMgr.getBlocks();
    BlockGroupsBuilder builder=new BlockGroupsBuilder();
    List<List<BlockReference>> groups=builder.buildGroups(blocks);
    for(List<BlockReference> group : groups)
    {
      GeoBoundingBox boundingBox=buildBoundingBox(group);
      LOGGER.info("Box: {}",boundingBox);
      MapDescription map=new MapDescription();
      map.setRegion(group.get(0).getRegion());
      map.setBoundingBox(boundingBox);
      int mapIndex=maps.size();
      for(BlockReference block : group)
      {
        List<AchievableGeoPoint> points=pointsMgr.getPointsForBlock(block);
        for(AchievableGeoPoint point : points)
        {
          point.setMapIndex(mapIndex);
        }
      }
      maps.add(map);
    }
    return maps;
  }

  private static GeoBoundingBox buildBoundingBox(List<BlockReference> blocks)
  {
    GeoBoundingBox box=null;
    for(BlockReference block : blocks)
    {
      GeoBoundingBox blockBox=buildBoxForBlock(block);
      if (box==null)
      {
        box=blockBox;
      }
      else
      {
        box.extend(blockBox);
      }
    }
    return box;
  }

  private static GeoBoundingBox buildBoxForBlock(BlockReference block)
  {
    int blockX=block.getBlockX();
    int blockY=block.getBlockY();
    float[] startLatLon=PositionDecoder.decodePosition(blockX,blockY,0,0);
    float[] endLatLon=PositionDecoder.decodePosition(blockX+1,blockY+1,0,0);
    return new GeoBoundingBox(startLatLon[0],startLatLon[1],endLatLon[0],endLatLon[1]);
  }

  private boolean useCondition(Achievable achievable, ObjectiveCondition condition)
  {
    if (condition instanceof NpcTalkCondition)
    {
      if (achievable instanceof QuestDescription)
      {
        QuestDescription quest=(QuestDescription)achievable;
        int code=quest.getCategory().getCode();
        return code!=112; // Ignore tasks
      }
      return true;
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

  /**
   * Perform injection.
   * @param achievables Achievables to use.
   */
  public void doIt(List<Achievable> achievables)
  {
    // Add geo data
    GeoData data=QuestEventTargetLocationLoader.loadGeoData(_facade);
    if (data==null)
    {
      return;
    }
    for(Achievable achievable : achievables)
    {
      handleAchievable(achievable,data);
    }
  }

  private void save(List<DeedDescription> deeds, List<QuestDescription> quests)
  {
    // Save data
    // - deeds
    DeedXMLWriter writer=new DeedXMLWriter();
    boolean ok=writer.writeDeeds(GeneratedFiles.DEEDS,deeds,EncodingNames.UTF_8);
    if (ok)
    {
      Console.println("Updated the deeds file: "+GeneratedFiles.DEEDS);
    }
    // - quests
    QuestXMLWriter questsWriter=new QuestXMLWriter();
    ok=questsWriter.writeQuests(GeneratedFiles.QUESTS,quests,EncodingNames.UTF_8);
    if (ok)
    {
      Console.println("Updated the quests file: "+GeneratedFiles.QUESTS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MainGeoDataInjector injector=new MainGeoDataInjector(facade);
    List<Achievable> achievables=new ArrayList<Achievable>();
    List<DeedDescription> deeds=DeedsManager.getInstance().getAll();
    achievables.addAll(deeds);
    List<QuestDescription> quests=QuestsManager.getInstance().getAll();
    achievables.addAll(quests);
    injector.doIt(achievables);
    injector.save(deeds,quests);
  }
}
