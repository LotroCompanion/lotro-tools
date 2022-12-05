package delta.games.lotro.tools.dat.misc;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import delta.games.lotro.character.gear.GearSlot;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.ui.UIData;
import delta.games.lotro.dat.data.ui.UIElement;
import delta.games.lotro.dat.data.ui.UIFinder;
import delta.games.lotro.dat.data.ui.UIImage;
import delta.games.lotro.dat.data.ui.UILayout;
import delta.games.lotro.dat.loaders.ui.UILayoutLoader;
import delta.games.lotro.dat.utils.DatIconsUtils;

/**
 * Loader for equipment slot icons.
 * @author DAM
 */
public class SlotIconsLoader
{
  private static final Logger LOGGER=Logger.getLogger(SlotIconsLoader.class);

  private DataFacade _facade;
  private EnumMapper _uiElementId;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public SlotIconsLoader(DataFacade facade)
  {
    _facade=facade;
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
  }

  /**
   * Load icons.
   */
  public void doIt()
  {
    UILayout layout=new UILayoutLoader(_facade).loadUiLayout(0x2200084B);
    UIFinder finder=new UIFinder(_facade);
    String[] path={
        "RealPaperdollField",
        "RealPaperdollField_Player",
        "CharacterEquipmentPage_AvatarInfoElement",
        "CharacterEquipmentPage_SlotField"
    };
    UIElement parent=(UIElement)finder.find(layout,path);
    List<UIElement> slotElements=parent.getChildElements();
    for(UIElement slotElement : slotElements)
    {
      int id=slotElement.getIdentifier();
      String slotIdStr=_uiElementId.getString(id);
      GearSlot slot=getSlotFromId(slotIdStr);
      if (slot!=null)
      {
        List<UIData> datas=slotElement.getData();
        for(UIData data : datas)
        {
          if (data instanceof UIImage)
          {
            UIImage imageData=(UIImage)data;
            int imageDID=imageData.getImageDID();
            BufferedImage image=DatIconsUtils.buildImage(_facade,new int[]{imageDID});
            Image cropedIcon=Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),new CropImageFilter(6,6,32,32)));
            int width=cropedIcon.getWidth(null);
            int height=cropedIcon.getHeight(null);
            BufferedImage bufferedImage=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics big = bufferedImage.getGraphics();
            big.drawImage(cropedIcon, 0, 0, null);
            File to=new File("../lotro-companion/src/main/resources/resources/gui/equipment/"+slot.name()+".png");
            try
            {
              ImageIO.write(bufferedImage,"png",to);
            }
            catch(Exception e)
            {
              LOGGER.warn("Could not write image file: "+to,e);
            }
          }
        }
      }
    }
  }

  private GearSlot getSlotFromId(String slotId)
  {
    if ("BackSlotParent".equals(slotId)) return GearSlot.BACK;
    if ("BootsSlotParent".equals(slotId)) return GearSlot.FEET;
    if ("Bracelet1SlotParent".equals(slotId)) return GearSlot.LEFT_WRIST;
    if ("Bracelet2SlotParent".equals(slotId)) return GearSlot.RIGHT_WRIST;
    if ("Weapon1SlotParent".equals(slotId)) return GearSlot.MAIN_MELEE;
    if ("Weapon2SlotParent".equals(slotId)) return GearSlot.OTHER_MELEE;
    if ("WeaponRangedSlotParent".equals(slotId)) return GearSlot.RANGED;
    if ("Earring1SlotParent".equals(slotId)) return GearSlot.LEFT_EAR;
    if ("Earring2SlotParent".equals(slotId)) return GearSlot.RIGHT_EAR;
    if ("HeadSlotParent".equals(slotId)) return GearSlot.HEAD;
    if ("ShoulderSlotParent".equals(slotId)) return GearSlot.SHOULDER;
    if ("Pocket1SlotParent".equals(slotId)) return GearSlot.POCKET;
    if ("NecklaceSlotParent".equals(slotId)) return GearSlot.NECK;
    if ("CraftToolSlotParent".equals(slotId)) return GearSlot.TOOL;
    if ("ChestSlotParent".equals(slotId)) return GearSlot.BREAST;
    if ("LegsSlotParent".equals(slotId)) return GearSlot.LEGS;
    if ("GlovesSlotParent".equals(slotId)) return GearSlot.HANDS;
    if ("Ring1SlotParent".equals(slotId)) return GearSlot.LEFT_FINGER;
    if ("Ring2SlotParent".equals(slotId)) return GearSlot.RIGHT_FINGER;
    if ("ClassSlotParent".equals(slotId)) return GearSlot.CLASS_ITEM;
    if ("Weapon1FXSlotParent".equals(slotId)) return GearSlot.MAIN_HAND_AURA;
    if ("Weapon2FXSlotParent".equals(slotId)) return GearSlot.OFF_HAND_AURA;
    if ("WeaponRangedFXSlotParent".equals(slotId)) return GearSlot.RANGED_AURA;
    return null;
  }
}
