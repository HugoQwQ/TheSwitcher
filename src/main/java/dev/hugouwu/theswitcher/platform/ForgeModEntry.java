package dev.hugouwu.theswitcher.platform;

import dev.hugouwu.theswitcher.config.Config;
import dev.hugouwu.theswitcher.event.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "theswitcher", useMetadata = true, clientSideOnly = true,
		guiFactory = "dev.hugouwu.theswitcher.gui.GuiFactory")
public class ForgeModEntry {
	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		Config.load(Minecraft.getMinecraft().mcDataDir.toPath());
	}
}