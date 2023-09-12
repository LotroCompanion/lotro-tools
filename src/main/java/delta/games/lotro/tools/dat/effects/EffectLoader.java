package delta.games.lotro.tools.dat.effects;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.io.streams.IndentableStream;
import delta.games.lotro.common.effects.DumpEffect2;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.FellowshipEffect;
import delta.games.lotro.common.effects.ProcEffect;
import delta.games.lotro.common.effects.StatsEffect;
import delta.games.lotro.common.effects.VitalInstantChangeEffect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillType;
import delta.games.lotro.common.stats.StatDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.tools.dat.utils.DatStatUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.ProgressionUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader
{
  private static final Logger LOGGER=Logger.getLogger(EffectLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18nUtils;
  private Map<Integer,Effect2> _loadedEffects;
  private DumpEffect2 _dump;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public EffectLoader(DataFacade facade)
  {
    _facade=facade;
    //_i18nUtils=new I18nUtils("effects2",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18nUtils);
    _loadedEffects=new HashMap<Integer,Effect2>();
    IndentableStream is=new IndentableStream(System.out);
    _dump=new DumpEffect2(is);
  }

  /**
   * Constructor.
   * @param statUtils
   * @param i18nUtils
   */
  public EffectLoader(DatStatUtils statUtils, I18nUtils i18nUtils)
  {
    _statUtils=statUtils;
    _facade=statUtils.getFacade();
    _i18nUtils=i18nUtils;
    _loadedEffects=new HashMap<Integer,Effect2>();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect2 getEffect(int effectId)
  {
    Integer key=Integer.valueOf(effectId);
    if (_loadedEffects.containsKey(key))
    {
      return _loadedEffects.get(key);
    }
    Effect2 ret=loadEffect(effectId);
    _loadedEffects.put(key,ret);
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  public Effect2 loadEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps==null)
    {
      return null;
    }
    //System.out.println(effectProps.dump());
    Effect2 ret=new Effect2();
    ret.setId(effectId);
    // Name
    String effectName;
    if (_i18nUtils!=null)
    {
      effectName=_i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
    }
    else
    {
      effectName=DatUtils.getStringProperty(effectProps,"Effect_Name");
    }
    ret.setName(effectName);
    // Description
    String description;
    if (_i18nUtils!=null)
    {
      description=_i18nUtils.getStringProperty(effectProps,"Effect_Definition_Description");
    }
    else
    {
      description=DatUtils.getStringProperty(effectProps,"Effect_Definition_Description");
    }
    ret.setDescription(description);
    // Description
    String appliedDescription;
    if (_i18nUtils!=null)
    {
      appliedDescription=_i18nUtils.getStringProperty(effectProps,"Effect_Applied_Description");
    }
    else
    {
      appliedDescription=DatUtils.getStringProperty(effectProps,"Effect_Applied_Description");
    }
    ret.setAppliedDescription(appliedDescription);

    // Also:
    // From: Effect_Description_Override: ${PROCPROBABILITY} chance on heal to proc,
    //private String _descriptionOverride;

    // Icon
    Integer effectIconId=(Integer)effectProps.getProperty("Effect_Icon");
    if (effectIconId!=null)
    {
      ret.setIconId(effectIconId);
    }
    // TODO: use Effect_Applied_Description too!
    // Aspects
    loadAspects(ret,effectProps);

    _dump.dumpEffect(ret);
    return ret;
  }

  private void loadAspects(Effect2 effect, PropertiesSet effectProps)
  {
    StatsEffect statsAspect=loadStatsAspect(effectProps);
    if (statsAspect!=null)
    {
      effect.addAspect(statsAspect);
    }
    ProcEffect procEffect=loadProcAspect(effectProps);
    if (procEffect!=null)
    {
      effect.addAspect(procEffect);
    }
    VitalInstantChangeEffect vitalInstanceChange=loadVitalInstantChangeAspect(effectProps);
    if (vitalInstanceChange!=null)
    {
      effect.addAspect(vitalInstanceChange);
    }
    FellowshipEffect fellowship=loadFellowhipAspect(effectProps);
    if (fellowship!=null)
    {
      effect.addAspect(fellowship);
    }
  }

  private StatsEffect loadStatsAspect(PropertiesSet effectProps)
  {
    Object modArray=effectProps.getProperty("Mod_Array");
    if (modArray==null)
    {
      return null;
    }
    StatsEffect ret=null;
    // Stats
    StatsProvider statsProvider=_statUtils.buildStatProviders(effectProps);
    if (statsProvider.getNumberOfStatProviders()>0)
    {
      ret=new StatsEffect();
      ret.setStatsProvider(statsProvider);
    }
    return ret;
  }

  private ProcEffect loadProcAspect(PropertiesSet effectProps)
  {
    Object[] userEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_UserEffectList");
    Object[] targetEffectsList=(Object[])effectProps.getProperty("EffectGenerator_SkillProc_TargetEffectList");
    if ((userEffectsList==null) && (targetEffectsList==null))
    {
      return null;
    }
    
    ProcEffect ret=new ProcEffect();
    // Skill types
    Long skillTypeFlags=(Long)effectProps.getProperty("Effect_SkillProc_SkillTypes");
    if (skillTypeFlags!=null)
    {
      BitSet bitset=BitSetUtils.getBitSetFromFlags(skillTypeFlags.longValue());
      LotroEnum<SkillType> skillTypesEnum=LotroEnumsRegistry.getInstance().get(SkillType.class);
      List<SkillType> skillTypes=skillTypesEnum.getFromBitSet(bitset);
      ret.setSkillTypes(skillTypes);
    }
    // Probability
    Float probability=(Float)effectProps.getProperty("Effect_SkillProc_ProcProbability");
    ret.setProcProbability(probability);
    // Proc'ed effects
    if (userEffectsList!=null)
    {
      for(Object entry : userEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addProcedEffect(generator);
      }
    }
    if (targetEffectsList!=null)
    {
      for(Object entry : targetEffectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addProcedEffect(generator); // TODO Distinguish User/Target
      }
    }
    // Cooldown
    Float cooldown=(Float)effectProps.getProperty("Effect_MinTimeBetweenProcs");
    ret.setCooldown(cooldown);
    return ret;
  }

  private VitalInstantChangeEffect loadVitalInstantChangeAspect(PropertiesSet effectProps)
  {
    Integer vitalType=(Integer)effectProps.getProperty("Effect_BaseVital_VitalType");
    if (vitalType==null)
    {
      return null;
    }
    Float constant=(Float)effectProps.getProperty("Effect_InstantVital_InitialChangeConstant");
    Integer progressionID=(Integer)effectProps.getProperty("Effect_InstantVital_InitialChangeProgression");
    Float min=(Float)effectProps.getProperty("Effect_RandomValueMin");
    Float max=(Float)effectProps.getProperty("Effect_RandomValueMax");
    if (!((constant!=null) || (progressionID!=null) || ((min!=null) && (max!=null))))
    {
      LOGGER.warn("No value data for a VitalInstantChangeEffect!");
      return null;
    }

    VitalInstantChangeEffect ret=new VitalInstantChangeEffect();
    // Stat
    StatDescription stat=DatStatUtils.getStatFromVitalType(vitalType.intValue());
    ret.setStat(stat);
    // Multiplicative?
    Integer multiplicativeInt=(Integer)effectProps.getProperty("Effect_InstantVital_Multiplicative");
    boolean multiplicative=((multiplicativeInt!=null)&&(multiplicativeInt.intValue()==1));
    ret.setMultiplicative(multiplicative);

    // Initial change: constant, progression or range:
    if (constant!=null)
    {
      ret.setConstant(constant.floatValue());
    }
    else if (progressionID!=null)
    {
      Progression progression=ProgressionUtils.getProgression(_facade,progressionID.intValue());
      ret.setProgression(progression);
    }
    else
    {
      ret.setMinValue(min.floatValue());
      ret.setMaxValue(max.floatValue());
    }
    return ret;
  }

  private FellowshipEffect loadFellowhipAspect(PropertiesSet effectProps)
  {
    Object[] effectsList=(Object[])effectProps.getProperty("EffectGenerator_InstantFellowship_AppliedEffectList");
    if (effectsList==null)
    {
      return null;
    }
    FellowshipEffect ret=new FellowshipEffect();
    // Effects
    if (effectsList!=null)
    {
      for(Object entry : effectsList)
      {
        PropertiesSet entryProps=(PropertiesSet)entry;
        EffectGenerator generator=loadGenerator(entryProps);
        ret.addEffect(generator);
      }
    }
    // Flags
    Integer raidGroups=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToRaidGroups");
    if (raidGroups!=null)
    {
      ret.setAppliesToRaidGroups(raidGroups.intValue()==1);
    }
    Integer pets=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToPets");
    if (pets!=null)
    {
      ret.setAppliesToPets(pets.intValue()==1);
    }
    Integer target=(Integer)effectProps.getProperty("Effect_InstantFellowship_ApplyToTarget");
    if (target!=null)
    {
      ret.setAppliesToTarget(target.intValue()==1);
    }
    // Range
    Float range=(Float)effectProps.getProperty("Effect_InstantFellowship_MaxRange");
    if ((range!=null) && (range.floatValue()>0))
    {
      ret.setRange(range.floatValue());
    }
    return ret;
  }

  private EffectGenerator loadGenerator(PropertiesSet generatorProps)
  {
    int effectID=((Integer)generatorProps.getProperty("EffectGenerator_EffectID")).intValue(); 
    Float spellcraft=(Float)generatorProps.getProperty("EffectGenerator_EffectSpellcraft");
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    Effect2 effect=getEffect(effectID);
    EffectGenerator ret=new EffectGenerator(effect,spellcraft);
    return ret;
  }
}
