package be.hize.nes.mixin.transformers;

import be.hize.nes.NES;
import be.hize.nes.utils.ItemRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    private static final ResourceLocation RARITY = new ResourceLocation("nes", "rarity.png");
    private static final Pattern PATTERN = Pattern.compile("(?<color>\\u00a7[0-9a-fk-or]).+");

    @Inject(method = "renderItemIntoGUI(Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    private void renderRarity(ItemStack itemStack, int xPosition, int yPosition, CallbackInfo info) {
        if (NES.getFeature().misc.itemRarity.enabled) {
            renderRarity(itemStack, xPosition, yPosition);
        }
    }

    public void renderRarity(ItemStack itemStack, int xPos, int yPos) {
        if (itemStack != null && itemStack.hasTagCompound()) {
            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("display");
            NBTTagCompound extra = itemStack.getTagCompound().getCompoundTag("ExtraAttributes");
            String displayName = compound.getString("Name");
            String id = extra.getString("id");
            boolean upgrade = extra.hasKey("rarity_upgrades");


            if (extra.hasKey("id")) {
                if (id.equals("PARTY_HAT_CRAB")) {
                    ItemRarity rarity = upgrade ? ItemRarity.COMMON.getNextRarity() : ItemRarity.COMMON;
                    renderRarity(xPos, yPos, rarity);
                    return;
                }
                if (id.equals("SKYBLOCK_MENU") || id.contains("GENERATOR") || id.contains("RUNE")) {
                    if (compound.getTagId("Lore") == Constants.NBT.TAG_LIST) {
                        NBTTagList list = compound.getTagList("Lore", Constants.NBT.TAG_STRING);

                        if (list.tagCount() > 0) {
                            for (int j1 = 0; j1 < list.tagCount(); ++j1) {
                                String lore = list.getStringTagAt(j1);

                                if (lore.contains("COSMETIC")) // temp
                                {
                                    renderRarity(xPos, yPos, ItemRarity.byBaseColor(lore.charAt(0) + "" + lore.charAt(1)));
                                }
                            }
                        }
                    }
                    return;
                }

                if (displayName.startsWith("\u00a7f\u00a7f")) {
                    displayName = displayName.substring(4);

                    if (displayName.matches("\\u00a7[0-9a-fk-or]\\d.*")) {
                        displayName = displayName.replaceAll("\\u00a7[0-9a-fk-or]\\d.*x \\u00a7f", "");
                    }
                }

                Matcher mat = PATTERN.matcher(displayName);

                if (mat.matches()) {
                renderRarity(xPos, yPos, ItemRarity.byBaseColor(mat.group("color")));
                }
            }
        }
    }

    private  void renderRarity(int xPos, int yPos, ItemRarity rarity) {
        if (rarity != null) {
            float itemRarityOpacity = 100;
            float alpha = itemRarityOpacity / 100.0F;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            Minecraft.getMinecraft().getTextureManager().bindTexture(RARITY);
            GlStateManager.color(rarity.getColorToRender().floatRed(), rarity.getColorToRender().floatGreen(), rarity.getColorToRender().floatBlue(), alpha);
            GlStateManager.blendFunc(770, 771);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
            Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, 16, 16, 16, 16);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.disableAlpha();
        }
    }

}
