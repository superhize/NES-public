package be.hize.nes.config.core.config.features;

import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.*;

public class About {

    @ConfigOption(name = "Auto Updates", desc = "Automatically check for updates on each startup")
    @Expose
    @ConfigEditorBoolean
    public boolean autoUpdates = true;
    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses")
    @Accordion
    @Expose
    public Licenses licenses = new Licenses();
    public static class Licenses {

        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the LGPL v2.1")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable skyhanni = () -> OSUtils.openBrowser("https://github.com/hannibal002/SkyHanni");
        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> OSUtils.openBrowser("https://github.com/NotEnoughUpdates/MoulConfig");

        @ConfigOption(name = "NotEnoughUpdates", desc = "NotEnoughUpdates is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable notEnoughUpdates = () -> OSUtils.openBrowser("https://github.com/NotEnoughUpdates/NotEnoughUpdates");

        @ConfigOption(name = "Forge", desc = "Forge is available under the LGPL 3.0 license")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable forge = () -> OSUtils.openBrowser("https://github.com/MinecraftForge/MinecraftForge");

        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> OSUtils.openBrowser("https://git.nea.moe/nea/libautoupdate/");

        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable mixin = () -> OSUtils.openBrowser("https://github.com/SpongePowered/Mixin/");

        @ConfigOption(name = "DiscordIPC", desc = "DiscordIPC is available under the Apache License 2.0")
        @ConfigEditorButton(buttonText = "GitHub")
        public Runnable discordRPC = () -> OSUtils.openBrowser("https://github.com/jagrosh/DiscordIPC");
    }

}
