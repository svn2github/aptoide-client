package cm.aptoidetv.pt;

import android.app.Activity;
import android.util.Log;

public class ThemePicker {

    private static int themeId;

    public static void setThemePicker(Activity activity) {

        String theme_string = "APTOIDE_THEME_" + activity.getString(R.string.aptoidetheme).toUpperCase();
        Log.d("ThemePicker", "THEME: "+theme_string);

		try {
            EnumAptoideThemes theme = EnumAptoideThemes.valueOf(theme_string);
			switch (theme) {
                case APTOIDE_THEME_DEFAULT:
                    activity.setTheme(R.style.AptoideThemeDefault);
                    themeId=R.style.AptoideThemeDefault;
                    break;
                case APTOIDE_THEME_DEFAULT_DARK:
                    activity.setTheme(R.style.AptoideThemeDefault);
                    themeId=R.style.AptoideThemeDefault;
                    break;
                case APTOIDE_THEME_BLACK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlack);
                    themeId=R.style.AptoideThemeDefaultBlack;
                    break;
                case APTOIDE_THEME_BLACK_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlack);
                    themeId=R.style.AptoideThemeDefaultBlack;
                    break;
                case APTOIDE_THEME_MIDNIGHT:
                    activity.setTheme(R.style.AptoideThemeDefaultMidnight);
                    themeId=R.style.AptoideThemeDefaultMidnight;
                    break;
                case APTOIDE_THEME_MIDNIGHT_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMidnight);
                    themeId=R.style.AptoideThemeDefaultMidnight;
                    break;
                case APTOIDE_THEME_MAROON:
                    activity.setTheme(R.style.AptoideThemeDefaultMaroon);
                    themeId=R.style.AptoideThemeDefaultMaroon;
                    break;
                case APTOIDE_THEME_MAROON_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMaroon);
                    themeId=R.style.AptoideThemeDefaultMaroon;
                    break;
                case APTOIDE_THEME_GOLD:
                    activity.setTheme(R.style.AptoideThemeDefaultGold);
                    themeId=R.style.AptoideThemeDefaultGold;
                    break;
                case APTOIDE_THEME_GOLD_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGold);
                    themeId=R.style.AptoideThemeDefaultGold;
                    break;
                case APTOIDE_THEME_ORANGE:
                    activity.setTheme(R.style.AptoideThemeDefaultOrange);
                    themeId=R.style.AptoideThemeDefaultOrange;
                    break;
                case APTOIDE_THEME_ORANGE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultOrange);
                    themeId=R.style.AptoideThemeDefaultOrange;
                    break;
                case APTOIDE_THEME_SPRINGGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSpringgreen);
                    themeId=R.style.AptoideThemeDefaultSpringgreen;
                    break;
                case APTOIDE_THEME_SPRINGGREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSpringgreen);
                    themeId=R.style.AptoideThemeDefaultSpringgreen;
                    break;
                case APTOIDE_THEME_LIGHTSKY:
                    activity.setTheme(R.style.AptoideThemeDefaultLightsky);
                    themeId=R.style.AptoideThemeDefaultLightsky;
                    break;
                case APTOIDE_THEME_LIGHTSKY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultLightsky);
                    themeId=R.style.AptoideThemeDefaultLightsky;
                    break;
                case APTOIDE_THEME_PINK:
                    activity.setTheme(R.style.AptoideThemeDefaultPink);
                    themeId=R.style.AptoideThemeDefaultPink;
                    break;
                case APTOIDE_THEME_PINK_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultPink);
                    themeId=R.style.AptoideThemeDefaultPink;
                    break;
                case APTOIDE_THEME_BLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultBlue);
                    themeId=R.style.AptoideThemeDefaultBlue;
                    break;
                case APTOIDE_THEME_BLUE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlue);
                    themeId=R.style.AptoideThemeDefaultBlue;
                    break;
                case APTOIDE_THEME_RED:
                    activity.setTheme(R.style.AptoideThemeDefaultRed);
                    themeId=R.style.AptoideThemeDefaultRed;
                    break;
                case APTOIDE_THEME_RED_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultRed);
                    themeId=R.style.AptoideThemeDefaultRed;
                    break;
                case APTOIDE_THEME_MAGENTA:
                    activity.setTheme(R.style.AptoideThemeDefaultMagenta);
                    themeId=R.style.AptoideThemeDefaultMagenta;
                    break;
                case APTOIDE_THEME_MAGENTA_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMagenta);
                    themeId=R.style.AptoideThemeDefaultMagenta;
                    break;
                case APTOIDE_THEME_SLATEGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultSlategray);
                    themeId=R.style.AptoideThemeDefaultSlategray;
                    break;
                case APTOIDE_THEME_SLATEGRAY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSlategray);
                    themeId=R.style.AptoideThemeDefaultSlategray;
                    break;
                case APTOIDE_THEME_SEAGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSeagreen);
                    themeId=R.style.AptoideThemeDefaultSeagreen;
                    break;
                case APTOIDE_THEME_SEAGREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSeagreen);
                    themeId=R.style.AptoideThemeDefaultSeagreen;
                    break;
                case APTOIDE_THEME_SILVER:
                    activity.setTheme(R.style.AptoideThemeDefaultSilver);
                    themeId=R.style.AptoideThemeDefaultSilver;
                    break;
                case APTOIDE_THEME_SILVER_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSilver);
                    themeId=R.style.AptoideThemeDefaultSilver;
                    break;
                case APTOIDE_THEME_DIMGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultDimgray);
                    themeId=R.style.AptoideThemeDefaultDimgray;
                    break;
                case APTOIDE_THEME_DIMGRAY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultDimgray);
                    themeId=R.style.AptoideThemeDefaultDimgray;
                    break;
                case APTOIDE_THEME_GREENAPPLE:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenApple);
                    themeId=R.style.AptoideThemeDefaultGreenApple;
                    break;
                case APTOIDE_THEME_GREENAPPLE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenApple);
                    themeId=R.style.AptoideThemeDefaultGreenApple;
                    break;
                case APTOIDE_THEME_HAPPYBLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultHappyBlue);
                    themeId=R.style.AptoideThemeDefaultHappyBlue;
                    break;
                case APTOIDE_THEME_HAPPYBLUE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultHappyBlue);
                    themeId=R.style.AptoideThemeDefaultHappyBlue;
                    break;
                case APTOIDE_THEME_GREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultGreen);
                    themeId=R.style.AptoideThemeDefaultGreen;
                    break;
                case APTOIDE_THEME_GREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGreen);
                    themeId=R.style.AptoideThemeDefaultGreen;
                    break;
                case APTOIDE_THEME_YELLOW:
                    activity.setTheme(R.style.AptoideThemeDefaultYellow);
                    themeId=R.style.AptoideThemeDefaultYellow;
                    break;
                case APTOIDE_THEME_YELLOW_DARK:
                    themeId=R.style.AptoideThemeDefaultYellow;
                    activity.setTheme(R.style.AptoideThemeDefaultYellow);
                    break;
                default:
                    activity.setTheme(R.style.AptoideThemeDefault);
                    themeId=R.style.AptoideThemeDefault;
				break;
			}
		} catch (Exception e) {
			activity.setTheme(R.style.AptoideThemeDefault);
            themeId=R.style.AptoideThemeDefault;
        }

	}

    public static int getThemePicker() {
//        Log.d("ThemePicker", "THEME: "+themeId);
        return themeId;
    }

}
