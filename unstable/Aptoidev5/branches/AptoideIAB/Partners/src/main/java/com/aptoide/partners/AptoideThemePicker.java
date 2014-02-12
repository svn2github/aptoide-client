package com.aptoide.partners;

import android.content.Context;
import android.util.Log;
import cm.aptoide.ptdev.EnumAptoideThemes;
import cm.aptoide.ptdev.R;

public class AptoideThemePicker extends cm.aptoide.ptdev.AptoideThemePicker{

	public void setAptoideTheme(Context activity) {

		String theme_string = "APTOIDE_THEME_" + ((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getTheme();
        Log.d("AptoideThemePicker", "THEME: "+theme_string);

		try {
			EnumAptoideThemes theme = EnumAptoideThemes.valueOf(theme_string);
			switch (theme) {
                case APTOIDE_THEME_DEFAULT_LIGHT:
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
                    break;
                case APTOIDE_THEME_ORANGE:
                    activity.setTheme(R.style.AptoideThemeDefaultOrange);
                    break;
                case APTOIDE_THEME_ORANGE_DARK:
                    break;
                case APTOIDE_THEME_SPRINGGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSpringgreen);
                    break;
                case APTOIDE_THEME_SPRINGGREEN_DARK:
                    break;
                case APTOIDE_THEME_LIGHTSKY:
                    activity.setTheme(R.style.AptoideThemeDefaultLightsky);
                    break;
                case APTOIDE_THEME_LIGHTSKY_DARK:
                    break;
                case APTOIDE_THEME_PINK:
                    activity.setTheme(R.style.AptoideThemeDefaultPink);
                    break;
                case APTOIDE_THEME_PINK_DARK:
                    break;
                case APTOIDE_THEME_BLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultBlue);
                    break;
                case APTOIDE_THEME_BLUE_DARK:
                    break;
                case APTOIDE_THEME_RED:
                    activity.setTheme(R.style.AptoideThemeDefaultRed);
                    break;
                case APTOIDE_THEME_RED_DARK:
                    break;
                case APTOIDE_THEME_MAGENTA:
                    activity.setTheme(R.style.AptoideThemeDefaultMagenta);
                    break;
                case APTOIDE_THEME_MAGENTA_DARK:
                    break;
                case APTOIDE_THEME_SLATEGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultSlategray);
                    break;
                case APTOIDE_THEME_SLATEGRAY_DARK:
                    break;
                case APTOIDE_THEME_SEAGREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultSeagreen);
                    break;
                case APTOIDE_THEME_SEAGREEN_DARK:
                    break;
                case APTOIDE_THEME_SILVER:
                    activity.setTheme(R.style.AptoideThemeDefaultSilver);
                    break;
                case APTOIDE_THEME_SILVER_DARK:
                    break;
                case APTOIDE_THEME_DIMGRAY:
                    activity.setTheme(R.style.AptoideThemeDefaultDimgray);
                    break;
                case APTOIDE_THEME_DIMGRAY_DARK:
                    break;
                case APTOIDE_THEME_GREENAPPLE:
                    activity.setTheme(R.style.AptoideThemeDefaultGreenApple);
                    break;
                case APTOIDE_THEME_GREENAPPLE_DARK:
                    break;
                case APTOIDE_THEME_HAPPYBLUE:
                    activity.setTheme(R.style.AptoideThemeDefaultHappyBlue);
                    break;
                case APTOIDE_THEME_HAPPYBLUE_DARK:
                    break;
                case APTOIDE_THEME_GREEN:
                    activity.setTheme(R.style.AptoideThemeDefaultGreen);
                    break;
                case APTOIDE_THEME_GREEN_DARK:
                    break;
                case APTOIDE_THEME_YELLOW:
                    activity.setTheme(R.style.AptoideThemeDefaultYellow);
                    break;
                case APTOIDE_THEME_YELLOW_DARK:
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
