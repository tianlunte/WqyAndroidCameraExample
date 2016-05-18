package com.tianlunte.wangqytest.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tianlunte.wangqytest.R;
import com.tianlunte.wangqytest.models.ChatMessage;
import com.tianlunte.wangqytest.utils.CommUtils;

import java.util.List;

/**
 * Created by wangqingyun on 5/17/16.
 */
public class OneChatAdapter extends BaseAdapter {

    private List<ChatMessage> mDataList;

    public OneChatAdapter(List<ChatMessage> dataList) {
        mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        OneChatHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(root.getContext()).inflate(R.layout.item_one_chat_list, null);
            viewHolder = new OneChatHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OneChatHolder)convertView.getTag();
        }

        ChatMessage chatMessage = mDataList.get(position);
        if(!CommUtils.isNullOrEmpty(chatMessage.getTextMessage())) {
            viewHolder.pTxtMessageView.setVisibility(View.VISIBLE);
            viewHolder.pImgMessageView.setVisibility(View.GONE);
            viewHolder.pTxtMessageView.setText(chatMessage.getTextMessage());
        } else if(!CommUtils.isNullOrEmpty(chatMessage.getImageMessage())) {
            viewHolder.pTxtMessageView.setVisibility(View.GONE);
            viewHolder.pImgMessageView.setVisibility(View.VISIBLE);
            byte[] decodedString = Base64.decode(chatMessage.getImageMessage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            viewHolder.pImgMessageView.setImageBitmap(decodedByte);
        }

        return convertView;
    }

    protected static class OneChatHolder {
        protected TextView pTxtMessageView;
        protected ImageView pImgMessageView;

        protected OneChatHolder(View root) {
            pTxtMessageView = (TextView)root.findViewById(R.id.item_one_chat_text_message);
            pImgMessageView = (ImageView)root.findViewById(R.id.item_one_chat_image_message);
        }
    }

}