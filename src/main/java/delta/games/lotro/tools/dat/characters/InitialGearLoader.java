package delta.games.lotro.tools.dat.characters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.classes.initialGear.InitialGearDefinition;
import delta.games.lotro.character.classes.initialGear.InitialGearElement;
import delta.games.lotro.character.classes.initialGear.io.xml.InitialGearXMLWriter;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Loader for initial gear data.
 * @author DAM
 */
public class InitialGearLoader
{
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public InitialGearLoader(DataFacade facade)
  {
    _facade=facade;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<InitialGearDefinition> gearDefinitions=new ArrayList<InitialGearDefinition>();
    for(ClassDescription characterClass : ClassesManager.getInstance().getAllCharacterClasses())
    {
      int classId=characterClass.getIdentifier();
      String classKey=characterClass.getKey();
      InitialGearDefinition gearDefinition=handleClass(classId,classKey);
      gearDefinitions.add(gearDefinition);
    }
    File to=GeneratedFiles.INITIAL_GEAR;
    InitialGearXMLWriter.write(to,gearDefinitions);
  }

  private InitialGearDefinition handleClass(int classId, String classKey)
  {
    PropertiesSet properties=_facade.loadProperties(classId+DATConstants.DBPROPERTIES_OFFSET);
    // Initial gear:
    InitialGearDefinition initialGear=new InitialGearDefinition(classKey);
    // AdvTable_StartingInventory_List: initial gear at level 1
    Object[] inventory=(Object[])properties.getProperty("AdvTable_StartingInventory_List");
    for(Object inventoryElement : inventory)
    {
      PropertiesSet inventoryElementProps=(PropertiesSet)inventoryElement;
      int startsEquipped=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_StartsEquipped")).intValue();
      int quantity=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_Quantity")).intValue();
      if ((startsEquipped>0) && (quantity==1))
      {
        int itemId=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_Item")).intValue();
        InitialGearElement element=new InitialGearElement();
        Item item=ItemsManager.getInstance().getItem(itemId);
        element.setItem(item);
        int raceId=((Integer)inventoryElementProps.getProperty("AdvTable_StartingInventory_RequiredRace")).intValue();
        if (raceId!=0)
        {
          RaceDescription race=RacesManager.getInstance().getByCode(raceId);
          element.setRequiredRace(race);
        }
        initialGear.addGearElement(element);
      }
    }
    return initialGear;
  }
}
