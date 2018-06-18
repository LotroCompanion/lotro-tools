package delta.games.lotro.tools.lore.items.complements;

import delta.games.lotro.lore.items.ArmourType;
import delta.games.lotro.lore.items.ItemBinding;

/**
 * Adds complements on armors from the 'High Enchanter' of Mordor.
 * @author DAM
 */
public class MordorHighEnchanterArmors
{
  private static final String COMMENT="Barter Mordor High Enchanter: 420 Ash";

  private FactoryCommentsInjector _injector;

  /**
   * Constructor.
   * @param injector Injector.
   */
  public MordorHighEnchanterArmors(FactoryCommentsInjector injector)
  {
    _injector=injector;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    doHeavyArmor();
    doMediumArmor();
    doLightArmor();
  }

  private void doHeavyArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361726 Polished Helm of the Expedition's Vanguard - BonA
      // 1879361647 Engraved Pauldrons of the Expedition's Vanguard - BonA
      // 1879361723 Articulated Gauntlets of the Expedition's Vanguard - BonA
      // 1879361653 Hardened Sabatons of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361726, 1879361647, 1879361723, 1879361653 };
      _injector.injectComment(COMMENT,vanguard1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard1);
      _injector.injectArmourType(ArmourType.HEAVY,vanguard1);

      // 1879362155 Polished Helm of the Expedition's Vanguard - BonA
      // 1879362143 Engraved Pauldrons of the Expedition's Vanguard - BonA
      // 1879362138 Articulated Gauntlets of the Expedition's Vanguard - BonA
      // 1879362174 Hardened Sabatons of the Expedition's Vanguard - BonA
      int[] vanguard2=new int[]{ 1879362155, 1879362143, 1879362138, 1879362174 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard2);
      _injector.injectArmourType(ArmourType.HEAVY,vanguard2);

      // 1879361025 Polished Helm of the Expedition's Vanguard - BAonA
      // 1879361042 Engraved Pauldrons of the Expedition's Vanguard - BAonA
      // 1879361064 Articulated Gauntlets of the Expedition's Vanguard - BAonA
      // 1879361073 Hardened Sabatons of the Expedition's Vanguard - BAonA
      int[] vanguardBAonA=new int[]{ 1879361025, 1879361042, 1879361064, 1879361073 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,vanguardBAonA);
      _injector.injectArmourType(ArmourType.HEAVY,vanguardBAonA);

      // Share stats
      for(int i=0;i<vanguard1.length;i++)
      {
        int[] ids=new int[]{vanguard1[i],vanguard2[i],vanguardBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361672 Polished Helm of Mordor's Bane - BonA
      // 1879361745 Engraved Pauldrons of Mordor's Bane - BonA
      // 1879361703 Hardened Sabatons of Mordor's Bane - BonA
      // 1879361660 Articulated Gauntlets of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361672, 1879361745, 1879361703, 1879361660 };
      _injector.injectComment(COMMENT,bane1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane1);
      _injector.injectArmourType(ArmourType.HEAVY,bane1);

      // 1879362160 Polished Helm of Mordor's Bane - BonA
      // 1879362126 Engraved Pauldrons of Mordor's Bane - BonA
      // 1879362119 Hardened Sabatons of Mordor's Bane - BonA
      // 1879362133 Articulated Gauntlets of Mordor's Bane - BonA
      int[] bane2=new int[]{ 1879362160, 1879362126, 1879362119, 1879362133 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane2);
      _injector.injectArmourType(ArmourType.HEAVY,bane2);

      // 1879361121 Polished Helm of Mordor's Bane - BAonA
      // 1879361098 Engraved Pauldrons of Mordor's Bane - BAonA
      // 1879361095 Hardened Sabatons of Mordor's Bane - BAonA
      // 1879361041 Articulated Gauntlets of Mordor's Bane - BAonA
      int[] baneBAonA=new int[]{ 1879361121, 1879361098, 1879361095, 1879361041 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,baneBAonA);
      _injector.injectArmourType(ArmourType.HEAVY,baneBAonA);

      // Share stats
      for(int i=0;i<bane1.length;i++)
      {
        int[] ids=new int[]{bane1[i],bane2[i],baneBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Cloaks
    {
      // 1879361648 Embroidered Heavy Cloak of the Expedition's Vanguard - BonA
      // 1879361708 Embroidered Heavy Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361648, 1879361708 };
      _injector.injectComment(COMMENT,cloaks1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks1);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks1);

      // 1879362161 Embroidered Heavy Cloak of the Expedition's Vanguard - BonA
      // 1879362131 Embroidered Heavy Cloak of Mordor's Bane - BonA
      int[] cloaks2=new int[]{ 1879362161, 1879362131 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks2);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks2);

      // 1879361127 Embroidered Heavy Cloak of the Expedition's Vanguard - BAonA
      // 1879361067 Embroidered Heavy Cloak of Mordor's Bane - BAonA
      int[] cloaksBAonA=new int[]{ 1879361127, 1879361067 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,cloaksBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,cloaksBAonA);

      // Share stats
      for(int i=0;i<cloaks1.length;i++)
      {
        int[] ids=new int[]{cloaks1[i],cloaks2[i],cloaksBAonA[i]};
        _injector.shareStats(ids);
      }
    }
  }

  private void doMediumArmor()
  {
    doMediumMightArmor();
    doMediumAgilityArmor();
  }

  private void doMediumMightArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361750 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879361674 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879361732 Mighty Gages of the Expedition's Vanguard - BonA
      // 1879361680 Forceful Boots of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361750, 1879361674, 1879361732, 1879361680 };
      _injector.injectComment(COMMENT,vanguard1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard1);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguard1);

      // 1879362167 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879362149 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879362120 Mighty Gages of the Expedition's Vanguard - BonA
      // 1879362151 Forceful Boots of the Expedition's Vanguard - BonA
      int[] vanguard2=new int[]{ 1879362167, 1879362149, 1879362120, 1879362151 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard2);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguard2);

      // 1879361074 Reinforced Coif of the Expedition's Vanguard - BAonA
      // 1879361016 Rivetted Camail of the Expedition's Vanguard - BAonA
      // 1879361054 Mighty Gages of the Expedition's Vanguard - BAonA
      // 1879361065 Forceful Boots of the Expedition's Vanguard - BAonA
      int[] vanguardBAonA=new int[]{ 1879361074, 1879361016, 1879361054, 1879361065 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,vanguardBAonA);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguardBAonA);

      // Share stats
      for(int i=0;i<vanguard1.length;i++)
      {
        int[] ids=new int[]{vanguard1[i],vanguard2[i],vanguardBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361749 Reinforced Coif of Mordor's Bane - BonA
      // 1879361665 Rivetted Camail of Mordor's Bane - BonA
      // 1879361686 Mighty Gages of Mordor's Bane - BonA
      // 1879361704 Forceful Boots of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361749, 1879361665, 1879361686, 1879361704 };
      _injector.injectComment(COMMENT,bane1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane1);
      _injector.injectArmourType(ArmourType.MEDIUM,bane1);

      // 1879362139 Reinforced Coif of Mordor's Bane - BonA
      // 1879362147 Rivetted Camail of Mordor's Bane - BonA
      // 1879362144 Mighty Gages of Mordor's Bane - BonA
      // 1879362169 Forceful Boots of Mordor's Bane - BonA
      int[] bane2=new int[]{ 1879362139, 1879362147, 1879362144, 1879362169 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane2);
      _injector.injectArmourType(ArmourType.MEDIUM,bane2);

      // 1879361060 Reinforced Coif of Mordor's Bane - BAonA
      // 1879361077 Rivetted Camail of Mordor's Bane - BAonA
      // 1879361104 Mighty Gages of Mordor's Bane - BAonA
      // 1879361117 Forceful Boots of Mordor's Bane - BAonA
      int[] baneBAonA=new int[]{ 1879361060, 1879361077, 1879361104, 1879361117 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,baneBAonA);
      _injector.injectArmourType(ArmourType.MEDIUM,baneBAonA);

      // Share stats
      for(int i=0;i<bane1.length;i++)
      {
        int[] ids=new int[]{bane1[i],bane2[i],baneBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Cloaks
    {
      // 1879361685 Embroidered Wool Cloak of the Expedition's Vanguard - BonA
      // 1879361737 Embroidered Wool Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361685, 1879361737 };
      _injector.injectComment(COMMENT,cloaks1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks1);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks1);

      // 1879362127 Embroidered Wool Cloak of the Expedition's Vanguard - BonA
      // 1879362123 Embroidered Wool Cloak of Mordor's Bane - BonA
      int[] cloaks2=new int[]{ 1879362127, 1879362123 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks2);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks2);

      // 1879361092 Embroidered Wool Cloak of the Expedition's Vanguard - BAonA
      // 1879361027 Embroidered Wool Cloak of Mordor's Bane - BAonA
      int[] cloaksBAonA=new int[]{ 1879361092, 1879361027 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,cloaksBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,cloaksBAonA);

      // Share stats
      for(int i=0;i<cloaks1.length;i++)
      {
        int[] ids=new int[]{cloaks1[i],cloaks2[i],cloaksBAonA[i]};
        _injector.shareStats(ids);
      }
    }
  }

  private void doMediumAgilityArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361649 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879361709 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879361690 Quick Gages of the Expedition's Vanguard - BonA
      // 1879361661 Nimble Boots of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361649, 1879361709, 1879361690, 1879361661 };
      _injector.injectComment(COMMENT,vanguard1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard1);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguard1);

      // 1879362124 Reinforced Coif of the Expedition's Vanguard - BonA
      // 1879362162 Rivetted Camail of the Expedition's Vanguard - BonA
      // 1879362141 Quick Gages of the Expedition's Vanguard - BonA
      // 1879362172 Nimble Boots of the Expedition's Vanguard - BonA
      int[] vanguard2=new int[]{ 1879362124, 1879362162, 1879362141, 1879362172 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard2);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguard2);

      // 1879361119 Reinforced Coif of the Expedition's Vanguard - BAonA
      // 1879361061 Rivetted Camail of the Expedition's Vanguard - BAonA
      // 1879361055 Quick Gages of the Expedition's Vanguard - BAonA
      // 1879361122 Nimble Boots of the Expedition's Vanguard - BAonA
      int[] vanguardBAonA=new int[]{ 1879361119, 1879361061, 1879361055, 1879361122 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,vanguardBAonA);
      _injector.injectArmourType(ArmourType.MEDIUM,vanguardBAonA);

      // Share stats
      for(int i=0;i<vanguard1.length;i++)
      {
        int[] ids=new int[]{vanguard1[i],vanguard2[i],vanguardBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361691 Reinforced Coif of Mordor's Bane - BonA
      // 1879361734 Rivetted Camail of Mordor's Bane - BonA
      // 1879361657 Quick Gages of Mordor's Bane - BonA
      // 1879361755 Nimble Boots of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361691, 1879361734, 1879361657, 1879361755 };
      _injector.injectComment(COMMENT,bane1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane1);
      _injector.injectArmourType(ArmourType.MEDIUM,bane1);

      // 1879362142 Reinforced Coif of Mordor's Bane - BonA
      // 1879362153 Rivetted Camail of Mordor's Bane - BonA
      // 1879362121 Quick Gages of Mordor's Bane - BonA
      // 1879362135 Nimble Boots of Mordor's Bane - BonA
      int[] bane2=new int[]{ 1879362142, 1879362153, 1879362121, 1879362135 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane2);
      _injector.injectArmourType(ArmourType.MEDIUM,bane2);

      // 1879361036 Reinforced Coif of Mordor's Bane - BAonA
      // 1879361058 Rivetted Camail of Mordor's Bane - BAonA
      // 1879361105 Quick Gages of Mordor's Bane - BAonA
      // 1879361118 Nimble Boots of Mordor's Bane - BAonA
      int[] baneBAonA=new int[]{ 1879361036, 1879361058, 1879361105, 1879361118 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,baneBAonA);
      _injector.injectArmourType(ArmourType.MEDIUM,baneBAonA);

      // Share stats
      for(int i=0;i<bane1.length;i++)
      {
        int[] ids=new int[]{bane1[i],bane2[i],baneBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Cloaks
    {
      // 1879361700 Embroidered Silk Cloak of the Expedition's Vanguard - BonA
      // 1879361677 Embroidered Silk Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361700, 1879361677 };
      _injector.injectComment(COMMENT,cloaks1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks1);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks1);

      // 1879362150 Embroidered Silk Cloak of the Expedition's Vanguard - BonA
      // 1879362163 Embroidered Silk Cloak of Mordor's Bane - BonA
      int[] cloaks2=new int[]{ 1879362150, 1879362163 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks2);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks2);

      // 1879361019 Embroidered Silk Cloak of the Expedition's Vanguard - BAonA
      // 1879361051 Embroidered Silk Cloak of Mordor's Bane - BAonA
      int[] cloaksBAonA=new int[]{ 1879361019, 1879361051 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,cloaksBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,cloaksBAonA);

      // Share stats
      for(int i=0;i<cloaks1.length;i++)
      {
        int[] ids=new int[]{cloaks1[i],cloaks2[i],cloaksBAonA[i]};
        _injector.shareStats(ids);
      }
    }
  }

  private void doLightArmor()
  {
    // Barter Incomparable Armor (Vanguard)
    {
      // 1879361668 Gilded Cap of the Expedition's Vanguard - BonA
      // 1879361646 Embossed Mantle of the Expedition's Vanguard - BonA
      // 1879361662 Runed Gloves of the Expedition's Vanguard - BonA
      // 1879361733 Rugged Shoes of the Expedition's Vanguard - BonA
      int[] vanguard1=new int[]{ 1879361668, 1879361646, 1879361662, 1879361733 };
      _injector.injectComment(COMMENT,vanguard1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard1);
      _injector.injectArmourType(ArmourType.LIGHT,vanguard1);

      // 1879362148 Gilded Cap of the Expedition's Vanguard - BonA
      // 1879362159 Embossed Mantle of the Expedition's Vanguard - BonA
      // 1879362157 Runed Gloves of the Expedition's Vanguard - BonA
      // 1879362146 Rugged Shoes of the Expedition's Vanguard - BonA
      int[] vanguard2=new int[]{ 1879362148, 1879362159, 1879362157, 1879362146 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,vanguard2);
      _injector.injectArmourType(ArmourType.LIGHT,vanguard2);

      // 1879361063 Gilded Cap of the Expedition's Vanguard - BAonA
      // 1879361029 Embossed Mantle of the Expedition's Vanguard - BAonA
      // 1879361038 Runed Gloves of the Expedition's Vanguard - BAonA
      // 1879361082 Rugged Shoes of the Expedition's Vanguard - BAonA
      int[] vanguardBAonA=new int[]{ 1879361063, 1879361029, 1879361038, 1879361082 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,vanguardBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,vanguardBAonA);

      // Share stats
      for(int i=0;i<vanguard1.length;i++)
      {
        int[] ids=new int[]{vanguard1[i],vanguard2[i],vanguardBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Barter Incomparable Armor (Mordor's Bane)
    {
      // 1879361675 Gilded Cap of Mordor's Bane - BonA
      // 1879361714 Embossed Mantle of Mordor's Bane - BonA
      // 1879361710 Runed Gloves of Mordor's Bane - BonA
      // 1879361650 Rugged Shoes of Mordor's Bane - BonA
      int[] bane1=new int[]{ 1879361675, 1879361714, 1879361710, 1879361650 };
      _injector.injectComment(COMMENT,bane1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane1);
      _injector.injectArmourType(ArmourType.LIGHT,bane1);

      // 1879362145 Gilded Cap of Mordor's Bane - BonA
      // 1879362166 Embossed Mantle of Mordor's Bane - BonA
      // 1879362128 Runed Gloves of Mordor's Bane - BonA
      // 1879362154 Rugged Shoes of Mordor's Bane - BonA
      int[] bane2=new int[]{ 1879362145, 1879362166, 1879362128, 1879362154 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,bane2);
      _injector.injectArmourType(ArmourType.LIGHT,bane2);

      // 1879361032 Gilded Cap of Mordor's Bane - BAonA
      // 1879361048 Embossed Mantle of Mordor's Bane - BAonA
      // 1879361109 Runed Gloves of Mordor's Bane - BAonA
      // 1879361050 Rugged Shoes of Mordor's Bane - BAonA
      int[] baneBAonA=new int[]{ 1879361032, 1879361048, 1879361109, 1879361050 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,baneBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,baneBAonA);

      // Share stats
      for(int i=0;i<bane1.length;i++)
      {
        int[] ids=new int[]{bane1[i],bane2[i],baneBAonA[i]};
        _injector.shareStats(ids);
      }
    }
    // Cloaks
    {
      // 1879361738 Embroidered Light Cloak of the Expedition's Vanguard - BonA
      // 1879361689 Embroidered Light Cloak of Mordor's Bane - BonA
      int[] cloaks1=new int[]{ 1879361738, 1879361689 };
      _injector.injectComment(COMMENT,cloaks1);
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks1);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks1);

      // 1879362134 Embroidered Light Cloak of the Expedition's Vanguard - BonA
      // 1879362158 Embroidered Light Cloak of Mordor's Bane - BonA
      int[] cloaks2=new int[]{ 1879362134, 1879362158 };
      _injector.injectBinding(ItemBinding.BIND_ON_ACQUIRE,cloaks2);
      _injector.injectArmourType(ArmourType.LIGHT,cloaks2);

      // 1879361114 Embroidered Light Cloak of the Expedition's Vanguard - BAonA
      // 1879361090 Embroidered Light Cloak of Mordor's Bane - BAonA
      int[] cloaksBAonA=new int[]{ 1879361114, 1879361090 };
      _injector.injectBinding(ItemBinding.BOUND_TO_ACCOUNT_ON_ACQUIRE,cloaksBAonA);
      _injector.injectArmourType(ArmourType.LIGHT,cloaksBAonA);

      // Share stats
      for(int i=0;i<cloaks1.length;i++)
      {
        int[] ids=new int[]{cloaks1[i],cloaks2[i],cloaksBAonA[i]};
        _injector.shareStats(ids);
      }
    }
  }
}
