package dev.hugouwu.theswitcher.gui;

import dev.hugouwu.theswitcher.account.*;
import dev.hugouwu.theswitcher.utils.Shared;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

public class LoginScreen extends GuiScreen {
	private final GuiScreen prev;
	private final String title;
	private final String buttonText;
	private final String buttonTip;
	private final Consumer<Account> handler;
	private final MicrosoftAuthCallback callback = new MicrosoftAuthCallback();
	private GuiTextField username;
    private GuiTextField token;
    private GuiButton addToken;
	private GuiButton offline;
	private GuiButton microsoft;
	private String state;
	public LoginScreen(GuiScreen prev, String title, String buttonText, String buttonTip,
			Consumer<Account> handler) {
		this.prev = prev;
		this.title = title;
		this.buttonText = buttonText;
		this.buttonTip = buttonTip;
		this.handler = handler;
	}

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 28, 150, 20,
                I18n.format("gui.cancel")));
        int startY = this.height / 2 - 70;

        token = new GuiTextField(4, this.fontRendererObj, this.width / 2 - 100, startY, 200, 20);
        token.setMaxStringLength(2048);

        addToken = new GuiButton(5, this.width / 2 - 50, startY + 28, 100, 20,
                I18n.format("ias.loginGui.token"));
        addToken.enabled = false;
        buttonList.add(addToken);

        username = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, startY + 56, 200, 20);
        username.setMaxStringLength(16);

        offline = new GuiButton(0, this.width / 2 - 50, startY + 84, 100, 20, buttonText);
        offline.enabled = false;
        buttonList.add(offline);

        microsoft = new GuiButton(3, this.width / 2 - 50, startY + 112, 100, 20,
                I18n.format("ias.loginGui.microsoft"));
        buttonList.add(microsoft);
    }

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0)
			loginOffline();
		else if (button.id == 1)
			mc.displayGuiScreen(prev);
		else if (button.id == 3)
			loginMicrosoft();
        else if (button.id == 5)
            loginToken();
		super.actionPerformed(button);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		username.mouseClicked(mouseX, mouseY, mouseButton);
        token.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

    @Override
    public void drawScreen(int mx, int my, float delta) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, this.title, this.width / 2, 5, -1);
        drawCenteredString(fontRendererObj, I18n.format("ias.loginGui.nickname"), this.width / 2,
                height / 2 - 22, -1);
        drawCenteredString(fontRendererObj, I18n.format("ias.loginGui.token") + ":", this.width / 2,
                height / 2 - 70 - 10, -1);

        if (state != null) {
            drawCenteredString(fontRendererObj, state, width / 2, height / 3 * 2, 0xFFFF9900);
        }
        username.drawTextBox();
        token.drawTextBox();

        super.drawScreen(mx, my, delta);
    }

	@Override
	public void keyTyped(char c, int key) throws IOException {
		if (key == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(prev);
			return;
		}
		if (username.textboxKeyTyped(c, key))
			return;
        if (token.textboxKeyTyped(c, key))
            return;
		super.keyTyped(c, key);
	}

	@Override
	public void onGuiClosed() {
		Shared.EXECUTOR.execute(callback::close);
		super.onGuiClosed();
	}

    @Override
    public void updateScreen() {
        offline.enabled = !username.getText().trim().isEmpty() && state == null;
        addToken.enabled = !token.getText().trim().isEmpty() && state == null;
        username.setEnabled(state == null);
        token.setEnabled(state == null);
        microsoft.enabled = state == null;
        username.updateCursorCounter();
        token.updateCursorCounter();
        super.updateScreen();
    }

	private void loginMicrosoft() {
		state = "";
		Shared.EXECUTOR.execute(() -> {
			state = I18n.format("ias.loginGui.microsoft.checkBrowser");
			openURI(MicrosoftAuthCallback.MICROSOFT_AUTH_URL);
			callback.start((s, o) -> state = I18n.format(s, o),
					I18n.format("ias.loginGui.microsoft.canClose")).whenComplete((acc, t) -> {
						if (mc.currentScreen != this)
							return;
						if (t != null) {
							mc.addScheduledTask(() -> mc.displayGuiScreen(
									new AlertScreen(() -> mc.displayGuiScreen(prev),
											EnumChatFormatting.RED + I18n.format("ias.error"),
											String.valueOf(t))));
							return;
						}
						if (acc == null) {
							mc.addScheduledTask(() -> mc.displayGuiScreen(prev));
							return;
						}
						mc.addScheduledTask(() -> {
							handler.accept(acc);
							mc.displayGuiScreen(prev);
						});
					});
		});
	}

	private void loginOffline() {
		state = "";
		Shared.EXECUTOR.execute(() -> {
			state = I18n.format("ias.loginGui.offline.progress");
			Account account = new OfflineAccount(username.getText(),
					Auth.resolveUUID(username.getText()));
			mc.addScheduledTask(() -> {
				handler.accept(account);
				mc.displayGuiScreen(prev);
			});
		});
	}

    private void loginToken() {
        state = "";
        Shared.EXECUTOR.execute(() -> {
            state = I18n.format("ias.loginGui.token.progress");
            try {
                Account account = new TokenAccount(token.getText());
                mc.addScheduledTask(() -> {
                    handler.accept(account);
                    mc.displayGuiScreen(prev);
                });
            } catch (Exception e) {
                Shared.LOG.error("Token login failed", e);
                mc.addScheduledTask(() -> {
                    if (mc.currentScreen != this) return;
                    mc.displayGuiScreen(new AlertScreen(() -> mc.displayGuiScreen(prev),
                            EnumChatFormatting.RED + I18n.format("ias.error"),
                            e.getMessage()));
                });
            }
        });
    }

	private void openURI(String uri) {
		try {
			Desktop.getDesktop().browse(new URI(uri));
		} catch (Throwable t) {
			Sys.openURL(uri);
		}
	}
}
