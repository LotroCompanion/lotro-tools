package delta.games.lotro.tools.dat.effects;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Get effects from DAT files.
 * @author DAM
 */
public class MainDatEffectsLoader
{
  private DataFacade _facade;
  private EffectLoader _loader;
  private Item _current;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new EffectLoader(facade);
  }

  private boolean _itemDisplayed;

  private void doIt()
  {
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      _current=item;
      _itemDisplayed=false;
      PropertiesSet props=_facade.loadProperties(item.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      handleEffects(props);
    }
  }

  private void handleEffects(PropertiesSet properties)
  {
    // On equip
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_EquipperEffectList");
    if (effects!=null)
    {
      handleEffectGenerators(effects);
    }
    // On use
    handleOnUseEffects(properties);
    // Skills
    handleSkillEffects(properties);
  }

  private void handleEffectGenerators(Object[] effects)
  {
    for(Object effectObj : effects)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      handleEffect(effectId);
    }
  }

  private void handleOnUseEffects(PropertiesSet properties)
  {
    Object[] effectsOnUse=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effectsOnUse!=null)
    {
      handleEffectGenerators(effectsOnUse);
    }
  }

  /**
   * Handle skill effects.
   * @param properties Item properties.
   */
  private void handleSkillEffects(PropertiesSet properties)
  {
    Integer skillID=(Integer)properties.getProperty("Usage_SkillToExecute");
    if (skillID==null)
    {
      return;
    }
    PropertiesSet skillProps=_facade.loadProperties(skillID.intValue()+DATConstants.DBPROPERTIES_OFFSET);
    if (skillProps==null)
    {
      return;
    }
    handleSkillProps(skillProps);
  }

  private void handleSkillProps(PropertiesSet skillProps)
  {
    /*
Skill_AttackHookList: 
  #1: Skill_AttackHookInfo 
    Skill_AttackHook_ActionDurationContributionMultiplier: 0.0
    Skill_AttackHook_TargetEffectList: 
     */
    Object[] attackHookList=(Object[])skillProps.getProperty("Skill_AttackHookList");
    if ((attackHookList==null) || (attackHookList.length==0))
    {
      return;
    }
    for(Object attackHookInfoObj : attackHookList)
    {
      PropertiesSet attackHookInfoProps=(PropertiesSet)attackHookInfoObj;
      Object[] effectList=(Object[])attackHookInfoProps.getProperty("Skill_AttackHook_TargetEffectList");
      if ((effectList!=null) && (effectList.length>0))
      {
        for(Object effectEntry : effectList)
        {
          PropertiesSet effectProps=(PropertiesSet)effectEntry;
          handleSkillEffect(effectProps);
        }
      }
    }
  }

  private void handleSkillEffect(PropertiesSet effectProps)
  {
    Integer effectID=(Integer)effectProps.getProperty("Skill_Effect");
    if ((effectID!=null) && (effectID.intValue()!=0))
    {
      handleEffect(effectID.intValue());
    }
  }

  private void handleEffect(int effectID)
  {
    if (!_itemDisplayed)
    {
      System.out.println("Item: "+_current);
      _itemDisplayed=true;
    }
    _loader.getEffect(effectID);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatEffectsLoader(facade).doIt();
    facade.dispose();
  }
}
