package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.TravelEffect;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;

/**
 * Loader for 'travel' effects.
 * @author DAM
 */
public class TravelEffectLoader extends AbstractEffectLoader<TravelEffect>
{
  @Override
  public void loadSpecifics(TravelEffect effect, PropertiesSet effectProps)
  {
    /*
    EffectGenerator_EffectDataList: 
      #1: EffectGenerator_EffectData_SceneID 1879048837
      #2: EffectGenerator_EffectData_Destination rohan_wold_harwick_meadhall_exit
    Effect_Applied_Description: You travel to Harwick.
    Effect_Travel_PrivateEncounter: 1879262955
    */
    ArrayPropertyValue dataList=(ArrayPropertyValue)effectProps.getPropertyValueByName("EffectGenerator_EffectDataList");
    for(PropertyValue childValue : dataList.getValues())
    {
      String propertyName=childValue.getDefinition().getName();
      Object value=childValue.getValue();
      if ("EffectGenerator_EffectData_Destination".equals(propertyName))
      {
        String destination=(String)value;
        ExtendedPosition position=getPositionForName(destination);
        if (position!=null)
        {
          effect.setDestination(position.getPosition());
        }
      }
      else if ("EffectGenerator_EffectData_SceneID".equals(propertyName))
      {
        Integer sceneID=(Integer)value;
        effect.setSceneID(sceneID.intValue());
      }
      else if ("EffectGenerator_EffectData_RemoveFromPrivateInstance".equals(propertyName))
      {
        Integer removeFlagInt=(Integer)value;
        boolean removeFlag=((removeFlagInt!=null)&& (removeFlagInt.intValue()==1));
        effect.setRemoveFromInstance(removeFlag);
      }
    }
    Integer privateEncounterID=(Integer)effectProps.getProperty("Effect_Travel_PrivateEncounter");
    if ((privateEncounterID!=null) && (privateEncounterID.intValue()!=0))
    {
      effect.setPrivateEncounterID(privateEncounterID);
    }
  }
}
