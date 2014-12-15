package cm.aptoide.ptdev;

import cm.aptoide.ptdev.model.Login;

public class StoreItem {

    private final Login login;
    private String storeName;

	private String storeDwnNumber;

	private String storeAvatar;

	private EnumStoreTheme theme;

    private boolean list;
    private long id;

    public StoreItem(String name, String number, String avatar, EnumStoreTheme theme, boolean list, long id, Login login) {
        this.login = login;
		this.storeName = name;
		this.storeDwnNumber = number;
		this.storeAvatar = avatar;
		this.theme = theme;
        this.list = list;
        this.id = id;
	}

	public String getName() {
		return storeName;
	}

	public String getDwnNumber() {
		return storeDwnNumber;
	}

	public String getStoreAvatar() {
		return storeAvatar;
	}

	public EnumStoreTheme getTheme() {
		return theme;
	}

    public boolean isList() {
        return list;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Login getLogin() {
        return login;
    }
}