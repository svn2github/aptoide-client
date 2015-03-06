/*
 * EnumOptionsMenu		typeSafes Scattered menu options in Aptoide
 * Copyright (C) 20011  Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoidetv.pt;

public enum EnumAptoideThemes {


    APTOIDE_THEME_DEFAULT,
    APTOIDE_THEME_DEFAULT_DARK,
    APTOIDE_THEME_BLACK,
    APTOIDE_THEME_BLACK_DARK,
    APTOIDE_THEME_MIDNIGHT,
    APTOIDE_THEME_MIDNIGHT_DARK,
    APTOIDE_THEME_MAROON,
    APTOIDE_THEME_MAROON_DARK,
    APTOIDE_THEME_GOLD,
    APTOIDE_THEME_GOLD_DARK,
    APTOIDE_THEME_ORANGE,
    APTOIDE_THEME_ORANGE_DARK,
    APTOIDE_THEME_SPRINGGREEN,
    APTOIDE_THEME_SPRINGGREEN_DARK,
    APTOIDE_THEME_LIGHTSKY,
    APTOIDE_THEME_LIGHTSKY_DARK,
    APTOIDE_THEME_PINK,
    APTOIDE_THEME_PINK_DARK,
    APTOIDE_THEME_BLUE,
    APTOIDE_THEME_BLUE_DARK,
    APTOIDE_THEME_RED,
    APTOIDE_THEME_RED_DARK,
    APTOIDE_THEME_MAGENTA,
    APTOIDE_THEME_MAGENTA_DARK,
    APTOIDE_THEME_SLATEGRAY,
    APTOIDE_THEME_SLATEGRAY_DARK,
    APTOIDE_THEME_SEAGREEN,
    APTOIDE_THEME_SEAGREEN_DARK,
    APTOIDE_THEME_SILVER,
    APTOIDE_THEME_SILVER_DARK,
    APTOIDE_THEME_DIMGRAY,
    APTOIDE_THEME_DIMGRAY_DARK,
    APTOIDE_THEME_GREENAPPLE,
    APTOIDE_THEME_GREENAPPLE_DARK,
    APTOIDE_THEME_HAPPYBLUE,
    APTOIDE_THEME_HAPPYBLUE_DARK,
    APTOIDE_THEME_GREEN,
    APTOIDE_THEME_GREEN_DARK,
    APTOIDE_THEME_YELLOW,
    APTOIDE_THEME_YELLOW_DARK,
    APTOIDE_THEME_CRIMSON,
    APTOIDE_THEME_CRIMSON_DARK;


    public static EnumAptoideThemes reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
}
