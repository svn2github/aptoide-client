package cm.aptoide.ptdev;


public enum EnumStoreTheme {

    APTOIDE_STORE_THEME_DEFAULT(R.color.transparent_green, R.color.green ),

    APTOIDE_STORE_THEME_MIDNIGHT(R.color.transparent_midnight, R.color.midnight ),
    APTOIDE_STORE_THEME_MAROON(R.color.transparent_maroon, R.color.maroon ),
    APTOIDE_STORE_THEME_GOLD(R.color.transparent_gold, R.color.gold ),
    APTOIDE_STORE_THEME_ORANGE(R.color.transparent_orange, R.color.orange ),
    APTOIDE_STORE_THEME_SPRINGGREEN(R.color.transparent_springgreen, R.color.springgreen ),
    APTOIDE_STORE_THEME_MAGENTA(R.color.transparent_magenta, R.color.magenta ),

    APTOIDE_STORE_THEME_LIGHTSKY(R.color.transparent_lightsky, R.color.lightsky ),
    APTOIDE_STORE_THEME_PINK(R.color.transparent_pink, R.color.pink ),
    APTOIDE_STORE_THEME_BLUE(R.color.transparent_blue, R.color.blue ),
    APTOIDE_STORE_THEME_RED(R.color.transparent_red, R.color.red ),
    APTOIDE_STORE_THEME_SLATEGRAY(R.color.transparent_slategray, R.color.slategray ),
    APTOIDE_STORE_THEME_SEAGREEN(R.color.transparent_seagreen, R.color.seagreen ),
    APTOIDE_STORE_THEME_SILVER(R.color.transparent_silver, R.color.silver ),
    APTOIDE_STORE_THEME_DIMGRAY(R.color.transparent_dimgray, R.color.dimgray ),
    APTOIDE_STORE_THEME_BLACK(R.color.transparent_black, R.color.black );

    private int storeAlphaColor;

    public int getStoreHeader() {
        return storeHeader;
    }

    public int getStoreAlphaColor() {
        return storeAlphaColor;
    }

    private int storeHeader;

    EnumStoreTheme(int storeAlphaColor, int storeHeader){

        this.storeAlphaColor = storeAlphaColor;
        this.storeHeader = storeHeader;


    }




	
	public static EnumStoreTheme reverseOrdinal(int ordinal){
		return values()[ordinal];
	}
}
