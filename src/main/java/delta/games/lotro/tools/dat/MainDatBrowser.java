package delta.games.lotro.tools.dat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.KPM;

/**
 * @author DAM
 */
public class MainDatBrowser
{
  private DataFacade _facade;
  private Map<Integer,IntegerHolder> _countByType;
  private byte[] _toSearch;

  private byte[] intToByteArray(int value)
  {
    byte[] ret=new byte[4];
    ret[0]=(byte)(value&0xff);
    value>>=8;
    ret[1]=(byte)(value&0xff);
    value>>=8;
    ret[2]=(byte)(value&0xff);
    value>>=8;
    ret[3]=(byte)(value&0xff);
    return ret;
  }

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatBrowser(DataFacade facade)
  {
    _facade=facade;
    _countByType=new HashMap<Integer,IntegerHolder>();
  }

  private static int[] IDS= {1879049908,1879059895,1879060264,1879084796,1879084798,1879084800,1879085263,1879085275,1879085288,1879085289,1879102770,
      1879105669,1879105702,1879105719,1879105751,1879105764,1879105766,1879105808,1879105875,1879105891,1879105894,1879105901,1879105921,1879106009,
      1879106037,1879106052,1879106072,1879106073,1879106093,1879106104,1879106124,1879106127,1879106147,1879106213,1879106249,1879119370,1879136185,
      1879151960,1879156819,1879157300,1879189153,1879189154,1879189155,1879189156,1879189157,1879192023,1879211576,1879211633,1879212135,1879220100,
      1879222091,1879222092,1879224296,1879232939,1879239108,1879267320,1879325961,1879381487};

  private List<Integer> ids=new ArrayList<Integer>();

  private void handleEntry(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int type=getType(data);
      Integer typeKey=Integer.valueOf(type);
      // Count by type
      IntegerHolder holder=_countByType.get(typeKey);
      if (holder==null)
      {
        holder=new IntegerHolder();
        _countByType.put(typeKey,holder);
      }
      holder.increment();
      // Select
      if ((type!=WStateClass.CLOTHING) && (type!=WStateClass.CHISEL) && (type!=WStateClass.SHIELD) && (type!=WStateClass.WEAPON) && (type!=WStateClass.JEWEL) &&
          (type!=WStateClass.PROGRESSION) && (type!=WStateClass.PROGRESSION_ARRAY) && (type!=WStateClass.FLOAT_PROGRESSION_ARRAY) && 
          (type!=WStateClass.QUEST) && (type!=WStateClass.RELIC) &&
          //(type!=SKILL) && (type!=TRAIT) && (type!=STEED_TRAIT) &&
          //(type!=EFFECT) && (type!=EFFECT2) && (type!=EFFECT3) && (type!=EFFECT4) && (type!=EFFECT5) &&
          (type!=WStateClass.VIRTUE) && (type!=WStateClass.PROPERTY_METADATA) && (type!=WStateClass.PROPERTY_METADATA_LIST) &&
          (type!=WStateClass.QUEST_TREASURE) && (type!=WStateClass.LOOT) && (type!=WStateClass.LOOT2) && (type!=WStateClass.SKIRMISH_LOOT) &&
          (type!=WStateClass.SET) && (type!=WStateClass.IA_LEGACY) && (type!=WStateClass.IA_EFFECT) &&
          (type!=WStateClass.WEB_STORE_ITEM))
      {
        int index=findBuffer(data,_toSearch);
        if (index!=-1)
        {
          System.out.println("Found "+id+" index="+index+", type="+type);
          PropertiesSet props=_facade.loadProperties(id+0x9000000);
          System.out.println("*********** entry "+id+"******************");
          System.out.println(props.dump());
          ids.add(Integer.valueOf(id));
        }
      }
    }
  }

  private int findBuffer(byte[] buffer, byte[] toFind)
  {
    return KPM.indexOf(buffer,toFind);
  }

  private int getType(byte[] data)
  {
    int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
    return classDefIndex;
  }

  private void searchId(int idToSearch) {
    int propId=idToSearch;
    //int propId=268435801;
    //int propId=1879085275;
    _toSearch=intToByteArray(propId);
    int revert=BufferUtils.readUInt32(new ByteArrayInputStream(_toSearch));
    if (revert!=propId)
    {
      System.out.println("Bad int->byte[] conversion!");
    }
    System.out.println("************** searching id=" + idToSearch + "******************");
    // Iterate on wstates
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      handleEntry(id);
    }
  }

  private void doIt()
  {
    for(int id: IDS)
    {
      searchId(id);
    }
    System.out.println(_countByType);
    System.out.println(ids);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatBrowser(facade).doIt();
    facade.dispose();
  }
}
