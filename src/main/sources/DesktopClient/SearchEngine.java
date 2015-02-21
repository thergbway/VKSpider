package DesktopClient;

import VKSDK.DataTypes.Comment;
import VKSDK.DataTypes.Group;
import VKSDK.DataTypes.Post;
import VKSDK.DataTypes.User;
import VKSDK.Exceptions.VKException;
import VKSDK.Methods.LikesIsLiked;
import VKSDK.Methods.WallGet;
import VKSDK.Methods.WallGetComments;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchEngine {
    private final List<User> usersList;
    private final List<Group> groupList;
    private final Long spyingId;
    private final long maxUserPostsCount;
    private final long maxUserCommentsCount;
    private final long maxGroupPostsCount;
    private final long maxGroupCommentsCount;
    private final String accessToken;
    private final Node parentNode;

    private final List<ResultEntry> results;
    private final List<LogMessageEntry> logMessages;
    private final AtomicInteger totalCheckedCount;
    private final AtomicInteger usersCheckedCount;
    private final AtomicInteger groupsCheckedCount;
    private final AtomicInteger commentsCheckedCount;
    private final AtomicInteger postsCheckedCount;
    private final AtomicInteger itemsFoundCount;
    private final AtomicInteger errorCount;

    public SearchEngine(List<User> usersList, List<Group> groupList,
                        Long spyingId, String accessToken, Node parentNode,
                        int maxUserPostsCount, int maxUserCommentsCount,
                        int maxGroupPostsCount, int maxGroupCommentsCount,
                        List<ResultEntry> results, List<LogMessageEntry> logMessages,
                        AtomicInteger totalCheckedCount, AtomicInteger usersCheckedCount,
                        AtomicInteger groupsCheckedCount, AtomicInteger commentsCheckedCount,
                        AtomicInteger postsCheckedCount, AtomicInteger itemsFoundCount,
                        AtomicInteger errorCount) {
        this.usersList = usersList;
        this.groupList = groupList;
        this.spyingId = spyingId;
        this.accessToken = accessToken;
        this.parentNode = parentNode;
        this.maxUserPostsCount = maxUserPostsCount;
        this.maxUserCommentsCount = maxUserCommentsCount;
        this.maxGroupPostsCount = maxGroupPostsCount;
        this.maxGroupCommentsCount = maxGroupCommentsCount;
        this.results = results;
        this.logMessages = logMessages;
        this.totalCheckedCount = totalCheckedCount;
        this.usersCheckedCount = usersCheckedCount;
        this.groupsCheckedCount = groupsCheckedCount;
        this.commentsCheckedCount = commentsCheckedCount;
        this.postsCheckedCount = postsCheckedCount;
        this.itemsFoundCount = itemsFoundCount;
        this.errorCount = errorCount;
    }

    public void start() {
        Thread thread = new Thread(() -> {
            for (User user : usersList) {
                try {
                    checkUser(user);
                    usersCheckedCount.incrementAndGet();
                    totalCheckedCount.incrementAndGet();
                    fireEventToUpdate();
                } catch (Throwable e) {
                    logMessages.add(new LogMessageEntry("ERROR with user= " + user + ". REASON: " + e));
                    errorCount.incrementAndGet();
                    fireEventToUpdate();
                }
            }

            for (Group group : groupList) {
                try {
                    checkGroup(group);
                    groupsCheckedCount.incrementAndGet();
                    totalCheckedCount.incrementAndGet();
                    fireEventToUpdate();
                } catch (Throwable e) {
                    logMessages.add(new LogMessageEntry("ERROR with group= " + group + ". REASON: " + e));
                    errorCount.incrementAndGet();
                    fireEventToUpdate();
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void checkUser(User user) throws VKException {
        logMessages.add(new LogMessageEntry("Checking " + user));
        fireEventToUpdate();

        WallGet wallGet = new WallGet(user.getId(), accessToken);
        wallGet.setCount(maxUserPostsCount);
        LinkedList<Post> posts = wallGet.invoke();

        boolean needToCheckComments = true;
        for (Post post : posts) {
            try {
                checkUserPost(post, user, needToCheckComments);
                postsCheckedCount.incrementAndGet();
                fireEventToUpdate();
            } catch (VKException e) {
                needToCheckComments = false;
                logMessages.add(new LogMessageEntry("ERROR with post= " + post + ". REASON: " + e));
                errorCount.incrementAndGet();
                fireEventToUpdate();
            }
        }
    }

    private void checkUserPost(Post post, User correspondingUser, boolean needToCheckComments) throws VKException {
        logMessages.add(new LogMessageEntry("Checking " + post));
        fireEventToUpdate();

        LikesIsLiked likesIsLiked = new LikesIsLiked(post.getOwnerId(), spyingId,
                post.getId(), LikesIsLiked.Type.POST, accessToken);
        boolean isLikedPost = likesIsLiked.invoke();
        if (isLikedPost) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.USER, ResultEntry.Type.POST_LIKE,
                    correspondingUser.getFullName(), post.getText(), post.getDate(), post.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }
        boolean isPostAuthor = post.getFromId().equals(spyingId);
        if (isPostAuthor) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.USER, ResultEntry.Type.POST_AUTHOR,
                    correspondingUser.getFullName(), post.getText(), post.getDate(), post.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }

        if (needToCheckComments) {
            WallGetComments postGetComments = new WallGetComments(accessToken, post.getOwnerId(), post.getId());
            postGetComments.setCount(maxUserCommentsCount);
            LinkedList<Comment> comments = postGetComments.invoke();

            for (Comment comment : comments) {
                try {
                    checkUserPostComment(comment, correspondingUser);
                    commentsCheckedCount.incrementAndGet();
                    fireEventToUpdate();
                } catch (VKException e) {
                    logMessages.add(new LogMessageEntry("ERROR with comment= " + comment + ". REASON: " + e));
                    errorCount.incrementAndGet();
                    fireEventToUpdate();
                }
            }
        }
    }

    private void checkUserPostComment(Comment comment, User correspondingUser) throws VKException {
        logMessages.add(new LogMessageEntry("Checking " + comment));
        fireEventToUpdate();


        boolean isCommentAuthor = comment.getFromId().equals(spyingId);
        if (isCommentAuthor) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.USER, ResultEntry.Type.COMMENT_AUTHOR,
                    correspondingUser.getFullName(), comment.getText(), comment.getDate(), comment.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }

        Boolean isLikedComment = new LikesIsLiked(comment.getOwnerId(), spyingId, comment.getId(),
                LikesIsLiked.Type.COMMENT, accessToken).invoke();
        if (isLikedComment) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.USER, ResultEntry.Type.COMMENT_LIKE,
                    correspondingUser.getFullName(), comment.getText(), comment.getDate(), comment.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }
}

    private void checkGroup(Group group) throws VKException {
        logMessages.add(new LogMessageEntry("Checking " + group));
        fireEventToUpdate();

        WallGet wallGet = new WallGet(-group.getId(), accessToken);
        wallGet.setCount(maxGroupPostsCount);
        LinkedList<Post> posts = wallGet.invoke();

        boolean needToCheckComments = true;
        for (Post post : posts) {
            try {
                checkGroupPost(post, group, needToCheckComments);
                postsCheckedCount.incrementAndGet();
                fireEventToUpdate();
            } catch (VKException e) {
                needToCheckComments = false;
                logMessages.add(new LogMessageEntry("ERROR with post= " + post + ". REASON: " + e));
                errorCount.incrementAndGet();
                fireEventToUpdate();
            }
        }
    }

    private void checkGroupPost(Post post, Group correspondingGroup, boolean needToCheckComments) throws VKException {
        logMessages.add(new LogMessageEntry("Checking " + post));
        fireEventToUpdate();

        LikesIsLiked likesIsLiked = new LikesIsLiked(post.getOwnerId(), spyingId,
                post.getId(), LikesIsLiked.Type.POST, accessToken);
        Boolean isLikedPost = likesIsLiked.invoke();
        if (isLikedPost) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.GROUP, ResultEntry.Type.POST_LIKE,
                    correspondingGroup.getName(), post.getText(), post.getDate(), post.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }
        boolean isPostAuthor = post.getFromId().equals(spyingId);
        if (isPostAuthor) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.GROUP, ResultEntry.Type.POST_AUTHOR,
                    correspondingGroup.getName(), post.getText(), post.getDate(), post.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }

        if (needToCheckComments) {
            WallGetComments postGetComments = new WallGetComments(accessToken, post.getOwnerId(), post.getId());
            postGetComments.setCount(maxGroupCommentsCount);
            LinkedList<Comment> comments = postGetComments.invoke();

            for (Comment comment : comments) {
                try {
                    checkGroupPostComment(comment, correspondingGroup);
                    commentsCheckedCount.incrementAndGet();
                    fireEventToUpdate();
                } catch (VKException e) {
                    logMessages.add(new LogMessageEntry("ERROR with comment= " + comment + ". REASON: " + e));
                    errorCount.incrementAndGet();
                    fireEventToUpdate();
                }
            }
        }
    }

    private void checkGroupPostComment(Comment comment, Group correspondingGroup) throws VKException{
        logMessages.add(new LogMessageEntry("Checking " + comment));
        fireEventToUpdate();

        boolean isCommentAuthor = comment.getFromId().equals(spyingId);
        if (isCommentAuthor) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.GROUP, ResultEntry.Type.COMMENT_AUTHOR,
                    correspondingGroup.getName(), comment.getText(), comment.getDate(), comment.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }

        Boolean isLikedComment = new LikesIsLiked(comment.getOwnerId(), spyingId, comment.getId(),
                LikesIsLiked.Type.COMMENT, accessToken).invoke();
        if (isLikedComment) {
            ResultEntry resultEntry = new ResultEntry(ResultEntry.GroupOrUser.GROUP, ResultEntry.Type.COMMENT_LIKE,
                    correspondingGroup.getName(), comment.getText(), comment.getDate(), comment.getLink());
            results.add(resultEntry);
            itemsFoundCount.incrementAndGet();
            logMessages.add(new LogMessageEntry("FOUND " + resultEntry));
            fireEventToUpdate();
        }
    }

    private void fireEventToUpdate() {
        Platform.runLater(() -> Event.fireEvent(parentNode, new SearchEngineMadeChangesEvent()));
    }

    public static class SearchEngineMadeChangesEvent extends Event {
        public static final EventType<SearchEngineMadeChangesEvent> SEARCH_ENGINE_MADE_CHANGES_EVENT =
                new EventType<>(ANY, "SEARCH_ENGINE_MADE_CHANGES");

        public SearchEngineMadeChangesEvent() {
            super(SEARCH_ENGINE_MADE_CHANGES_EVENT);
        }
    }

    public static class ResultEntry {
        private final GroupOrUser groupOrUser;
        private final Type type;
        private final String groupOrUserName;
        private final String text;
        private final Long unixTime;
        private final String hyperlink;

        public ResultEntry(GroupOrUser groupOrUser, Type type, String groupOrUserName, String text,
                           Long unixTime, String hyperlink) {
            this.groupOrUser = groupOrUser;
            this.type = type;
            this.groupOrUserName = groupOrUserName;
            this.text = text;
            this.unixTime = unixTime;
            this.hyperlink = hyperlink;
        }

        public GroupOrUser getGroupOrUser() {
            return groupOrUser;
        }

        public Type getType() {
            return type;
        }

        public String getGroupOrUserName() {
            return groupOrUserName;
        }

        public String getText() {
            return text;
        }

        public Long getUnixTime() {
            return unixTime;
        }

        public String getHyperlink() {
            return hyperlink;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ResultEntry{");
            sb.append("time=").append(DateFormat.getInstance().format(new Date(unixTime * 1000L)));
            sb.append(", type=").append(type);
            sb.append(", text='").append(text).append('\'');
            sb.append(", hyperlink='").append(hyperlink).append('\'');
            sb.append(", groupOrUserName='").append(groupOrUserName).append('\'');
            sb.append(", groupOrUser=").append(groupOrUser);
            sb.append('}');
            return sb.toString();
        }

        public static enum GroupOrUser {GROUP, USER}

        public static enum Type {POST_LIKE, POST_AUTHOR, COMMENT_LIKE, COMMENT_AUTHOR}
    }

    public static class LogMessageEntry {
        private final String message;
        private final Long unixTime = System.currentTimeMillis() / 1000L;

        public LogMessageEntry(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return DateFormat.getInstance().format(new Date(unixTime * 1000L)) + " message = " + message;
        }
    }
}
