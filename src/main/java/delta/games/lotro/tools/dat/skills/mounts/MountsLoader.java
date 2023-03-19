package delta.games.lotro.tools.dat.skills.mounts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.lore.collections.mounts.MountDescription;
import delta.games.lotro.lore.collections.mounts.io.xml.MountXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get definition of mounts from DAT files.
 * @author DAM
 */
public class MountsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MountsLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  private EnumMapper _mountType;
  private EnumMapper _subCategory;
  private Map<Integer,MountDescription> _mounts=new HashMap<Integer,MountDescription>();

  /**
   * Constructor.
   * @param facade Data facade.
   * @param i18n I18n utils.
   */
  public MountsLoader(DataFacade facade, I18nUtils i18n)
  {
    _facade=facade;
    _i18n=i18n;
    _mountType=facade.getEnumsManager().getEnumMapper(587203200);
    _subCategory=facade.getEnumsManager().getEnumMapper(587203478);
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
    //int hidden=((Integer)properties.getProperty("Collection_Hide_Entry")).intValue();
    // Source description (null for war-steeds)
    String sourceDescription=DatUtils.getStringProperty(properties,"Collection_Piece_SourceDesc");
    ret.setSourceDescription(sourceDescription);
    // Hide?
    Integer hide=(Integer)properties.getProperty("Collection_Hide_Entry");
    if ((hide!=null) && (hide.intValue()!=0))
    {
      //System.out.println("Hide: "+name);
    }

    // Sub category
    int subCategoryCode=((Integer)properties.getProperty("Skill_SubCategory")).intValue();
    String subCategory=_subCategory.getString(subCategoryCode);
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
        String initialName=DatUtils.getStringProperty(effectProps,"Mount_Name_Initial");
        if (initialName==null) continue;
        ret.setInitialName(initialName);
        // Mount type
        int mountTypeCode=((Integer)effectProps.getProperty("Mount_Type")).intValue();
        BitSet mountTypesBitSet=BitSetUtils.getBitSetFromFlags(mountTypeCode);
        String mountTypes=BitSetUtils.getStringFromBitSet(mountTypesBitSet,_mountType,"/");
        ret.setMountType(mountTypes);
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

  private boolean useMount(MountDescription mount)
  {
    String mountName=mount.getName();
    return (!mountName.contains("TBD"));
  }

  /**
   * Save the loaded mounts to a file.
   */
  public void saveMounts()
  {
    LOGGER.info("Loaded "+_mounts.size()+" mounts.");
    List<MountDescription> mounts=new ArrayList<MountDescription>(_mounts.values());
    Collections.sort(mounts,new IdentifiableComparator<MountDescription>());
    MountXMLWriter.write(GeneratedFiles.MOUNTS,mounts);
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
