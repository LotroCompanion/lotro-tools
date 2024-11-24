package delta.games.lotro.tools.extraction.effects;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loads more effects (difficulty effects).
 * @author DAM
 */
public class SomeMoreEffectsLoader
{
  private static final int[] IDS= {
      // Difficulty effects
      1879414605, // Difficulty 1: Adventurous
      1879414607, // Difficulty 2: Daring
      1879414608, // Difficulty 3: Fearless
      1879416240, // Difficulty 4: Fearless +1
      1879419739, // Difficulty 5: Fearless +2
      1879419742, // Difficulty 6: Fearless +3
      1879419743, // Difficulty 7: Heroic
      1879419740, // Difficulty 8: Heroic +1
      1879419741, // Difficulty 9: Heroic +2
      // Others
      1879415437, // DNT - landscape difficulty effect trigger
      1879463881, // DNT - Difficulty currency t3
      1879320173, // DNT - Class Dual Wield
      1879348624, // Shadow of Mordor
  };

  private DataFacade _facade;
  private EffectLoader _effectsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public SomeMoreEffectsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
  }

  /**
   * Do it!
   */
  public void doIt()
  {
    for(int id : IDS)
    {
      _effectsLoader.getEffect(id);
    }
    loadLoEEffects();
    loadPipEffects();
    loadMountedCombatEffects();
    _effectsLoader.save();
  }

  private void loadLoEEffects()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"LoEControl");
    if (props==null)
    {
      return;
    }
    Object[] array=(Object[])props.getProperty("LoE_Control_Array");
    /*
    LoE_Control_Array: 
      #1: LoE_Control_Struct 
        LoE_Control_Effect: 1879355295
        LoE_Control_Level: 1
    */
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      int effectID=((Integer)entryProps.getProperty("LoE_Control_Effect")).intValue();
      _effectsLoader.getEffect(effectID);
    }
  }

  private void loadPipEffects()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"PipControl");
    if (props==null)
    {
      return;
    }
    Object[] array=(Object[])props.getProperty("PipControl_Directory");
    /*
    PipControl_Directory: 
      #1: PipControl_Entry 1879458249
      #2: PipControl_Entry 1879411402
      #3: PipControl_Entry 1879052504
         */
    for(Object entry : array)
    {
      Integer entryID=(Integer)entry;
      handlePip(entryID.intValue());
    }
  }

  private void handlePip(int pipID)
  {
    PropertiesSet props=_facade.loadProperties(pipID+DATConstants.DBPROPERTIES_OFFSET);
    /*
Pip_ValueProperty: 268454469 (Pip_WitchKing_Dread)
Threshold_Pip_IntegerValue_Policy: 
  Threshold_Policy_CurrentStateIndexProperty: 268455005 (Threshold_Pip_Dread_CurrentIndex)
  Threshold_Policy_StateList: 
    #1: Threshold_EffectScript_State 
      Threshold_State_EffectList: 
        #1: Threshold_State_EffectStruct 
          EffectGenerator_EffectID: 1879240494
     */
    PropertiesSet policyProps=(PropertiesSet)props.getProperty("Threshold_Pip_IntegerValue_Policy");
    if (policyProps==null)
    {
      return;
    }
    Object[] stateList=(Object[])policyProps.getProperty("Threshold_Policy_StateList");
    for(Object stateEntry : stateList)
    {
      PropertiesSet entryProps=(PropertiesSet)stateEntry;
      Object[] effectList=(Object[])entryProps.getProperty("Threshold_State_EffectList");
      if (effectList!=null)
      {
        for(Object effectEntryObj : effectList)
        {
          PropertiesSet effectProps=(PropertiesSet)effectEntryObj;
          Integer effectID=(Integer)effectProps.getProperty("EffectGenerator_EffectID");
          if ((effectID!=null) && (effectID.intValue()>0))
          {
            _effectsLoader.getEffect(effectID.intValue());
          }
        }
      }
    }
  }

  private void loadMountedCombatEffects()
  {
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"MountedCombatControl");
    if (props==null)
    {
      return;
    }
    Object[] array=(Object[])props.getProperty("Mount_Control_Tree_Array");
    /*
Mount_Control_Tree_Array: 
  #1: Mount_Control_Tree_Struct 
    Mount_Control_Trait_Tree: 2 (Light)
    Mount_Control_Tree_Effect: 1879253873
    Mount_WarSteed_SkillCombo: 1 (Light)
    */
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      int effectID=((Integer)entryProps.getProperty("Mount_Control_Tree_Effect")).intValue();
      _effectsLoader.getEffect(effectID);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    SomeMoreEffectsLoader loader=new SomeMoreEffectsLoader(facade,effectsLoader);
    loader.doIt();
  }
}
