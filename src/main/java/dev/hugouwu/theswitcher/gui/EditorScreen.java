package dev.hugouwu.theswitcher.gui;

import dev.hugouwu.theswitcher.account.Account;
import dev.hugouwu.theswitcher.platform.mixin.MinecraftAccessor;
import dev.hugouwu.theswitcher.utils.APIUtils;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

public class EditorScreen extends GuiScreen {
    private final GuiScreen previousScreen;
    private final Account account;
    private String status = "";
    private GuiTextField nameField;
    private GuiTextField skinField;

    public EditorScreen(GuiScreen previousScreen, Account account) {
        this.previousScreen = previousScreen;
        this.account = account;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        nameField = new GuiTextField(1, mc.fontRendererObj, width / 2 - 100, height / 2 - 20, 200, 20);
        nameField.setMaxStringLength(16);
        nameField.setFocused(true);
        skinField = new GuiTextField(2, mc.fontRendererObj, width / 2 - 100, height / 2 + 20, 200, 20);
        skinField.setMaxStringLength(32767);
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 50, 98, 20,
                I18n.format("ias.editorGui.changeName")));
        buttonList.add(new GuiButton(1, width / 2 + 2, height / 2 + 50, 98, 20,
                I18n.format("ias.editorGui.changeSkin")));
        buttonList.add(new GuiButton(2, width / 2 - 100, height / 2 + 80, 200, 20,
                I18n.format("gui.cancel")));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("ias.editorGui.title", account.name()),
                width / 2, 20, -1);
        if (!status.isEmpty()) {
            drawCenteredString(fontRendererObj, status, width / 2, height / 2 - 60, 0xFFFFFFFF);
        }
        drawString(fontRendererObj, I18n.format("ias.editorGui.nameLabel"), width / 2 - 100, height / 2 - 32, 0xFFFFFFFF);
        drawString(fontRendererObj, I18n.format("ias.editorGui.skinLabel"), width / 2 - 100, height / 2 + 8, 0xFFFFFFFF);
        nameField.drawTextBox();
        skinField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            changeName();
        } else if (button.id == 1) {
            changeSkin();
        } else if (button.id == 2) {
            mc.displayGuiScreen(previousScreen);
        }
        super.actionPerformed(button);
    }

    private void changeName() {
        String newName = nameField.getText();
        if (newName.isEmpty()) {
            status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.emptyName");
            return;
        }
        String token = mc.getSession().getToken();
        if (token == null || token.isEmpty() || "0".equals(token)) {
            status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.noToken");
            return;
        }
        status = EnumChatFormatting.YELLOW + I18n.format("ias.editorGui.changing");
        new Thread(() -> {
            try {
                int statusCode = APIUtils.changeName(newName, token);
                if (statusCode == 200) {
                    status = EnumChatFormatting.GREEN + I18n.format("ias.editorGui.success.nameChanged");
                    mc.addScheduledTask(() -> {
                        Session current = mc.getSession();
                        ((MinecraftAccessor) Minecraft.getMinecraft()).setSession(
                                new Session(newName, current.getPlayerID(), current.getToken(), current.getSessionType().name()));
                    });
                } else if (statusCode == 429) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.tooManyRequests");
                } else if (statusCode == 400) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.invalidName");
                } else if (statusCode == 401) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.invalidToken");
                } else if (statusCode == 403) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.nameUnavailable");
                } else {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.unknown", statusCode);
                }
            } catch (Exception e) {
                status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.exception", e.getMessage());
            }
        }, "IAS Name Changer").start();
    }

    private void changeSkin() {
        String skinUrl = skinField.getText();
        if (skinUrl.isEmpty()) {
            status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.emptySkin");
            return;
        }
        String token = mc.getSession().getToken();
        if (token == null || token.isEmpty() || "0".equals(token)) {
            status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.noToken");
            return;
        }
        status = EnumChatFormatting.YELLOW + I18n.format("ias.editorGui.changing");
        new Thread(() -> {
            try {
                int statusCode = APIUtils.changeSkin(skinUrl, token);
                if (statusCode == 200) {
                    status = EnumChatFormatting.GREEN + I18n.format("ias.editorGui.success.skinChanged");
                } else if (statusCode == 429) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.tooManyRequests");
                } else if (statusCode == 401) {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.invalidToken");
                } else {
                    status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.invalidSkin");
                }
            } catch (Exception e) {
                status = EnumChatFormatting.RED + I18n.format("ias.editorGui.error.exception", e.getMessage());
            }
        }, "IAS Skin Changer").start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.isFocused()) {
            nameField.textboxKeyTyped(typedChar, keyCode);
        } else if (skinField.isFocused()) {
            skinField.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == Keyboard.KEY_TAB) {
            if (nameField.isFocused()) {
                nameField.setFocused(false);
                skinField.setFocused(true);
            } else {
                skinField.setFocused(false);
                nameField.setFocused(true);
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        skinField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        skinField.updateCursorCounter();
    }
}
