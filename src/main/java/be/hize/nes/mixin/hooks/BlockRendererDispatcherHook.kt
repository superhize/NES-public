package be.hize.nes.mixin.hooks

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.LorenzUtils
import be.hize.nes.NES
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

private val colorList = listOf(
    EnumDyeColor.WHITE,
    EnumDyeColor.ORANGE,
    EnumDyeColor.MAGENTA,
    EnumDyeColor.LIGHT_BLUE,
    EnumDyeColor.YELLOW,
    EnumDyeColor.LIME,
    EnumDyeColor.PINK,
    EnumDyeColor.GRAY,
    EnumDyeColor.CYAN,
    EnumDyeColor.PURPLE,
    EnumDyeColor.BLUE,
    EnumDyeColor.GREEN,
    EnumDyeColor.RED,
    EnumDyeColor.BLACK
)

fun modifyGetModelFromBlockState(
    blockRendererDispatcher: BlockRendererDispatcher,
    state: IBlockState?,
    worldIn: IBlockAccess,
    pos: BlockPos?,
    cir: CallbackInfoReturnable<IBakedModel>
) {
    if (!LorenzUtils.inSkyBlock || state == null || pos == null) return
    var returnState = state
    if (LorenzUtils.skyBlockIsland == IslandType.DWARVEN_MINES) {
        if (NES.feature.misc.ghost.recolorMist.get() && pos.y <= 76) {
            if (state.block === Blocks.stained_glass) {
                returnState = state.withProperty(BlockStainedGlass.COLOR, colorList[NES.feature.misc.ghost.color.get()])
            }
        }
    }

    if (returnState !== state) {
        cir.returnValue = blockRendererDispatcher.blockModelShapes.getModelForState(returnState)
    }
}