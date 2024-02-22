package quarri6343.openarpg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * アイテム実装テスト
 */
public class ItemStoneSpear extends Item {

    private static final int DAMAGE = 5;

    public ItemStoneSpear(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.openarpg.stone_spear"));

        if (pStack.getTag() == null) {
            pStack.getOrCreateTag().putInt("Damage", DAMAGE);
            pStack.getTag().putInt("HideFlags", 2);
        }
        int currentDamage = pStack.getTag().getInt("Damage");
        pTooltipComponents.add(Component.translatable("攻撃力: " + currentDamage));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pStack = pPlayer.getItemInHand(pUsedHand);

        if (pStack.getTag() == null) {
            pStack.getOrCreateTag().putInt("Damage", DAMAGE);
            pStack.getTag().putInt("HideFlags", 2);
        }
        int currentDamage = pStack.getTag().getInt("Damage");
        CompoundTag tag = pStack.getOrCreateTag();
        tag.putInt("Damage", currentDamage + 1);
        pStack.setTag(tag);

        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        final Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slot != EquipmentSlot.MAINHAND)
            return modifiers;

        if (stack.getTag() == null) {
            stack.getOrCreateTag().putInt("Damage", DAMAGE);
        }
        int currentDamage = stack.getTag().getInt("Damage");
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", currentDamage - 1, AttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
