package com.example.espmod.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends Screen {

    public interface BooleanGetter { boolean get(); }
    public interface BooleanSetter { void set(boolean v); }
    public interface NumberGetter { float get(); }
    public interface NumberSetter { void set(float v); }

    public static class Setting {
        public String label;
        public Setting(String label) { this.label = label; }
    }

    public static class BoolSetting extends Setting {
        public BooleanGetter getter;
        public BooleanSetter setter;
        public BoolSetting(String label, BooleanGetter getter, BooleanSetter setter) {
            super(label);
            this.getter = getter;
            this.setter = setter;
        }
    }

    public static class NumSetting extends Setting {
        public NumberGetter getter;
        public NumberSetter setter;
        public float min, max, step;
        public String format;
        public NumSetting(String label, NumberGetter getter, NumberSetter setter, float min, float max, float step, String format) {
            super(label);
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
            this.format = format;
        }
    }

    public static class ModuleItem {
        public String name;
        public String desc;
        public BooleanGetter getter;
        public BooleanSetter setter;
        public boolean expanded = false;
        public List<Setting> settings = new ArrayList<>();

        public ModuleItem(String name, String desc, BooleanGetter getter, BooleanSetter setter) {
            this.name = name;
            this.desc = desc;
            this.getter = getter;
            this.setter = setter;
        }

        public ModuleItem addSetting(Setting s) {
            this.settings.add(s);
            return this;
        }
    }

    private int activeTab = 0; // 0: VISUALS, 1: COMBAT, 2: MOVEMENT
    private static final String[] TABS = {"VISUALS", "COMBAT", "MOVEMENT"};
    private final List<List<ModuleItem>> categories = new ArrayList<>();

    private int scrollOffset = 0;

    public ClickGuiScreen() {
        super(new StringTextComponent("DoomsDay Style ClickGUI"));

        // Initialize Module Architecture
        List<ModuleItem> visuals = new ArrayList<>();
        List<ModuleItem> combat = new ArrayList<>();
        List<ModuleItem> movement = new ArrayList<>();

        // ===== 1. VISUALS: ESP =====
        ModuleItem esp = new ModuleItem("ESP", "Подсветка сквозь стены (ПКМ для настроек)", () -> ModConfig.espEnabled, v -> ModConfig.espEnabled = v);
        esp.addSetting(new BoolSetting("3D Box (Рамка вокруг модели)", () -> ModConfig.espBox, v -> ModConfig.espBox = v));
        esp.addSetting(new BoolSetting("Tracers (Линии от прицела)", () -> ModConfig.espTracers, v -> ModConfig.espTracers = v));
        esp.addSetting(new NumSetting("Line Width", () -> ModConfig.espLineWidth, v -> ModConfig.espLineWidth = v, 1.0f, 5.0f, 0.5f, "%.1f px"));
        esp.addSetting(new NumSetting("Max Distance", () -> ModConfig.maxDistance, v -> ModConfig.maxDistance = v, 16.0f, 128.0f, 16.0f, "%.0f m"));
        esp.addSetting(new BoolSetting("Цель: Игроки (Players)", () -> ModConfig.targetPlayers, v -> ModConfig.targetPlayers = v));
        esp.addSetting(new BoolSetting("Цель: Монстры (Monsters)", () -> ModConfig.targetMonsters, v -> ModConfig.targetMonsters = v));
        esp.addSetting(new BoolSetting("Цель: Животные (Animals)", () -> ModConfig.targetAnimals, v -> ModConfig.targetAnimals = v));
        esp.addSetting(new BoolSetting("Цель: Прочее (Misc Items/Carts)", () -> ModConfig.targetMisc, v -> ModConfig.targetMisc = v));
        visuals.add(esp);

        // ===== VISUALS: NameTags =====
        ModuleItem nameTags = new ModuleItem("NameTags", "Плавающие имена через стены (ПКМ настройки)", () -> ModConfig.nameTagsEnabled, v -> ModConfig.nameTagsEnabled = v);
        nameTags.addSetting(new BoolSetting("Show Health (Здоровье HP)", () -> ModConfig.nameTagsHealth, v -> ModConfig.nameTagsHealth = v));
        nameTags.addSetting(new BoolSetting("Show Distance (Дистанция в метрах)", () -> ModConfig.nameTagsDistance, v -> ModConfig.nameTagsDistance = v));
        visuals.add(nameTags);

        // ===== VISUALS: Fullbright =====
        visuals.add(new ModuleItem("Fullbright", "Ночное зрение (100% гамма света)", () -> ModConfig.fullbrightEnabled, v -> ModConfig.setFullbright(v)));

        // ===== 2. COMBAT: HitBox =====
        ModuleItem hitbox = new ModuleItem("HitBox Expander", "Расширение хитбоксов для ударов и F3+B (ПКМ настройки)", () -> ModConfig.hitboxEnabled, v -> HitboxHandler.setEnabled(v));
        hitbox.addSetting(new NumSetting("Expansion Size", () -> ModConfig.hitboxSize, v -> HitboxHandler.setExpansion(v), 0.1f, 3.0f, 0.1f, "+%.1f блоков"));
        combat.add(hitbox);

        // ===== 3. MOVEMENT: AutoSprint =====
        movement.add(new ModuleItem("AutoSprint", "Автоматический бег при движении вперёд", () -> ModConfig.autoSprint, v -> ModConfig.autoSprint = v));

        categories.add(visuals);
        categories.add(combat);
        categories.add(movement);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Dim world
        fill(matrixStack, 0, 0, this.width, this.height, 0x880A0E17);

        int winW = 440;
        int winH = 280;
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // Main Frame with Neon Border
        fill(matrixStack, winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFF00B4D8);
        fill(matrixStack, winX, winY, winX + winW, winY + winH, 0xFF111722);

        // Top Header
        fill(matrixStack, winX, winY, winX + winW, winY + 34, 0xFF192231);
        drawCenteredString(matrixStack, this.font, "✦ DOOMSDAY STYLE CLIENT ✦", winX + winW / 2, winY + 8, 0xFF00E5FF);
        drawCenteredString(matrixStack, this.font, "ЛКМ: Вкл/Выкл | ПКМ: Открыть настройки модуля | [INSERT]/[ESC]: Выход", winX + winW / 2, winY + 22, 0xFF7D8B9E);

        // Tab Navigation
        int tabY = winY + 36;
        int tabW = winW / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            int tabX = winX + i * tabW;
            boolean isSelected = (activeTab == i);
            int bgColor = isSelected ? 0xFF0077B6 : 0xFF1A2332;
            fill(matrixStack, tabX + 2, tabY, tabX + tabW - 2, tabY + 24, bgColor);
            if (isSelected) {
                fill(matrixStack, tabX + 2, tabY + 22, tabX + tabW - 2, tabY + 24, 0xFF00E5FF);
            }
            drawCenteredString(matrixStack, this.font, TABS[i], tabX + tabW / 2, tabY + 8, isSelected ? 0xFFFFFFFF : 0xFFA0B0C0);
        }

        // Content Area with Scissor / Clipping Box
        int contentX = winX + 10;
        int contentY = winY + 68;
        int contentW = winW - 20;
        int contentH = winH - 78;
        fill(matrixStack, contentX, contentY, contentX + contentW, contentY + contentH, 0xFF0B0F17);

        List<ModuleItem> modules = categories.get(activeTab);

        int curY = contentY + 6 - scrollOffset;

        for (ModuleItem mod : modules) {
            // Draw Main Module Card
            if (curY + 32 >= contentY && curY <= contentY + contentH) {
                boolean hovered = (mouseX >= contentX + 6 && mouseX <= contentX + contentW - 6 && mouseY >= curY && mouseY <= curY + 32);
                int cardBg = hovered ? 0xFF202A3C : 0xFF161E2C;
                fill(matrixStack, contentX + 6, curY, contentX + contentW - 6, curY + 32, cardBg);

                // Module name & description
                drawString(matrixStack, this.font, mod.name, contentX + 14, curY + 7, 0xFFFFFFFF);
                drawString(matrixStack, this.font, mod.desc, contentX + 14, curY + 19, 0xFF7D8B9E);

                // Right side: expand arrow + ON/OFF pill
                String arrow = mod.settings.isEmpty() ? "" : (mod.expanded ? "▼" : "►");
                if (!arrow.isEmpty()) {
                    drawString(matrixStack, this.font, arrow, contentX + contentW - 74, curY + 11, 0xFF00E5FF);
                }

                int btnW = 46;
                int btnH = 20;
                int btnX = contentX + contentW - btnW - 12;
                int btnY = curY + 6;
                int btnColor = mod.getter.get() ? 0xFF2ECC71 : 0xFF4A5568;
                fill(matrixStack, btnX, btnY, btnX + btnW, btnY + btnH, btnColor);
                drawCenteredString(matrixStack, this.font, mod.getter.get() ? "ON" : "OFF", btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
            }
            curY += 36;

            // Draw Dropdown Settings if expanded via Right-Click (ПКМ)
            if (mod.expanded) {
                for (Setting s : mod.settings) {
                    if (curY + 24 >= contentY && curY <= contentY + contentH) {
                        fill(matrixStack, contentX + 18, curY, contentX + contentW - 18, curY + 24, 0xFF121924);

                        if (s instanceof BoolSetting) {
                            BoolSetting bs = (BoolSetting) s;
                            boolean val = bs.getter.get();
                            drawString(matrixStack, this.font, (val ? "§a[✓] §f" : "§7[  ] §7") + bs.label, contentX + 26, curY + 8, 0xFFCBD5E1);
                        } else if (s instanceof NumSetting) {
                            NumSetting ns = (NumSetting) s;
                            drawString(matrixStack, this.font, ns.label + ":", contentX + 26, curY + 8, 0xFFCBD5E1);

                            int btnSize = 16;
                            int plusX = contentX + contentW - 18 - btnSize - 6;
                            int minusX = contentX + contentW - 18 - btnSize * 2 - 64;
                            int bY = curY + 4;

                            fill(matrixStack, minusX, bY, minusX + btnSize, bY + btnSize, 0xFF2D3748);
                            drawCenteredString(matrixStack, this.font, "-", minusX + btnSize / 2, bY + 4, 0xFFFFFFFF);

                            String valStr = String.format(Locale.US, ns.format, ns.getter.get());
                            drawCenteredString(matrixStack, this.font, valStr, minusX + btnSize + 28, bY + 4, 0xFF00E5FF);

                            fill(matrixStack, plusX, bY, plusX + btnSize, bY + btnSize, 0xFF2D3748);
                            drawCenteredString(matrixStack, this.font, "+", plusX + btnSize / 2, bY + 4, 0xFFFFFFFF);
                        }
                    }
                    curY += 26;
                }
                curY += 4;
            }
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = 440;
        int winH = 280;
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // Check Tab Clicks
        int tabY = winY + 36;
        int tabW = winW / TABS.length;
        if (mouseY >= tabY && mouseY <= tabY + 24) {
            for (int i = 0; i < TABS.length; i++) {
                int tabX = winX + i * tabW;
                if (mouseX >= tabX && mouseX <= tabX + tabW) {
                    activeTab = i;
                    scrollOffset = 0;
                    playClickSound();
                    return true;
                }
            }
        }

        int contentX = winX + 10;
        int contentY = winY + 68;
        int contentW = winW - 20;
        int contentH = winH - 78;

        if (mouseX < contentX || mouseX > contentX + contentW || mouseY < contentY || mouseY > contentY + contentH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<ModuleItem> modules = categories.get(activeTab);
        int curY = contentY + 6 - scrollOffset;

        for (ModuleItem mod : modules) {
            // Check Module Header Click
            if (mouseX >= contentX + 6 && mouseX <= contentX + contentW - 6 && mouseY >= curY && mouseY <= curY + 32) {
                if (button == 1) { // ПКМ (Right Click) -> Expand/Collapse Settings Dropdown
                    if (!mod.settings.isEmpty()) {
                        mod.expanded = !mod.expanded;
                        playClickSound();
                        return true;
                    }
                } else if (button == 0) { // ЛКМ (Left Click) -> Toggle Module ON/OFF
                    mod.setter.set(!mod.getter.get());
                    playClickSound();
                    return true;
                }
            }
            curY += 36;

            // Check Dropdown Settings Clicks
            if (mod.expanded) {
                for (Setting s : mod.settings) {
                    if (mouseX >= contentX + 18 && mouseX <= contentX + contentW - 18 && mouseY >= curY && mouseY <= curY + 24) {
                        if (s instanceof BoolSetting) {
                            BoolSetting bs = (BoolSetting) s;
                            bs.setter.set(!bs.getter.get());
                            playClickSound();
                            return true;
                        } else if (s instanceof NumSetting) {
                            NumSetting ns = (NumSetting) s;
                            int btnSize = 16;
                            int plusX = contentX + contentW - 18 - btnSize - 6;
                            int minusX = contentX + contentW - 18 - btnSize * 2 - 64;
                            int bY = curY + 4;

                            if (mouseX >= minusX && mouseX <= minusX + btnSize && mouseY >= bY && mouseY <= bY + btnSize) {
                                float val = Math.max(ns.min, ns.getter.get() - ns.step);
                                ns.setter.set(Math.round(val * 100.0f) / 100.0f);
                                playClickSound();
                                return true;
                            }
                            if (mouseX >= plusX && mouseX <= plusX + btnSize && mouseY >= bY && mouseY <= bY + btnSize) {
                                float val = Math.min(ns.max, ns.getter.get() + ns.step);
                                ns.setter.set(Math.round(val * 100.0f) / 100.0f);
                                playClickSound();
                                return true;
                            }
                        }
                    }
                    curY += 26;
                }
                curY += 4;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset = Math.max(0, scrollOffset - (int) (delta * 18));
        return true;
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_INSERT) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
