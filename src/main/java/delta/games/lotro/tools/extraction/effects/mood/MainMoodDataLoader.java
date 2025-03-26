package delta.games.lotro.tools.extraction.effects.mood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.stats.ConstantStatProvider;
import delta.games.lotro.common.stats.StatOperator;
import delta.games.lotro.common.stats.StatProvider;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.WellKnownStat;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.mood.MoodEntry;
import delta.games.lotro.lore.mood.io.xml.MoodXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Loader for mood data.
 * @author DAM
 */
public class MainMoodDataLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainMoodDataLoader.class);

  private DataFacade _facade;
  private EffectLoader _effectsLoader;
  private Map<Integer,MoodEntry> _data;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public MainMoodDataLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _data=new HashMap<Integer,MoodEntry>();
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    loadData();
    save();
  }

  private void loadData()
  {
    /*
    Mood_Control_Dread_Decay_Rate: 3.0
    Mood_Control_Dread_Growth_Rate: 3.0
    Mood_Control_Maximum_Dread_Level: 15
    Mood_Control_Maximum_Hope_Level: 15
    Mood_Control_Moods: 
      #1: Mood_Control_Mood_Struct 
        Mood_Control_Inbound_Damage_Modifier: 1.0
        Mood_Control_Inbound_Healing_Modifier: 1.0
        Mood_Control_Max_Morale_Modifier: 1.0
        Mood_Control_Miss_Chance_Modifier: 0.0
        Mood_Control_Mood_Description: You sense the favour of the Valar upon you.
        Mood_Control_Mood_Effects: 
          #1: Mood_Control_Mood_Effect_Struct 
            Mood_Control_Mood_Effect: 1879138519
            Mood_Control_Mood_Effect_Spellcraft: -1.0
          #2: Mood_Control_Mood_Effect_Struct 
            Mood_Control_Mood_Effect: 1879208746
            Mood_Control_Mood_Effect_Spellcraft: -1.0
          #3: Mood_Control_Mood_Effect_Struct 
            Mood_Control_Mood_Effect: 0
            Mood_Control_Mood_Effect_Spellcraft: -1.0
        Mood_Control_Mood_Icon: 1090641320
        Mood_Control_Mood_Level: 15
        Mood_Control_Outbound_Damage_Modifier: 1.03
        Mood_Control_Spellcraft_Modifier: 0
    */
    /*
     * For hope: one of the effects will give something like:
Mod_Array: 
  #1: Mod_Entry 
    Health_MaxLevel: 1.05
    Mod_Modified: 268437691 (Health_MaxLevel)
    Mod_Op: 8 (Multiply)
     */
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(_facade,"MoodControl");
    @SuppressWarnings("unused")
    int maxHope=((Integer)props.getProperty("Mood_Control_Maximum_Hope_Level")).intValue();
    @SuppressWarnings("unused")
    int maxDread=((Integer)props.getProperty("Mood_Control_Maximum_Dread_Level")).intValue();
    Object[] array=(Object[])props.getProperty("Mood_Control_Moods");
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      float moraleModifier=((Float)entryProps.getProperty("Mood_Control_Max_Morale_Modifier")).floatValue();
      int moodLevel=((Integer)entryProps.getProperty("Mood_Control_Mood_Level")).intValue();
      if (moodLevel>0)
      {
        Float moraleModifierFromEffects=findMoraleMultiplierFromEffects(entryProps);
        if (moraleModifierFromEffects!=null)
        {
          moraleModifier=moraleModifierFromEffects.floatValue();
        }
      }
      MoodEntry moodEntry=new MoodEntry(moodLevel,moraleModifier);
      _data.put(Integer.valueOf(moodLevel),moodEntry);
    }
  }

  private Float findMoraleMultiplierFromEffects(PropertiesSet props)
  {
    /*
    Mood_Control_Mood_Effects: 
      #1: Mood_Control_Mood_Effect_Struct 
        Mood_Control_Mood_Effect: 1879138519
        Mood_Control_Mood_Effect_Spellcraft: -1.0
    */
    Object[] array=(Object[])props.getProperty("Mood_Control_Mood_Effects");
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      int effectID=((Integer)entryProps.getProperty("Mood_Control_Mood_Effect")).intValue();
      if (effectID==0)
      {
        continue;
      }
      Float moraleMultiplier=findMoraleMultiplierFromEffect(effectID);
      if (moraleMultiplier!=null)
      {
        return moraleMultiplier;
      }
    }
    return null;
  }

  private Float findMoraleMultiplierFromEffect(int effectID)
  {
    Effect effect=_effectsLoader.getEffect(effectID);
    if (effect instanceof PropertyModificationEffect)
    {
      PropertyModificationEffect propModEffect=(PropertyModificationEffect)effect;
      StatsProvider provider=propModEffect.getStatsProvider();
      StatProvider moraleProvider=provider.getStat(WellKnownStat.MORALE);
      if (moraleProvider==null)
      {
        return null;
      }
      if (moraleProvider.getOperator()!=StatOperator.MULTIPLY)
      {
        return null;
      }
      if (moraleProvider instanceof ConstantStatProvider)
      {
        ConstantStatProvider constantStatProvider=(ConstantStatProvider)moraleProvider;
        return Float.valueOf(constantStatProvider.getValue());
      }
    }
    return null;
  }

  /**
   * Save data.
   */
  public void save()
  {
    LOGGER.info("Writing mood data");
    List<Integer> keys=new ArrayList<Integer>(_data.keySet());
    Collections.sort(keys);
    List<MoodEntry> data=new ArrayList<MoodEntry>();
    for(Integer key : keys)
    {
      data.add(_data.get(key));
    }
    boolean ok=MoodXMLWriter.writeMoodsFile(GeneratedFiles.MOOD,data);
    if (ok)
    {
      LOGGER.info("Wrote moods file: {}",GeneratedFiles.MOOD);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    MainMoodDataLoader loader=new MainMoodDataLoader(facade,effectsLoader);
    loader.doIt();
    facade.dispose();
  }
}
