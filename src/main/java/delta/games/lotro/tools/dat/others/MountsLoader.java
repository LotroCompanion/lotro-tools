package delta.games.lotro.tools.dat.others;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
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
import delta.games.lotro.utils.StringUtils;

/**
 * Get definition of mounts from DAT files.
 * @author DAM
 */
public class MountsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MountsLoader.class);

  private DataFacade _facade;
  private EnumMapper _mountType;
  private EnumMapper _category;
  private EnumMapper _subCategory;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MountsLoader(DataFacade facade)
  {
    _facade=facade;
    _mountType=facade.getEnumsManager().getEnumMapper(587203200);
    _category=facade.getEnumsManager().getEnumMapper(587202586);
    _subCategory=facade.getEnumsManager().getEnumMapper(587203478);
  }

  /**
   * Load a mount definition.
   * @param indexDataId Mount skill identifier.
   * @return the loaded mount or <code>null</code> if not loaded.
   */
  public MountDescription load(int indexDataId)
  {
    MountDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new MountDescription(indexDataId);
      // Hidden?
      //int hidden=((Integer)properties.getProperty("Collection_Hide_Entry")).intValue();
      // Name
      String name=DatUtils.getStringProperty(properties,"Skill_Name");
      name=StringUtils.fixName(name);
      ret.setName(name);

      if (!useMount(ret))
      {
        return null;
      }
      // Description
      String description=DatUtils.getStringProperty(properties,"Skill_Desc");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)properties.getProperty("Skill_Icon")).intValue();
      int largeIconId=((Integer)properties.getProperty("Skill_LargeIcon")).intValue();
      int smallIconId=((Integer)properties.getProperty("Skill_SmallIcon")).intValue();
      if ((iconId!=smallIconId) || (iconId!=largeIconId))
      {
        LOGGER.warn("Icons mismatch: small="+smallIconId+"/regular="+iconId+"/large="+largeIconId);
      }
      ret.setIconId(iconId);
      // Source description (null for war-steeds)
      String sourceDescription=DatUtils.getStringProperty(properties,"Collection_Piece_SourceDesc");
      ret.setSourceDescription(sourceDescription);
      // Hide?
      Integer hide=(Integer)properties.getProperty("Collection_Hide_Entry");
      if ((hide!=null) && (hide.intValue()!=0))
      {
        //System.out.println("Hide: "+name);
      }

      // Category
      int categoryCode=((Integer)properties.getProperty("Skill_Category")).intValue();
      if (categoryCode!=88) // Standard Mounts
      {
        String category=_category.getString(categoryCode);
        LOGGER.warn("Unexpected mount category: code="+categoryCode+", name="+category);
      }
      // Sub category
      int subCategoryCode=((Integer)properties.getProperty("Skill_SubCategory")).intValue();
      String subCategory=_subCategory.getString(subCategoryCode);
      ret.setCategory(subCategory);

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
    }
    else
    {
      LOGGER.warn("Could not handle mount skill ID="+indexDataId);
    }
    return ret;
  }

  private boolean useMount(MountDescription mount)
  {
    String mountName=mount.getName();
    return (!mountName.contains("TBD"));
  }

  /**
   * Save the loaded mounts to a file.
   * @param mounts Mounts to save.
   */
  public void saveMounts(List<MountDescription> mounts)
  {
    // Data
    Collections.sort(mounts,new IdentifiableComparator<MountDescription>());
    MountXMLWriter.write(GeneratedFiles.MOUNTS,mounts);
  }

  /**
   * Load mounts.
   */
  public void doIt()
  {
    Map<Integer,MountDescription> mounts=new HashMap<Integer,MountDescription>();
    // MountDirectory
    PropertiesSet mountsDirectoryProps=_facade.loadProperties(0x70048B29+DATConstants.DBPROPERTIES_OFFSET);
    ArrayPropertyValue skillListValue=(ArrayPropertyValue)mountsDirectoryProps.getPropertyValueByName("Mount_SkillList");
    for(PropertyValue mountSkillEntry : skillListValue.getValues())
    {
      int mountSkillId=((Integer)(mountSkillEntry.getValue())).intValue();
      PropertyDefinition propertyDef=mountSkillEntry.getDefinition();
      String propertyName=propertyDef.getName();
      boolean tall=("Mount_SkillToGrantTall".equals(propertyName));
      MountDescription mount=load(mountSkillId);
      if (mount!=null)
      {
        mount.setTall(tall);
        mounts.put(Integer.valueOf(mountSkillId),mount);
      }
    }
    // Load missing mounts
    findMissingMounts(mounts);
    LOGGER.info("Loaded "+mounts.size()+" mounts.");
    saveMounts(new ArrayList<MountDescription>(mounts.values()));
  }

  private void findMissingMounts(Map<Integer,MountDescription> mounts)
  {
    SkillsManager skillsMgr=SkillsManager.getInstance();
    LotroEnum<SkillCategory> categoryEnum=LotroEnumsRegistry.getInstance().get(SkillCategory.class);
    SkillCategory category=categoryEnum.getEntry(88); // Mounts
    List<SkillDescription> skills=skillsMgr.getSkillsByCategory(category);
    for(SkillDescription skill : skills)
    {
      Integer key=Integer.valueOf(skill.getIdentifier());
      MountDescription mount=mounts.get(key);
      if (mount==null)
      {
        mount=load(key.intValue());
        if (mount!=null)
        {
          mounts.put(key,mount);
        }
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MountsLoader(facade).doIt();
    facade.dispose();
  }
}
