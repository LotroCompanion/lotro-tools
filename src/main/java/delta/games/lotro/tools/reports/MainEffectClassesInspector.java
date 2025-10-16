package delta.games.lotro.tools.reports;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * @author dm
 */
public class MainEffectClassesInspector
{
  private void doIt()
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    for(Effect effect : EffectsManager.getInstance().getEffects())
    {
      int id=effect.getIdentifier();
      PropertiesSet props=facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
      Integer priority=(Integer)props.getProperty("Effect_ClassPriority");
      Integer eqClass=(Integer)props.getProperty("Effect_EquivalenceClass");
      System.out.println(id+"\t"+effect.getName()+"\t"+priority+"\t"+eqClass);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new MainEffectClassesInspector().doIt();
  }
}
