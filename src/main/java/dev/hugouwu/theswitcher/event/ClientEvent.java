package dev.hugouwu.theswitcher.event;

import dev.hugouwu.theswitcher.config.Config;
import dev.hugouwu.theswitcher.gui.AccountListScreen;
import dev.hugouwu.theswitcher.utils.Expression;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEvent {
	private static int tx;
	private static int ty;
	@SubscribeEvent
	public void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.gui instanceof GuiMultiplayer && Config.multiplayerScreenButton) {
			int bx;
			int by;
			try {
				bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX, event.gui.width,
						event.gui.height);
				by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY, event.gui.width,
						event.gui.height);
			} catch (Throwable t) {
				bx = event.gui.width / 2 + 4 + 76 + 79;
				by = event.gui.height - 28;
			}
			event.buttonList.add(new GuiButton(104027, bx, by, 20, 20, "S"));
		}
		if (event.gui instanceof GuiMainMenu) {
			if (Config.titleScreenButton) {
				int bx;
				int by;
				try {
					bx = (int) Expression.parseWidthHeight(Config.titleScreenButtonX,
							event.gui.width, event.gui.height);
					by = (int) Expression.parseWidthHeight(Config.titleScreenButtonY,
							event.gui.width, event.gui.height);
				} catch (Throwable t) {
					bx = event.gui.width / 2 + 104;
					by = event.gui.height / 4 + 48 + 72 - 24;
				}
				event.buttonList.add(new GuiButton(104027, bx, by, 20, 20, "S"));
			}
			if (Config.titleScreenText) {
				try {
					tx = (int) Expression.parseWidthHeight(Config.titleScreenTextX, event.gui.width,
							event.gui.height);
					ty = (int) Expression.parseWidthHeight(Config.titleScreenTextY, event.gui.width,
							event.gui.height);
				} catch (Throwable t) {
					tx = event.gui.width / 2;
					ty = event.gui.height / 4 + 48 + 72 + 12 + 22;
				}
			}
		}
	}

	@SubscribeEvent
	public void onScreenAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if ((event.gui instanceof GuiMainMenu || event.gui instanceof GuiMultiplayer)
				&& event.button.id == 104027) {
			event.gui.mc.displayGuiScreen(new AccountListScreen(event.gui));
		}
	}

	@SubscribeEvent
	public void onScreenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (event.gui instanceof GuiMainMenu && Config.titleScreenText) {
			if (Config.titleScreenTextAlignment == Config.Alignment.LEFT) {
				event.gui.drawString(event.gui.mc.fontRendererObj,
						I18n.format("ias.title", event.gui.mc.getSession().getUsername()), tx, ty,
						0xFFCC8888);
			} else if (Config.titleScreenTextAlignment == Config.Alignment.RIGHT) {
				event.gui.drawString(event.gui.mc.fontRendererObj,
						I18n.format("ias.title", event.gui.mc.getSession().getUsername()),
						tx - event.gui.mc.fontRendererObj.getStringWidth(
								I18n.format("ias.title", event.gui.mc.getSession().getUsername())),
						ty, 0xFFCC8888);
			} else {
				event.gui.drawCenteredString(event.gui.mc.fontRendererObj,
						I18n.format("ias.title", event.gui.mc.getSession().getUsername()), tx, ty,
						0xFFCC8888);
			}
		}
	}
}
