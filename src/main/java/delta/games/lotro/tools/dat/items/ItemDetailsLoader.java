package delta.games.lotro.tools.dat.items;

import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.Race;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.details.GrantType;
import delta.games.lotro.lore.items.details.GrantedElement;
import delta.games.lotro.lore.items.details.ItemXP;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;

/**
 * Loader for item details.
 * @author DAM
 */
public class ItemDetailsLoader
{
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
    Integer amount=(Integer)props.getProperty("ItemAdvancement_XPToAdd");
    if (amount!=null)
    {
      ItemXP itemXP=new ItemXP(amount.intValue());
      Item.addDetail(item,itemXP);
    }
  }
}
