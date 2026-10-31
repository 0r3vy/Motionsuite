package dev.r3vy.motionsuite.ui;

final class HubTheme {
    static final int COLUMN_WIDTH = 156;
    static final int COLUMN_GAP = 14;
    static final int ROW_HEIGHT = 22;
    static final int WIDGET_HEIGHT = 20;
    static final int LIST_WIDTH = 156;
    static final int TITLE_ZONE = 52;
    static final int FOOTER_ZONE = 52;
    static final int PANEL_PADDING = 10;
    static final int HALF_GAP = 4;
    static final int TAB_GAP = 2;
    static final int SCROLLBAR_WIDTH = 2;
    static final int PANEL_COLOR = 0xB0101018;
    static final int TITLE_COLOR = 0xFFF2F2F7;
    static final int SECTION_COLOR = 0xFFC9A6FF;
    static final int NOTE_COLOR = 0xFF8E8EA0;
    static final int CREDIT_COLOR = 0xFFC9A6FF;
    static final int FOOTER_CREDIT_COLOR = 0xFF8E8EA0;
    static final int SCROLL_TRACK_COLOR = 0x30FFFFFF;
    static final int SCROLL_THUMB_COLOR = 0x90C9A6FF;
    static final int ERROR_COLOR = 0xFFFF6B6B;
    static final double UNLIMITED_OFFSET = 64.0;

    private HubTheme() {
    }

    static int thumbHeight(int contentHeight, int maxScroll) {
        return Math.max(12, contentHeight * contentHeight / (contentHeight + maxScroll));
    }
}
