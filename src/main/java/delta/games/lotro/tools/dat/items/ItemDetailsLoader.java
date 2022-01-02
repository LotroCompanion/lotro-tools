package delta.games.lotro.tools.dat.items;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.details.GrantType;
import delta.games.lotro.lore.items.details.GrantedElement;

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
  }

  private void handleGrantedSkills(Item item, PropertiesSet props)
  {
    handleGrantedSkill(item,props,"Item_GrantedSkill",GrantType.SKILL);
    handleGrantedSkill(item,props,"Mount_SkillToGrantShort",GrantType.SHORT_MOUNT);
    handleGrantedSkill(item,props,"Mount_SkillToGrantTall",GrantType.TALL_MOUNT);
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
}
