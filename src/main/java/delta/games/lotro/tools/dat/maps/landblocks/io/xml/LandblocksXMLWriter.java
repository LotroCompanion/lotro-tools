package delta.games.lotro.tools.dat.maps.landblocks.io.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.helpers.AttributesImpl;

import delta.common.utils.io.xml.XmlFileWriterHelper;
import delta.common.utils.io.xml.XmlWriter;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.tools.dat.maps.data.Cell;
import delta.games.lotro.tools.dat.maps.landblocks.Landblock;
import delta.games.lotro.tools.dat.maps.landblocks.LandblocksManager;
import delta.games.lotro.tools.dat.maps.landblocks.comparators.LandblockIdComparator;

/**
 * Writes landblocks to XML files.
 * @author DAM
 */
public class LandblocksXMLWriter
{
  /**
   * Write a file with landblocks.
   * @param toFile Output file.
   * @param index Data to write.
   * @return <code>true</code> if it succeeds, <code>false</code> otherwise.
   */
  public static boolean writeLandblocksFile(File toFile, LandblocksManager index)
  {
    LandblocksXMLWriter writer=new LandblocksXMLWriter();
    List<Landblock> landblocks=index.getLandblocks();
    Collections.sort(landblocks,new LandblockIdComparator());
    boolean ok=writer.writeLandblocks(toFile,landblocks,EncodingNames.UTF_8);
    return ok;
  }

  /**
   * Write landblocks to a XML file.
   * @param outFile Output file.
   * @param data Data to write.
   * @param encoding Encoding to use.
   * @return <code>true</code> if it succeeds, <code>false</code> otherwise.
   */
  public boolean writeLandblocks(File outFile, final List<Landblock> data, String encoding)
  {
    XmlFileWriterHelper helper=new XmlFileWriterHelper();
    XmlWriter writer=new XmlWriter()
    {
      @Override
      public void writeXml(TransformerHandler hd) throws Exception
      {
        writeLandblocks(hd,data);
      }
    };
    boolean ret=helper.write(outFile,encoding,writer);
    return ret;
  }

  private void writeLandblocks(TransformerHandler hd, List<Landblock> data) throws Exception
  {
    hd.startElement("","",LandblocksXMLConstants.LANDBLOCKS_TAG,new AttributesImpl());
    for(Landblock landblock : data)
    {
      writeLandblock(hd,landblock);
    }
    hd.endElement("","",LandblocksXMLConstants.LANDBLOCKS_TAG);
  }

  private void writeLandblock(TransformerHandler hd, Landblock landblock) throws Exception
  {
    AttributesImpl attrs=new AttributesImpl();
    // Block
    BlockReference blockId=landblock.getBlockId();
    // - Region
    int region=blockId.getRegion();
    attrs.addAttribute("","",LandblocksXMLConstants.REGION_ATTR,XmlWriter.CDATA,String.valueOf(region));
    // - X
    int x=blockId.getBlockX();
    attrs.addAttribute("","",LandblocksXMLConstants.BLOCK_X_ATTR,XmlWriter.CDATA,String.valueOf(x));
    // - Y
    int y=blockId.getBlockY();
    attrs.addAttribute("","",LandblocksXMLConstants.BLOCK_Y_ATTR,XmlWriter.CDATA,String.valueOf(y));
    // Area
    Integer areaId=landblock.getParentArea();
    if (areaId!=null)
    {
      attrs.addAttribute("","",LandblocksXMLConstants.AREA_ID_ATTR,XmlWriter.CDATA,areaId.toString());
    }
    // Dungeon
    Integer dungeonId=landblock.getParentDungeon();
    if (dungeonId!=null)
    {
      attrs.addAttribute("","",LandblocksXMLConstants.DUNGEON_ID_ATTR,XmlWriter.CDATA,dungeonId.toString());
    }
    // Height
    float height=landblock.getCenterHeight();
    attrs.addAttribute("","",LandblocksXMLConstants.HEIGHT_ATTR,XmlWriter.CDATA,String.valueOf(height));
    hd.startElement("","",LandblocksXMLConstants.LANDBLOCK_TAG,attrs);
    // Cells
    List<Integer> cellIndexes=new ArrayList<Integer>(landblock.getCellIndexes());
    Collections.sort(cellIndexes);
    for(Integer cellIndex : cellIndexes)
    {
      Cell cell=landblock.getCell(cellIndex.intValue());
      AttributesImpl cellAttrs=new AttributesImpl();
      cellAttrs.addAttribute("","",LandblocksXMLConstants.CELL_INDEX_ATTR,XmlWriter.CDATA,cellIndex.toString());
      Integer dungeonIdForCell=landblock.getCellDungeon(cellIndex.intValue());
      if (dungeonIdForCell!=null)
      {
        cellAttrs.addAttribute("","",LandblocksXMLConstants.DUNGEON_ID_ATTR,XmlWriter.CDATA,dungeonIdForCell.toString());
      }
      DatPosition position=cell.getPosition();
      if (position!=null)
      {
        cellAttrs.addAttribute("","",LandblocksXMLConstants.CELL_X_ATTR,XmlWriter.CDATA,String.valueOf(position.getPosition().getX()));
        cellAttrs.addAttribute("","",LandblocksXMLConstants.CELL_Y_ATTR,XmlWriter.CDATA,String.valueOf(position.getPosition().getY()));
        cellAttrs.addAttribute("","",LandblocksXMLConstants.CELL_Z_ATTR,XmlWriter.CDATA,String.valueOf(position.getPosition().getZ()));
      }
      hd.startElement("","",LandblocksXMLConstants.CELL_TAG,cellAttrs);
      hd.endElement("","",LandblocksXMLConstants.CELL_TAG);
    }
    hd.endElement("","",LandblocksXMLConstants.LANDBLOCK_TAG);
  }
}
