package delta.games.lotro.tools.lore.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Tool to find the icon ID of the recipe items.
 * @author DAM
 */
public class MainBuildRecipeIcons
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Map<String,String> category2IconMap=new HashMap<String,String>();
    List<Item> items=ItemsManager.getInstance().getAllItems();
    for(Item item : items)
    {
      String subCategory=item.getSubCategory();
      if ((subCategory!=null) && (subCategory.startsWith("Recipe:")))
      {
        String icon=item.getIcon();
        String old=category2IconMap.put(subCategory,icon);
        if ((old!=null) && (!old.equals(icon)))
        {
          System.out.println("Inconsistent icons for category: "+subCategory+". "+old+"!="+icon);
        }
      }
    }
    Map<String,List<String>> sortedIcons=new HashMap<String,List<String>>();
    System.out.println("String[][] recipeIcons={");
    int nbTiers=CraftingLevel.ALL_TIERS.length-1; // Do not use 'beginner'
    for(Profession profession : Profession.getAll())
    {
      String professionName=profession.getLabel();
      List<String> icons=sortedIcons.get(profession);
      if (icons==null)
      {
        icons=new ArrayList<String>();
        sortedIcons.put(professionName,icons);
      }
      for(int tier=1;tier<=nbTiers;tier++)
      {
        String key="Recipe:"+professionName+":Tier"+tier;
        String icon=category2IconMap.get(key);
        icons.add(icon);
      }
      System.out.print("\t{ ");
      int index=0;
      for(String icon : icons)
      {
        if (index>0) System.out.print(", ");
        if (icon!=null)
        {
          System.out.print("\""+icon+"\"");
        }
        else
        {
          System.out.print("null");
        }
        index++;
      }
      System.out.println(" },");
    }
    System.out.println("};");
  }
}
