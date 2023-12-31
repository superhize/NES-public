package be.hize.nes.config.core.config.features;

import be.hize.nes.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.*;
import io.github.moulberry.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Misc {

    @Expose
    @Category(name = "Discord RPC", desc = "")
    public DiscordRPC discordRPC = new DiscordRPC();

    public static class DiscordRPC {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enabled discord RPC")
        @ConfigEditorBoolean
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "First Line", desc = "First line of the rpc")
        @ConfigEditorDropdown(values = {
                "Rien",
                "Purse",
                "Location",
                "Bits",
                "Stats",
                "Item in hand",
                "Time",
                "Profile name",
                "Slayers",
                "Custom first line",
                "Custom second line",
                "Crop Milestones",
                "Auto",
                "Motes Goal"})
        public Property<Integer> firstLine = Property.of(0);

        @Expose
        @ConfigOption(name = "Second Line", desc = "Second line of the rpc")
        @ConfigEditorDropdown(values = {
                "Rien",
                "Purse",
                "Location",
                "Bits",
                "Stats",
                "Item in hand",
                "Time",
                "Profile name",
                "Slayers",
                "Custom first line",
                "Custom second line",
                "Crop Milestones",
                "Auto",
                "Motes Goal"})
        public Property<Integer> secondLine = Property.of(0);

        @Expose
        @ConfigOption(name = "Custom First Line", desc = "Set a custom first line")
        @ConfigEditorText
        public Property<String> customFirstLine = Property.of("");

        @Expose
        @ConfigOption(name = "Custom Second Line", desc = "Set a custom second line")
        @ConfigEditorText
        public Property<String> customSecondLine = Property.of("");
    }

    @Expose
    @Category(name = "Ghosts", desc = "Ghost and Mist Config")
    public Ghost ghost = new Ghost();

    public static class Ghost {

        @Expose
        @ConfigOption(name = "Recolor mist", desc = "change color of blocs in the mist")
        @ConfigEditorBoolean
        public Property<Boolean> recolorMist = Property.of(false);

        @Expose
        @ConfigOption(name = "Mist color", desc = "Choose mist color")
        @ConfigEditorDropdown(values = {
                "White",
                "Orange",
                "Magenta",
                "Light Blue",
                "Yellow",
                "Lime",
                "Pink",
                "Gray",
                "Cyan",
                "Purple",
                "Blue",
                "Green",
                "Red",
                "Black",
                "Grey But Black",
                "Black But Grey",
                "Yes"
        })
        public Property<Integer> color = Property.of(7);

        @Expose
        @ConfigOption(name = "Recolor creeper", desc = "change color of blocs in the mist")
        @ConfigEditorBoolean
        public Property<Boolean> recolorCreeper = Property.of(false);

        @Expose
        @ConfigOption(name = "Creeper color", desc = "Choose creeper color")
        @ConfigEditorColour
        public Property<String> creeperColor = Property.of("0:245:85:255:85");
    }

    @Expose
    @Category(name = "Farm thing", desc = "Designed for me, not for you")
    public FarmBorderConfig farmBorderConfig = new FarmBorderConfig();

    public static class FarmBorderConfig {

        @Expose
        @ConfigOption(name = "Enable", desc = "Enable the feature")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        @ConfigOption(name = "Check Range", desc = "Range to check walls for")
        @ConfigEditorSlider(minStep = 1, minValue = 1, maxValue = 10)
        public int checkRange = 5;

        @Expose
        @ConfigOption(name = "Sound", desc = "Sound name")
        @ConfigEditorText
        public String soundName = "random.orb";

        @Expose
        @ConfigOption(name = "Sound Warp", desc = "Sound name")
        @ConfigEditorText
        public String soundNameWarp = "random.orb";

        @Expose
        @ConfigOption(name = "Sound Pitch", desc = "Pitch of the sound")
        @ConfigEditorSlider(minStep = 0.1f, minValue = 0.1f, maxValue = 2.0f)
        public float soundPitch = 0.5f;

        @Expose
        @ConfigOption(name = "Sound Volume", desc = "Volume of the sound")
        @ConfigEditorSlider(minStep = 0.1f, minValue = 0.1f, maxValue = 1.0f)
        public float soundVolume = 0.5f;

    }

    @Expose
    @Category(name = "FPS", desc = "")
    public FPS fps = new FPS();

    public static class FPS {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Show FPS HUD")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        @ConfigOption(name = "Format", desc = "Text format")
        @ConfigEditorText
        public String format = "&6FPS: &b%fps%";

        @Expose
        public Position position = new Position(0, 0, false, true);
    }

    @Expose
    @Category(name = "Coordinate", desc = "")
    public Coordinate coordinate = new Coordinate();

    public static class Coordinate {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Show Coordinate HUD")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        public Position position = new Position(0, 10, false, true);
    }

    @Expose
    @Category(name = "Facing", desc = "")
    public Facing facing = new Facing();

    public static class Facing {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Show Coordinate HUD")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        public Position position = new Position(0, 20, false, true);
    }

    @Expose
    @Category(name = "Item Rarity", desc = "")
    public ItemRarity itemRarity = new ItemRarity();

    public static class ItemRarity {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Render item rarity overlay")
        @ConfigEditorBoolean
        public boolean enabled = false;
    }

    @Expose
    @Category(name = "Trapper", desc = "")
    public Trapper trapper = new Trapper();

    public static class Trapper {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable Trevor the trapper helper")
        @ConfigEditorBoolean
        public boolean enabled = false;
    }

    @Expose
    @ConfigOption(name = "Config Button", desc = "Add a button to the pause menu to configure NES")
    @ConfigEditorBoolean
    public boolean pauseButton = true;

    @Expose
    @ConfigOption(name = "Highlight powder ghast", desc = "Highlight powder ghast in dwarven mines.")
    @ConfigEditorBoolean
    public boolean highlightPowderGhast = true;

    @Expose
    @ConfigOption(name = "Hide boss bar", desc = "Completely hide the boss bar")
    @ConfigEditorBoolean
    public boolean hideBossBar = false;

    @Expose
    @ConfigOption(name = "Enable Waypoints", desc = "Enable Waypoints feature")
    @ConfigEditorBoolean
    public boolean enableWaypoints = true;
}
