package delta.games.lotro.tools.lore.traitPoints;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.stats.traitPoints.TraitPoint;
import delta.games.lotro.stats.traitPoints.TraitPointCategories;
import delta.games.lotro.stats.traitPoints.TraitPointsRegistry;
import delta.games.lotro.stats.traitPoints.comparators.TraitPointLabelComparator;
import delta.games.lotro.stats.traitPoints.io.xml.TraitPointsRegistryXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

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
    File toFile=GeneratedFiles.TRAIT_POINTS;
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
      Collections.sort(points,new TraitPointLabelComparator());
      System.out.println(cClass+":"+points.size());
      for(TraitPoint point : points)
      {
        System.out.println("\t"+point.getLabel()+" -- "+point.getId());
      }
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
    initPoint("West Rohan:Kingstead", category, "West Rohan: complete Kingstead quest chain", null);
    initPoint("West Rohan:Eastfold", category, "West Rohan: complete Eastfold quest chain", null);
    initPoint("West Rohan:Broadacres", category, "West Rohan: complete Broadacres quest chain", null);
    initPoint("West Rohan:Stonedeans", category, "West Rohan: complete Stonedeans quest chain", null);
    initPoint("West Rohan:Westfold", category, "West Rohan: complete Westfold quest chain", null);

    initPoint("Central Gondor:Ringló Vale", category, "Central Gondor: complete Ringló Vale quest chain", null);
    initPoint("Central Gondor:Dor-en-Ernil", category, "Central Gondor: complete Dor-en-Ernil quest chain", null);
    initPoint("Central Gondor:Lebennin", category, "Central Gondor: complete Lebennin quest chain", null);
    initPoint("Central Gondor:Pelargir", category, "Central Gondor: complete Pelargir quest chain", null);

    initPoint("Eastern Gondor:AshesAndStars", category, "Osgiliath: complete Ashes and Stars, Chapter 4", null);

    category=TraitPointCategories.EPIC;
    initPoint("EpicVol4Book2", category, "Complete Volume IV, Book 2: The Dawnless Day", null);
    initPoint("EpicVol4Book4Chapter10", category, "Complete Volume IV, Book 4, Chapter 10: The Defence of Minas Tirith", null);
    initPoint("EpicVol4Book4Chapter11", category, "Complete Volume IV, Book 4, Chapter 11: Hammer of the Underworld", null);
    initPoint("EpicVol4Book8Chapter7", category, "Complete Volume IV, Book 8, Chapter 7: Mordor Triumphant", null);
    initPoint("EpicVol4Book9Chapter5", category, "Complete Volume IV, Book 9, Chapter 5: The Next Adventure", null);
    initPoint("BlackBookMordor4.4", category, "Complete The Black Book of Mordor, Chapter  4.4: Union of Evil", null);
    initPoint("BlackBookMordor5.5", category, "Complete The Black Book of Mordor, Chapter  5.5: The Walls Brought Down", null);
    initPoint("BlackBookMordor8.7", category, "Complete The Black Book of Mordor, Chapter  8.7: The First Promise", null);
    initPoint("BlackBookMordor10.7", category, "Complete The Black Book of Mordor, Chapter 10.7: The Arrival of the Wise", null);
    initPoint("BlackBookMordor12.5", category, "Complete The Black Book of Mordor, Chapter 12.5: The End of the Tale", null);
    initPoint("BlackBookMordor14.5", category, "Complete The Black Book of Mordor, Chapter 14.5: A Final Escape", null);

    category=TraitPointCategories.DEEDS;
    initPoint("OldAnórienQuests", category, "Old Anórien: complete Deed 'Quests of Old Anórien'", null);
    initPoint("OldAnórienDeeds", category, "Old Anórien: complete Deed 'Deeds of Old Anórien'", null);
  }

  private static String[][] BOOK_NAMES=
  {
    {"A Hobbit's Holiday","A Study of the Skin-changer","Geneology of the Beornings"},
    {"The Book of Knives","Knee-breaker's Manual","The Expert's Guide to Dirty Fighting"},
    {"The Candle's Flame","Treatise of Valour","The Book of Oaths"},
    {"The Tome of Swords","The Joy of Battle","The Artisan Blade"},
    {"The Best Defence","A Shield-maiden's Song","The Final Word"},
    {"A Shot in the Dark","The Way of the Hunter","The Furthest Charge"},
    {"The Book of Beasts","Lore of the Blade","Of Leaf and Twig"},
    {"Melodies of the Valar","The Rising Chord","Valour's Marches"},
    {"Golu o Maeth","Thunder and Flame","Whispers in the Dark"},
    {"The Watch Against the Night","Chieftains of the Dúnedain","Bullroarer's Boy"}
  };

  private static String[][] CLASS_QUESTS_CHAIN_NAMES=
  {
    {"Grimbeorn's Challenge", "The Path Homeward" },
    {"The Truest Course", "The Path of the Mischief-maker"},
    {"The Noblest Path", "The Path of the Healing Hands"},
    {"The Boldest Road", "Path of the Martial Champion"},
    {"The Bravest Deed", "The Path of the Defender of the Free"},
    {"The Swiftest Arrow", "The Path of the Foe-trapper"},
    {"The Wisest Way", "The Path of the Ancient Master Quests"},
    {"The Verses of the North", "The Path of the Resolve-watcher"},
    {"Learned in Letters", "The Path of the Restoring Rune"},
    {"A Strong Shield", "The Path of the Masterful Fist"}
  };

  private static String[] IRON_GARNISON_GUARDS_BOOKS=
  {
    "",
    "A Guide to the Quiet Knife",
    "The Master of the Charge",
    "The Boiling Rage",
    "A Keen Blade", 
    "The Jolly Hunter",
    "The Book of Nature",
    "The Verses of the North",
    "On the Patterns of Wind and Rain",
    "The Path Less Trod"
  };

  private void buildClassPoints()
  {
    int classIndex=0;
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String category=TraitPointCategories.CLASS;
      String key=cClass.getKey();
      String book1=BOOK_NAMES[classIndex][0];
      initPoint(key+":LegendaryBook1", category, "Complete Legendary Book Pages '"+book1+"'", cClass);
      String book2=BOOK_NAMES[classIndex][1];
      initPoint(key+":LegendaryBook2", category, "Complete Legendary Book Pages '"+book2+"'", cClass);
      String book3=BOOK_NAMES[classIndex][2];
      initPoint(key+":LegendaryBook3", category, "Complete Legendary Book Pages '"+book3+"'", cClass);
      String chain1=CLASS_QUESTS_CHAIN_NAMES[classIndex][0];
      initPoint(key+":ClassQuests50", category, "Complete the Level 50 Class Quest Chain '" + chain1 + "'", cClass);
      String chain2=CLASS_QUESTS_CHAIN_NAMES[classIndex][1];
      initPoint(key+":ClassQuests58", category, "Complete the Level 58 Class Quest Chain '" + chain2 + "'", cClass);
      if (cClass!=CharacterClass.BEORNING)
      {
        String dwarfBook=IRON_GARNISON_GUARDS_BOOKS[classIndex];
        initPoint(key+":ReadGuardsBook", category, "Read book of Iron Garrison Guards: '" + dwarfBook +"'", cClass);
      }
      classIndex++;
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
    initPoint("Beorning:ClassQuests15", TraitPointCategories.CLASS, "Complete the Level 15 Class Quest Chain 'The Speech of Animals'", CharacterClass.BEORNING);
    initPoint("Beorning:ClassQuests30", TraitPointCategories.CLASS, "Complete the Level 30 Class Quest Chain 'Hatred of Bear and Man'", CharacterClass.BEORNING);
  }

  private void buildClassDeeds()
  {
    String category=TraitPointCategories.CLASS;
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String key=cClass.getKey();
      for(int i=1;i<=8;i++)
      {
        initPoint(key+":ClassDeed"+i, category, "Class Deeds - Tier "+i, cClass);
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
