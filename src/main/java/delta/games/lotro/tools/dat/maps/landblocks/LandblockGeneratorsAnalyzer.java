package delta.games.lotro.tools.dat.maps.landblocks;

import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.EntityDescriptor;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.Vector3D;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.tools.dat.maps.MarkersLoadingUtils;
import delta.games.lotro.tools.dat.maps.data.LandBlockInfo;
import delta.games.lotro.tools.dat.maps.data.LbiLink;
import delta.games.lotro.tools.dat.maps.data.Weenie;

/**
 * Analyzer for generators found in a landblock.
 * @author DAM
 */
public class LandblockGeneratorsAnalyzer
{
  private static final Logger LOGGER=Logger.getLogger(LandblockGeneratorsAnalyzer.class);

  private DataFacade _facade;
  private MarkersLoadingUtils _markerUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param markerUtils Marker utils.
   */
  public LandblockGeneratorsAnalyzer(DataFacade facade, MarkersLoadingUtils markerUtils)
  {
    _facade=facade;
    _markerUtils=markerUtils;
  }

  /**
   * Handle a landblock.
   * @param lbi Landblock to use.
   */
  public void handleLandblock(LandBlockInfo lbi)
  {
    for(EntityDescriptor entity : lbi.getEntities())
    {
      if ("GeneratorPoint".equals(entity.getType()))
      {
        handleEntity(entity,lbi);
      }
    }
  }

  private void handleEntity(EntityDescriptor entity, LandBlockInfo lbi)
  {
    long entityId=entity.getIid();
    // Find link with type "Generator_PositionSet" and "To"=entityId
    for(LbiLink link : lbi.getLinks())
    {
      if ((link.getToIid()==entityId) && ("Generator_PositionSet".equals(link.getType())))
      {
        long from=link.getFromIid();
        // Search weenie:
        Weenie weenie=lbi.getWeenieByIid(from);
        if (weenie!=null)
        {
          int[] contentLayers=initContentLayers(weenie);
          Set<Integer> dids=weenie.getGeneratorDids();
          if ((dids!=null) && (dids.size()>0))
          {
            for(Integer did : dids)
            {
              buildMarker(lbi,entity,did.intValue(),contentLayers);
            }
          }
        }
      }
    }
  }

  private int[] initContentLayers(Weenie weenie)
  {
    int[] ids=null;
    PropertiesSet props=weenie.getProps();
    Object[] contentLayersArray=(Object[])props.getProperty("Entity_ContentLayers");
    if ((contentLayersArray!=null) && (contentLayersArray.length>0))
    {
      ids=new int[contentLayersArray.length];
      int index=0;
      for(Object contentLayerObj : contentLayersArray)
      {
        ids[index]=((Integer)contentLayerObj).intValue();
        index++;
      }
    }
    return ids;
  }

  private DatPosition buildPosition(LandBlockInfo lbi, EntityDescriptor entity)
  {
    int region=lbi.getRegion();
    int blockX=lbi.getBlockX();
    int blockY=lbi.getBlockY();
    DatPosition position=new DatPosition();
    position.setRegion(region);
    position.setBlock(blockX,blockY);
    Vector3D pos=entity.getPosition();
    position.setPosition(pos.getX(),pos.getY(),pos.getZ());
    // Cell?
    return position;
  }

  private void buildMarker(LandBlockInfo lbi, EntityDescriptor entity, int did, int[] contentLayers)
  {
    if (_markerUtils!=null)
    {
      DatPosition position=buildPosition(lbi,entity);
      DataIdentification dataId=DataIdentificationTools.identify(_facade,did);
      if (contentLayers==null)
      {
        _markerUtils.buildMarker(position,dataId,0);
      }
      else
      {
        for(int contentLayerId : contentLayers)
        {
          if (contentLayerId==0)
          {
            LOGGER.warn("Found CL 0!");
          }
          _markerUtils.buildMarker(position,dataId,contentLayerId);
        }
      }
    }
  }
}
