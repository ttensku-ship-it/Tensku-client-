package dev.tensku.client;

/** Palettes. Colors are 0xAARRGGBB. */
public record Theme(String name, int bg, int panel, int accent, int text, int dim, int up, int down) {
    public static final Theme[] ALL = {
        new Theme("Midnight", 0xE00A0E1A, 0xF0121830, 0xFF4DA3FF, 0xFFE8EEFF, 0xFF7C88A8, 0xFF3DDC84, 0xFFFF5470),
        new Theme("Ember",    0xE0140A08, 0xF0221210, 0xFFFF8A3D, 0xFFFFEFE5, 0xFFA88070, 0xFFB6F25C, 0xFFFF4D4D),
        new Theme("Mint",     0xE00A1512, 0xF0102420, 0xFF2FE6B0, 0xFFE6FFF7, 0xFF6FA397, 0xFF2FE6B0, 0xFFFF6B8A),
        new Theme("Mono",     0xE0101010, 0xF0181818, 0xFFFFFFFF, 0xFFF0F0F0, 0xFF808080, 0xFFDDDDDD, 0xFF888888),
    };
    public static Theme current() { return ALL[Math.floorMod(Config.I.theme, ALL.length)]; }
}
