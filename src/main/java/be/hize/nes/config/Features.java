package be.hize.nes.config;

import be.hize.nes.NES;
import be.hize.nes.config.core.config.features.About;
import be.hize.nes.config.core.config.features.GUI;
import be.hize.nes.config.core.config.features.Misc;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.Config;
import io.github.moulberry.moulconfig.Social;
import io.github.moulberry.moulconfig.annotations.Category;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class Features extends Config {

    public static final ResourceLocation DISCORD = new ResourceLocation("notenoughupdates:social/discord.png");
    public static final ResourceLocation GITHUB = new ResourceLocation("notenoughupdates:social/github.png");

    @Override
    public boolean shouldAutoFocusSearchbar() {
        return true;
    }

    @Override
    public List<Social> getSocials() {
        return Arrays.asList(
                Social.forLink("Join our Discord", DISCORD, "https://discord.gg/8DXVN4BJz3"),
                Social.forLink("Look at the code", GITHUB, "https://github.com/superhize/NES")
        );
    }

    @Override
    public void saveNow() {
        NES.configManager.saveConfig("close-gui");
    }

    @Override
    public String getTitle() {
        return "NES " + NES.getVersion() + " by §cHiZe§r, config by §5Moulberry §rand §5nea89";
    }

    @Expose
    @Category(name = "About", desc = "Information about the mod")
    public About about = new About();

    @Expose
    @Category(name = "GUI Locations", desc = "Change the locations of GUI elements. (§e/sh gui§7)")
    public GUI gui = new GUI();

    @Expose
    @Category(name = "Misc", desc = "Gonna put everything here, you have category on the left, that's good enough")
    public Misc misc = new Misc();

    /*@Expose
    @Category(name = "Inventory", desc = "Inventory features")
    public Inventory inventory = new Inventory();*/

    @Expose
    public Storage storage = new Storage();


}
