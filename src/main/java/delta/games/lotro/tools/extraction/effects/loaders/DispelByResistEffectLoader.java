package delta.games.lotro.tools.extraction.effects.loaders;

import java.util.BitSet;
import java.util.List;

import delta.games.lotro.common.effects.DispelByResistEffect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.ResistCategory;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BitSetUtils;

/**
 * Loader for 'dispell by resist' effects.
 * @author DAM
 */
public class DispelByResistEffectLoader extends AbstractEffectLoader<DispelByResistEffect>
{
  @Override
  public void loadSpecifics(DispelByResistEffect effect, PropertiesSet effectProps)
  {
  /*
Effect ID=1879157351, class=DispelByResistEffect (714)
Effect_DispelByResist_MaximumDispelCount: 1
Effect_DispelByResist_ResistCategoryFilter: 8 (Wound)
Effect_DispelByResist_UseStrengthRestriction: 1
 */

    // Dispel count
    Integer dispelCountInt=(Integer)effectProps.getProperty("Effect_DispelByResist_MaximumDispelCount");
    int dispelCount=(dispelCountInt!=null)?dispelCountInt.intValue():-1;
    effect.setMaxDispelCount(dispelCount);
    // Resist Categories
    int categoriesCode=((Integer)effectProps.getProperty("Effect_DispelByResist_ResistCategoryFilter")).intValue();
    LotroEnum<ResistCategory> categoriesEnum=LotroEnumsRegistry.getInstance().get(ResistCategory.class);
    BitSet bitset=BitSetUtils.getBitSetFromFlags(categoriesCode);
    List<ResistCategory> categories=categoriesEnum.getFromBitSet(bitset);
    for(ResistCategory category : categories)
    {
      effect.addResistCategory(category);
    }
    // Strength restriction
    Integer useStrengthResitriction=(Integer)effectProps.getProperty("Effect_DispelByResist_UseStrengthRestriction");
    if ((useStrengthResitriction!=null) && (useStrengthResitriction.intValue()==1))
    {
      effect.setUseStrengthRestriction(true);
    }
    // Strength offset:
    Float strengthOffset=(Float)effectProps.getProperty("Effect_DispelByResist_StrengthRestrictionOffset");
    if (strengthOffset!=null)
    {
      effect.setStrengthOffset(Integer.valueOf(strengthOffset.intValue()));
    }
  }
}
