package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.BitSet;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;

/**
 * Loader for map icons.
 * @author DAM
 */
public class MapIconsLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapIconsLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Do it.
   * @param imagesDir Images directory.
   */
  public void doIt(File imagesDir)
  {
    /*
UI_Map_MapNoteTypeArray: 
  #1: 
    UI_Map_MapNoteMenuImage: 1090543183
    UI_Map_MapNoteOnscreenImageStruct: 
      UI_Map_MapNoteHighlightImage: 1090543185
      UI_Map_MapNoteImage: 1090543182
      UI_Map_MapNoteSelectedImage: 1090543184
    UI_Map_MapNoteType: 2 ()
  #2: 
    UI_Map_MapNoteMenuImage: 1091733822
    UI_Map_MapNoteOnscreenImageStruct: 
      UI_Map_MapNoteHighlightImage: 1091733819
      UI_Map_MapNoteImage: 1091733821
      UI_Map_MapNoteSelectedImage: 1091733820
    UI_Map_MapNoteRelativeZLevel: 0
    UI_Map_MapNoteType: 67108864 (Task)
     */
    PropertiesSet props=_facade.loadProperties(0x78000001); // UNIQUEDB/UI_MapNoteIcons_Map
    //System.out.println(props.dump());

    Object[] mapNoteTypePropsArray=(Object[])props.getProperty("UI_Map_MapNoteTypeArray");
    for(Object mapNoteTypeObj : mapNoteTypePropsArray)
    {
      PropertiesSet mapNoteTypeProps=(PropertiesSet)mapNoteTypeObj;
      long mapNoteType=((Long)mapNoteTypeProps.getProperty("UI_Map_MapNoteType")).longValue();
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(mapNoteType);
      int index=typeSet.nextSetBit(0)+1;
      Integer imageId=null;
      PropertiesSet imageStruct=(PropertiesSet)mapNoteTypeProps.getProperty("UI_Map_MapNoteOnscreenImageStruct");
      if (imageStruct!=null)
      {
        imageId=(Integer)imageStruct.getProperty("UI_Map_MapNoteImage");
      }
      else
      {
        imageId=(Integer)mapNoteTypeProps.getProperty("UI_Map_MapNoteImage");
      }
      // Image
      if (imageId!=null)
      {
        File imageFile=new File(imagesDir,index+".png");
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),imageFile);
      }
      /*
      // Menu image
      int menuImageId=((Integer)mapNoteTypeProps.getProperty("UI_Map_MapNoteMenuImage")).intValue();
      File menuImageFile=new File(imagesDir,"menu-"+index+".png");
      DatIconsUtils.buildImageFile(_facade,menuImageId,menuImageFile);
      // Highlight image
      int highlightImageId=((Integer)imageStruct.getProperty("UI_Map_MapNoteHighlightImage")).intValue();
      File highlightImageFile=new File(imagesDir,"highlight-"+index+".png");
      DatIconsUtils.buildImageFile(_facade,highlightImageId,highlightImageFile);
      // Selected image
      int selectedImageId=((Integer)imageStruct.getProperty("UI_Map_MapNoteSelectedImage")).intValue();
      File selectedImageFile=new File(imagesDir,"selected-"+index+".png");
      DatIconsUtils.buildImageFile(_facade,selectedImageId,selectedImageFile);
      */
    }
  }
}
