package VKSDK.DataTypes;

public class User implements VKDataType {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public DeactivatedUserStatus getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(DeactivatedUserStatus deactivated) {
        this.deactivated = deactivated;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public enum DeactivatedUserStatus {
        DELETED, BANNED;

        public static DeactivatedUserStatus getFromString(String status) {
            if (status.equals("deleted"))
                return DELETED;
            if (status.equals("banned"))
                return BANNED;
            else
                throw new IllegalArgumentException("Unsupported status = " + status);
        }
    }

    public String getLink() {
        return "https://vk.com/id" + getId();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", link='" + getLink() + "\'" +
                '}';
    }

    private Long id;
    private String firstName;
    private String lastName;
    private Boolean hidden;
    private DeactivatedUserStatus deactivated;
}
