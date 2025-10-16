package delta.games.lotro.tools.extraction.effects;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.AIPetEffect;
import delta.games.lotro.common.effects.ApplicationProbability;
import delta.games.lotro.common.effects.ApplyOverTimeEffect;
import delta.games.lotro.common.effects.AreaEffect;
import delta.games.lotro.common.effects.AuraEffect;
import delta.games.lotro.common.effects.BubbleEffect;
import delta.games.lotro.common.effects.ComboEffect;
import delta.games.lotro.common.effects.CooldownEffect;
import delta.games.lotro.common.effects.CountDownEffect;
import delta.games.lotro.common.effects.DispelByResistEffect;
import delta.games.lotro.common.effects.DispelEffect;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.effects.EffectDuration;
import delta.games.lotro.common.effects.EffectFlags;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.common.effects.EffectsManager;
import delta.games.lotro.common.effects.FlagEffect;
import delta.games.lotro.common.effects.GenesisEffect;
import delta.games.lotro.common.effects.InduceCombatStateEffect;
import delta.games.lotro.common.effects.InstantFellowshipEffect;
import delta.games.lotro.common.effects.InstantVitalEffect;
import delta.games.lotro.common.effects.KillProcEffect;
import delta.games.lotro.common.effects.PersistentComboEffect;
import delta.games.lotro.common.effects.PipEffect;
import delta.games.lotro.common.effects.ProcEffect;
import delta.games.lotro.common.effects.PropertyModificationEffect;
import delta.games.lotro.common.effects.RandomEffect;
import delta.games.lotro.common.effects.ReactiveVitalEffect;
import delta.games.lotro.common.effects.RecallEffect;
import delta.games.lotro.common.effects.ReviveEffect;
import delta.games.lotro.common.effects.TieredEffect;
import delta.games.lotro.common.effects.TravelEffect;
import delta.games.lotro.common.effects.VitalOverTimeEffect;
import delta.games.lotro.common.effects.io.xml.EffectXMLWriter;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.common.progressions.ProgressionUtils;
import delta.games.lotro.tools.extraction.effects.loaders.AIPetEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.AbstractEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.ApplyOverTimeEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.AreaEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.AuraEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.BubbleEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.ComboEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.CooldownEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.CountDownEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.DispelByResistEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.DispelEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.EffectLoadingUtils;
import delta.games.lotro.tools.extraction.effects.loaders.FlagEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.GenesisEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.InduceCombatStateEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.InstantFellowshipEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.InstantVitalEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.KillProcEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.PersistentComboEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.PipEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.ProcEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.PropertyModificationEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.RandomEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.ReactiveVitalEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.RecallEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.ReviveEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.TieredEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.TravelEffectLoader;
import delta.games.lotro.tools.extraction.effects.loaders.VitalOverTimeEffectLoader;
import delta.games.lotro.tools.extraction.utils.DatStatUtils;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.utils.Proxy;
import delta.games.lotro.utils.maths.Progression;

/**
 * Loads effect data.
 * @author DAM
 */
public class EffectLoader implements EffectLoadingUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(EffectLoader.class);

  private DataFacade _facade;
  private DatStatUtils _statUtils;
  private I18nUtils _i18nUtils;
  private EffectsManager _effectsMgr;
  private PlacesLoader _placesLoader;

  private Map<Class<? extends Effect>,AbstractEffectLoader<?>> _loaders;
  private Map<Integer,Class<? extends Effect>> _classes;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param placesLoader Places loader.
   */
  public EffectLoader(DataFacade facade, PlacesLoader placesLoader)
  {
    _facade=facade;
    _placesLoader=placesLoader;
    _i18nUtils=new I18nUtils("effects",facade.getGlobalStringsManager());
    _statUtils=new DatStatUtils(facade,_i18nUtils);
    _effectsMgr=EffectsManager.getInstance();
    initMaps();
  }

  /**
   * Get an effect using its identifier.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found/loaded.
   */
  public Effect getEffect(int effectId)
  {
    Effect ret=_effectsMgr.getEffectById(effectId);
    if (ret==null)
    {
      ret=loadEffect(effectId);
    }
    return ret;
  }

  /**
   * Load an effect.
   * @param effectId Effect identifier.
   * @return An effect or <code>null</code> if not found.
   */
  private Effect loadEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    if (effectProps==null)
    {
      return null;
    }
    byte[] data=_facade.loadData(effectId);
    if (data==null)
    {
      return null;
    }
    Effect ret=null;
    int classIndex=BufferUtils.getDoubleWordAt(data,4);
    Class<? extends Effect> c=_classes.get(Integer.valueOf(classIndex));
    if (c!=null)
    {
      try
      {
        ret=c.newInstance();
      }
      catch(Exception e)
      {
        LOGGER.warn("Could not build an instance of "+c,e);
      }
    }
    if (ret==null)
    {
      ret=new Effect();
    }
    ret.setId(effectId);
    _effectsMgr.addEffect(ret);
    // Name
    String effectName=_i18nUtils.getNameStringProperty(effectProps,"Effect_Name",effectId,0);
    ret.setName(effectName);
    // Description
    String description=getStringProperty(effectProps,"Effect_Definition_Description");
    ret.setDescription(description);
    // Description override
    String descriptionOverride=getStringProperty(effectProps,"Effect_Description_Override");
    ret.setDescriptionOverride(descriptionOverride);
    // Applied description
    String appliedDescription=getStringProperty(effectProps,"Effect_Applied_Description");
    ret.setAppliedDescription(appliedDescription);
    // Icon
    Integer effectIconId=(Integer)effectProps.getProperty("Effect_Icon");
    if (effectIconId!=null)
    {
      ret.setIconId(effectIconId);
    }
    // Probability
    ApplicationProbability probability=getProbability(effectProps);
    ret.setApplicationProbability(probability);
    // Duration
    EffectDuration duration=getDuration(effectProps);
    ret.setEffectDuration(duration);
    // Specifics
    AbstractEffectLoader<?> loader=_loaders.get(c);
    if (loader!=null)
    {
      loader.loadEffectSpecifics(ret,effectProps);
    }
    // Icon
    Integer iconId=ret.getIconId();
    if (iconId!=null)
    {
      String iconFilename=iconId+".png";
      File to=new File(GeneratedFiles.EFFECT_ICONS_DIR,iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(_facade,iconId.intValue(),to);
        if (!ok)
        {
          LOGGER.warn("Could not build effect icon: {}", iconFilename);
        }
      }
    }
    // Flags
    ret.setBaseFlag(EffectFlags.DEBUFF,EffectLoadingUtils.getFlag(effectProps,"Effect_Debuff"));
    ret.setBaseFlag(EffectFlags.HARMFUL,EffectLoadingUtils.getFlag(effectProps,"Effect_Harmful"));
    ret.setBaseFlag(EffectFlags.CURABLE,EffectLoadingUtils.getFlag(effectProps,"Effect_IsCurable",true));
    ret.setBaseFlag(EffectFlags.REMOVAL_ONLY_IN_COMBAT,EffectLoadingUtils.getFlag(effectProps,"Effect_RemovalOnlyInCombat",true));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_AWAKEN,EffectLoadingUtils.getFlag(effectProps,"Effect_RemoveOnAwaken",true));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_DEFEAT,EffectLoadingUtils.getFlag(effectProps,"Effect_RemoveOnDefeat"));
    ret.setBaseFlag(EffectFlags.REMOVE_ON_PULSE_RESIST,EffectLoadingUtils.getFlag(effectProps,"Effect_RemoveOnPulseResist"));
    ret.setBaseFlag(EffectFlags.SEND_TO_CLIENT,EffectLoadingUtils.getFlag(effectProps,"Effect_SentToClient"));
    ret.setBaseFlag(EffectFlags.UI_VISIBLE,EffectLoadingUtils.getFlag(effectProps,"Effect_UIVisible"));
    ret.setBaseFlag(EffectFlags.DURATION_COMBAT_ONLY,EffectLoadingUtils.getFlag(effectProps,"Effect_Duration_CombatOnly"));
    ret.setBaseFlag(EffectFlags.DURATION_EXPIRES_IN_REAL_TIME,EffectLoadingUtils.getFlag(effectProps,"Effect_Duration_ExpiresInRealTime"));
    ret.setBaseFlag(EffectFlags.DURATION_PERMANENT,EffectLoadingUtils.getFlag(effectProps,"Effect_Duration_Permanent"));
    ret.setBaseFlag(EffectFlags.AUTO_EXAMINATION,EffectLoadingUtils.getFlag(effectProps,"Effect_Display_Procedurally_Generated_Examination_Information",true));
    return ret;
  }

  @Override
  public String getStringProperty(PropertiesSet props, String propertyName)
  {
    return _i18nUtils.getStringProperty(props,propertyName);
  }

  private <T extends Effect> void registerLoader(int classIndex, AbstractEffectLoader<T> loader, Class<T> effectClass)
  {
    loader.setUtils(this);
    _loaders.put(effectClass,loader);
    _classes.put(Integer.valueOf(classIndex),effectClass);
  }

  private void initMaps()
  {
    _classes=new HashMap<Integer,Class<? extends Effect>>();
    _loaders=new HashMap<Class<? extends Effect>,AbstractEffectLoader<?>>();
    // PropertyModificationEffect
    int[] propModEffectClasses=new int[] {734, 716, 717, 752, 753, 739, 748, 764, 780, 2156, 2258, 2259, 2441, 2459, 3218, 3690, 3833};

    for(int classIndex : propModEffectClasses)
    {
      PropertyModificationEffectLoader<PropertyModificationEffect> propModEffectLoader=new PropertyModificationEffectLoader<PropertyModificationEffect>();
      propModEffectLoader.setStatUtils(_statUtils);
      registerLoader(classIndex,propModEffectLoader,PropertyModificationEffect.class);
      
    }

    ProcEffectLoader procEffectLoader=new ProcEffectLoader();
    procEffectLoader.setStatUtils(_statUtils);
    registerLoader(3686,procEffectLoader,ProcEffect.class);
    registerLoader(725,new InstantVitalEffectLoader(),InstantVitalEffect.class);
    registerLoader(755,new VitalOverTimeEffectLoader(),VitalOverTimeEffect.class);
    registerLoader(724,new InstantFellowshipEffectLoader(),InstantFellowshipEffect.class);
    ReactiveVitalEffectLoader reactiveVitalEffectLoader=new ReactiveVitalEffectLoader();
    reactiveVitalEffectLoader.setStatUtils(_statUtils);
    registerLoader(736,reactiveVitalEffectLoader,ReactiveVitalEffect.class);
    registerLoader(719,new GenesisEffectLoader(),GenesisEffect.class);
    registerLoader(769,new InduceCombatStateEffectLoader(),InduceCombatStateEffect.class);
    registerLoader(714,new DispelByResistEffectLoader(),DispelByResistEffect.class);
    registerLoader(737,new RecallEffectLoader(),RecallEffect.class);
    registerLoader(749,new TravelEffectLoader(),TravelEffect.class);
    registerLoader(767,new ComboEffectLoader(),ComboEffect.class);
    registerLoader(3124,new PersistentComboEffectLoader(),PersistentComboEffect.class);
    registerLoader(3866,new TieredEffectLoader(),TieredEffect.class);
    registerLoader(2762,new AreaEffectLoader(),AreaEffect.class);
    BubbleEffectLoader bubbleEffectLoader=new BubbleEffectLoader();
    bubbleEffectLoader.setStatUtils(_statUtils);
    registerLoader(3222,bubbleEffectLoader,BubbleEffect.class);
    CountDownEffectLoader<CountDownEffect> countDownEffectLoader=new CountDownEffectLoader<CountDownEffect>();
    countDownEffectLoader.setStatUtils(_statUtils);
    registerLoader(713,countDownEffectLoader,CountDownEffect.class);
    registerLoader(708,new ApplyOverTimeEffectLoader(),ApplyOverTimeEffect.class);
    registerLoader(744,new ReviveEffectLoader(),ReviveEffect.class);
    registerLoader(731,new PipEffectLoader(),PipEffect.class);
    registerLoader(709,new AuraEffectLoader(),AuraEffect.class);
    registerLoader(715,new DispelEffectLoader(),DispelEffect.class);
    registerLoader(2063,new RandomEffectLoader(),RandomEffect.class);
    registerLoader(718,new FlagEffectLoader(),FlagEffect.class);
    registerLoader(763,new AIPetEffectLoader(),AIPetEffect.class);
    registerLoader(3184,new CooldownEffectLoader(),CooldownEffect.class);
    KillProcEffectLoader killProcEffectLoader=new KillProcEffectLoader();
    killProcEffectLoader.setStatUtils(_statUtils);
    registerLoader(3842,killProcEffectLoader,KillProcEffect.class);
  }

  @Override
  public EffectGenerator loadGenerator(PropertiesSet generatorProps)
  {
    EffectGenerator generator=new EffectGenerator();
    loadGenerator(generatorProps,generator);
    return generator;
  }

  @Override
  public void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator)
  {
    loadGenerator(generatorProps,generator,"EffectGenerator_EffectID","EffectGenerator_EffectSpellcraft");
  }

  @Override
  public EffectGenerator loadGenerator(PropertiesSet generatorProps, String idPropName, String spellcraftPropName)
  {
    EffectGenerator ret=new EffectGenerator();
    loadGenerator(generatorProps,ret,idPropName,spellcraftPropName);
    return ret;
  }

  @Override
  public void loadGenerator(PropertiesSet generatorProps, EffectGenerator generator, String idPropName, String spellcraftPropName)
  {
    // Effect
    int effectID=((Integer)generatorProps.getProperty(idPropName)).intValue();
    Effect effect=getEffect(effectID);
    generator.setEffect(effect);
    // Spellcraft
    Float spellcraft=(Float)generatorProps.getProperty(spellcraftPropName);
    if ((spellcraft!=null) && (spellcraft.floatValue()<0))
    {
      spellcraft=null;
    }
    generator.setSpellcraft(spellcraft);
  }
  private ApplicationProbability getProbability(PropertiesSet effectProps)
  {
    Float probabilityFloat=(Float)effectProps.getProperty("Effect_ConstantApplicationProbability");
    float probability=(probabilityFloat!=null)?probabilityFloat.floatValue():1.0f;
    Float varianceFloat=(Float)effectProps.getProperty("Effect_ApplicationProbabilityVariance");
    float variance=(varianceFloat!=null)?varianceFloat.floatValue():0;
    Integer modPropertyInt=(Integer)effectProps.getProperty("Effect_ApplicationProbability_AdditiveModProp");
    int modProperty=(modPropertyInt!=null)?modPropertyInt.intValue():0;
    return ApplicationProbability.from(probability,variance,modProperty);
    // Effect_VariableApplicationProbability, type=Struct
    //   used but always:
    // Effect_VariableApplicationProbability:
    //   Effect_VariableMax: 1.0
    //   Effect_VariableMin: 1.0
    // Effect_ApplicationProbabilityProgression: never used?
    // Effect_SpecialApplicationProbability: never used?
    // Effect_SuppressApplicationProbabilityExamination: never used?
    // Effect_ApplicationProbability_AdditiveModProp_Array: never used?
  }

  private EffectDuration getDuration(PropertiesSet effectProps)
  {
    // Constant duration
    // - value
    Float duration=(Float)effectProps.getProperty("Effect_Duration_ConstantInterval");
    if (duration!=null)
    {
      if (Math.abs(duration.floatValue())<0.0001)
      {
        duration=null;
      }
    }
    // - modifiers
    ModPropertyList durationModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_Duration_ConstantInterval_ModifierList");
    // Pulse count
    // - value
    Integer pulseCountInt=(Integer)effectProps.getProperty("Effect_Duration_ConstantPulseCount");
    int pulseCount=(pulseCountInt!=null)?pulseCountInt.intValue():0;
    // - modifiers
    ModPropertyList pulseCountModifiers=ModifiersUtils.getStatModifiers(effectProps,"Effect_PulseCount_AdditiveModifiers");
    // Effect_Duration_ProgressionInterval: unused?
    // Effect_Duration_ProgressionPulseCount: unused?
    EffectDuration ret=new EffectDuration();
    ret.setDuration(duration);
    ret.setDurationModifiers(durationModifiers);
    ret.setPulseCount(pulseCount);
    ret.setPulseCountModifiers(pulseCountModifiers);
    return ret;
  }

  @Override
  public Proxy<Effect> buildProxy(Integer effectID)
  {
    if ((effectID==null) || (effectID.intValue()==0))
    {
      return null;
    }
    Proxy<Effect> proxy=null;
    Effect effect=getEffect(effectID.intValue());
    if (effect!=null)
    {
      proxy=new Proxy<Effect>();
      proxy.setId(effectID.intValue());
      proxy.setName(effect.getName());
      proxy.setObject(effect);
    }
    return proxy;
  }

  @Override
  public PropertiesSet loadProperties(int id)
  {
    return _facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
  }

  @Override
  public Progression getProgression(int id)
  {
    return ProgressionUtils.getProgression(_facade,id);
  }

  @Override
  public ExtendedPosition getPositionForName(String telepad)
  {
    return _placesLoader.getPositionForName(telepad);
  }

  /**
   * Save loaded data.
   */
  public void save()
  {
    // Effects
    EffectXMLWriter w=new EffectXMLWriter();
    List<Effect> effects=_effectsMgr.getEffects();
    w.write(GeneratedFiles.EFFECTS,effects);
    _i18nUtils.save();
    // Progressions
    ProgressionUtils.PROGRESSIONS_MGR.writeToFile(GeneratedFiles.PROGRESSIONS_EFFECTS);
  }
}
