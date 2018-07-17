package com.laioffer.eventreporter;

public class Comment {
    private String commentId;
    private String commenter;
    private String eventId;

    private String description;
    private long time;
    private int good;

    public String getCommentId() {
        return commentId;
    }

    public String getCommenter() {
        return commenter;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDescription() {
        return description;
    }

    public long getTime() {
        return time;
    }

    public int getGood() {
        return good;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setGood(int good) {
        this.good = good;
    }
}
