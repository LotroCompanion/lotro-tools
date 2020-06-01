package delta.games.lotro.tools.dat.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
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

  /*
  private static int[] IDS= {1879049908,1879059895,1879060264,1879084796,1879084798,1879084800,1879085263,1879085275,1879085288,1879085289,1879102770,
      1879105669,1879105702,1879105719,1879105751,1879105764,1879105766,1879105808,1879105875,1879105891,1879105894,1879105901,1879105921,1879106009,
      1879106037,1879106052,1879106072,1879106073,1879106093,1879106104,1879106124,1879106127,1879106147,1879106213,1879106249,1879119370,1879136185,
      1879151960,1879156819,1879157300,1879189153,1879189154,1879189155,1879189156,1879189157,1879192023,1879211576,1879211633,1879212135,1879220100,
      1879222091,1879222092,1879224296,1879232939,1879239108,1879267320,1879325961,1879381487};
      */

  private List<Integer> ids=new ArrayList<Integer>();

  private void handleEntry(int id)
  {
    if (id%1000000==0) System.out.println("ID: "+id);
    byte[] data=_facade.loadData(id);
    if ((data!=null) && (data.length>=8))
    {
      int type=getType(data);
      Integer typeKey=Integer.valueOf(type);
      // Select
      /*
      if ((type!=WStateClass.CLOTHING) && (type!=WStateClass.CHISEL) && (type!=WStateClass.SHIELD) && (type!=WStateClass.WEAPON) && (type!=WStateClass.JEWEL) &&
          (type!=WStateClass.PROGRESSION) && (type!=WStateClass.PROGRESSION_ARRAY) && (type!=WStateClass.FLOAT_PROGRESSION_ARRAY) && 
          (type!=WStateClass.QUEST) && (type!=WStateClass.RELIC) &&
          //(type!=SKILL) && (type!=TRAIT) && (type!=STEED_TRAIT) &&
          //(type!=EFFECT) && (type!=EFFECT2) && (type!=EFFECT3) && (type!=EFFECT4) && (type!=EFFECT5) &&
          (type!=WStateClass.VIRTUE) && (type!=WStateClass.PROPERTY_METADATA) && (type!=WStateClass.PROPERTY_METADATA_LIST) &&
          (type!=WStateClass.QUEST_TREASURE) && (type!=WStateClass.LOOT) && (type!=WStateClass.LOOT2) && (type!=WStateClass.SKIRMISH_LOOT) &&
          (type!=WStateClass.SET) && (type!=WStateClass.IA_LEGACY) && (type!=WStateClass.IA_EFFECT) &&
          (type!=WStateClass.WEB_STORE_ITEM))
          */
      {
        int index=findBuffer(data,_toSearch);
        if (index!=-1)
        {
          // Count by type
          IntegerHolder holder=_countByType.get(typeKey);
          if (holder==null)
          {
            holder=new IntegerHolder();
            _countByType.put(typeKey,holder);
          }
          holder.increment();
          System.out.println("Found "+id+" index="+index+", type="+type);
          int propsId=(id<0x78FFFFFF)?id+0x9000000:id;
          PropertiesSet props=_facade.loadProperties(propsId);
          System.out.println("*********** entry "+id+"******************");
          if (props!=null)
          {
            System.out.println(props.dump());
          }
          else
          {
            System.out.println("props is null");
          }
          ids.add(Integer.valueOf(id));
          /*
          Progression prog=ProgressionFactory.buildProgression(id,props);
          if (prog!=null)
          {
            Float value=prog.getValue(120);
            if ((value!=null) && (Math.abs(value.floatValue()-3)<0.1))
            {
              System.out.println(props.dump());
            }
          }
          */
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
    for(int id=0x70000000;id<0x7FFFFFFF;id++)
    {
      handleEntry(id);
    }
  }

  private void doIt()
  {
    //searchId(268435801); // Vital_PowerCombatRegenAddMod
    //searchId(268438584); // Vital_HealthCombatRegenAddMod
    //searchId(268439070); // Vital_HealthCombatBaseRegen
    //searchId(268445007); // Trait_MP_Virtue_Core_Rank_Health_Regen
    //searchId(268437690); // Health_RegenRate
    //searchId(268435576); // Vital_HealthCombatCurrentRegen
    //searchId(1879141759); // Effect for IA_Minstrel_CalltoFate_CriticalMagnitude
    //searchId(268444877); // IA_Minstrel_CalltoFate_CriticalMagnitude
    //searchId(1879112405); // Item Boots of the Lady's Discernment
    //searchId(268438673); // Property: Item_QualityModLevel
    //searchId(268458123); // Property: LevelScalingControl_Quality_To_ItemLevelProgression_Hash
    //searchId(268457979); // Property: Examination_ItemLevel_Struct
    // Deed "Enmity of the Dourhands" for high-elves
    //searchId(1879346407);
    // A table it is found in...
    //searchId(1879346401);
    // Anfalas Star-lit Crystal
    //searchId(1879313853);
    // Property: 268438022 Version_VersionNumberDataList
    //searchId(268438022);

    // Effect: 'In Defence of Middle-earth'
    //searchId(1879053032);
    // Effect: 'DNT - In Defence of Middle-earth'
    //searchId(1879220578);
    // Effect: 'DNT - Improved Motivating Speech'
    //1879279091
    // Skill: 'In Defence of Middle-earth'
    //searchId(1879053069); // 0x7000130D
    // Found in "Version_UntrainSkills_DataArray" (1879287393)
    // searchId(1879287393);
    // Found in "2030043677": Version_VersionNumberDataList #39 / Version_Number: 1203

    //searchId(0x411BC61B); // = 1092339227 a interior map
    // Found in 2013290723 // 0x780060E3
    // searchId(0x780060E3);
    // Found in 0x7904BA81
    //searchId(0x7904BA81);

    // Another one: 4119330B.jpg
    // 4115B5CC.jpg a landscape
    // 4116B614.jpg Middle Earth map

    //searchId(0x410D9A54); // 1091410516 Map of the Bree-land Homesteads
    //searchId(0x410E8708); // 1091471112 Map of Moria, found in 1092239022 = 411A3EAE

    //searchId(268435789);
    searchId(268444327);

    /*
    for(int id: IDS)
    {
      searchId(id);
    }
    */
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
