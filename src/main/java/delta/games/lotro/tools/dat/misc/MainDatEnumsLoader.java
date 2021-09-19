package delta.games.lotro.tools.dat.misc;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.Difficulty;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.GroupSize;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumEntry;
import delta.games.lotro.common.enums.MobType;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.enums.io.xml.EnumXMLWriter;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.utils.StringUtils;

/**
 * Get difficulties from DAT files.
 * @author DAM
 */
public class MainDatEnumsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatEnumsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEnumsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Load difficulties.
   */
  public void doIt()
  {
    loadEnum(587203292,"SkirmishDifficultyTier",Difficulty.class);
    loadEnum(587203290,"SkirmishGroupSize",GroupSize.class);
    loadEnum(587202570,"GenusType",Genus.class);
    loadEnum(587202571,"Agent_Species",Species.class);
    loadEnum(587202572,"SubspeciesType",SubSpecies.class);
    loadEnum(587202672,"ExaminationModStatType",MobType.class);
  }

  private <T extends LotroEnumEntry> void loadEnum(int enumId, String name, Class<T> implClass)
  {
    LotroEnum<T> lotroEnum=new LotroEnum<T>(enumId,name,implClass);
    EnumMapper enumMapper=_facade.getEnumsManager().getEnumMapper(enumId);
    if (enumMapper!=null)
    {
      for(Integer code : enumMapper.getTokens())
      {
        if (code.intValue()==0)
        {
          continue;
        }
        String label=enumMapper.getLabel(code.intValue());
        label=StringUtils.fixName(label);
        String key=getKey(implClass,code.intValue());
        T entry=lotroEnum.buildEntryInstance(code.intValue(),key,label);
        lotroEnum.registerEntry(entry);
      }
      File enumsDir=GeneratedFiles.ENUMS_DIR;
      String fileName=implClass.getSimpleName()+".xml";
      File enumFile=new File(enumsDir,fileName);
      new EnumXMLWriter<T>().writeEnum(enumFile,lotroEnum);
    }
    else
    {
      LOGGER.warn("Could not load difficulties enum");
    }
  }

  private String getKey(Class<? extends LotroEnumEntry> implClass, int code)
  {
    if (implClass==GroupSize.class)
    {
      return getGroupSizeKey(code);
    }
    return null;
  }

  private String getGroupSizeKey(int code)
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
    new MainDatEnumsLoader(facade).doIt();
    facade.dispose();
  }
}
