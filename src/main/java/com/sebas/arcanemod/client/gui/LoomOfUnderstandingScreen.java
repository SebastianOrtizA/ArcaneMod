package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.client.ClientResearchKnowledge;
import com.sebas.arcanemod.client.ClientResearchTree;
import com.sebas.arcanemod.core.research.ResearchNode;
import com.sebas.arcanemod.network.ModNetwork;
import com.sebas.arcanemod.network.RequestResearchPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoomOfUnderstandingScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_WIDTH = 320;
    private static final int LIST_TOP = 40;
    private static final int LIST_BOTTOM_MARGIN = 10;
    private static final int SCROLL_STEP = ROW_HEIGHT * 2;

    private Set<Identifier> lastKnownCompleted;
    private int scrollOffset;
    private int totalContentHeight;

    public LoomOfUnderstandingScreen() {
        super(Component.literal("Loom of Understanding"));
    }

    @Override
    protected void init() {
        super.init();
        scrollOffset = 0;
        rebuildRows();
        lastKnownCompleted = ClientResearchKnowledge.getCompleted();
    }

    @Override
    public void tick() {
        super.tick();
        Set<Identifier> current = ClientResearchKnowledge.getCompleted();
        if (!current.equals(lastKnownCompleted)) {
            lastKnownCompleted = current;
            rebuildRows();
        }
    }

    private void rebuildRows() {
        clearWidgets();

        List<Map.Entry<Identifier, ResearchNode>> sorted = new ArrayList<>(ClientResearchTree.entries());
        sorted.sort(Comparator.<Map.Entry<Identifier, ResearchNode>>comparingInt(e -> e.getValue().section())
                .thenComparing(e -> e.getValue().displayName()));

        int rowX = this.width / 2 - ROW_WIDTH / 2;
        int visibleBottom = this.height - LIST_BOTTOM_MARGIN;
        totalContentHeight = sorted.size() * ROW_HEIGHT;

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Identifier, ResearchNode> entry = sorted.get(i);
            Identifier id = entry.getKey();
            ResearchNode node = entry.getValue();

            int rowY = LIST_TOP + i * ROW_HEIGHT - scrollOffset;

            boolean completed = ClientResearchKnowledge.hasCompleted(id);
            boolean available = !completed && ClientResearchKnowledge.getCompleted().containsAll(node.prerequisites());

            String prefix = completed ? "[done] " : available ? "[available] " : "[locked] ";
            Button button = Button.builder(Component.literal(prefix + node.displayName()), b -> onRowClicked(id, node))
                    .bounds(rowX, rowY, ROW_WIDTH, ROW_HEIGHT - 2)
                    .build();
            button.active = available;
            button.visible = rowY + ROW_HEIGHT > LIST_TOP && rowY < visibleBottom;
            button.setTooltip(Tooltip.create(Component.literal(node.description())));
            this.addRenderableWidget(button);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleHeight = this.height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(scrollY * SCROLL_STEP)));
        rebuildRows();
        return true;
    }

    private void onRowClicked(Identifier id, ResearchNode node) {
        if (node.pattern().isEmpty()) {
            ModNetwork.sendToServer(new RequestResearchPacket(id));
        } else {
            this.minecraft.gui.setScreen(new LoomPuzzleScreen(id, node, this));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
