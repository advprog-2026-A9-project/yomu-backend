package id.ac.ui.cs.advprog.yomu.social.constant;

public final class SocialConstants {

    private SocialConstants() {
    }

    public static final String CLAN_NOT_FOUND_MESSAGE = "Clan tidak ditemukan.";
    public static final String CLAN_NAME_ALREADY_USED_MESSAGE = "Nama Clan sudah digunakan";
    public static final String CLAN_ID_NULL_MESSAGE = "Class ID is null";
    public static final String USER_ID_NULL_MESSAGE = "User ID is null";
    public static final String ONLY_LEADER_CAN_EDIT_MESSAGE = "Only Clan Leader can change clan information";
    public static final String ALREADY_MEMBER_MESSAGE = "Kamu sudah menjadi anggota Clan ini";
    public static final String ALREADY_IN_OTHER_CLAN_MESSAGE = "Kamu sudah tergabung di Clan lain";
    public static final String ONLY_LEADER_CAN_DELETE_MESSAGE = "Hanya Leader yang bisa menghapus Clan";
    public static final String FAILED_FIND_REPLACEMENT_LEADER_MESSAGE = "Gagal menemukan pengganti Leader";

    public static final String ROLE_LEADER = "LEADER";
    public static final String ROLE_MEMBER = "MEMBER";
    public static final String MY_CLAN_ROLE_LEADER = "KETUA";
    public static final String MY_CLAN_ROLE_MEMBER = "ANGGOTA";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String JOIN_SUCCESS_MESSAGE = "Berhasil bergabung";
    public static final String LEAVE_SUCCESS_MESSAGE = "Berhasil keluar dari clan";
    public static final String DELETE_SUCCESS_MESSAGE = "Clan berhasil dihapus";
    public static final String END_SEASON_SUCCESS_MESSAGE = "Season ended. Clans promoted/demoted.";

    public static final int MIN_CLAN_SIZE = 1;
    public static final double SEASON_CHANGE_RATIO = 0.2d;
    public static final int LEADERBOARD_LIMIT = 100;
}