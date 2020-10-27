package delta.games.lotro.tools.dat.maps.landblocks.io.xml;

import java.io.File;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import delta.common.utils.xml.DOMParsingTools;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.tools.dat.maps.data.Cell;
import delta.games.lotro.tools.dat.maps.landblocks.Landblock;
import delta.games.lotro.tools.dat.maps.landblocks.LandblocksManager;

/**
 * Parser for the landblocks stored in XML.
 * @author DAM
 */
public class LandblocksXMLParser
{
  /**
   * Parse the XML file.
   * @param source Source file.
   * @return Parsed data or <code>null</code>.
   */
  public LandblocksManager parseXML(File source)
  {
    LandblocksManager ret=null;
    Element root=DOMParsingTools.parse(source);
    if (root!=null)
    {
      ret=parseLandblocks(root);
    }
    return ret;
  }

  /**
   * Build a landblocks manager from an XML tag.
   * @param rootTag Root tag.
   * @return A landblocks manager.
   */
  private LandblocksManager parseLandblocks(Element rootTag)
  {
    LandblocksManager mgr=new LandblocksManager();

    List<Element> landblockTags=DOMParsingTools.getChildTagsByName(rootTag,LandblocksXMLConstants.LANDBLOCK_TAG);
    for(Element landblockTag : landblockTags)
    {
      Landblock landblock=parseLandblock(landblockTag);
      mgr.addLandblock(landblock);
    }
    return mgr;
  }

  private Landblock parseLandblock(Element landblockTag)
  {
    NamedNodeMap attrs=landblockTag.getAttributes();
    // Block ID
    int region=DOMParsingTools.getIntAttribute(attrs,LandblocksXMLConstants.REGION_ATTR,1);
    int blockX=DOMParsingTools.getIntAttribute(attrs,LandblocksXMLConstants.BLOCK_X_ATTR,0);
    int blockY=DOMParsingTools.getIntAttribute(attrs,LandblocksXMLConstants.BLOCK_Y_ATTR,0);
    BlockReference blockId=new BlockReference(region,blockX,blockY);
    Landblock ret=new Landblock(blockId);
    // Area ID
    int areaId=DOMParsingTools.getIntAttribute(attrs,LandblocksXMLConstants.AREA_ID_ATTR,0);
    if (areaId!=0)
    {
      ret.setParentArea(areaId);
    }
    // Dungeon ID
    int dungeonId=DOMParsingTools.getIntAttribute(attrs,LandblocksXMLConstants.DUNGEON_ID_ATTR,0);
    if (dungeonId!=0)
    {
      ret.setParentDungeon(dungeonId);
    }
    // Height
    float height=DOMParsingTools.getFloatAttribute(attrs,LandblocksXMLConstants.HEIGHT_ATTR,0);
    ret.setCenterHeight(height);
    // Cells
    List<Element> cellTags=DOMParsingTools.getChildTagsByName(landblockTag,LandblocksXMLConstants.CELL_TAG);
    for(Element cellTag : cellTags)
    {
      NamedNodeMap cellAttrs=cellTag.getAttributes();
      int cellIndex=DOMParsingTools.getIntAttribute(cellAttrs,LandblocksXMLConstants.CELL_INDEX_ATTR,0);
      int dungeonIdForCellValue=DOMParsingTools.getIntAttribute(cellAttrs,LandblocksXMLConstants.DUNGEON_ID_ATTR,-1);
      Integer dungeonIdForCell=(dungeonIdForCellValue!=-1)?Integer.valueOf(dungeonIdForCellValue):null;
      float x=DOMParsingTools.getFloatAttribute(cellAttrs,LandblocksXMLConstants.CELL_X_ATTR,0);
      float y=DOMParsingTools.getFloatAttribute(cellAttrs,LandblocksXMLConstants.CELL_Y_ATTR,0);
      float z=DOMParsingTools.getFloatAttribute(cellAttrs,LandblocksXMLConstants.CELL_Z_ATTR,0);
      DatPosition position=new DatPosition();
      position.setPosition(x,y,z);
      Cell cell=new Cell(cellIndex,dungeonIdForCell);
      cell.setPosition(position);
      ret.addCell(cell);
    }
    return ret;
  }
}
