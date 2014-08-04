package cm.aptoide.ptdev.utils;

public class Filters {

    public static enum Screen {
        notfound,small,normal,large,xlarge;

        public static Screen lookup(String screen){
            try{
                return valueOf(screen);
            }catch (Exception e) {
                return notfound;
            }


        }

    }

    public static enum Age {
        All,Mature;
        public static Age lookup(String age){
            try{
                return valueOf(age);
            }catch (Exception e) {
                return All;
            }


        }
    }
}

