package cm.aptoide.ptdev;


public enum EnumStoreTheme {

    APTOIDE_STORE_THEME_GREEN(R.color.transparent_green, R.color.green, R.drawable.custom_categ_green , R.drawable.ab_background_green),
    APTOIDE_STORE_THEME_MIDNIGHT(R.color.transparent_midnight, R.color.midnight, R.drawable.custom_categ_midnight, R.drawable.ab_background_midnight),
    APTOIDE_STORE_THEME_MAROON(R.color.transparent_maroon, R.color.maroon, R.drawable.custom_categ_maroon, R.drawable.ab_background_maroon),
    APTOIDE_STORE_THEME_GOLD(R.color.transparent_gold, R.color.gold, R.drawable.custom_categ_gold, R.drawable.ab_background_gold),
    APTOIDE_STORE_THEME_ORANGE(R.color.transparent_orange, R.color.orange, R.drawable.custom_categ_orange, R.drawable.ab_background_orange),
    APTOIDE_STORE_THEME_SPRINGGREEN(R.color.transparent_springgreen, R.color.springgreen, R.drawable.custom_categ_springgreen, R.drawable.ab_background_springgreen),
    APTOIDE_STORE_THEME_MAGENTA(R.color.transparent_magenta, R.color.magenta, R.drawable.custom_categ_magenta, R.drawable.ab_background_magenta),
    APTOIDE_STORE_THEME_LIGHTSKY(R.color.transparent_lightsky, R.color.lightsky, R.drawable.custom_categ_lightsky, R.drawable.ab_background_lightsky),
    APTOIDE_STORE_THEME_PINK(R.color.transparent_pink, R.color.pink, R.drawable.custom_categ_pink, R.drawable.ab_background_pink),
    APTOIDE_STORE_THEME_BLUE(R.color.transparent_blue, R.color.blue, R.drawable.custom_categ_blue, R.drawable.ab_background_blue),
    APTOIDE_STORE_THEME_RED(R.color.transparent_red, R.color.red, R.drawable.custom_categ_red, R.drawable.ab_background_red),
    APTOIDE_STORE_THEME_SLATEGRAY(R.color.transparent_slategray, R.color.slategray, R.drawable.custom_categ_slategray, R.drawable.ab_background_slategray),
    APTOIDE_STORE_THEME_SEAGREEN(R.color.transparent_seagreen, R.color.seagreen, R.drawable.custom_categ_seagreen, R.drawable.ab_background_seagreen),
    APTOIDE_STORE_THEME_SILVER(R.color.transparent_silver, R.color.silver, R.drawable.custom_categ_silver, R.drawable.ab_background_silver),
    APTOIDE_STORE_THEME_DIMGRAY(R.color.transparent_dimgray, R.color.dimgray, R.drawable.custom_categ_dimgray, R.drawable.ab_background_dimgray),
    APTOIDE_STORE_THEME_BLACK(R.color.transparent_black, R.color.black, R.drawable.custom_categ_black, R.drawable.ab_background_black);

    private int storeAlphaColor;




    public int getStoreHeader() {
        return storeHeader;
    }

    public int getStoreAlphaColor() {
        return storeAlphaColor;
    }

    private int storeHeader;
    private int catDrawable;
    private int abColor;

    EnumStoreTheme(int storeAlphaColor, int storeHeader, int catDrawable, int abColor){

        this.storeAlphaColor = storeAlphaColor;
        this.storeHeader = storeHeader;
        this.catDrawable = catDrawable;
        this.abColor = abColor;
    }




	
	public static EnumStoreTheme reverseOrdinal(int ordinal){
		return values()[ordinal];
	}



    public static EnumStoreTheme get(String s) {

        EnumStoreTheme theme;
        try{
            theme = valueOf(s);
        }catch (Exception e){
            theme = APTOIDE_STORE_THEME_ORANGE;
        }

        return theme;
    }

    public int getStoreCategoryDrawable() {
        return catDrawable;
    }


    public int getActionBarColor() {
        return abColor;
    }
}
