package delta.games.lotro.tools.dat.items;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.Race;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.lore.emotes.EmoteDescription;
import delta.games.lotro.lore.emotes.EmotesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.details.GrantType;
import delta.games.lotro.lore.items.details.GrantedElement;
import delta.games.lotro.lore.items.details.ItemReputation;
import delta.games.lotro.lore.items.details.ItemXP;
import delta.games.lotro.lore.items.details.VirtueXP;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;

/**
 * Loader for item details.
 * @author DAM
 */
public class ItemDetailsLoader
{
  private static final Logger LOGGER=Logger.getLogger(ItemDetailsLoader.class);

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemDetailsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Handle an item.
   * @param item Item to use.
   * @param props Properties to use.
   */
  public void handleItem(Item item, PropertiesSet props)
  {
    handleGrantedSkills(item,props);
    handleGrantedTrait(item,props,"Item_GrantedTrait",GrantType.TRAIT);
    handleItemXP(item,props);
    handleItemReputation(item,props);
    handleEffects(item,props);
  }

  private void handleGrantedSkills(Item item, PropertiesSet props)
  {
    handleGrantedSkill(item,props,"Item_GrantedSkill",GrantType.SKILL);
    handleGrantedSkill(item,props,"Mount_SkillToGrantShort",GrantType.SHORT_MOUNT);
    handleGrantedSkill(item,props,"Mount_SkillToGrantTall",GrantType.TALL_MOUNT);
    handleGrantedMountSkillArray(item,props);
  }

  private void handleGrantedSkill(Item item, PropertiesSet props, String propertyName, GrantType type)
  {
    Integer grantedSkill=(Integer)props.getProperty(propertyName);
    if (grantedSkill!=null)
    {
      SkillDescription skill=SkillsManager.getInstance().getSkill(grantedSkill.intValue());
      if (skill!=null)
      {
        GrantedElement<SkillDescription> grantedElement=new GrantedElement<SkillDescription>(type,skill);
        Item.addDetail(item,grantedElement);
      }
    }
  }

  private void handleGrantedMountSkillArray(Item item, PropertiesSet props)
  {
/*
Mount_SkillToGrantRaceArray: 
  #1: Mount_SkillToGrantRace_Entry 
    Mount_SkillToGrant: 1879245852
    Usage_RequiredRaceEntry: 81 (Hobbit[O])
    ...
*/
    Object[] array=(Object[])props.getProperty("Mount_SkillToGrantRaceArray");
    if (array==null)
    {
      return;
    }
    for(Object entryObj : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      int skillID=((Integer)entryProps.getProperty("Mount_SkillToGrant")).intValue();
      SkillDescription skill=SkillsManager.getInstance().getSkill(skillID);
      if (skill!=null)
      {
        int raceCode=((Integer)entryProps.getProperty("Usage_RequiredRaceEntry")).intValue();
        Race race=DatEnumsUtils.getRaceFromRaceId(raceCode);
        RacesManager racesMgr=RacesManager.getInstance();
        RaceDescription raceDescription=racesMgr.getRaceDescription(race);
        boolean tall=raceDescription.isTall();
        GrantType grantType=tall?GrantType.TALL_MOUNT:GrantType.SHORT_MOUNT;
        GrantedElement<SkillDescription> grantedElement=new GrantedElement<SkillDescription>(grantType,skill);
        Item.addDetail(item,grantedElement);
      }
    }
  }

  private void handleGrantedTrait(Item item, PropertiesSet props, String propertyName, GrantType type)
  {
    Integer grantedTrait=(Integer)props.getProperty(propertyName);
    if (grantedTrait!=null)
    {
      TraitDescription trait=TraitsManager.getInstance().getTrait(grantedTrait.intValue());
      if (trait!=null)
      {
        GrantedElement<TraitDescription> grantedElement=new GrantedElement<TraitDescription>(type,trait);
        Item.addDetail(item,grantedElement);
      }
    }
  }

  private void handleItemXP(Item item, PropertiesSet props)
  {
    //ItemAdvancement_XPToAdd
    {
      Integer amount=(Integer)props.getProperty("ItemAdvancement_XPToAdd");
      if (amount!=null)
      {
        ItemXP itemXP=new ItemXP(amount.intValue());
        Item.addDetail(item,itemXP);
      }
    }
    //RewardTrack_ExperienceType: 1 (ItemAdvancement)
    //RewardTrack_XPToAdd: 130000
    {
      Integer amount=(Integer)props.getProperty("RewardTrack_XPToAdd");
      Integer type=(Integer)props.getProperty("RewardTrack_ExperienceType");
      if (amount!=null)
      {
        if ((type!=null) && (type.intValue()==1))
        {
          ItemXP itemXP=new ItemXP(amount.intValue());
          Item.addDetail(item,itemXP);
        }
        else
        {
          LOGGER.warn("Type not found or type is not 1 for "+item);
        }
      }
    }
  }

  private void handleItemReputation(Item item, PropertiesSet props)
  {
    Integer isRepItem=(Integer)props.getProperty("Reputation_IsReputationItem");
    if ((isRepItem!=null) && (isRepItem.intValue()==1))
    {
      Integer factionID=(Integer)props.getProperty("Reputation_Faction");
      if ((factionID!=null) && (factionID.intValue()!=0))
      {
        Faction faction=FactionsRegistry.getInstance().getById(factionID.intValue());
        if (faction!=null)
        {
          Long reputationGain=(Long)props.getProperty("Reputation_ReputationGain");
          Long reputationLoss=(Long)props.getProperty("Reputation_ReputationLoss");
          int value=(reputationGain!=null)?reputationGain.intValue():((reputationLoss!=null)?reputationLoss.intValue():0);
          ItemReputation reputation=new ItemReputation(faction,value);
          Item.addDetail(item,reputation);
        }
        else
        {
          LOGGER.warn("Could not find faction with ID: "+factionID+" for item: "+item);
        }
      }
      else
      {
        LOGGER.debug("No faction ID, and isRepItem is "+isRepItem+" for item: "+item);
      }
    }
  }

  private void handleEffects(Item item, PropertiesSet props)
  {
/*
EffectGenerator_UsageEffectList: 
  #1: EffectGenerator_EffectStruct 
    EffectGenerator_EffectDataList: 
      #1: Effect_GrantTraitRank_Grant_List 
        #1: Effect_GrantTraitRank_Grant_Node 
          Effect_GrantTraitRank_Grant_XP: 1000
    EffectGenerator_EffectID: 1879186818
    EffectGenerator_EffectSpellcraft: -1.0

  #1: EffectGenerator_EffectStruct 
    EffectGenerator_EffectID: 1879187320
    EffectGenerator_EffectSpellcraft: -1.0
*/
    Object[] usageEffectList=(Object[])props.getProperty("EffectGenerator_UsageEffectList");
    if ((usageEffectList!=null) && (usageEffectList.length>0))
    {
      Set<Integer> handleEffects=new HashSet<Integer>();
      for(Object entry : usageEffectList)
      {
        PropertiesSet effectProps=(PropertiesSet)entry;
        handleEffectDataList(item,effectProps);
        Integer effectID=(Integer)effectProps.getProperty("EffectGenerator_EffectID");
        if ((effectID!=null) && (!handleEffects.contains(effectID)))
        {
          handleEffectID(item,effectID.intValue());
          handleEffects.add(effectID);
        }
      }
    }
  }

  private void handleEffectDataList(Item item, PropertiesSet effectProps)
  {
    ArrayPropertyValue dataListValue=(ArrayPropertyValue)effectProps.getPropertyValueByName("EffectGenerator_EffectDataList");
    //Integer effectID=(Integer)effectProps.getProperty("EffectGenerator_EffectID");
    if (dataListValue!=null)
    {
      for(PropertyValue dataListEntry : dataListValue.getValues())
      {
        PropertyDefinition dataListEntryPropDef=dataListEntry.getDefinition();
        if ("Effect_GrantTraitRank_Grant_List".equals(dataListEntryPropDef.getName()))
        {
          ArrayPropertyValue dataListEntryArray=(ArrayPropertyValue)dataListEntry;
          handleGrantTraitRankGrantList(item,dataListEntryArray);
        }
        else if ("Effect_Crafting_Recipe".equals(dataListEntryPropDef.getName()))
        {
          // Data ID
        }
        else if ("Effect_Crafting_Profession".equals(dataListEntryPropDef.getName()))
        {
          // Data ID
        }
        else if ("Effect_Crafting_RecipeList".equals(dataListEntryPropDef.getName()))
        {
          // Array of recipe IDs?
        }
        else if ("Effect_GrantSkill_Skill_To_Grant".equals(dataListEntryPropDef.getName()))
        {
          // Skill Data ID?
        }
        else if ("Effect_GrantTrait_Trait_To_Grant".equals(dataListEntryPropDef.getName()))
        {
          // Trait Data ID?
        }
        else
        {
          LOGGER.warn("Unmanaged data list entry: "+dataListEntryPropDef);
        }
      }
    }
  }

  private void handleGrantTraitRankGrantList(Item item, ArrayPropertyValue entriesArray)
  {
    for(PropertyValue grantListEntryValue : entriesArray.getValues())
    {
      PropertyDefinition grantEntryPropDef=grantListEntryValue.getDefinition();
      if ("Effect_GrantTraitRank_Grant_Node".equals(grantEntryPropDef.getName()))
      {
        PropertiesSet grantNode=(PropertiesSet)grantListEntryValue.getValue();
        /*
268449607 - Effect_GrantTraitRank_Grant_Node, type=Struct
Property: Effect_GrantTraitRank_Grant_N_Ranks, ID=268449606, type=Int
Property: Effect_GrantTraitRank_Trait_To_Grant, ID=268449609, type=Data File ID
Property: Effect_GrantTraitRank_Grant_Up_To_Rank, ID=268449610, type=Int
Property: Effect_GrantTraitRank_Grant_XP, ID=268461837, type=Int
         */
        Integer virtueXPAmount=(Integer)grantNode.getProperty("Effect_GrantTraitRank_Grant_XP");
        Integer upToRank=(Integer)grantNode.getProperty("Effect_GrantTraitRank_Grant_Up_To_Rank");
        Integer traitToGrant=(Integer)grantNode.getProperty("Effect_GrantTraitRank_Trait_To_Grant");
        Integer grantNRanks=(Integer)grantNode.getProperty("Effect_GrantTraitRank_Grant_N_Ranks");
        if (((virtueXPAmount!=null) && (virtueXPAmount.intValue()!=0)) ||
            ((upToRank!=null) && (upToRank.intValue()!=0)) ||
            ((traitToGrant!=null) && (traitToGrant.intValue()!=0)) ||
            ((grantNRanks!=null) && (grantNRanks.intValue()!=0)))
        {
          //System.out.println("Item : "+item);
          if ((virtueXPAmount!=null) && (virtueXPAmount.intValue()!=0))
          {
            VirtueXP virtueXP=new VirtueXP(virtueXPAmount.intValue());
            Item.addDetail(item,virtueXP);
          }
          /*
          if ((upToRank!=null) && (upToRank.intValue()!=0))
          {
            System.out.println("\tUp to rank="+upToRank);
          }
          if ((traitToGrant!=null) && (traitToGrant.intValue()!=0))
          {
            System.out.println("\tTrait to grant="+traitToGrant);
          }
          if ((grantNRanks!=null) && (grantNRanks.intValue()!=0))
          {
            System.out.println("\tGrant N ranks="+grantNRanks);
          }
          */
        }
      }
      else
      {
        System.out.println("Unmanaged grant entry: "+grantEntryPropDef);
      }
    }
  }

  private void handleEffectID(Item item, int effectID)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectID+DATConstants.DBPROPERTIES_OFFSET);
    Integer grantedEmoteID=(Integer)effectProps.getProperty("Effect_EmoteToGrant");
    if (grantedEmoteID!=null)
    {
      EmoteDescription emote=EmotesManager.getInstance().getEmote(grantedEmoteID.intValue());
      if (emote!=null)
      {
        GrantedElement<EmoteDescription> grantedElement=new GrantedElement<EmoteDescription>(GrantType.EMOTE,emote);
        Item.addDetail(item,grantedElement);
      }
    }
  }
}
