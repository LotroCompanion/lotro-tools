package delta.games.lotro.tools.dat.maps.landblocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.maps.landblocks.io.xml.LandblocksXMLParser;

/**
 * Manager for landblocks.
 * <ul>
 * <li>Gives the parent zone for positions.
 * </ul>
 * @author DAM
 */
public class LandblocksManager
{
  private static final Logger LOGGER=Logger.getLogger(LandblocksManager.class);

  private static final LandblocksManager _instance=load();
  private Map<String,Landblock> _index;

  /**
   * Get the reference instance of this class.
   * @return the reference instance of this class.
   */
  public static LandblocksManager getInstance()
  {
    return _instance;
  }

  private static LandblocksManager load()
  {
    return new LandblocksXMLParser().parseXML(GeneratedFiles.LANDBLOCKS);
  }

  /**
   * Constructor.
   */
  public LandblocksManager()
  {
    _index=new HashMap<String,Landblock>();
  }

  private String getKey(int region, int blockX, int blockY)
  {
    return region+"#"+blockX+"#"+blockY;
  }

  /**
   * Get the managed landblocks.
   * @return a list of landblocks.
   */
  public List<Landblock> getLandblocks()
  {
    return new ArrayList<Landblock>(_index.values());
  }

  /**
   * Get a landblock.
   * @param region Region.
   * @param blockX Block X.
   * @param blockY Block Y.
   * @return A landblock or <code>null</code> if not found.
   */
  public Landblock getLandblock(int region, int blockX, int blockY)
  {
    String key=getKey(region,blockX,blockY);
    Landblock data=_index.get(key);
    return data;
  }

  /**
   * Add a landblock.
   * @param landblock Landblock to add.
   */
  public void addLandblock(Landblock landblock)
  {
    BlockReference blockId=landblock.getBlockId();
    String key=getKey(blockId.getRegion(),blockId.getBlockX(),blockId.getBlockY());
    _index.put(key,landblock);
  }

  /**
   * Get the parent zone (area or dungeon) for a position.
   * @param position Position to use.
   * @return A parent zone identifier or <code>null</code>.
   */
  public Integer getParentZone(DatPosition position)
  {
    int region=position.getRegion();
    int blockX=position.getBlockX();
    int blockY=position.getBlockY();
    int cell=position.getCell();
    Landblock data=getLandblock(region,blockX,blockY);
    Integer ret=null;
    if (data!=null)
    {
      ret=data.getParentData(cell,position.getPosition().getZ());
    }
    else
    {
      LOGGER.warn("No parent data for: "+position);
    }
    return ret;
  }
}
