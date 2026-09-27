package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.research.FacetConnection;
import com.sebas.arcanemod.core.research.ResearchNode;
import com.sebas.arcanemod.network.ModNetwork;
import com.sebas.arcanemod.network.RequestResearchPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The Loom's actual puzzle board (design doc: "discovered Facets appear as nodes the player
 * physically drags threads between"). Reached from {@link LoomOfUnderstandingScreen} only for a
 * node whose {@code pattern} is non-empty — a node with no pattern completes directly from the
 * list, with no puzzle at all.
 * <p>
 * No server round trip happens until the puzzle is actually solved: the target pattern comparison
 * runs entirely client-side (the server re-validates prerequisites when the completion request
 * arrives regardless, via {@code RequestResearchPacket}, the same way the plain list's button
 * does) — there's no anti-cheat concern here worth a synced puzzle-state protocol for a
 * single-player-facing research minigame.
 */
public class LoomPuzzleScreen extends Screen {
    private static final int NODE_SIZE = 24;
    private static final int NODE_RADIUS = NODE_SIZE / 2;
    private static final int MAX_LAYOUT_RADIUS = 90;
    private static final int MIN_LAYOUT_RADIUS = 30;
    /** Extra clearance below each node reserved for its name label, on top of the node's own radius. */
    private static final int LABEL_CLEARANCE = 14;
    private static final int SIDE_MARGIN = 24;
    private static final int DESCRIPTION_WIDTH = 300;
    private static final int LINE_THICKNESS = 3;
    private static final int HINT_THICKNESS = 2;

    private static final int COLOR_HINT = 0x60FFFFFF;
    private static final int COLOR_DRAWN = 0xFFE8C468;
    private static final int COLOR_PENDING = 0xA0FFFFFF;
    private static final int COLOR_SOLVED_TEXT = 0xFF55FF55;
    private static final int COLOR_LABEL = 0xFFF0F0F0;

    private final Identifier nodeId;
    private final ResearchNode node;
    private final Screen parent;

    private final List<Facet> boardFacets;
    private final Set<FacetConnection> targetPattern;
    private final Set<FacetConnection> drawnConnections = new HashSet<>();
    private Facet pendingFrom;

    private Button completeButton;

    // Computed once in init() from the actual screen size and description length, rather than
    // fixed constants — a fixed radius overflowed into the title/description text on a puzzle
    // with only 2 nodes at a typical GUI scale, since the board's vertical extent (2x the radius)
    // wasn't checked against the space actually available between the header and the button row.
    private int boardCenterY;
    private int layoutRadius;

    public LoomPuzzleScreen(Identifier nodeId, ResearchNode node, Screen parent) {
        super(Component.literal(node.displayName()));
        this.nodeId = nodeId;
        this.node = node;
        this.parent = parent;
        this.boardFacets = node.requiredFacets().isEmpty()
                ? node.pattern().stream().flatMap(c -> Stream.of(c.a(), c.b())).distinct().toList()
                : node.requiredFacets();
        this.targetPattern = Set.copyOf(node.pattern());
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(this.width / 2 - 105, this.height - 30, 100, 20)
                .build());

        this.completeButton = this.addRenderableWidget(Button.builder(Component.literal("Complete"), b -> complete())
                .bounds(this.width / 2 + 5, this.height - 30, 100, 20)
                .build());
        this.completeButton.active = false;

        recomputeLayout();
    }

    /**
     * Fits the board's circle into whatever vertical band is actually free between the header
     * text (title + wrapped description) and the button row — instead of a fixed radius that
     * assumes a header short enough and a screen tall enough to never matter.
     */
    private void recomputeLayout() {
        int descriptionLines = this.font.split(FormattedText.of(node.description()), DESCRIPTION_WIDTH).size();
        int headerBottom = 16 + this.font.lineHeight + 6 + descriptionLines * this.font.lineHeight + 10;
        int footerTop = this.height - 30 - 16; // button row, minus room for the "solved" line above it

        int boardTop = headerBottom;
        int boardBottom = footerTop;
        if (boardBottom - boardTop < MIN_LAYOUT_RADIUS * 2) {
            // Screen too short to fit a minimum-size board between header and buttons without
            // overlap — center a minimum band on the midpoint rather than collapsing to nothing.
            int mid = (boardTop + boardBottom) / 2;
            boardTop = mid - MIN_LAYOUT_RADIUS;
            boardBottom = mid + MIN_LAYOUT_RADIUS;
        }
        this.boardCenterY = (boardTop + boardBottom) / 2;

        int verticalHalf = (boardBottom - boardTop) / 2 - NODE_RADIUS - LABEL_CLEARANCE;
        int horizontalHalf = this.width / 2 - NODE_RADIUS - SIDE_MARGIN;
        this.layoutRadius = Math.max(MIN_LAYOUT_RADIUS, Math.min(MAX_LAYOUT_RADIUS, Math.min(verticalHalf, horizontalHalf)));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    private int centerX() {
        return this.width / 2;
    }

    /** Evenly spaced around a circle — {@code index}/{@code count} of the way around. */
    private int nodeX(int index, int count) {
        double angle = 2 * Math.PI * index / count - Math.PI / 2;
        return centerX() + (int) Math.round(Math.cos(angle) * layoutRadius);
    }

    private int nodeY(int index, int count) {
        double angle = 2 * Math.PI * index / count - Math.PI / 2;
        return boardCenterY + (int) Math.round(Math.sin(angle) * layoutRadius);
    }

    private int indexOf(Facet facet) {
        return boardFacets.indexOf(facet);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            Facet clicked = facetAt(event.x(), event.y());
            if (clicked != null) {
                handleNodeClick(clicked);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private Facet facetAt(double mouseX, double mouseY) {
        int count = boardFacets.size();
        for (int i = 0; i < count; i++) {
            double dx = mouseX - nodeX(i, count);
            double dy = mouseY - nodeY(i, count);
            if (dx * dx + dy * dy <= NODE_RADIUS * NODE_RADIUS) {
                return boardFacets.get(i);
            }
        }
        return null;
    }

    private void handleNodeClick(Facet clicked) {
        if (pendingFrom == null) {
            pendingFrom = clicked;
        } else if (pendingFrom == clicked) {
            pendingFrom = null;
        } else {
            FacetConnection connection = FacetConnection.of(pendingFrom, clicked);
            if (!drawnConnections.remove(connection)) {
                drawnConnections.add(connection);
            }
            pendingFrom = null;
        }
        completeButton.active = drawnConnections.equals(targetPattern);
    }

    private void complete() {
        ModNetwork.sendToServer(new RequestResearchPacket(nodeId));
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, node.displayName(), centerX(), 16, 0xFFFFFFFF);
        graphics.textWithWordWrap(this.font, FormattedText.of(node.description()),
                centerX() - DESCRIPTION_WIDTH / 2, 16 + this.font.lineHeight + 6, DESCRIPTION_WIDTH, 0xFFC0C0C0);

        int count = boardFacets.size();

        // Hints only for low difficulty — see the design doc's difficulty table.
        if (node.difficulty() <= 1) {
            for (FacetConnection connection : targetPattern) {
                drawConnection(graphics, connection, HINT_THICKNESS, COLOR_HINT);
            }
        }

        for (FacetConnection connection : drawnConnections) {
            drawConnection(graphics, connection, LINE_THICKNESS, COLOR_DRAWN);
        }

        if (pendingFrom != null) {
            int fromX = nodeX(indexOf(pendingFrom), count);
            int fromY = nodeY(indexOf(pendingFrom), count);
            drawLine(graphics, fromX, fromY, mouseX, mouseY, LINE_THICKNESS, COLOR_PENDING);
        }

        for (int i = 0; i < count; i++) {
            Facet facet = boardFacets.get(i);
            int x = nodeX(i, count);
            int y = nodeY(i, count);
            int color = facetColor(facet);
            if (facet == pendingFrom) {
                graphics.fill(x - NODE_RADIUS - 2, y - NODE_RADIUS - 2, x + NODE_RADIUS + 2, y + NODE_RADIUS + 2, 0xFFFFFFFF);
            }
            graphics.fill(x - NODE_RADIUS, y - NODE_RADIUS, x + NODE_RADIUS, y + NODE_RADIUS, color);
            // The square is a pure color swatch — cramming even a 4-letter abbreviation inside it
            // was unreadable, so the full Facet name renders as its own label below the square
            // instead, in plain light text with the default drop shadow for contrast against
            // whatever's behind the screen (grass, sky, etc. — this screen has no opaque backdrop).
            String label = facet.name().substring(0, 1) + facet.name().substring(1).toLowerCase(Locale.ROOT);
            graphics.text(this.font, label, x - this.font.width(label) / 2, y + NODE_RADIUS + 2, COLOR_LABEL, true);
        }

        if (drawnConnections.equals(targetPattern)) {
            graphics.centeredText(this.font, "Pattern complete!", centerX(), this.height - 46, COLOR_SOLVED_TEXT);
        }
    }

    private void drawConnection(GuiGraphicsExtractor graphics, FacetConnection connection, int thickness, int color) {
        int count = boardFacets.size();
        int ia = indexOf(connection.a());
        int ib = indexOf(connection.b());
        if (ia < 0 || ib < 0) return;
        drawLine(graphics, nodeX(ia, count), nodeY(ia, count), nodeX(ib, count), nodeY(ib, count), thickness, color);
    }

    /** No diagonal-line primitive exists on {@code GuiGraphicsExtractor} — stepped small squares stand in for one. */
    private void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int thickness, int color) {
        double distance = Math.hypot(x2 - x1, y2 - y1);
        int steps = Math.max(1, (int) distance);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(x1 + (x2 - x1) * t);
            int y = (int) Math.round(y1 + (y2 - y1) * t);
            graphics.fill(x - thickness / 2, y - thickness / 2, x + thickness / 2 + 1, y + thickness / 2 + 1, color);
        }
    }

    private static int facetColor(Facet facet) {
        return switch (facet) {
            case IGNIS -> 0xFFE05A2B;
            case AQUA -> 0xFF3B7CE0;
            case TERRA -> 0xFF7A5230;
            case AER -> 0xFFCFE8F0;
            case LUX -> 0xFFF2E14A;
            case UMBRA -> 0xFF3A2A4A;
            case VITA -> 0xFF4CAF50;
            case MORTIS -> 0xFF808080;
            case ORDO -> 0xFF7EC8E3;
            case PERDO -> 0xFF8B1E1E;
            case MOTUS -> 0xFFE08A2B;
            case COGNITIO -> 0xFF9B59B6;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
