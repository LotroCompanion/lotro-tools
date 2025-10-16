package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.Interactable;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.GenesisEffect;
import delta.games.lotro.common.effects.Hotspot;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for 'genesis' effects.
 * @author DAM
 */
public class GenesisEffectLoader extends AbstractEffectLoader<GenesisEffect>
{
  @Override
  public void loadSpecifics(GenesisEffect effect, PropertiesSet effectProps)
  {
    /*
Effect ID=1879163860, class=GenesisEffect (719)
Effect_Genesis_ConstantSummonDuration: 20.0
Effect_Genesis_PermanentSummonDuration: 0
Effect_Genesis_SummonedObject: 1879163733
     */
    int summonedObjectID=((Integer)effectProps.getProperty("Effect_Genesis_SummonedObject")).intValue();
    Float summonDuration=(Float)effectProps.getProperty("Effect_Genesis_ConstantSummonDuration");
    Integer permanent=(Integer)effectProps.getProperty("Effect_Genesis_PermanentSummonDuration");
    handleSummonedObject(summonedObjectID,effect);
    if (summonDuration!=null)
    {
      effect.setSummonDuration(summonDuration.floatValue());
    }
    if ((permanent!=null) && (permanent.intValue()==1))
    {
      effect.setPermanent();
    }
  }

  private void handleSummonedObject(int objectID, GenesisEffect effect)
  {
    PropertiesSet props=loadProperties(objectID);
    String name=DatStringUtils.getStringProperty(props,"Name");
    Interactable interactable=null;
    int weenieType=((Integer)props.getProperty("WeenieType")).intValue();
    if (weenieType==262145) // Hotspot
    {
      Hotspot hotspot=loadHotspot(objectID,props);
      effect.setHotspot(hotspot);
    }
    else if (weenieType==129) // Item
    {
      Item item=new Item();
      item.setIdentifier(objectID);
      item.setName(name);
      interactable=item;
    }
    else
    {
      interactable=EffectLoadingUtils.buildAgent(objectID,weenieType,name);
    }
    if (interactable!=null)
    {
      Proxy<Interactable> proxy=new Proxy<Interactable>();
      proxy.setId(objectID);
      proxy.setName(name);
      proxy.setObject(interactable);
      effect.setInteractable(proxy);
    }
  }

  private Hotspot loadHotspot(int hotspotID, PropertiesSet props)
  {
    /*
EffectGenerator_HotspotEffectList:
  #1: EffectGenerator_EffectStruct
    EffectGenerator_EffectID: 1879163549
    EffectGenerator_EffectSpellcraft: -1.0
Name: Greater Emblem of Defence
WeenieType: 262145 (Hotspot)
    */
    Hotspot ret=new Hotspot(hotspotID);
    String name=DatStringUtils.getStringProperty(props,"Name");
    ret.setName(name);
    Object[] effectsList=(Object[])props.getProperty("EffectGenerator_HotspotEffectList");
    if (effectsList!=null)
    {
      for(Object effectEntry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)effectEntry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addEffect(generator);
      }
    }
    return ret;
  }

}
