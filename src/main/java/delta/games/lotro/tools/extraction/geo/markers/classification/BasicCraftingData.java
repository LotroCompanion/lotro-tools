package delta.games.lotro.tools.extraction.geo.markers.classification;

import delta.games.lotro.common.enums.CraftTier;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.tools.extraction.crafting.MainDatCraftingLoader;

/**
 * Replacement for crafting data used in geo data classification..
 * @author DAM
 */
public class BasicCraftingData
{
  private Professions _professions;
  private LotroEnum<CraftTier> _craftTiers;

  /**
   * Constructor.
   */
  public BasicCraftingData()
  {
    _craftTiers=buildCraftTiers();
    _professions=new Professions();
  }

  private LotroEnum<CraftTier> buildCraftTiers()
  {
    LotroEnum<CraftTier> ret=new LotroEnum<CraftTier>(0,"CraftTier",CraftTier.class);
    for(int i=1;i<20;i++)
    {
      CraftTier entry=ret.buildEntryInstance(i,null,null);
      ret.registerEntry(entry);
    }
    return ret;
  }

  private Profession getProfession(int professionId)
  {
    Profession ret=_professions.getProfessionById(professionId);
    if (ret==null)
    {
      ret=buildProfession(professionId);
      _professions.addProfession(ret);
    }
    return ret;
  }

  private Profession buildProfession(int professionId)
  {
    String key=MainDatCraftingLoader.getProfessionKey(professionId);
    Profession profession=new Profession();
    profession.setIdentifier(professionId);
    profession.setKey(key);
    profession.setName(key);
    for(CraftTier craftTier : _craftTiers.getAll())
    {
      CraftingLevel level=new CraftingLevel(profession,craftTier);
      profession.addLevel(level);
    }
    return profession;
  }

  /**
   * Get a crafting level.
   * @param professionId Profession identifier.
   * @param tier Tier.
   * @return A crafting level.
   */
  public CraftingLevel getLevel(int professionId, int tier)
  {
    Profession profession=getProfession(professionId);
    return profession.getByTier(tier);
  }
}
