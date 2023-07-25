package be.hize.nes.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class ColorUtils
{

    public static RGB stringToRGB(String color)
    {
        return ColorUtils.stringToRGB(color, false, null);
    }

    public static RGB stringToRGB(String color, boolean printException, String optionName)
    {
        try
        {
            String[] colorArray = color.split(",");
            float red = Float.parseFloat(colorArray[0]);
            float green = Float.parseFloat(colorArray[1]);
            float blue = Float.parseFloat(colorArray[2]);
            return new RGB(red, green, blue, 255.0F);
        }
        catch (Exception e)
        {
            if (printException)
            {
                e.printStackTrace();
            }
            return new RGB(true);
        }
    }

    public static class RGB
    {
        float red;
        float green;
        float blue;
        float alpha;
        boolean error;

        public RGB(float red, float green, float blue, float alpha)
        {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        RGB(boolean error)
        {
            this.error = error;
        }
        public float floatRed()
        {
            return this.red / 255.0F;
        }
        public float floatGreen()
        {
            return this.green / 255.0F;
        }
        public float floatBlue()
        {
            return this.blue / 255.0F;
        }
    }
}
