package VKSDK.DataTypes;

public class Group implements VKDataType {
    private Long id;
    private String name;
    private String screenName;
    private Long isClosed;
    private String deactivated;
    private Long isAdmin;
    private Long adminLevel;
    private Long isMember;
    private Type type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public Long getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Long isClosed) {
        this.isClosed = isClosed;
    }

    public String getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(String deactivated) {
        this.deactivated = deactivated;
    }

    public Long getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Long isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Long getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(Long adminLevel) {
        this.adminLevel = adminLevel;
    }

    public Long getIsMember() {
        return isMember;
    }

    public void setIsMember(Long isMember) {
        this.isMember = isMember;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLink() {
        return "https://vk.com/public" + getId();
    }

    @Override
    public String toString() {
        return "Group{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", link='" + getLink() + "\'" +
                '}';
    }

    public enum Type {
        GROUP, PAGE, EVENT;

        public static Type getFromString(String type) {
            switch (type) {
                case "group":
                    return GROUP;
                case "page":
                    return PAGE;
                case "event":
                    return EVENT;
                default:
                    throw new RuntimeException("Unsupported type = " + type);
            }
        }
    }
}
