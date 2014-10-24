package cm.aptoidetv.pt;

import android.content.Context;
import android.util.Log;
import cm.aptoide.ptdev.EnumAptoideThemes;

public class AptoideThemePicker extends cm.aptoide.ptdev.AptoideThemePicker{

	public void setAptoideTheme(Context activity) {

		String theme_string = "APTOIDE_THEME_" + ((AptoideConfigurationTV)AptoideTV.getConfiguration()).getTheme();
        Log.d("AptoideThemePicker", "THEME: "+theme_string);

		try {
			EnumAptoideThemes theme = EnumAptoideThemes.valueOf(theme_string);
			switch (theme) {
                case APTOIDE_THEME_DEFAULT:
                    activity.setTheme(R.style.AptoideThemeDefault);
                    break;
                case APTOIDE_THEME_DEFAULT_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultDark);
                    break;
                case APTOIDE_THEME_BLACK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlack);
                    break;
                case APTOIDE_THEME_BLACK_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlackDark);
                    break;
                case APTOIDE_THEME_MIDNIGHT:
                    activity.setTheme(R.style.AptoideThemeDefaultMidnight);
                    break;
                case APTOIDE_THEME_MIDNIGHT_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMidnightDark);
                    break;
                case APTOIDE_THEME_MAROON:
                    activity.setTheme(R.style.AptoideThemeDefaultMaroon);
                    break;
                case APTOIDE_THEME_MAROON_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMaroonDark);
                    break;
                case APTOIDE_THEME_GOLD:
                    activity.setTheme(R.style.AptoideThemeDefaultGold);
                    break;
                case APTOIDE_THEME_GOLD_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGoldDark);
                    break;
                case APTOIDE_THEME_ORANGE:
                    activity.setTheme(R.style.AptoideThemeDefaultOrange);
                    break;
                case APTOIDE_THEME_ORANGE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultOrangeDark);
                    break;
                case APTOIDE_THEME_SPRINGGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSpringgreen);
                    break;
                case APTOIDE_THEME_SPRINGGREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSpringgreenDark);
                    break;
                case APTOIDE_THEME_LIGHTSKY:
                    activity.setTheme(R.style.AptoideThemeDefaultLightsky);
                    break;
                case APTOIDE_THEME_LIGHTSKY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultLightskyDark);
                    break;
                case APTOIDE_THEME_PINK:
                    activity.setTheme(R.style.AptoideThemeDefaultPink);
                    break;
                case APTOIDE_THEME_PINK_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultPinkDark);
                    break;
                case APTOIDE_THEME_BLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultBlue);
                    break;
                case APTOIDE_THEME_BLUE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultBlueDark);
                    break;
                case APTOIDE_THEME_RED:
                    activity.setTheme(R.style.AptoideThemeDefaultRed);
                    break;
                case APTOIDE_THEME_RED_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultRedDark);
                    break;
                case APTOIDE_THEME_MAGENTA:
                    activity.setTheme(R.style.AptoideThemeDefaultMagenta);
                    break;
                case APTOIDE_THEME_MAGENTA_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultMagentaDark);
                    break;
                case APTOIDE_THEME_SLATEGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultSlategray);
                    break;
                case APTOIDE_THEME_SLATEGRAY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSlategrayDark);
                    break;
                case APTOIDE_THEME_SEAGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSeagreen);
                    break;
                case APTOIDE_THEME_SEAGREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSeagreenDark);
                    break;
                case APTOIDE_THEME_SILVER:
                    activity.setTheme(R.style.AptoideThemeDefaultSilver);
                    break;
                case APTOIDE_THEME_SILVER_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultSilverDark);
                    break;
                case APTOIDE_THEME_DIMGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultDimgray);
                    break;
                case APTOIDE_THEME_DIMGRAY_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultDimgrayDark);
                    break;
                case APTOIDE_THEME_GREENAPPLE:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenApple);
                    break;
                case APTOIDE_THEME_GREENAPPLE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenAppleDark);
                    break;
                case APTOIDE_THEME_HAPPYBLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultHappyBlue);
                    break;
                case APTOIDE_THEME_HAPPYBLUE_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultHappyBlueDark);
                    break;
                case APTOIDE_THEME_GREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultGreen);
                    break;
                case APTOIDE_THEME_GREEN_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenDark);
                    break;
                case APTOIDE_THEME_YELLOW:
                    activity.setTheme(R.style.AptoideThemeDefaultYellow);
                    break;
                case APTOIDE_THEME_YELLOW_DARK:
                    activity.setTheme(R.style.AptoideThemeDefaultYellowDark);
                    break;
                default:
				activity.setTheme(R.style.AptoideThemeDefault);
				break;
			}
		} catch (Exception e) {
			activity.setTheme(R.style.AptoideThemeDefault);
		}

	}

}
