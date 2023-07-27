package be.hize.nes.mixin.hooks

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.toLorenzVec
import be.hize.nes.NES
import be.hize.nes.features.misc.Ghost
import net.minecraft.block.BlockCarpet
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

val colorList = listOf(
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
        if (NES.feature.misc.ghost.recolorMist.get() && (pos.y <= 76)) {

            val greyButBlack = when (pos.y){
                75 -> EnumDyeColor.GRAY
                74 -> EnumDyeColor.GRAY
                73 -> EnumDyeColor.BLACK
                72 -> EnumDyeColor.BLACK
                else -> EnumDyeColor.BLACK
            }

            val blackButGrey = when (pos.y){
                75 -> EnumDyeColor.BLACK
                74 -> EnumDyeColor.BLACK
                73 -> EnumDyeColor.GRAY
                72 -> EnumDyeColor.GRAY
                else -> EnumDyeColor.GRAY
            }
            var color = when (val c = NES.feature.misc.ghost.color.get()){
                in 0 .. 13 -> colorList[c]
                14 -> greyButBlack
                15 -> blackButGrey
                16 -> colorList.random()
                else -> EnumDyeColor.BLACK
            }

            if (state.block === Blocks.stained_glass) {
                returnState = state.withProperty(BlockStainedGlass.COLOR, color)
            } else if (state.block === Blocks.carpet) {
                returnState = state.withProperty(BlockCarpet.COLOR, color)
            }
        }
    }

    if (returnState !== state) {
        cir.returnValue = blockRendererDispatcher.blockModelShapes.getModelForState(returnState)
    }
}