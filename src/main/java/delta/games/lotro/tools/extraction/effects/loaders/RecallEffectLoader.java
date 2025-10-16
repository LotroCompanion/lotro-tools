package delta.games.lotro.tools.extraction.effects.loaders;

import delta.games.lotro.common.effects.RecallEffect;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Loader for 'recall' effects.
 * @author DAM
 */
public class RecallEffectLoader extends AbstractEffectLoader<RecallEffect>
{
  @Override
  public void loadSpecifics(RecallEffect effect, PropertiesSet effectProps)
  {
  /*
Effect_Recall_Location_Type: 3 (Telepad)
Effect_Recall_Radius: 0.0
Effect_Recall_Raid: 0
Effect_Recall_Telepad: eredluin_thorinshall_exit
Effect_Recall_Travel_Link: 0 (Undef)
  */
    // Location type
    @SuppressWarnings("unused")
    Integer locationType=(Integer)effectProps.getProperty("Effect_Recall_Location_Type");
    // Position
    String telepad=(String)effectProps.getProperty("Effect_Recall_Telepad");
    if ((telepad!=null) && (!telepad.isEmpty()))
    {
      ExtendedPosition position=getPositionForName(telepad);
      if (position!=null)
      {
        effect.setPosition(position.getPosition());
      }
    }
  }


}
