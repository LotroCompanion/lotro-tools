package delta.games.lotro.tools.extraction.skills.mounts;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MountType;
import delta.games.lotro.common.enums.SkillCharacteristicSubCategory;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.collections.mounts.MountDescription;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Get definition of mounts from DAT files.
 * @author DAM
 */
public class MountsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MountsLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private LotroEnum<MountType> _mountType;
  private LotroEnum<SkillCharacteristicSubCategory> _subCategory;
  private Map<Integer,MountDescription> _mounts;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n utils.
   */
  public MountsLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    _mountType=registry.get(MountType.class);
    _subCategory=registry.get(SkillCharacteristicSubCategory.class);
    _mounts=new HashMap<Integer,MountDescription>();
  }

  /**
   * Load a mount data.
   * @param properties Input properties.
   * @param ret Storage for loaded data.
   */
  public void loadMountData(PropertiesSet properties, MountDescription ret)
  {
    if (!useMount(ret))
    {
      return;
    }
    // Hidden?
    @SuppressWarnings("unused")
    Integer hidden=(Integer)properties.getProperty("Collection_Hide_Entry");
    // Source description (null for war-steeds)
    String sourceDescription=_i18n.getStringProperty(properties,"Collection_Piece_SourceDesc");
    ret.setSourceDescription(sourceDescription);

    // Sub category
    int subCategoryCode=((Integer)properties.getProperty("Skill_SubCategory")).intValue();
    SkillCharacteristicSubCategory subCategory=_subCategory.getEntry(subCategoryCode);
    ret.setMountCategory(subCategory);
    // Peer mount
    Integer peerMountId=(Integer)properties.getProperty("Skill_MountRacialConversionAnalog");
    if (peerMountId!=null)
    {
      ret.setPeerMountId(peerMountId.intValue());
    }

    Object[] effectsList=(Object[])properties.getProperty("Skill_Toggle_Effect_List");
    if (effectsList!=null)
    {
      for(Object effectObj : effectsList)
      {
        PropertiesSet effectRefProps=(PropertiesSet)effectObj;
        int effectId=((Integer)effectRefProps.getProperty("Skill_Toggle_Effect")).intValue();
        PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);

        // Initial Name
        String initialName=DatStringUtils.getStringProperty(effectProps,"Mount_Name_Initial");
        if (initialName==null) continue;
        initialName=_i18n.getStringProperty(effectProps,"Mount_Name_Initial",I18nUtils.OPTION_REMOVE_TRAILING_MARK);
        ret.setInitialName(initialName);
        // Mount type
        int mountTypeCode=((Integer)effectProps.getProperty("Mount_Type")).intValue();
        BitSet mountTypesBitSet=BitSetUtils.getBitSetFromFlags(mountTypeCode);
        List<MountType> mountTypes=_mountType.getFromBitSet(mountTypesBitSet);
        MountType mountType=mountTypes.get(0);
        ret.setMountType(mountType);
        if (mountTypes.size()>1)
        {
          LOGGER.warn("More than one mount type: "+mountTypes);
        }
        // Morale
        int morale=((Integer)effectProps.getProperty("Mount_Durability_Base_Max")).intValue();
        ret.setMorale(morale);
        // Speed (always present)
        Float speed=null;
        Object[] mods=(Object[])effectProps.getProperty("Mod_Array");
        for(Object modObj : mods)
        {
          PropertiesSet modProps=(PropertiesSet)modObj;
          speed=(Float)modProps.getProperty("ForwardSource_Movement_MountSpeedMultiplier");
          if (speed!=null)
          {
            ret.setSpeed(speed.floatValue());
            break;
          }
        }
        // Tall or small?
        Object scale=effectProps.getProperty("Mounted_Player_Cosmetic_Slot_Scale");
        ret.setTall(scale==null);
      }
    }
    _mounts.put(Integer.valueOf(ret.getIdentifier()),ret);
  }

  /**
   * Use mount or not.
   * @param mount Mount to test.
   * @return <code>true</code> to use it, <code>false</code> otherwise.
   */
  public boolean useMount(MountDescription mount)
  {
    String mountName=mount.getName();
    return (!mountName.contains("TBD"));
  }

  /**
   * Load size data.
   */
  public void loadSizeData()
  {
    // MountDirectory
    PropertiesSet mountsDirectoryProps=_facade.loadProperties(0x70048B29+DATConstants.DBPROPERTIES_OFFSET);
    ArrayPropertyValue skillListValue=(ArrayPropertyValue)mountsDirectoryProps.getPropertyValueByName("Mount_SkillList");
    for(PropertyValue mountSkillEntry : skillListValue.getValues())
    {
      int mountSkillId=((Integer)(mountSkillEntry.getValue())).intValue();
      PropertyDefinition propertyDef=mountSkillEntry.getDefinition();
      String propertyName=propertyDef.getName();
      boolean tall=("Mount_SkillToGrantTall".equals(propertyName));
      MountDescription mount=_mounts.get(Integer.valueOf(mountSkillId));
      if (mount!=null)
      {
        mount.setTall(tall);
      }
    }
  }
}
