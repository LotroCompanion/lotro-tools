package delta.games.lotro.tools.lore.traitPoints;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.stats.traitPoints.TraitPoint;
import delta.games.lotro.stats.traitPoints.TraitPointCategories;
import delta.games.lotro.stats.traitPoints.TraitPointsRegistry;
import delta.games.lotro.stats.traitPoints.io.xml.TraitPointsRegistryXMLWriter;

/**
 * Builder for the trait points registry.
 * @author DAM
 */
public class TraitPointsRegistryBuilder
{
  private static final Logger LOGGER=Logger.getLogger(TraitPointsRegistryBuilder.class);

  private TraitPointsRegistry _registry;

  /**
   * Constructor.
   */
  public TraitPointsRegistryBuilder()
  {
    _registry=new TraitPointsRegistry();
  }

  /**
   * Get the built registry.
   * @return the trait points registry.
   */
  public TraitPointsRegistry getRegistry()
  {
    return _registry;
  }

  private void doIt()
  {
    buildRegistry();
    checks();
    // Target file
    File toFile=new File("traitPoints.xml").getAbsoluteFile();
    TraitPointsRegistryXMLWriter writer=new TraitPointsRegistryXMLWriter();
    boolean ok=writer.write(toFile,_registry,EncodingNames.UTF_8);
    if (ok)
    {
      LOGGER.info("Wrote file: "+toFile);
    }
    else
    {
      LOGGER.error("Failed to build trait points registry file: "+toFile);
    }
  }

  private void checks()
  {
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      List<TraitPoint> points=_registry.getPointsForClass(cClass);
      System.out.println(cClass+":"+points.size());
    }
    List<TraitPoint> all=_registry.getAll();
    System.out.println("All:"+all.size());
  }

  private void buildRegistry()
  {
    buildGenericPoints();
    buildClassPoints();
    buildClassDeeds();
  }

  private void buildGenericPoints()
  {
    String category=TraitPointCategories.EPIC;
    initPoint("Epic Battles 1", category, "Earn 100 Promotion Points", null);
    initPoint("Epic Battles 2", category, "Earn 200 Promotion Points", null);

    category=TraitPointCategories.QUESTS;
    initPoint("West Rohan:Kingstead", category, "Complete Kingstead quest chain", null);
    initPoint("West Rohan:Eastfold", category, "Complete Eastfold quest chain", null);
    initPoint("West Rohan:Broadacres", category, "Complete Broadacres quest chain", null);
    initPoint("West Rohan:Stonedeans", category, "Complete Stonedeans quest chain", null);
    initPoint("West Rohan:Westfold", category, "Complete Westfold quest chain", null);

    initPoint("Central Gondor:Ringló Vale", category, "Complete Ringló Vale quest chain", null);
    initPoint("Central Gondor:Dor-en-Ernil", category, "Complete Dor-en-Ernil quest chain", null);
    initPoint("Central Gondor:Lebennin", category, "Complete Lebennin quest chain", null);
    initPoint("Central Gondor:Pelargir", category, "Complete Pelargir quest chain", null);

    initPoint("Eastern Gondor:AshesAndStars", category, "Complete Ashes and Stars, Chapter 4", null);

    category=TraitPointCategories.EPIC;
    initPoint("EpicVol4Book2", category, "Complete Volume IV, Book 2: The Dawnless Day", null);
    initPoint("EpicVol4Book4Chapter10", category, "Complete Volume IV, Book 4, Chapter 10: The Defence of Minas Tirith", null);
    initPoint("EpicVol4Book4Chapter11", category, "Complete Volume IV, Book 4, Chapter 11: Hammer of the Underworld", null);
    initPoint("EpicVol4Book8Chapter7", category, "Complete Volume IV, Book 8, Chapter 7: Mordor Triumphant", null);

    category=TraitPointCategories.DEEDS;
    initPoint("OldAnórienQuests", category, "Complete Deed 'Quests of Old Anórien'", null);
    initPoint("OldAnórienDeeds", category, "Complete Deed 'Deeds of Old Anórien'", null);
  }

  private void buildClassPoints()
  {
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String category=TraitPointCategories.CLASS;
      String key=cClass.getKey();
      // TODO change label according to class
      initPoint(key+":LegendaryBook1", category, "Complete Legendary Book Pages 1", cClass);
      initPoint(key+":LegendaryBook2", category, "Complete Legendary Book Pages 2", cClass);
      initPoint(key+":LegendaryBook3", category, "Complete Legendary Book Pages 3", cClass);
      // TODO change label according to class
      initPoint(key+":ClassQuests50", category, "Complete the Level 50 Class Quests", cClass);
      // TODO change label according to class
      initPoint(key+":ClassQuests58", category, "Complete the Level 58 Class Quest Chain", cClass);
      if (cClass!=CharacterClass.BEORNING)
      {
        // TODO change label according to class
        initPoint(key+":ReadGuardsBook", category, "Obtain Kindred with the Iron Garrison Guards and read their book", cClass);
      }
    }
    // Epic
    TraitPoint epicVol2Book6=initPoint("EpicVol2Book6", TraitPointCategories.EPIC, "Complete Volume II, Book 6 (Moria)", null);
    for(CharacterClass characterClass : CharacterClass.ALL_CLASSES)
    {
      if (characterClass!=CharacterClass.BEORNING)
      {
        epicVol2Book6.addRequiredClass(characterClass);
      }
    }

    // Add Beorning specifics
    initPoint("Beorning:ClassQuests15", TraitPointCategories.CLASS, "Complete the Level 15 Class Quest Chain", CharacterClass.BEORNING);
    initPoint("Beorning:ClassQuests30", TraitPointCategories.CLASS, "Complete the Level 30 Class Quest Chain", CharacterClass.BEORNING);
  }

  private void buildClassDeeds()
  {
    String category=TraitPointCategories.CLASS;
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String key=cClass.getKey();
      for(int i=1;i<=8;i++)
      {
        initPoint(key+":ClassDeed"+i, category, "Class Deeds ("+key+") - Tier "+i, cClass);
      }
    }
  }

  private TraitPoint initPoint(String id, String category, String label, CharacterClass requiredCharacterClass)
  {
    TraitPoint point=new TraitPoint(id);
    point.setLabel(label);
    point.setCategory(category);
    if (requiredCharacterClass!=null)
    {
      point.addRequiredClass(requiredCharacterClass);
    }
    _registry.registerTraitPoint(point);
    return point;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    TraitPointsRegistryBuilder builder=new TraitPointsRegistryBuilder();
    builder.doIt();
  }
}
