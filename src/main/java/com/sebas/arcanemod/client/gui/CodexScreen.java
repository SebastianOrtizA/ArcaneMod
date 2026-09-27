package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CodexScreen extends Screen {

    private static final Identifier BOOK_BACKGROUND =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/codex_background.png");

    private static final int PANEL_MARGIN = 16;
    private static final int BORDER_THICKNESS = 14;
    private static final int TAB_SIZE = 22;
    private static final int SELECTED_GROW = 8;
    private static final int TAB_TOP_MARGIN = 10;
    private static final int BUTTON_ROW_HEIGHT = 30;
    private static final int MAX_READING_WIDTH = 480;
    private static final int CONTENT_PADDING = 20;

    private static final int COLOR_INK = 0xFF3F2A14;
    private static final int COLOR_INK_FADED = 0xFF6B5636;

    private int currentCategory = 0;
    private int currentPage = 0;

    private Button nextButton;
    private Button prevButton;
    private final List<CodexTabButton> tabButtons = new ArrayList<>();

    private int tabRowCount = 1;
    private int tabsPerRow = 1;

    public CodexScreen() {
        super(Component.literal("Codex Arcanum"));
    }

    private int panelX() {
        return PANEL_MARGIN;
    }

    private int panelY() {
        return TAB_TOP_MARGIN + tabRowCount * TAB_SIZE + SELECTED_GROW;
    }

    private int panelWidth() {
        return this.width - PANEL_MARGIN * 2;
    }

    private int panelHeight() {
        return this.height - panelY() - PANEL_MARGIN - BUTTON_ROW_HEIGHT;
    }

    private int contentWidth() {
        int available = panelWidth() - 2 * (BORDER_THICKNESS + CONTENT_PADDING);
        return Math.max(60, Math.min(MAX_READING_WIDTH, available));
    }

    @Override
    protected void init() {
        super.init();
        tabButtons.clear();

        int categoryCount = CodexData.CATEGORIES.size();
        this.tabsPerRow = Math.max(1, this.width / TAB_SIZE);
        this.tabRowCount = (int) Math.ceil((double) categoryCount / tabsPerRow);

        for (int i = 0; i < categoryCount; i++) {
            final int categoryIndex = i;
            CodexCategory category = CodexData.CATEGORIES.get(i);

            CodexTabButton tabButton = this.addRenderableWidget(new CodexTabButton(
                    0, 0, TAB_SIZE,
                    Component.literal(category.name()),
                    category.icon(),
                    category.customIconTexture(),
                    () -> currentCategory == categoryIndex,
                    () -> switchCategory(categoryIndex)
            ));
            tabButton.setTooltip(Tooltip.create(Component.literal(category.name())));
            tabButtons.add(tabButton);
        }

        layoutTabs();

        int panelY = panelY();
        int panelHeight = panelHeight();
        int buttonY = panelY + panelHeight + 8;

        this.prevButton = this.addRenderableWidget(
                Button.builder(Component.literal("< Prev"), button -> changePage(-1))
                        .bounds(this.width / 2 - 105, buttonY, 100, 20)
                        .build()
        );

        this.nextButton = this.addRenderableWidget(
                Button.builder(Component.literal("Next >"), button -> changePage(1))
                        .bounds(this.width / 2 + 5, buttonY, 100, 20)
                        .build()
        );

        updateButtonStates();
    }

    private void switchCategory(int index) {
        this.currentCategory = index;
        this.currentPage = 0;
        layoutTabs();
        updateButtonStates();
    }

    /**
     * Positions every tab in its row, widening the currently selected tab and
     * shifting its row-mates aside so nothing overlaps.
     */
    private void layoutTabs() {
        int categoryCount = tabButtons.size();

        for (int row = 0; row * tabsPerRow < categoryCount; row++) {
            int rowStart = row * tabsPerRow;
            int tabsInThisRow = Math.min(tabsPerRow, categoryCount - rowStart);

            int rowWidth = 0;
            for (int j = rowStart; j < rowStart + tabsInThisRow; j++) {
                rowWidth += (j == currentCategory) ? TAB_SIZE + SELECTED_GROW : TAB_SIZE;
            }
            int rowStartX = this.width / 2 - rowWidth / 2;

            int x = rowStartX;
            for (int col = 0; col < tabsInThisRow; col++) {
                int index = rowStart + col;
                boolean isSelected = index == currentCategory;
                int size = isSelected ? TAB_SIZE + SELECTED_GROW : TAB_SIZE;
                int y = TAB_TOP_MARGIN + row * TAB_SIZE - (isSelected ? SELECTED_GROW / 2 : 0);

                CodexTabButton button = tabButtons.get(index);
                button.setX(x);
                button.setY(y);
                button.setWidth(size);
                button.setHeight(size);

                x += size;
            }
        }
    }

    /** Recomputed on demand rather than cached — see {@link CodexCategory}'s javadoc. */
    private List<CodexPage> currentPages() {
        return CodexResearchPages.forSection(CodexData.CATEGORIES.get(currentCategory).section());
    }

    private void changePage(int direction) {
        List<CodexPage> pages = currentPages();
        int newPage = this.currentPage + direction;
        if (newPage >= 0 && newPage < pages.size()) {
            this.currentPage = newPage;
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        List<CodexPage> pages = currentPages();
        this.prevButton.active = currentPage > 0;
        this.nextButton.active = currentPage < pages.size() - 1;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBlurredBackground(graphics);

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();

        graphics.blit(BOOK_BACKGROUND, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0.0F, 1.0F, 0.0F, 1.0F);

        net.minecraftforge.client.event.ForgeEventFactoryClient.onRenderScreenBackground(this, graphics);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        List<CodexPage> pages = currentPages();
        CodexPage page = pages.get(Math.min(currentPage, pages.size() - 1));

        int panelY = panelY();
        int panelHeight = panelHeight();
        int contentTop = panelY + BORDER_THICKNESS + 20;

        drawCenteredText(graphics, page.title(), this.width / 2, contentTop, COLOR_INK);

        int contentWidth = contentWidth();
        graphics.textWithWordWrap(
                this.font,
                FormattedText.of(page.content()),
                this.width / 2 - contentWidth / 2,
                contentTop + 20,
                contentWidth,
                COLOR_INK,
                false
        );

        drawCenteredText(graphics,
                (currentPage + 1) + " / " + pages.size(),
                this.width / 2, panelY + panelHeight - BORDER_THICKNESS - 12, COLOR_INK_FADED);
    }

    private void drawCenteredText(GuiGraphicsExtractor graphics, String text, int centerX, int y, int color) {
        graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
