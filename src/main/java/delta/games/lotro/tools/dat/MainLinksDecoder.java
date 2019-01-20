package delta.games.lotro.tools.dat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.Dump;
import delta.games.lotro.plugins.LuaParser;
import delta.games.lotro.tools.dat.utils.DatStatUtils;

/**
 * Parser for the main data as found in LotroCompanion plugin data.
 * @author DAM
 */
public class MainLinksDecoder
{
  private static final Logger LOGGER=Logger.getLogger(MainLinksDecoder.class);

  private DataFacade _facade;

  /**
   * Constructor.
   */
  public MainLinksDecoder()
  {
    _facade=new DataFacade();
  }

  /**
   * Parse/use data from the given file.
   * @param dataFile Input file.
   * @throws Exception If an error occurs.
   */
  public void doIt(File dataFile) throws Exception
  {
    LuaParser parser=new LuaParser();
    Map<String,Object> data=parser.read(dataFile);
    byte[] buffer=loadBuffer(data);
    Object legAttr=data.get("legendary");
    boolean legendary=((legAttr==null) || (Boolean.TRUE.equals(legAttr)));
    decodeBuffer(buffer,legendary);
  }

  @SuppressWarnings("unchecked")
  private byte[] loadBuffer(Map<String,Object> data)
  {
    byte[] buffer=null;
    Map<String,Double> rawData=(Map<String,Double>)data.get("rawData");
    if (rawData!=null)
    {
      int nb=rawData.size();
      buffer=new byte[nb];
      for(int i=0;i<nb;i++)
      {
        String key=(i+1)+".0";
        Double value=rawData.get(key);
        if (value!=null)
        {
          buffer[i]=value.byteValue();
        }
      }
      Dump.dump(buffer);
    }
    return buffer;
  }

  private void decodeBuffer(byte[] buffer, boolean isLegendary)
  {
    ByteArrayInputStream bis=new ByteArrayInputStream(buffer);
    int lowInstanceId=BufferUtils.readUInt32(bis);
    int highInstanceId=BufferUtils.readUInt32(bis);
    System.out.println("Instance ID: low="+lowInstanceId+", high="+highInstanceId);
    int itemId=BufferUtils.readUInt32(bis);
    System.out.println("Item ID: "+itemId);
    if (isLegendary)
    {
      decodeLegendary(bis);
    }
  }

  private void decodeLegendary(ByteArrayInputStream bis)
  {
    // Name
    int hasName=BufferUtils.readUInt8(bis);
    if (hasName==1)
    {
      String name=decodeName(bis);
      System.out.println("Name: "+name);
    }
    else
    {
      int liNameId1=BufferUtils.readUInt32(bis);
      int liNameId2=BufferUtils.readUInt32(bis);
      System.out.println("Name id1="+liNameId1+", id2="+liNameId2);
    }

    BufferUtils.skip(bis,6); // Usually 1 0 0 0 1 0
    // Title
    int titleId=BufferUtils.readUInt32(bis);
    System.out.println("Title: "+titleId); // 0 if no title

    // Legacies
    BufferUtils.skip(bis,1); // Usually 0
    int nbLegacies=BufferUtils.readUInt8(bis);
    for(int i=0;i<nbLegacies;i++)
    {
      int legacyID=BufferUtils.readUInt32(bis);
      int rank=BufferUtils.readUInt32(bis);
      loadEffect(legacyID,rank);
    }

    // Relics
    /*int fRelics=*/BufferUtils.readUInt8(bis); // 0 if relics, 1 if no relic
    int nbRelics=BufferUtils.readUInt8(bis);
    for(int i=0;i<nbRelics;i++)
    {
      int relicID=BufferUtils.readUInt32(bis);
      // 1: Setting, 2: Gem, 3: Rune ; Crafted=4
      int slot=BufferUtils.readUInt32(bis);
      System.out.println("Relic: ID="+relicID+", slot="+slot);
    }

    // Passives
    int nbPassives=BufferUtils.readUInt32(bis);
    for(int i=0;i<nbPassives;i++)
    {
      int passiveId=BufferUtils.readUInt32(bis);
      System.out.println("Passive: "+passiveId);
      loadPassive(passiveId);
    }

    int test=BufferUtils.readUInt32(bis);
    boolean imbued=false;
    if (test==268460298)
    {
      imbued=true;
    }
    else if (test==0)
    {
      imbued=false;
    }
    else
    {
      System.out.println("Expected test value: "+test);
      return;
    }
    // If non imbued
    if (!imbued)
    {
      // points spent / left (only for non imbued ones)
      int nbPointsLeft=BufferUtils.readUInt32(bis);
      int nbPointsSpent=BufferUtils.readUInt32(bis);
      System.out.println("Left: "+nbPointsLeft+" / Spent: "+nbPointsSpent);
      /*int n2=*/BufferUtils.readUInt32(bis); // Got 0, 62, 83, 189, 192...

      // Default legacy
      int defaultLegacyRank=BufferUtils.readUInt32(bis);
      int defaultLegacyID=BufferUtils.readUInt32(bis);
      if (defaultLegacyID!=0)
      {
        loadDefaultEffect(defaultLegacyID,defaultLegacyRank);
      }
    }
    else
    {
      int nbImbuedLegacies=BufferUtils.readUInt32(bis);
      System.out.println("Found "+nbImbuedLegacies+" legacies");
      for(int i=0;i<nbImbuedLegacies;i++)
      {
        System.out.println("Legacy #"+(i+1));
        int dataStructId=BufferUtils.readUInt32(bis);
        if (dataStructId!=268460297) // 268460297=ItemAdvancement_AdvanceableWidget_Data_Struct
        {
          System.out.println("Expected dataStructId=268460297, got: "+dataStructId);
          return;
        }
        /*int n3=*/BufferUtils.readUInt8(bis); // Always 0

        int legacyId=-1;
        int unlockedLevels=0;
        int legacyXp=0;
        int nbProps=BufferUtils.readUInt8(bis);
        for(int j=0;j<nbProps;j++)
        {
          int propId=BufferUtils.readUInt32(bis);
          int propId2=BufferUtils.readUInt32(bis);
          if (propId!=propId2)
          {
            System.out.println("Decoding error: propId="+propId+", propId2="+propId2);
            return;
          }
          if (propId==268460305)
          {
            // ItemAdvancement_WidgetDID
            legacyId=BufferUtils.readUInt32(bis);
          }
          else if (propId==268460306)
          {
            // ItemAdvancement_AdvanceableWidget_UnlockedLevels
            unlockedLevels=BufferUtils.readUInt32(bis);
            System.out.println("Unlocked levels: "+unlockedLevels);
          }
          else if (propId==268442976)
          {
            // ItemAdvancement_WidgetDID
            legacyXp=BufferUtils.readUInt32(bis);
            System.out.println("Legacy XP: "+legacyXp);
            BufferUtils.skip(bis,4);
          }
        }
        loadImbuedLegacy(legacyId,unlockedLevels,legacyXp);
      }
      /*
      3E  ...`...Rn......>
      0x000001b0  03 00 00 00 00 00 00 00 00 00 00 21 00 00 00 29  ...........!...)
      0x000001c0  3A 04 70 [EF 10 00 10 00 03 C5 12 00 10 C5 12 00  :.p.............
           */
      BufferUtils.skip(bis,20);
    }
    int marker=BufferUtils.readUInt32(bis); // Expected 0x100010EF
    if (marker!=0x100010EF)
    {
      System.out.println("Bad marker: "+marker);
    }

    /*int padding=*/BufferUtils.readUInt8(bis);
    int nbSubs=BufferUtils.readUInt8(bis);
    System.out.println("Nb subs: "+nbSubs);
    for(int i=0;i<nbSubs;i++)
    {
      /*
      if (i>0)
      {
        for(int k=0;k<5;k++)
        {
          int header=BufferUtils.readUInt32(bis);
          System.out.println("Next headers: "+header);
        }
      }
      */
      int header=BufferUtils.readUInt32(bis);
      int header2=BufferUtils.readUInt32(bis);
      if (header!=header2)
      {
        System.out.println("Decoding error: header="+header+", header2="+header2);
        return;
      }
      if (header==0x100012C5)
      {
        int nbExtras=BufferUtils.readUInt32(bis);
        System.out.println("Extras: "+nbExtras);
        for(int j=0;j<nbExtras;j++)
        {
          int subHeader=BufferUtils.readUInt32(bis);
          if(subHeader==0x34E)
          {
            // Container slot
            int bitsSet=BufferUtils.readUInt32(bis);
            System.out.println("Container slots: "+bitsSet);
          }
          else if(subHeader==0x10000E20)
          {
            // Crafted by
            BufferUtils.readUInt8(bis);
            String crafter=decodeName(bis);
            System.out.println("Crafter: "+crafter);
            BufferUtils.skip(bis,6);
          }
          else if (subHeader==0x10000884)
          {
            // Item name
            BufferUtils.readUInt8(bis);
            String crafter=decodeName(bis);
            System.out.println("Item name: "+crafter);
            BufferUtils.skip(bis,6);
          }
          else if (subHeader==0x10000AC1)
          {
            // Bound to
            int boundToLowId=BufferUtils.readUInt32(bis);
            int boundToHighId=BufferUtils.readUInt32(bis);
            System.out.println("Bound to high: "+boundToHighId+", low: "+boundToLowId);
          }
          else if (subHeader==0x10000AC2)
          {
            // Binds on acquire
            int boA=BufferUtils.readUInt8(bis);
            System.out.println("Bind on acquire: "+boA);
          }
          else if (subHeader==0x10000E7B)
          {
            // Quantity
            int quantity=BufferUtils.readUInt32(bis);
            System.out.println("Quantity: "+quantity);
          }
          else if (subHeader==0x100031A4)
          {
            // Default legacy rank
            int defaultLegacyRank=BufferUtils.readUInt32(bis);
            System.out.println("Default legacy rank: "+defaultLegacyRank);
          }
          else if (subHeader==0x10001D5F)
          {
            // LI level (for a non imbued item. Max 60 or 70)
            int liLevel=BufferUtils.readUInt32(bis);
            System.out.println("LI level: "+liLevel);
          }
          else if (subHeader==0x100000C4)
          {
            // Item level (for a non imbued item. Max 60 or 70)
            int itemLevel=BufferUtils.readUInt32(bis);
            System.out.println("Item level: "+itemLevel);
          }
          else if (subHeader==0x1000132C)
          {
            // Current item durability
            int durability=BufferUtils.readUInt32(bis);
            System.out.println("Durability: "+durability);
          }
          else if (subHeader==0x100060B5)
          {
            // ItemAdvancement_Imbued
            int iaImbued=BufferUtils.readUInt8(bis);
            System.out.println("Imbued: "+iaImbued);
          }
          else if (subHeader==0x10001042)
          {
            // Combat_Damage (Max Damage)
            float damage=BufferUtils.readFloat(bis);
            System.out.println("Max Damage: "+damage);
          }
          else if (subHeader==0x10000835)
          {
            // Item worth
            int itemWorth=BufferUtils.readUInt32(bis);
            System.out.println("Item worth: "+itemWorth);
          }
          else if (subHeader==0x10000669)
          {
            // True level
            int itemTrueLevel=BufferUtils.readUInt32(bis);
            System.out.println("Item true level: "+itemTrueLevel);
          }
          else if (subHeader==0x10004996)
          {
            // Upgrades (crystals)
            int itemUpgrades=BufferUtils.readUInt32(bis);
            System.out.println("Item upgrades: "+itemUpgrades);
          }
          else
          {
            System.out.println("Unmanaged header: "+subHeader);
          }
        }
      }
      else if (header==0x10000421)
      {
        // Item ID, reloaded
        int itemId=BufferUtils.readUInt32(bis);
        System.out.println("Item ID: "+itemId);
      }
      else if (header==0x10002897)
      {
        // Instance ID, reloaded
        int lowInstanceId=BufferUtils.readUInt32(bis);
        int highInstanceId=BufferUtils.readUInt32(bis);
        System.out.println("Instance ID: low="+lowInstanceId+", high="+highInstanceId);
      }
    }
    // Properties
    /*
        elseif header == 0x100026BC then
          -- legacies, we have them already
          local nLegs = ins:GetLongLE();
          ins:Consume( 8 * nLegs );
        elseif header == 0x100038A7 then -- Binds  to account ?
          result.itemBindToAccount = ins:Get();
        elseif header == 0x0000034E then -- storage info
          result.itemStorageInfo = ins:GetLongLE();
          write("result.itemStorageInfo: "..result.itemStorageInfo);
        elseif header == 0x10001042 then -- LI 2nd range of DPS
          result.liMaxHit = ins:GetLongLE();
          write("result.liMaxHit: "..result.liMaxHit);
        elseif header == 0x10000ACD then -- dye info
          result.itemDye = ins:GetLongLE();
          write("result.itemDye: "..result.itemDye);
        elseif header == 0x10000570 then -- armour
          result.itemArmour = ins:GetLongLE();
          write("result.itemArmour: "..result.itemArmour);
        elseif header == 0x10005F0E then
          local nbSlots = ins:GetLongLE(); -- ?
          write("nbSlots: "..nbSlots);
          for i = 1, nbSlots do
            local subheader = ins:GetLongLE();
            write("subheader "..i.." - "..subheader);
            if header == 0x10005F0E then
              for i = 1, 20 do
                local value = ins:Get();
                write("value #"..i..": "..value);
              end
            end
          end
        end
      end
  end
     */
  }

  private void loadDefaultEffect(int effectId, int rank)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+0x9000000);
    StatsProvider provider=DatStatUtils.buildStatProviders(_facade,effectProps);
    System.out.println(provider.getStats(1,rank));
  }

  private void loadImbuedLegacy(int legacyId, int unlockedLevels, int legacyXp)
  {
    PropertiesSet legacyProps=_facade.loadProperties(legacyId+0x9000000);
    if (legacyProps!=null)
    {
      int maxLevel=((Integer)legacyProps.getProperty("ItemAdvancement_AdvanceableWidget_AbsoluteMaxLevel")).intValue();
      //int category=((Integer)effectProps.getProperty("ItemAdvancement_AdvanceableWidget_Category")).intValue();
      int initialMaxLevel=((Integer)legacyProps.getProperty("ItemAdvancement_AdvanceableWidget_InitialMaxLevel")).intValue();
      //int iconId=((Integer)effectProps.getProperty("ItemAdvancement_AdvanceableWidget_Icon")).intValue();
      //int smallIconId=((Integer)effectProps.getProperty("ItemAdvancement_AdvanceableWidget_SmallIcon")).intValue();
      int levelTable=((Integer)legacyProps.getProperty("ItemAdvancement_AdvanceableWidget_LevelTable")).intValue();
      int currentMaxTiers=initialMaxLevel+unlockedLevels;
      System.out.println("Legacy ID="+legacyId+", initMaxLevel="+initialMaxLevel+", currentMaxLevel="+currentMaxTiers+", maxLevel="+maxLevel);
      System.out.println("Level table: "+levelTable);
      int tiers=getTierFromXp(legacyXp);
      Integer effectId=(Integer)legacyProps.getProperty("ItemAdvancement_ImbuedLegacy_Effect");
      if (effectId!=null)
      {
        loadEffect(effectId.intValue(),tiers);
      }
      Integer dpsLut=(Integer)legacyProps.getProperty("ItemAdvancement_AdvanceableWidget_DPSLUT");
      if (dpsLut!=null)
      {
        float dps=loadDps(dpsLut.intValue(),tiers);
        System.out.println("DPS: "+dps);
      }
      
    }
  }

  private int getTierFromXp(int xp)
  {
    int tier=(xp/30000)+((xp%30000!=0)?1:0);
    return tier;
  }

  private void loadEffect(int effectId, int rank)
  {
    System.out.println("Effect ID="+effectId+", rank="+rank);
    PropertiesSet effectProps=_facade.loadProperties(effectId+0x9000000);
    //System.out.println(effectProps.dump());
    StatsProvider provider=DatStatUtils.buildStatProviders(_facade,effectProps);
    System.out.println(provider.getStats(1,rank));
  }

  private void loadPassive(int passiveId)
  {
    PropertiesSet effectProps=_facade.loadProperties(passiveId+0x9000000);
    StatsProvider provider=DatStatUtils.buildStatProviders(_facade,effectProps);
    int itemLevel=236;
    System.out.println(provider.getStats(1,itemLevel));
  }

  private float loadDps(int dpsLut, int tier)
  {
    PropertiesSet dpsLutProperties=_facade.loadProperties(dpsLut+0x9000000);
    Object[] dpsArray=(Object[])dpsLutProperties.getProperty("Combat_BaseDPSArray");
    float baseDPSFromTable=((Float)(dpsArray[tier-1])).floatValue();
    return baseDPSFromTable;
  }

  private String decodeName(ByteArrayInputStream bis)
  {
    return BufferUtils.readPrefixedUtf16String(bis);
  }

  private static void doFile(File dataFile)
  {
    try
    {
      MainLinksDecoder parser=new MainLinksDecoder();
      parser.doIt(dataFile);
    }
    catch(Exception e)
    {
      LOGGER.error("Error when loading link from file "+dataFile, e);
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    File linksDir=new File("D:\\shared\\damien\\dev\\lotrocompanion\\lua\\links");
    // Ethell
    File ethell=new File(linksDir,"ethell");
    doFile(new File(ethell,"weapon.txt")); // Imbued
    doFile(new File(ethell,"emblem.txt")); // Non imbued
    // Lorewyne
    File lorewyne=new File(linksDir,"lorewyne");
    doFile(new File(lorewyne,"legendary book.plugindata")); // Imbued
    // Tilmo
    File tilmo=new File(linksDir,"tilmo");
    doFile(new File(tilmo,"club.txt")); // Imbued
    // Glumlug
    File glumlug=new File(linksDir,"glumlug");
    doFile(new File(glumlug,"katioucha5.txt")); // Imbued
    doFile(new File(glumlug,"axe.txt")); // Non imbued
    doFile(new File(glumlug,"oldbow.txt")); // Non imbued
    doFile(new File(glumlug,"3rdage axe.txt")); // Non imbued
    // Kargarth
    File kargarth=new File(linksDir,"kargarth");
    doFile(new File(kargarth,"belt.txt")); // Non imbued
    // Utharr
    File utharr=new File(linksDir,"utharr");
    doFile(new File(utharr,"satchel.txt")); // Non imbued
    // Beleganth
    File beleganth=new File(linksDir,"beleganth");
    doFile(new File(beleganth,"weapon.txt")); // Non imbued
  }
}
