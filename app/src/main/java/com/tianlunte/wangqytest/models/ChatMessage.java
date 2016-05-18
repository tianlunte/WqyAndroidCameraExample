package com.tianlunte.wangqytest.models;

/**
 * Created by wangqingyun on 5/17/16.
 */
public class ChatMessage {

    private String textMessage;
    private String imageMessage;

    private ChatMessage() {

    }

    public ChatMessage(String txtMsg, String imgMsg) {
        textMessage = txtMsg;
        imageMessage = imgMsg;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getImageMessage() {
        return imageMessage;
    }

}