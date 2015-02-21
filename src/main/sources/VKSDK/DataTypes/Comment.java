package VKSDK.DataTypes;

public class Comment implements VKDataType {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getReplyToUser() {
        return replyToUser;
    }

    public void setReplyToUser(Long replyToUser) {
        this.replyToUser = replyToUser;
    }

    public Long getReplyToComment() {
        return replyToComment;
    }

    public void setReplyToComment(Long replyToComment) {
        this.replyToComment = replyToComment;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getLink() {
        return "https://vk.com/wall" + getOwnerId() + "_" + getPostId() + "?reply=" + getId();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", link='" + getLink() + "\'" +
                ", ownerId=" + ownerId +
                ", fromId=" + fromId +
                '}';
    }

    private Long id;
    private Long ownerId;
    private Long fromId;
    private Long postId;
    private Long date;
    private Long replyToUser;
    private Long replyToComment;
    private String text;
}
