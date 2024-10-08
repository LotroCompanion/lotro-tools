package delta.games.lotro.tools.extraction.geo.landblocks;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.math.geometry.Vector3D;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.EntityDescriptor;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.geo.markers.MarkersLoadingUtils;

/**
 * Analyzer for generators found in a landblock.
 * @author DAM
 */
public class LandblockGeneratorsAnalyzer
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LandblockGeneratorsAnalyzer.class);

  private MarkersLoadingUtils _markerUtils;

  /**
   * Constructor.
   * @param markerUtils Marker utils.
   */
  public LandblockGeneratorsAnalyzer(MarkersLoadingUtils markerUtils)
  {
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
          if ((dids!=null) && (!dids.isEmpty()))
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
      if (contentLayers==null)
      {
        _markerUtils.buildMarker(position,did,0);
      }
      else
      {
        for(int contentLayerId : contentLayers)
        {
          if (contentLayerId==0)
          {
            LOGGER.warn("Found CL 0!");
          }
          _markerUtils.buildMarker(position,did,contentLayerId);
        }
      }
    }
  }
}
