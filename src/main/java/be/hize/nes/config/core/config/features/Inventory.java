package be.hize.nes.config.core.config.features;

import be.hize.nes.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.*;

public class Inventory {

    @Expose
    @ConfigOption(name = "Chest Value", desc = "")
    @Accordion
    public ChestValueConfig chestValueConfig = new ChestValueConfig();

    public static class ChestValueConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enabled estimated value of chest")
        @ConfigEditorBoolean
        public boolean enabled = false;

        @Expose
        @ConfigOption(name = "Sorting Type", desc = "Price sorting type.")
        @ConfigEditorDropdown(values = {"Descending", "Ascending"})
        public int sortingType = 0;

        @Expose
        @ConfigOption(name = "Value formatting Type", desc = "Format of the price.")
        @ConfigEditorDropdown(values = {"Short", "Long"})
        public int formatType = 0;

        @Expose
        @ConfigOption(name = "Item To Show", desc = "Choose how many items are displayed.\nAll items in the chest are still counted for the total value.")
        @ConfigEditorSlider(
                minValue = 0,
                maxValue = 54,
                minStep = 1
        )
        public int itemToShow = 15;

        @Expose
        @ConfigOption(name = "Hide below", desc = "Item item value below configured amount.\nItems are still counted for the total value.")
        @ConfigEditorSlider(
                minValue = 100_00,
                maxValue = 10_000_000,
                minStep = 100_000
        )
        public int hideBelow = 100_000;


        @Expose
        public Position position = new Position(100, 100, false, true);
    }
}
