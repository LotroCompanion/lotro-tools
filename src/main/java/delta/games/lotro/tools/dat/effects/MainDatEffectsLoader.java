package delta.games.lotro.tools.dat.effects;

import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.EffectDisplay;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSetsManager;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;

/**
 * Get effects from DAT files.
 * @author DAM
 */
public class MainDatEffectsLoader
{
  private DataFacade _facade;
  private EffectLoader2 _loader;
  private Item _item;
  private ItemsSet _set;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new EffectLoader2(facade);
  }

  private boolean _itemDisplayed;
  private boolean _setDisplayed;

  private static int[] TEST_ITEM_IDS= {
      1879150044, // Lothlórien Protector's Locket
      1879049652, // Lesser Celebrant Salve
      1879049653, // Roast Pork
      1879049715, // Mushroom Pie
      1879050268, // Garth Agarwen Gate Key
      1879050465, // Flask of Lhinestad
      1879050706, // Dwarf Padded Waistcoat of Absorption
      1879052430, // Fire-oil
      1879054882, // Rust Dye Recipe
      1879055045, // Scroll of Battle Lore
      1879066712, // Rowan Camp-fire Kit
      1879070441, // Dwarf-iron Caltrops
      1879087990, // King's Leggings
      1879088738, // Stonehelm Shield
      1879090863, // Cloak of the Cluck
      1879097386, // Threkrand
      1879102287, // Strange Chicken Nest  (ambiguous)
      1879112236, // Trickster's Boots
      1879162588, // Race Horse
      1879163962, // Emblem of Wisdom
      1879408989, // Grant the Wine Tasting Emote
      1879401700, // Champion's Silk-steel Helm of the Endless Duel
      1879264476, // Greater Helm of the Erebor Gambler
      1879387359, // Purple Dwarf-candle
      1879285840, // Potent Enduring Bow of Evasion
      1879459939, // Pristine Carn Dûm Etched Necklace
  };
 
  private static int[] TEST_SET_IDS= {
      1879150692, // Protector's Reproach (Max Level: 69)
  };

  private void doIt()
  {
    // Items
    //for(int itemId : TEST_ITEM_IDS)
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      //Item item=ItemsManager.getInstance().getItem(itemId);
      _item=item;
      _itemDisplayed=false;
      PropertiesSet props=_facade.loadProperties(item.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      handleItemEffects(props);
    }
    _item=null;
    // Sets
    //for(int sedId : TEST_SET_IDS)
    for(ItemsSet set : ItemsSetsManager.getInstance().getAll())
    {
      //ItemsSet set=ItemsSetsManager.getInstance().getSetById(sedId);
      _set=set;
      _setDisplayed=false;
      PropertiesSet props=_facade.loadProperties(set.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      handleSetEffects(props);
    }
  }

  private void handleItemEffects(PropertiesSet properties)
  {
    // On equip
    handleOnEquipEffects(properties);
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
      /*Effect2 effect=*/handleEffect(effectId);
    }
  }

  private void handleOnEquipEffects(PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_EquipperEffectList");
    if (effects!=null)
    {
      handleEffectGenerators(effects);
    }
  }

  private void handleOnUseEffects(PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effects!=null)
    {
      handleEffectGenerators(effects);
    }
  }

  /**
   * Handle skill effects.
   * @param properties Item properties.
   */
  private void handleSkillEffects(PropertiesSet properties)
  {
    // TODO Avoid duplicate effects for skills:
    // For instance, Item 1879069473 (a hope token), uses a skill that
    // triggers 2 effects: a fellowship effect that triggers effect X, and effect X a second time!
    Integer skillID=(Integer)properties.getProperty("Usage_SkillToExecute");
    if (skillID==null)
    {
      return;
    }
    // TODO Use Usage_SkillLevel is any!
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

  private Effect2 handleEffect(int effectID)
  {
    if ((_item!=null) && (!_itemDisplayed))
    {
      System.out.println("Item: "+_item);
      _itemDisplayed=true;
    }
    if ((_set!=null) && (!_setDisplayed))
    {
      System.out.println("Set: "+_set);
      _setDisplayed=true;
    }
    Effect2 ret=_loader.getEffect(effectID);
    showEffect(ret);
    return ret;
  }

  private void handleSetEffects(PropertiesSet properties)
  {
    //System.out.println(properties.dump());
    /*
    Set_ActiveCountDataList: 
      #1: Set_ActiveCountData 
        Set_ActiveCount: 2
        Set_EffectDataList: 
          #1: EffectGenerator_EffectStruct 
            EffectGenerator_EffectID: 1879098038
            EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] activeCountDataList=(Object[])properties.getProperty("Set_ActiveCountDataList");
    if (activeCountDataList==null)
    {
      return;
    }
    for(Object activeCountDataEntry : activeCountDataList)
    {
      PropertiesSet entryProps=(PropertiesSet)activeCountDataEntry;
      //int count=((Integer)entryProps.getProperty("Set_ActiveCount")).intValue();
      Object[] effectsList=(Object[])entryProps.getProperty("Set_EffectDataList");
      if (effectsList==null)
      {
        continue;
      }
      handleEffectGenerators(effectsList);
    }
  }

  private void showEffect(Effect2 effect)
  {
    int level=getLevel();
    EffectDisplay display=new EffectDisplay(level);
    StringBuilder sb=new StringBuilder();
    display.displayEffect(sb,effect);
    if (sb.length()>0)
    {
      System.out.println(sb.toString().trim());
    }
  }

  private int getLevel()
  {
    int level=1;
    Integer itemLevel=null;
    if (_item!=null)
    {
      itemLevel=_item.getItemLevel();
      if (itemLevel!=null)
      {
        level=itemLevel.intValue();
      }
    }
    else if (_set!=null)
    {
      level=_set.getSetLevel();
    }
    return level;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new MainDatEffectsLoader(facade).doIt();
    facade.dispose();
  }
}
