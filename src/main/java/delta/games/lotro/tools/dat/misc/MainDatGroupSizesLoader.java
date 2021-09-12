package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.groupSize.GroupSize;
import delta.games.lotro.common.groupSize.io.xml.GroupSizeXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Get group sizes from DAT files.
 * @author DAM
 */
public class MainDatGroupSizesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatGroupSizesLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatGroupSizesLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load group sizes.
   */
  public void doIt()
  {
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(587203290);
    if (enumMapper!=null)
    {
      List<GroupSize> groupSizes=new ArrayList<GroupSize>();
      for(Integer code : enumMapper.getTokens())
      {
        if (code.intValue()==0)
        {
          continue;
        }
        String label=enumMapper.getLabel(code.intValue());
        String legacyKey=getLegacyKey(code.intValue());
        GroupSize groupSize=new GroupSize(code.intValue(),legacyKey,label);
        groupSizes.add(groupSize);
      }
      GroupSizeXMLWriter.writeGroupSizesFile(GeneratedFiles.GROUP_SIZES,groupSizes);
    }
    else
    {
      LOGGER.warn("Could not load group sizes enum");
    }
  }

  private String getLegacyKey(int code)
  {
    if (code==1) return "SOLO";
    if (code==2) return "DUO";
    if (code==3) return "SMALL_FELLOWSHIP";
    if (code==6) return "FELLOWSHIP";
    if (code==12) return "RAID12";
    if (code==24) return "RAID24";
    return "";
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatGroupSizesLoader(facade).doIt();
    facade.dispose();
  }
}
