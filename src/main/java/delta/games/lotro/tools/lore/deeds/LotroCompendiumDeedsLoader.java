package delta.games.lotro.tools.lore.deeds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.common.utils.NumericTools;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Emote;
import delta.games.lotro.common.Reputation;
import delta.games.lotro.common.ReputationItem;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Title;
import delta.games.lotro.common.Trait;
import delta.games.lotro.common.Virtue;
import delta.games.lotro.common.VirtueId;
import delta.games.lotro.common.objects.ObjectItem;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.lore.deeds.io.xml.DeedXMLWriter;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.plugins.LuaParser;

/**
 * Deeds loader from LotroCompendium data.
 * @author DAM
 */
public class LotroCompendiumDeedsLoader
{
  private Set<String> _keys=new HashSet<String>();

  @SuppressWarnings({"rawtypes","unchecked"})
  private void doIt() throws Exception
  {
    File root=new File(new File("data"),"deeds");
    File luaDb=new File(root,"deeds.lua");
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    //File luaDb=new File("indexes.lua");
    LuaParser parser=new LuaParser();
    Object map=parser.readObject(luaDb);
    if (map instanceof Map)
    {
      System.out.println(((Map)map).keySet());
    }
    else if (map instanceof List)
    {
      List<Object> datas=(List<Object>)map;
      int length=datas.size();
      System.out.println("Array of "+length+" items.");
      for(Object data : datas)
      {
        DeedDescription deed=buildDeedFromRawData(data);
        deeds.add(deed);
      }
      System.out.println(_keys);
    }
    File loreDir=LotroCoreConfig.getInstance().getLoreDir();
    File out=new File(loreDir,"deeds_lc.xml");
    DeedXMLWriter writer=new DeedXMLWriter();
    writer.writeDeeds(out,deeds,EncodingNames.UTF_8);
  }

  @SuppressWarnings("unchecked")
  private DeedDescription buildDeedFromRawData(Object data)
  {
    if (!(data instanceof Map))
    {
      return null;
    }
    Map<String,Object> map=(Map<String,Object>)data;
    DeedDescription deed=new DeedDescription();
    //[d, c, next, pois, o, virtues, prev, id, mobs, t, level, emotes, name, reputation, titles, traits, receive, zone]
    //[mobs, zone]
    // ID
    Double id=(Double)map.get("id");
    deed.setIdentifier(id.intValue());
    // Name
    String name=(String)map.get("name");
    deed.setName(name);
    // Description
    String description=(String)map.get("d");
    deed.setDescription(description);
    // c=Comments?
    List<String> comments=(List<String>)map.get("c");
    // TODO
    // Previous
    List<Double> nextIds=(List<Double>)map.get("next");
    // TODO
    // Next
    List<Double> prevIds=(List<Double>)map.get("prev");
    // TODO
    // pois
    // TODO
    // Type
    String type=(String)map.get("t");
    handleType(deed,type);
    // Zone
    String zone=(String)map.get("zone");
    _keys.add(zone);
    // Objectives
    String objectives=(String)map.get("o");
    deed.setObjectives(objectives);
    // Level
    Double level=(Double)map.get("level");
    if (level!=null)
    {
      deed.setMinLevel(Integer.valueOf(level.intValue()));
    }
    // Rewards
    Rewards rewards=deed.getRewards();
    // - titles
    {
      List<Object> titles=(List<Object>)map.get("titles");
      if (titles!=null)
      {
        for(Object title : titles)
        {
          Map<String,Object> titleMap=(Map<String,Object>)title;
          String titleLabel=(String)titleMap.get("val");
          Title titleRewards=new Title(titleLabel,titleLabel);
          rewards.addTitle(titleRewards);
        }
      }
    }
    // - traits
    {
      List<Object> traits=(List<Object>)map.get("traits");
      if (traits!=null)
      {
        for(Object trait : traits)
        {
          Map<String,Object> traitMap=(Map<String,Object>)trait;
          String traitLabel=(String)traitMap.get("val");
          Trait traitRewards=new Trait(traitLabel,traitLabel);
          rewards.addTrait(traitRewards);
        }
      }
    }
    // - emotes
    {
      List<Object> emotes=(List<Object>)map.get("emotes");
      if (emotes!=null)
      {
        for(Object emote : emotes)
        {
          Map<String,Object> emoteMap=(Map<String,Object>)emote;
          String emoteCommand=(String)emoteMap.get("val");
          if (emoteCommand.startsWith("/"))
          {
            emoteCommand=emoteCommand.substring(1);
          }
          Emote emoteRewards=new Emote(emoteCommand,emoteCommand);
          rewards.addEmote(emoteRewards);
        }
      }
    }
    // - items
    //receive={{id="70018CBD",q="(x2)",val="Yule Festival Token"}}
    {
      List<Object> items=(List<Object>)map.get("receive");
      if (items!=null)
      {
        for(Object item : items)
        {
          Map<String,Object> itemMap=(Map<String,Object>)item;
          handleItem(rewards,itemMap);
        }
      }
    }
    // - virtues
    List<Object> virtueItems=(List<Object>)map.get("virtues");
    handleVirtues(rewards,virtueItems);
    // - reputation
    List<Object> reputationItems=(List<Object>)map.get("reputation");
    handleReputation(rewards,reputationItems);
    //_keys.addAll(map.keySet());
    return deed;
  }

  private void handleItem(Rewards rewards, Map<String,Object> itemMap)
  {
    Object idObject=itemMap.get("id");
    String idStr="0";
    if (idObject instanceof String) idStr=(String)idObject;
    if (idObject instanceof Double)
    {
      idStr=String.valueOf(((Double)idObject).intValue());
    }
    int id=Integer.parseInt(idStr,16);
    String quantityStr=(String)itemMap.get("q");
    int quantity=1;
    if ((quantityStr!=null) && (quantityStr.startsWith("(x")) && (quantityStr.endsWith(")")))
    {
      quantityStr=quantityStr.substring(2,quantityStr.length()-1);
      quantity=Integer.parseInt(quantityStr);
    }
    String itemName=(String)itemMap.get("val");
    ObjectItem item=new ObjectItem(itemName);
    item.setItemId(id);
    rewards.getObjects().addObject(item,quantity);
  }

  private void handleType(DeedDescription deed, String type)
  {
    CharacterClass characterClass=null;
    DeedType deedType=null;
    if ("Minstrel".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.MINSTREL; }
    if ("Hunter".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.HUNTER; }
    if ("Lore-master".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.LORE_MASTER; }
    if ("Captain".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.CAPTAIN; }
    if ("Warden".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.WARDEN; }
    if ("Champion".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.CHAMPION; }
    if ("Rune-keeper".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.RUNE_KEEPER; }
    if ("Burglar".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.BURGLAR; }
    if ("Guardian".equals(type)) { deedType=DeedType.CLASS; characterClass=CharacterClass.GUARDIAN; }
    if ("Event".equals(type)) deedType=DeedType.EVENT;
    if ("Explorer".equals(type)) deedType=DeedType.EXPLORER;
    if ("Lore".equals(type)) deedType=DeedType.LORE;
    if ("Slayer".equals(type)) deedType=DeedType.SLAYER;
    if ("Reputation".equals(type)) deedType=DeedType.REPUTATION;
    if (deedType==null)
    {
      String name=deed.getName();
      if (name!=null)
      {
        if (name.toLowerCase().indexOf("slayer")!=-1)
        {
          deedType=DeedType.SLAYER;
        }
      }
    }
    deed.setType(deedType);
    if (characterClass!=null)
    {
      deed.setClassName(characterClass.getLabel());
    }
  }

  @SuppressWarnings("unchecked")
  private void handleReputation(Rewards rewards, List<Object> reputationItems)
  {
    //reputation={{val="+700 with Iron Garrison Guards"}}
    if (reputationItems==null)
    {
      return;
    }
    FactionsRegistry registry=FactionsRegistry.getInstance();
    Reputation reputation=rewards.getReputation();
    for(Object reputationItem : reputationItems)
    {
      Map<String,Object> reputationMap=(Map<String,Object>)reputationItem;
      String reputationStr=(String)reputationMap.get("val");
      int spaceIndex=reputationStr.indexOf(" ");
      Integer value=NumericTools.parseInteger(reputationStr.substring(0,spaceIndex));
      String factionStr=reputationStr.substring(spaceIndex+1).trim();
      if (factionStr.startsWith("with ")) factionStr=factionStr.substring(5);
      Faction faction=registry.getByName(factionStr);
      if ((faction!=null) && (value!=null))
      {
        ReputationItem repItem=new ReputationItem(faction);
        repItem.setAmount(value.intValue());
        reputation.add(repItem);
      }
      else
      {
        System.out.println("Not handled ["+reputationStr+"]");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleVirtues(Rewards rewards, List<Object> virtueItems)
  {
    //virtues={{val="Discipline"}}
    if (virtueItems==null)
    {
      return;
    }
    for(Object virtueItem : virtueItems)
    {
      Map<String,Object> virtueMap=(Map<String,Object>)virtueItem;
      String virtueName=(String)virtueMap.get("val");
      VirtueId virtueId=VirtueId.valueOf(virtueName.toUpperCase());
      if (virtueId!=null)
      {
        Virtue virtue=new Virtue(virtueId.name(),virtueId.getLabel());
        rewards.addVirtue(virtue);
      }
      else
      {
        System.out.println("Not handled ["+virtueName+"]");
      }
    }
  }

  private void handleMobs()
  {
    //mobs={
    // {locations={"33.84S, 55.81W"},name="Barrow-hound",zone="Bree-land"},
    // {locations={"33.04S, 55.36W"},name="Brishzel",zone="Bree-land"},
    // {locations={"33.04S, 55.36W","34.10S, 54.95W","34.60S, 54.36W","35.10S, 55.28W","35.13S, 55.13W"},name="Howling Barrow-hound",zone="Bree-land"}
    //}
  }

  private String normalizeZone(String zone)
  {
    if ("The Lone-lands".equals(zone)) zone="Lone-lands";
    if ("The Misty Mountains".equals(zone)) zone="Misty Mountains";
    if ("The Trollshaws".equals(zone)) zone="Trollshaws";
    return zone;
    // [Moria, null, Forochel, Mirkwood, Trollshaws, Bree-land, Eregion,
    // Evendim, Lone-lands, Angmar, The Shire, Enedwaith,
    // Misty Mountains, The North Downs, Ettenmoors, Ered Luin, Lothlórien]
  }

  /**
   * Main method for this loader.
   * @param args Not used.
   * @throws Exception if a problem occurs.
   */
  public static void main(String[] args) throws Exception
  {
    new LotroCompendiumDeedsLoader().doIt();
  }
}
