package delta.games.lotro.tools.reports;

import java.util.List;

import delta.common.utils.text.EndOfLine;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.AbstractMapper;
import delta.games.lotro.dat.data.enums.DIDMapper;
import delta.games.lotro.dat.data.enums.MapperUtils;
import delta.games.lotro.dat.data.strings.StringInfoUtils;
import delta.games.lotro.dat.data.strings.StringTable;
import delta.games.lotro.dat.data.strings.StringTableEntry;
import delta.games.lotro.dat.loaders.DataIdMapLoader;
import delta.games.lotro.dat.utils.hash.KnownVariablesManager;

/**
 * Tool for DAT inspection.
 * @author DAM
 */
public class DATInspectionTool
{
  private static void doIt()
  {
    DataFacade facade=new DataFacade();
    loadAllDbProperties(facade);
    loadAllDbPropertiesFromWeenie(facade);
    loadStringTables(facade);
    loadAllStringTables(facade);
    KnownVariablesManager.getInstance().showFailures();
    facade.dispose();
  }

  /**
   * Load all enums and dump them into the provided StringBuilder.
   * @param facade Data access facade.
   * @param sb Storage.
   */
  public static void loadEnumsRegistry(DataFacade facade, StringBuilder sb)
  {
    byte[] data=facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer id=map.getDataIdForLabel("EMAPPER");
    if (id!=null)
    {
      data=facade.loadData(id.intValue());
      map=DataIdMapLoader.decodeDataIdMap(data);
      List<String> enumNames=map.getLabels();
      for(String enumName : enumNames)
      {
        Integer enumId=map.getDataIdForLabel(enumName);
        if ((enumId==null) || (enumId.intValue()==-1)) continue;
        sb.append("Enum: ").append(enumName).append(", (id=").append(enumId).append(")").append(EndOfLine.NATIVE_EOL);
        AbstractMapper mapper=MapperUtils.getEnum(facade,enumId.intValue());
        if (mapper==null)
        {
          sb.append("*** enum not found: "+enumName+" ***").append(EndOfLine.NATIVE_EOL);
          continue;
        }
        sb.append(mapper.dump()).append(EndOfLine.NATIVE_EOL);
      }
    }
  }

  static void loadAllDbProperties(DataFacade facade)
  {
    byte[] data=facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    for(String label : map.getLabels())
    {
      Integer dataId=map.getDataIdForLabel(label);
      System.out.println(label+" ("+dataId+")");
      if (dataId!=null)
      {
        data=facade.loadData(dataId.intValue());
        if (data==null) continue;
        DIDMapper subMap=DataIdMapLoader.decodeDataIdMap(data);
        System.out.println(subMap.dump());
      }
    }
  }

  static void loadAllDbPropertiesFromWeenie(DataFacade facade)
  {
    byte[] data=facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer dataId=map.getDataIdForLabel("WEENIECONTENT");
    if (dataId!=null)
    {
      data=facade.loadData(dataId.intValue());
      DIDMapper subMap=DataIdMapLoader.decodeDataIdMap(data);
      List<String> subLabels=subMap.getLabels();
      for(String subLabel : subLabels)
      {
        if (subLabel.startsWith("\u0000\u0000")) continue;
        int subDataId=subMap.getDataIdForLabel(subLabel).intValue();
        long dbPropsId=subDataId+DATConstants.DBPROPERTIES_OFFSET;
        System.out.println("****** Weenie: "+subLabel+" - "+dbPropsId);
        PropertiesSet props=facade.loadProperties(dbPropsId);
        if (props!=null)
        {
          System.out.println(props.dump());
        }
      }
    }
  }

  static void loadStringTables(DataFacade facade)
  {
    byte[] data=facade.loadData(0x28000000);
    DIDMapper map=DataIdMapLoader.decodeDataIdMap(data);
    Integer id=map.getDataIdForLabel("STRINGTABLE");
    if (id!=null)
    {
      data=facade.loadData(id.intValue());
      DIDMapper map2=DataIdMapLoader.decodeDataIdMap(data);
      List<String> enumNames=map2.getLabels();
      for(String enumName : enumNames)
      {
        int stringTableId=map2.getDataIdForLabel(enumName).intValue();
        System.out.println(enumName+" - "+stringTableId);
        StringTable table=facade.getStringsManager().getStringTable(stringTableId);
        if (table!=null)
        {
          for(Integer token : table.getTokens())
          {
            StringTableEntry entry=table.getEntry(token.intValue());
            handleEntry(stringTableId,token.intValue(),entry);
          }
        }
        else
        {
          System.out.println("\tNot found!");
        }
      }
    }
  }

  static void loadAllStringTables(DataFacade facade)
  {
    for(int stringTableId=0x25000000;stringTableId<=0x26ffffff;stringTableId++)
    {
      StringTable table=facade.getStringsManager().getStringTable(stringTableId);
      if (table!=null)
      {
        System.out.println("String Table ID: "+stringTableId);
        for(Integer token : table.getTokens())
        {
          StringTableEntry entry=table.getEntry(token.intValue());
          handleEntry(stringTableId,token.intValue(),entry);
        }
      }
    }
  }

  private static void handleEntry(int tableId, int token, StringTableEntry entry)
  {
    String stringFormat=StringInfoUtils.buildStringFormat(entry);
    System.out.println("\t"+token+": "+stringFormat);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    doIt();
  }
}
