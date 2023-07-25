package be.hize.nes.events


open class GuiRenderEvent: LorenzEvent() {
    class ChestBackgroundRenderEvent: GuiRenderEvent()
    class GameOverlayRenderEvent: GuiRenderEvent()
}