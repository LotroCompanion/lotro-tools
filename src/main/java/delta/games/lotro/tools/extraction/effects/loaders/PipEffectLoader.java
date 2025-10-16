package delta.games.lotro.tools.extraction.effects.loaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.effects.PipEffect;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.PipAdjustmentType;
import delta.games.lotro.common.enums.PipType;
import delta.games.lotro.common.properties.ModPropertyList;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.ModifiersUtils;

/**
 * Loader for 'PIP' effects.
 * @author DAM
 */
public class PipEffectLoader extends AbstractEffectLoader<PipEffect>
{
  private static final Logger LOGGER=LoggerFactory.getLogger(PipEffectLoader.class);

  @Override
  public void loadSpecifics(PipEffect effect, PropertiesSet props)
  {
    /*
Effect_Pip_AdjustmentAmount: 5
Effect_Pip_AdjustmentType: 2 (Natural)
Effect_Pip_Type: 2 (Fervour)
    */
    // Type
    LotroEnum<PipType> pipTypeEnum=LotroEnumsRegistry.getInstance().get(PipType.class);
    int typeCode=((Integer)props.getProperty("Effect_Pip_Type")).intValue();
    PipType type=pipTypeEnum.getEntry(typeCode);
    effect.setType(type);
    // Reset?
    Integer resetInt=(Integer)props.getProperty("Effect_Pip_Reset");
    boolean reset=((resetInt!=null)&&(resetInt.intValue()==1));
    effect.setReset(reset);
    // Adjustment type
    LotroEnum<PipAdjustmentType> pipAdjustmentTypeEnum=LotroEnumsRegistry.getInstance().get(PipAdjustmentType.class);
    Integer adjustmentTypeCode=(Integer)props.getProperty("Effect_Pip_AdjustmentType");
    if (adjustmentTypeCode!=null)
    {
      PipAdjustmentType adjustmentType=pipAdjustmentTypeEnum.getEntry(adjustmentTypeCode.intValue());
      effect.setAdjustmentType(adjustmentType);
    }
    // Amount
    Integer amount=(Integer)props.getProperty("Effect_Pip_AdjustmentAmount");
    if (amount!=null)
    {
      effect.setAmount(amount.intValue());
    }
    // Modifiers
    ModPropertyList modifiers=ModifiersUtils.getStatModifiers(props,"Effect_Pip_AdjustmentAmount_AdditiveModifiers");
    effect.setAmountModifiers(modifiers);
    // Some checks!
    if (reset)
    {
      if ((adjustmentTypeCode!=null) || (amount!=null) || (modifiers!=null))
      {
        LOGGER.warn("Unexpected PIP effect value (reset=true)!");
      }
    }
    else
    {
      if ((adjustmentTypeCode==null) || (amount==null))
      {
        LOGGER.warn("Unexpected PIP effect value (reset=false)!");
      }
    }
  }
}
