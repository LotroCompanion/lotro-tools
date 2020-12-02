package delta.games.lotro.tools.dat.traitPoints;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.rewards.Rewards;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
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

  /**
   * Build trait points registry.
   */
  public void doIt()
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
    showTraitPointsPerClass();
    checkAvailableTraitPoints();
  }

  private void showTraitPointsPerClass()
  {
    System.out.println("Checking trait points / class:");
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      List<TraitPoint> points=_registry.getPointsForClass(cClass);
      Collections.sort(points,new TraitPointLabelComparator());
      System.out.println(cClass+": "+points.size());
      for(TraitPoint point : points)
      {
        System.out.println("\t"+point.getAchievableId()+" - "+point.getLabel()+" -- "+point.getId());
      }
    }
    List<TraitPoint> all=_registry.getAll();
    System.out.println("All: "+all.size());
  }

  private void checkAvailableTraitPoints()
  {
    Map<Integer,Achievable> achievables=getAchievablesWithTraitPointsRewards();
    List<TraitPoint> points=_registry.getAll();
    for(TraitPoint point : points)
    {
      int achievableId=point.getAchievableId();
      achievables.remove(Integer.valueOf(achievableId));
    }
    if (achievables.size()>0)
    {
      for(Achievable achievable : achievables.values())
      {
        LOGGER.warn("Unmanaged achievable: "+achievable);
      }
    }
  }

  private Map<Integer,Achievable> getAchievablesWithTraitPointsRewards()
  {
    Map<Integer,Achievable> ret=new HashMap<Integer,Achievable>();
    QuestsManager questsMgr=QuestsManager.getInstance();
    for(QuestDescription quest : questsMgr.getAll())
    {
      Rewards rewards=quest.getRewards();
      int classPoints=rewards.getClassPoints();
      if (classPoints==1)
      {
        ret.put(Integer.valueOf(quest.getIdentifier()),quest);
      }
      if (classPoints>1)
      {
        LOGGER.warn("More than 1 point for: "+quest);
      }
    }
    DeedsManager deedsMgr=DeedsManager.getInstance();
    for(DeedDescription deed : deedsMgr.getAll())
    {
      Rewards rewards=deed.getRewards();
      int classPoints=rewards.getClassPoints();
      if (classPoints==1)
      {
        ret.put(Integer.valueOf(deed.getIdentifier()),deed);
      }
      if (classPoints>1)
      {
        LOGGER.warn("More than 1 point for: "+deed);
      }
    }
    return ret;
  }

  private void buildRegistry()
  {
    buildGenericPoints();
    buildClassPoints();
    buildClassDeeds();
  }

  private void buildGenericPoints()
  {
    // Moria
    initPoints("EpicVol2Book6", TraitPointCategories.EPIC, "Complete Volume II, Book 6 (Moria)", "The Mines of Moria");
    // Rohan quest chains
    String category=TraitPointCategories.QUESTS;
    initPoint("West Rohan:Kingstead", category, "West Rohan: complete Kingstead quest chain", null, "The Road to Dunharrow");
    initPoint("West Rohan:Eastfold", category, "West Rohan: complete Eastfold quest chain", null, "All Roads Lead Back to Aldburg");
    initPoint("West Rohan:Broadacres", category, "West Rohan: complete Broadacres quest chain", null, "The Broadacres Betrayed");
    initPoint("West Rohan:Stonedeans", category, "West Rohan: complete Stonedeans quest chain", null, "Woodhurst Has Fallen");
    initPoint("West Rohan:Westfold", category, "West Rohan: complete Westfold quest chain", null, "To Helm's Deep");
    // Central Gondor quest chain
    initPoint("Central Gondor:Ringló Vale", category, "Central Gondor: complete Ringló Vale quest chain", null, "A Ruthless End");
    initPoint("Central Gondor:Dor-en-Ernil", category, "Central Gondor: complete Dor-en-Ernil quest chain", null, "Blood for Blood");
    initPoint("Central Gondor:Lebennin", category, "Central Gondor: complete Lebennin quest chain", null, "Sons of the Usurper");
    initPoint("Central Gondor:Pelargir", category, "Central Gondor: complete Pelargir quest chain", null, "Faltharan's Confrontation");
    // Eastern Gondor quest chain
    initPoint("Eastern Gondor:AshesAndStars", category, "Osgiliath: complete Ashes and Stars, Chapter 4", null, "Ashes and Stars, Chapter 4");
    // Epic quests
    category=TraitPointCategories.EPIC;
    initPoint("EpicVol4Book2", category, "Complete Volume IV, Book 2: The Dawnless Day", null, "Book 2, Chapter 9: Even In Darkness");
    initPoint("EpicVol4Book4Chapter10", category, "Complete Volume IV, Book 4, Chapter 10: The Defence of Minas Tirith", null, "Book 4, Chapter 10: The Defence of Minas Tirith");
    initPoint("EpicVol4Book4Chapter11", category, "Complete Volume IV, Book 4, Chapter 11: Hammer of the Underworld", null, "Book 4, Chapter 11: Hammer of the Underworld");
    initPoint("EpicVol4Book8Chapter7", category, "Complete Volume IV, Book 8, Chapter 7: Mordor Triumphant", null, "Book 8, Chapter 7: Mordor Triumphant");
    initPoint("EpicVol4Book9Chapter5", category, "Complete Volume IV, Book 9, Chapter 5: The Next Adventure", null, "Book 9, Chapter 5: The Next Adventure");
    initPoint("BlackBookMordor4.4", category, "Complete The Black Book of Mordor, Chapter  4.4: Union of Evil", null, "Chapter 4.4: Union of Evil");
    initPoint("BlackBookMordor5.5", category, "Complete The Black Book of Mordor, Chapter  5.5: The Walls Brought Down", null, "Chapter 5.5: The Walls Brought Down");
    initPoint("BlackBookMordor8.7", category, "Complete The Black Book of Mordor, Chapter  8.7: The First Promise", null, "Chapter 8.7: The First Promise");
    initPoint("BlackBookMordor10.7", category, "Complete The Black Book of Mordor, Chapter 10.7: The Arrival of the Wise", null, "Chapter 10.7: The Arrival of the Wise");
    initPoint("BlackBookMordor12.5", category, "Complete The Black Book of Mordor, Chapter 12.5: The End of the Tale", null, "Chapter 12.5: The End of the Tale");
    initPoint("BlackBookMordor14.5", category, "Complete The Black Book of Mordor, Chapter 14.5: A Final Escape", null, "Chapter 14.5: A Final Escape");
    initPoint("Durin2.7", category, "Complete The Legacy of Durin and the Trials of the Dwarves, Chapter 2.7: A Thirst for Blood", null, "Chapter 2.7: A Thirst for Blood");
    initPoint("Wedding", category, "Complete Volume V, Book 1, Chapter 8: The Wedding Banquet", null, "Book 1, Chapter 8: The Wedding Banquet");

    category=TraitPointCategories.DEEDS;
    initPoint("Epic Battles 1", category, "Earn 100 Promotion Points", null, "Promotion Points 1");
    initPoint("Epic Battles 2", category, "Earn 200 Promotion Points", null, "Promotion Points 2");
    initPoint("OldAnórienQuests", category, "Old Anórien: complete Deed 'Quests of Old Anórien'", null, "Quests of Old Anórien");
    initPoint("OldAnórienDeeds", category, "Old Anórien: complete Deed 'Deeds of Old Anórien'", null, "Deeds of Old Anórien");
  }

  private static String[][] BOOK_NAMES=
  {
    {"A Hobbit's Holiday","A Study of the Skin-changer","Genealogy of the Beornings"},
    null,
    {"The Book of Knives","Knee-breaker's Manual","The Expert's Guide to Dirty Fighting"},
    {"The Candle's Flame","The Treatise of Valour","The Book of Oaths"},
    {"The Tome of Swords","The Joy of Battle","The Artisan Blade"},
    {"The Best Defence","A Shield-maiden's Song","The Final Word"},
    {"A Shot in the Dark","The Way of the Hunter","The Furthest Charge"},
    {"The Book of Beasts","Lore of the Blade","Of Leaf and Twig"},
    {"Melodies of the Valar","The Rising Chord","Valour's Marches"},
    {"Golu o Maeth","Thunder and Flame","Whispers in the Dark"},
    {"The Watch Against the Night","Chieftains of the Dúnedain","Bullroarer's Boy"}
  };

  private static String[][] CLASS_ACHIEVABLES_NAMES=
  {
    {"Grimbeorn's Challenge", "The Path Homeward" },
    null,
    {"A Lesson from Bilbo Baggins", "The Path of the Mischief-maker"},
    {"A Lesson from Boromir", "The Path of the Healing Hands"},
    {"A Lesson from Gimli", "The Path of the Martial Champion"},
    {"A Lesson from Samwise Gamgee", "The Path of Freedom's Defender"},
    {"A Lesson from Legolas", "The Path of the Foe-trapper"},
    {"A Lesson from Lord Elrond", "The Path of the Ancient Master"},
    {"A Lesson from Lindir", "The Path of the Resolve-watcher"},
    {"Deep Secrets of Rune-craft", "The Path of the Restoring Rune"},
    {"Wisdom of the Wardens", "The Path of the Masterful Fist"}
  };

  private static String[] IRON_GARNISON_GUARDS_BOOKS=
  {
    null,
    null,
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
    // Add Beorning specifics
    {
      String name="The Speech of Animals";
      Achievable achievable=findAchievableByNameAndClass(name,CharacterClass.BEORNING);
      int id=(achievable!=null)?achievable.getIdentifier():0;
      initPoint("Beorning:ClassQuests15", TraitPointCategories.CLASS, "Complete the Level 15 Class Quest '"+name+"'", CharacterClass.BEORNING, id);
    }
    {
      String name="Hatred of Bear and Man";
      Achievable achievable=findAchievableByNameAndClass(name,CharacterClass.BEORNING);
      int id=(achievable!=null)?achievable.getIdentifier():0;
      initPoint("Beorning:ClassQuests30", TraitPointCategories.CLASS, "Complete the Level 30 Class Quest '"+name+"'", CharacterClass.BEORNING, id);
    }

    int classIndex=0;
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String category=TraitPointCategories.CLASS;
      String key=cClass.getKey();
      // Legendary Books
      for(int i=0;i<3;i++)
      {
        if (BOOK_NAMES[classIndex]==null)
        {
          continue;
        }
        String book=BOOK_NAMES[classIndex][i];
        String pointId=key+":LegendaryBook"+(i+1);
        Achievable achievable=findAchievableByNameAndClass(book,cClass);
        int id=(achievable!=null)?achievable.getIdentifier():0;
        initPoint(pointId, category, "Complete Legendary Book Pages '"+book+"'", cClass, id);
      }
      // Class quest chains
      for(int i=0;i<2;i++)
      {
        int level=(i==0)?50:58;
        if (CLASS_ACHIEVABLES_NAMES[classIndex]==null)
        {
          continue;
        }
        String name=CLASS_ACHIEVABLES_NAMES[classIndex][i];
        String pointId=key+":ClassQuests"+level;
        Achievable achievable=findAchievableByNameAndClass(name,cClass);
        int id=(achievable!=null)?achievable.getIdentifier():0;
        String type=(achievable instanceof QuestDescription)?"Quest":"Deed";
        String label="Complete the Level "+level+" "+type+" '" + name + "'";
        initPoint(pointId, category, label, cClass, id);
      }
      if (IRON_GARNISON_GUARDS_BOOKS[classIndex]!=null)
      {
        String dwarfBook=IRON_GARNISON_GUARDS_BOOKS[classIndex];
        Achievable achievable=findAchievableByNameAndClass(dwarfBook,cClass);
        int id=(achievable!=null)?achievable.getIdentifier():0;
        initPoint(key+":ReadGuardsBook", category, "Read book of Iron Garrison Guards: '" + dwarfBook +"'", cClass, id);
      }
      classIndex++;
    }
  }

  private void buildClassDeeds()
  {
    String category=TraitPointCategories.CLASS;
    for(CharacterClass cClass : CharacterClass.ALL_CLASSES)
    {
      String key=cClass.getKey();
      for(int i=1;i<=8;i++)
      {
        String name="Class Deeds - Tier "+i;
        Achievable achievable=findAchievableByNameAndClass(name,cClass);
        int id=(achievable!=null)?achievable.getIdentifier():0;
        initPoint(key+":ClassDeed"+i, category, name, cClass, id);
      }
    }
  }

  private void initPoints(String pointId, String category, String label, String name)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    for(DeedDescription deed : deedsMgr.getAll())
    {
      if (name.equals(deed.getName()))
      {
        UsageRequirement requirements=deed.getUsageRequirement();
        ClassRequirement classRequirement=requirements.getClassRequirement();
        if (classRequirement!=null)
        {
          CharacterClass requiredClass=classRequirement.getAllowedClasses().get(0);
          initPoint(pointId,category,label,requiredClass,deed.getIdentifier());
        }
      }
    }

  }

  private TraitPoint initPoint(String pointId, String category, String label, CharacterClass requiredCharacterClass, String achievableName)
  {
    Achievable achievable=findAchievableByNameAndClass(achievableName,null);
    int id=(achievable!=null)?achievable.getIdentifier():0;
    return initPoint(pointId,category,label,requiredCharacterClass,id);
  }

  private TraitPoint initPoint(String id, String category, String label, CharacterClass requiredCharacterClass, int achievableId)
  {
    TraitPoint point=new TraitPoint(id);
    point.setLabel(label);
    point.setCategory(category);
    if (requiredCharacterClass!=null)
    {
      point.addRequiredClass(requiredCharacterClass);
    }
    point.setAchievableId(achievableId);
    _registry.registerTraitPoint(point);
    return point;
  }

  private boolean checkClassRequirement(Achievable achievable, CharacterClass characterClass)
  {
    if (characterClass==null)
    {
      return true;
    }
    UsageRequirement requirements=achievable.getUsageRequirement();
    ClassRequirement classRequirement=requirements.getClassRequirement();
    if (classRequirement==null)
    {
      return false;
    }
    return classRequirement.accept(characterClass);
  }

  private Achievable findAchievableByNameAndClass(String name, CharacterClass characterClass)
  {
    QuestsManager questsMgr=QuestsManager.getInstance();
    for(QuestDescription quest : questsMgr.getAll())
    {
      if (name.equals(quest.getName()))
      {
        if (checkClassRequirement(quest,characterClass))
        {
          return quest;
        }
      }
    }
    DeedsManager deedsMgr=DeedsManager.getInstance();
    for(DeedDescription deed : deedsMgr.getAll())
    {
      if (name.equals(deed.getName()))
      {
        if (checkClassRequirement(deed,characterClass))
        {
          return deed;
        }
      }
    }
    return null;
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
