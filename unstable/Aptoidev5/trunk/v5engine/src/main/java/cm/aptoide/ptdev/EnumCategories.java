package cm.aptoide.ptdev;

import android.util.SparseArray;
import android.util.SparseIntArray;
import cm.aptoide.ptdev.utils.IconSizes;

import java.util.Locale;

public class EnumCategories {


    public static final int APPLICATIONS = 1;
    public static final int GAMES = 2;
    public static final int TOP_APPS = 500;
    public static final int LATEST_APPS = 501;
    public static final int LATEST_LIKES = 502;
    public static final int LATEST_COMMENTS = 503;
    public static final int RECOMMENDED_APPS = 504;
    private final String repo;

    public EnumCategories(String repo) {
        this.repo = repo;
    }

    private static SparseIntArray categoryNames = new SparseIntArray() {
        {
            put(1, R.string.applications);
            put(2, R.string.games);

            put(3, R.string.comics);
            put(4, R.string.communication);
            put(5, R.string.entertainment);
            put(6, R.string.finance);
            put(7, R.string.health);
            put(8, R.string.lifestyle);
            put(9, R.string.multimedia);
            put(10, R.string.news_and_weather);
            put(11, R.string.productivity);
            put(12, R.string.reference);
            put(13, R.string.shopping);
            put(14, R.string.social);
            put(15, R.string.sports);
            put(16, R.string.themes);
            put(17, R.string.tools);
            put(18, R.string.travel);
            put(19, R.string.demo);
            put(20, R.string.software_libraries);
            put(26, R.string.news_and_magazine);
            put(31, R.string.music_and_audio);
            put(39, R.string.photography);
            put(40, R.string.personalization);
            put(78, R.string.books_and_reference);
            put(86, R.string.health_and_fitness);
            put(89, R.string.media_and_video);
            put(95, R.string.education);
            put(149, R.string.business);
            put(310, R.string.weather);
            put(415, R.string.travel_and_local);
            put(418, R.string.transportation);
            put(459, R.string.medical);
            put(736, R.string.libraries_and_demo);
            put(850, R.string.transport);
            put(21, R.string.arcade_and_action);
            put(22, R.string.brain_and_puzzle);
            put(23, R.string.cards_and_casino);
            put(24, R.string.casual);
            put(47, R.string.racing);
            put(293, R.string.sport_games);

            put(500, R.string.top_apps);
            put(501, R.string.latest_apps);
            put(502, R.string.latest_likes);
            put(503, R.string.latest_comments);
            put(504, R.string.recommended_for_you);

        }
    };

    private static SparseArray<String> categoryIcons = new SparseArray<String>() {

        {

            String sizeString = IconSizes.generateSizeString(Aptoide.getContext());

            //Comics
            put(3, "a0633939c4c948ce88099a38268fa812_categ_" + sizeString + ".png");
            //Communication
            put(4, "fe138e160a34f5b7f96e671a3085faef_categ_" + sizeString + ".png");
            //Entertainment
            put(5, "b905f0fde1d4dba0e7978a94fc9410b8_categ_" + sizeString + ".png");
            //Finance
            put(6, "d979d89da2dec3ce9a481a02bade50ce_categ_" + sizeString + ".png");
            //Health
            put(7, "6b9e5a8e912c99a3ea3966577a46d5fa_categ_" + sizeString + ".png");
            //Lifestyle
            put(8, "6880540ad0e4d41507f914ce1bf228b5_categ_" + sizeString + ".png");
            //Multimedia
            put(9, "2c5930caf966e505d1e61d5dac62c439_categ_" + sizeString + ".png");
            //News &amp; Weather
            put(10, "986a2ae5d5e84e5c186c2b2bb127dd1c_categ_" + sizeString + ".png");
            //Productivity
            put(11, "7a9449c50c347e280b065ce6f63c17d7_categ_" + sizeString + ".png");
            //Reference
            put(12, "4b9fe01fca638b2ace7a4fef5c00e5a3_categ_" + sizeString + ".png");
            //Shopping
            put(13, "9790944f2d6f646f6d2a3f7d4c24b305_categ_" + sizeString + ".png");
            //Social
            put(14, "58cd3349635f4ff61d896a7f50b8bada_categ_" + sizeString + ".png");
            //Sports
            put(15, "cdbd83d8c13599ca571057b7c830e3aa_categ_" + sizeString + ".png");
            //Themes
            put(16, "e1b24f239078e8e75dbd3a87165cb292_categ_" + sizeString + ".png");
            //Tools
            put(17, "c1a736fb6473c7f5f8d9e8e078a170fd_categ_" + sizeString + ".png");
            //Travel
            put(18, "4b521a4060750160a93d3b858ed17614_categ_" + sizeString + ".png");
            //Demo
            put(19, "dc1d53f705bec03cbf5bd215b578bfb5_categ_" + sizeString + ".png");
            //Software Libraries
            put(20, "ad53cfef3127bb41346ed3ca9cfea895_categ_" + sizeString + ".png");
            //News &amp; Magazines
            put(26, "eab4e198cd442897d00e45626c66b239_categ_" + sizeString + ".png");
            //Music &amp; Audio
            put(31, "ee168dc1fb704a1b62543730f7f5b23e_categ_" + sizeString + ".png");
            //Photography
            put(39, "9d9437f791adb2636581547d5a45f1b1_categ_" + sizeString + ".png");
            //Personalization
            put(40, "a3b88603c3e1162008ae6850d53b2885_categ_" + sizeString + ".png");
            //Books &amp; Reference
            put(78, "a097ccd5d4cc4feec9ab07be66b96114_categ_" + sizeString + ".png");
            //Health &amp; Fitness
            put(86, "ea5b35e0dc4b79093484cfe6ee1dcc5e_categ_" + sizeString + ".png");
            //Media &amp; Video
            put(89, "9dadfdeafadd06037776a585d779babe_categ_" + sizeString + ".png");
            //Education
            put(95, "b8afec4bac7a6277830df46f97e7574c_categ_" + sizeString + ".png");
            //Business
            put(149, "f704c53b9c5beb06de4fb6a3983b1675_categ_" + sizeString + ".png");
            //Weather
            put(310, "87edc40c839b6db703ca9505b7902f77_categ_" + sizeString + ".png");
            //Travel &amp; Local
            put(415, "7f8855019147609e6500a12718fc661b_categ_" + sizeString + ".png");
            //Transportation
            put(418, "e4c9881c57fb59b2e288cf91cc27b9f3_categ_" + sizeString + ".png");
            //Medical
            put(459, "e26a787ee09c284a6eeb4b01d27dc05c_categ_" + sizeString + ".png");
            //Libraries &amp; Demo
            put(736, "70d6e4c82a27a397ad1334a262d71008_categ_" + sizeString + ".png");
            //Transport
            put(850, "30f4a9b81871ac8bbea6db8cc47a7b7d_categ_" + sizeString + ".png");
            //Arcade &amp; Action
            put(21, "3b6cfe3b37c288ccc2c01cdcca499d8f_categ_" + sizeString + ".png");
            //Brain &amp; Puzzle
            put(22, "6e99f871fb4c1267c59d1ae622bddab6_categ_" + sizeString + ".png");
            //Cards &amp; Casino
            put(23, "262215e619dc7f8edfffa705b5419694_categ_" + sizeString + ".png");
            //Casual
            put(24, "9cd867c3e70963fa0eb77b41551f4629_categ_" + sizeString + ".png");
            //Racing
            put(47, "f1e6961a8c7671793deba9295426877b_categ_" + sizeString + ".png");
            //Sports Games
            put(293, "87be84837667190abf4f0882158bfc04_categ_" + sizeString + ".png");


        }

    };

    public static String getCategoryIcon(int id, String repo) {
        String iconUrl = categoryIcons.get(id);
        if (iconUrl == null) {
            return null;
        }
        return "http://pool.img.aptoide.com/" + repo.toLowerCase(Locale.ENGLISH) + "/" + iconUrl;
    }

    public static int getCategoryName(int id) {
        int resCategName = categoryNames.get(id);
        return resCategName;
    }
}