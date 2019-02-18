package delta.games.lotro.tools.dat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Effect;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.Dump;
import delta.games.lotro.lore.items.legendary.LegaciesManager;
import delta.games.lotro.lore.items.legendary.LegendaryAttrs;
import delta.games.lotro.lore.items.legendary.LegendaryTitle;
import delta.games.lotro.lore.items.legendary.LegendaryTitlesManager;
import delta.games.lotro.lore.items.legendary.PassivesManager;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacy;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.imbued.ImbuedLegendaryAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacy;
import delta.games.lotro.lore.items.legendary.non_imbued.DefaultNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegaciesManager;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegacyTier;
import delta.games.lotro.lore.items.legendary.non_imbued.NonImbuedLegendaryAttrs;
import delta.games.lotro.lore.items.legendary.non_imbued.TieredNonImbuedLegacyInstance;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.plugins.LuaParser;

/**
 * Parser for the main data as found in LotroCompanion plugin data.
 * @author DAM
 */
public class MainLinksDecoder
{
  private static final Logger LOGGER=Logger.getLogger(MainLinksDecoder.class);

  /**
   * Constructor.
   */
  public MainLinksDecoder()
  {
    // Nothing
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
      LegendaryAttrs legAttrs=decodeLegendary(bis);
      System.out.println(legAttrs.dump());
    }
    else
    {
      decodeNonLegendary(bis);
    }
  }

  private LegendaryAttrs decodeLegendary(ByteArrayInputStream bis)
  {
    LegendaryAttrs ret=new LegendaryAttrs();
    // Name
    int hasName=BufferUtils.readUInt8(bis);
    if (hasName==1)
    {
      String name=decodeName(bis);
      System.out.println("Name: "+name);
      ret.setLegendaryName(name);
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
    //System.out.println("Title: "+titleId); // 0 if no title
    if (titleId!=0)
    {
      LegendaryTitlesManager legendaryTitlesMgr=LegendaryTitlesManager.getInstance();
      LegendaryTitle legendaryTitle=legendaryTitlesMgr.getLegendaryTitle(titleId);
      ret.setTitle(legendaryTitle);
    }

    // Legacies
    NonImbuedLegaciesManager nonImbuedMgr=NonImbuedLegaciesManager.getInstance();
    BufferUtils.skip(bis,1); // Usually 0
    int nbLegacies=BufferUtils.readUInt8(bis);
    for(int i=0;i<nbLegacies;i++)
    {
      int legacyID=BufferUtils.readUInt32(bis);
      int rank=BufferUtils.readUInt32(bis);
      NonImbuedLegacyTier legacyTier=nonImbuedMgr.getLegacyTier(legacyID);
      TieredNonImbuedLegacyInstance legacyInstance=new TieredNonImbuedLegacyInstance();
      legacyInstance.setLegacyTier(legacyTier);
      legacyInstance.setRank(rank);
      ret.getNonImbuedAttrs().addLegacy(legacyInstance);
    }

    // Relics
    /*int fRelics=*/BufferUtils.readUInt8(bis); // 0 if relics, 1 if no relic
    int nbRelics=BufferUtils.readUInt8(bis);
    if (nbRelics>0)
    {
      RelicsManager relicsMgr=RelicsManager.getInstance();
      for(int i=0;i<nbRelics;i++)
      {
        int relicID=BufferUtils.readUInt32(bis);
        // 1: Setting, 2: Gem, 3: Rune ; Crafted=4
        int slot=BufferUtils.readUInt32(bis);
        //System.out.println("Relic: ID="+relicID+", slot="+slot);
        Relic relic=relicsMgr.getById(relicID);
        if (relic!=null)
        {
          if (slot==1) ret.setSetting(relic);
          else if (slot==2) ret.setGem(relic);
          else if (slot==3) ret.setRune(relic);
          else if (slot==4) ret.setCraftedRelic(relic);
        }
      }
    }

    // Passives
    PassivesManager passivesMgr=PassivesManager.getInstance();
    int nbPassives=BufferUtils.readUInt32(bis);
    for(int i=0;i<nbPassives;i++)
    {
      int passiveId=BufferUtils.readUInt32(bis);
      //System.out.println("Passive: "+passiveId);
      Effect passive=passivesMgr.getEffect(passiveId);
      ret.addPassive(passive);
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
      return null;
    }
    // If non imbued
    if (!imbued)
    {
      NonImbuedLegendaryAttrs nonImbuedAttrs=ret.getNonImbuedAttrs();
      // points spent / left (only for non imbued ones)
      int nbPointsLeft=BufferUtils.readUInt32(bis);
      nonImbuedAttrs.setPointsLeft(nbPointsLeft);
      int nbPointsSpent=BufferUtils.readUInt32(bis);
      nonImbuedAttrs.setPointsSpent(nbPointsSpent);
      //System.out.println("Left: "+nbPointsLeft+" / Spent: "+nbPointsSpent);

      /*int n2=*/BufferUtils.readUInt32(bis); // Got 0, 62, 83, 189, 192...

      // Default legacy
      int defaultLegacyRank=BufferUtils.readUInt32(bis);
      int defaultLegacyID=BufferUtils.readUInt32(bis);
      if (defaultLegacyID!=0)
      {
        DefaultNonImbuedLegacy legacy=nonImbuedMgr.getDefaultLegacy(defaultLegacyID);
        DefaultNonImbuedLegacyInstance defaultLegacy=new DefaultNonImbuedLegacyInstance();
        defaultLegacy.setLegacy(legacy);
        defaultLegacy.setRank(defaultLegacyRank);
        nonImbuedAttrs.setDefaultLegacy(defaultLegacy);
      }
    }
    else
    {
      ImbuedLegendaryAttrs imbuedAttrs=new ImbuedLegendaryAttrs();
      ret.setImbuedAttrs(imbuedAttrs);

      int nbImbuedLegacies=BufferUtils.readUInt32(bis);
      System.out.println("Found "+nbImbuedLegacies+" legacies");
      for(int i=0;i<nbImbuedLegacies;i++)
      {
        //System.out.println("Legacy #"+(i+1));
        int dataStructId=BufferUtils.readUInt32(bis);
        if (dataStructId!=268460297) // 268460297=ItemAdvancement_AdvanceableWidget_Data_Struct
        {
          System.out.println("Expected dataStructId=268460297, got: "+dataStructId);
          return null;
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
            return null;
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
            //System.out.println("Unlocked levels: "+unlockedLevels);
          }
          else if (propId==268442976)
          {
            // ItemAdvancement_EarnedXP
            legacyXp=BufferUtils.readUInt32(bis);
            //System.out.println("Legacy XP: "+legacyXp);
            BufferUtils.skip(bis,4);
          }
        }
        ImbuedLegacyInstance legacy=loadImbuedLegacy(legacyId);
        if (legacy!=null)
        {
          legacy.setUnlockedLevels(unlockedLevels);
          legacy.setXp(legacyXp);
          imbuedAttrs.addLegacy(legacy);
        }
      }
      /*
      3E  ...`...Rn......>
      0x000001b0  03 00 00 00 00 00 00 00 00 00 00 21 00 00 00 29  ...........!...)
      0x000001c0  3A 04 70 [EF 10 00 10 00 03 C5 12 00 10 C5 12 00  :.p.............
           */
    // 29 3A 04 70 is an effect that gives Tactical HPS
      BufferUtils.skip(bis,20);
    }
    int marker=BufferUtils.readUInt32(bis); // Expected 0x100010EF
    if (marker!=0x100010EF)
    {
      System.out.println("Bad marker: "+marker);
    }

    decodeShared(bis);
    return ret;
  }

  private void decodeNonLegendary(ByteArrayInputStream bis)
  {
    decodeShared(bis);
  }

  private void decodeShared(ByteArrayInputStream bis)
  {
    /*int padding=*/BufferUtils.readUInt8(bis);
    int nbSubs=BufferUtils.readUInt8(bis);
    System.out.println("Nb subs: "+nbSubs);
    for(int i=0;i<nbSubs;i++)
    {
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
            // Crafted Item Inscription
            BufferUtils.readUInt8(bis);
            String inscription=decodeName(bis);
            System.out.println("Crafted item inscription: "+inscription);
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
            // Usage min level
            int itemLevel=BufferUtils.readUInt32(bis);
            System.out.println("Usage min level: "+itemLevel);
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
            // Item value
            int itemValue=BufferUtils.readUInt32(bis);
            System.out.println("Item value: "+itemValue);
          }
          else if (subHeader==0x10000669)
          {
            // Item level
            int itemLevel=BufferUtils.readUInt32(bis);
            System.out.println("Item level: "+itemLevel);
          }
          else if (subHeader==0x10004996)
          {
            // Upgrades (crystals)
            int itemUpgrades=BufferUtils.readUInt32(bis);
            System.out.println("Item upgrades: "+itemUpgrades);
          }
          else if (subHeader==0x10005F0E) // 268459790 - Item_Socket_Gem_Array
          {
            decodeEssences(bis);
          }
          else if (subHeader==0x10000ACD) // 268438221 - Item_ClothingColor
          {
            // Indigo: 0.15; Umber: 0.5/3F000000 ; Orange: 0.75/3F400000
            float dye=BufferUtils.readFloat(bis);
            //int dye=BufferUtils.readUInt32(bis);
            System.out.println("Dye: "+dye);
          }
          else if (subHeader==0x10000570) // 268436848 - Item_Armor_Value
          {
            int armorValue=BufferUtils.readUInt32(bis);
            System.out.println("Armour: "+armorValue);
          }
          else
          {
            System.out.println("Unmanaged header: "+subHeader);
          }
        }
      }
      else if (header==0x10000421) // UI_Examination_Tooltip_DID
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
        elseif header == 0x100026BC then // ItemAdvancement_SelectedEffect_Array
          -- legacies, we have them already
          local nLegs = ins:GetLongLE();
          ins:Consume( 8 * nLegs );
        elseif header == 0x100038A7 then -- Inventory_BindToAccount
          result.itemBindToAccount = ins:Get();
        end
      end
  end
     */
  }

  private void decodeEssences(ByteArrayInputStream bis)
  {
    int nbEssences=BufferUtils.readUInt32(bis);
    System.out.println("Nb essences: "+nbEssences);
    for(int i=0;i<nbEssences;i++)
    {
      // 0x10005F3D=268459837 - Item_Socket_Gem_Array_Entry
      int propId=BufferUtils.readUInt32(bis);
      if (propId!=268459837)
      {
        System.out.println("Expected property ID: "+268459837+", got: "+propId);
        return;
      }
      /*int n3=*/BufferUtils.readUInt8(bis); // Always 0
      int nbProps=BufferUtils.readUInt8(bis);
      for(int j=0;j<nbProps;j++)
      {
        int header=BufferUtils.readUInt32(bis);
        int header2=BufferUtils.readUInt32(bis);
        if (header!=header2)
        {
          System.out.println("Decoding error: header="+header+", header2="+header2);
          return;
        }
        // 0x10005F05=268459781 - Item_Socket_GemDID
        if (header==0x10005F05)
        {
          int essenceId=BufferUtils.readUInt32(bis);
          System.out.println("Essence #"+(i+1)+": "+essenceId);
        }
        // 0x10005F3E=268459838 - Item_Socket_GemLevel
        else if (header==0x10005F3E)
        {
          int essenceLevel=BufferUtils.readUInt32(bis);
          System.out.println("Essence level: "+essenceLevel);
        }
        else
        {
          System.out.println("Unmanaged property: "+propId);
        }
      }
    }
  }

  private ImbuedLegacyInstance loadImbuedLegacy(int legacyId)
  {
    ImbuedLegacyInstance ret=null;
    LegaciesManager legaciesMgr=LegaciesManager.getInstance();
    ImbuedLegacy legacy=legaciesMgr.getLegacy(legacyId);
    if (legacy!=null)
    {
      ret=new ImbuedLegacyInstance();
      ret.setLegacy(legacy);
    }
    else
    {
      LOGGER.warn("Legacy not found: "+legacyId);
    }
    return ret;
  }

  private String decodeName(ByteArrayInputStream bis)
  {
    return BufferUtils.readPrefixedUtf16String(bis);
  }

  private static void doFile(File dataFile)
  {
    if (!dataFile.exists())
    {
      return;
    }
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
    doFile(new File(ethell,"supdoomfoldtstandardofwar.txt"));
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
    // Meva
    File meva=new File(linksDir,"meva");
    doFile(new File(meva,"EarringOfTheWillfulDefender.txt")); // Earring, with essences
    doFile(new File(meva,"EmbossedMantleOfBardsWill.txt")); // Mantle, with essences
    doFile(new File(meva,"RunedGlovesOfBardWill.txt")); // Gloves, with essences
    doFile(new File(meva,"old book.txt")); // old book, non imbued
    // Giswald
    File giswald=new File(linksDir,"giswald");
    doFile(new File(giswald,"2h weapon.txt")); // LI weapon, non imbued
    doFile(new File(giswald,"dyed helm.txt")); // umber dyed helm
    doFile(new File(giswald,"indigo dyed gauntlets.txt")); // indigo dyed gauntlets
    doFile(new File(giswald,"helm-thorin.txt")); // scaled helm
    doFile(new File(giswald,"mathomhunter box.txt")); // Box
    doFile(new File(giswald,"orange dye.txt")); // orange dye
  }
}
