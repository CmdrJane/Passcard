package ru.aiefu.passcard;

import net.minecraft.util.Identifier;

public class PacketsIDs {
    public static final Identifier OPEN_AUTH_SCREEN = new Identifier(Passcard.MOD_ID, "open_auth_screen");
    public static final Identifier SEND_AUTH_REQUEST = new Identifier(Passcard.MOD_ID, "auth_request");
    public static final Identifier CLOSE_ALL_SCREENS = new Identifier(Passcard.MOD_ID, "close_all_screens");
    public static final Identifier OPEN_CLIENT_SCREEN = new Identifier(Passcard.MOD_ID, "open_client_screen");
    public static final Identifier SYNC_PLAYER_POS = new Identifier(Passcard.MOD_ID, "sync_player_pos");
    public static final Identifier PASSCARD_ORIGINS_SCREEN = new Identifier(Passcard.MOD_ID, "open_origins_selection");
}
