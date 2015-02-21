package VKSDK.DataTypes;

public class Post implements VKDataType {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getReplyOwnerId() {
        return replyOwnerId;
    }

    public void setReplyOwnerId(Long replyOwnerId) {
        this.replyOwnerId = replyOwnerId;
    }

    public Long getReplyPostId() {
        return replyPostId;
    }

    public void setReplyPostId(Long replyPostId) {
        this.replyPostId = replyPostId;
    }

    public Long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    public Long getRepostsCount() {
        return repostsCount;
    }

    public void setRepostsCount(Long repostsCount) {
        this.repostsCount = repostsCount;
    }

    public String getLink() {
        return "https://vk.com/wall" + getOwnerId() + "_" + getId();
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", link='" + getLink() + "\'" +
                ", ownerId=" + ownerId +
                ", fromId=" + fromId +
                ", date=" + date +
                '}';
    }

    private Long id;
    private Long ownerId;
    private Long fromId;
    private Long date;
    private String text;
    private Long replyOwnerId;
    private Long replyPostId;
    private Long commentsCount;
    private Long likesCount;
    private Long repostsCount;
}
