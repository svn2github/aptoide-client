package cm.aptoide.ptdev;

import android.util.SparseArray;
import cm.aptoide.ptdev.utils.IconSizes;

public class EnumCategories {


    public static final int APPLICATIONS = 1;
    public static final int GAMES = 2;
    public static final int TOP_APPS = 500;
    public static final int LATEST_APPS = 501;
    public static final int LATEST_LIKES = 502;
    public static final int LATEST_COMMENTS = 503;
    public static final int RECOMMENDED_APPS = 504;

    public static SparseArray<String> categoryIcons = new SparseArray<String>(){

        {

            String sizeString = IconSizes.generateSizeString(Aptoide.getContext());
            String url = "http://dl.dropboxusercontent.com/u/4804935/categs/";

            //Comics
            put(3,url + "a0633939c4c948ce88099a38268fa812_categ_" + sizeString+ ".png");
            //Communication
            put(4,url + "fe138e160a34f5b7f96e671a3085faef_categ_" + sizeString+ ".png");
            //Entertainment
            put(5,url + "b905f0fde1d4dba0e7978a94fc9410b8_categ_" + sizeString+ ".png");
            //Finance
            put(6,url + "d979d89da2dec3ce9a481a02bade50ce_categ_" + sizeString+ ".png");
            //Health
            put(7,url + "6b9e5a8e912c99a3ea3966577a46d5fa_categ_" + sizeString+ ".png");
            //Lifestyle
            put(8,url + "6880540ad0e4d41507f914ce1bf228b5_categ_" + sizeString+ ".png");
            //Multimedia
            put(9,url + "2c5930caf966e505d1e61d5dac62c439_categ_" + sizeString+ ".png");
            //News &amp; Weather
            put(10,url + "986a2ae5d5e84e5c186c2b2bb127dd1c_categ_" + sizeString+ ".png");
            //Productivity
            put(11,url + "7a9449c50c347e280b065ce6f63c17d7_categ_" + sizeString+ ".png");
            //Reference
            put(12,url + "4b9fe01fca638b2ace7a4fef5c00e5a3_categ_" + sizeString+ ".png");
            //Shopping
            put(13,url + "9790944f2d6f646f6d2a3f7d4c24b305_categ_" + sizeString+ ".png");
            //Social
            put(14,url + "58cd3349635f4ff61d896a7f50b8bada_categ_" + sizeString+ ".png");
            //Sports
            put(15,url + "cdbd83d8c13599ca571057b7c830e3aa_categ_" + sizeString+ ".png");
            //Themes
            put(16,url + "e1b24f239078e8e75dbd3a87165cb292_categ_" + sizeString+ ".png");
            //Tools
            put(17,url + "c1a736fb6473c7f5f8d9e8e078a170fd_categ_" + sizeString+ ".png");
            //Travel
            put(18,url + "4b521a4060750160a93d3b858ed17614_categ_" + sizeString+ ".png");
            //Demo
            put(19,url + "dc1d53f705bec03cbf5bd215b578bfb5_categ_" + sizeString+ ".png");
            //Software Libraries
            put(20,url + "ad53cfef3127bb41346ed3ca9cfea895_categ_" + sizeString+ ".png");
            //News &amp; Magazines
            put(26,url + "eab4e198cd442897d00e45626c66b239_categ_" + sizeString+ ".png");
            //Music &amp; Audio
            put(31,url + "ee168dc1fb704a1b62543730f7f5b23e_categ_" + sizeString+ ".png");
            //Photography
            put(39,url + "9d9437f791adb2636581547d5a45f1b1_categ_" + sizeString+ ".png");
            //Personalization
            put(40,url + "a3b88603c3e1162008ae6850d53b2885_categ_" + sizeString+ ".png");
            //Books &amp; Reference
            put(78,url + "a097ccd5d4cc4feec9ab07be66b96114_categ_" + sizeString+ ".png");
            //Health &amp; Fitness
            put(86,url + "ea5b35e0dc4b79093484cfe6ee1dcc5e_categ_" + sizeString+ ".png");
            //Media &amp; Video
            put(89,url + "9dadfdeafadd06037776a585d779babe_categ_" + sizeString+ ".png");
            //Education
            put(95,url + "b8afec4bac7a6277830df46f97e7574c_categ_" + sizeString+ ".png");
            //Business
            put(149,url + "f704c53b9c5beb06de4fb6a3983b1675_categ_" + sizeString+ ".png");
            //Weather
            put(310,url + "87edc40c839b6db703ca9505b7902f77_categ_" + sizeString+ ".png");
            //Travel &amp; Local
            put(415,url + "7f8855019147609e6500a12718fc661b_categ_" + sizeString+ ".png");
            //Transportation
            put(418,url + "e4c9881c57fb59b2e288cf91cc27b9f3_categ_" + sizeString+ ".png");
            //Medical
            put(459,url + "e26a787ee09c284a6eeb4b01d27dc05c_categ_" + sizeString+ ".png");
            //Libraries &amp; Demo
            put(736,url + "70d6e4c82a27a397ad1334a262d71008_categ_" + sizeString+ ".png");
            //Transport
            put(850,url + "30f4a9b81871ac8bbea6db8cc47a7b7d_categ_" + sizeString+ ".png");
            //Arcade &amp; Action
            put(21,url + "3b6cfe3b37c288ccc2c01cdcca499d8f_categ_" + sizeString+ ".png");
            //Brain &amp; Puzzle
            put(22,url + "6e99f871fb4c1267c59d1ae622bddab6_categ_" + sizeString+ ".png");
            //Cards &amp; Casino
            put(23,url + "262215e619dc7f8edfffa705b5419694_categ_" + sizeString+ ".png");
            //Casual
            put(24,url + "9cd867c3e70963fa0eb77b41551f4629_categ_" + sizeString+ ".png");
            //Racing
            put(47,url + "f1e6961a8c7671793deba9295426877b_categ_" + sizeString+ ".png");
            //Sports Games
            put(293,url + "87be84837667190abf4f0882158bfc04_categ_" + sizeString+ ".png");


        }

    };

}