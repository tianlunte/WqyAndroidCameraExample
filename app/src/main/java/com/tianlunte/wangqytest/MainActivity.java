package com.tianlunte.wangqytest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.tianlunte.wangqytest.adapters.OneChatAdapter;
import com.tianlunte.wangqytest.fragments.FragmentTakePhoto;
import com.tianlunte.wangqytest.models.ChatMessage;
import com.tianlunte.wangqytest.utils.CommUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String FIRE_BASE_BASE_URL = "https://wqy-test.firebaseio.com";

    private static final int REQUEST_MULTIPLE_PERMISSION = 0x8888;

    private Firebase mFirebaseRef;

    private EditText mEditChat;

    private List<ChatMessage> mChatList;
    private OneChatAdapter mChatAdapter;

    private FragmentTakePhoto mFragmentTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseRef = new Firebase(FIRE_BASE_BASE_URL).child("chat");
        mFirebaseRef.addChildEventListener(mChildListener);

        mChatList = new ArrayList<>();
        ListView listView = (ListView)findViewById(R.id.chat_list_view);
        mChatAdapter = new OneChatAdapter(mChatList);
        listView.setAdapter(mChatAdapter);

        mEditChat = (EditText)findViewById(R.id.chat_edit);

        findViewById(R.id.chat_button_send).setOnClickListener(this);
        findViewById(R.id.chat_button_pic).setOnClickListener(this);

        mFragmentTakePhoto = new FragmentTakePhoto();
        getSupportFragmentManager().beginTransaction().replace(R.id.take_photo_layout, mFragmentTakePhoto).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mFragmentTakePhoto.hide();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_button_send: {
                String message = mEditChat.getText().toString();
                if(message.length() > 0) {
                    mEditChat.setText("");

                    sendTextMessage(message);
                }
            }
            break;
            case R.id.chat_button_pic: {
                requestPermission();
            }
            break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_MULTIPLE_PERMISSION) {
            for(int result : grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            requestPermissionSuccess();
        }
    }

    public void sendImageMessage(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        ChatMessage msg = new ChatMessage(null, encoded);

        mFirebaseRef.push().setValue(msg);

        setFullScreen(false);
        findViewById(R.id.take_photo_layout).setVisibility(View.GONE);
        mFragmentTakePhoto.hide();
    }

    private void sendTextMessage(String text) {
        ChatMessage msg = new ChatMessage(text, null);

        mFirebaseRef.push().setValue(msg);
    }

    private void requestPermission() {
        ArrayList<String> permissionList = new ArrayList<>();
        if (!CommUtils.isCameraPermissionActive(this)) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (permissionList.size() > 0) {
            requestMultiplePermission(this, permissionList.toArray(new String[permissionList.size()]));
        } else {
            requestPermissionSuccess();
        }
    }

    private void requestMultiplePermission(Activity activity,String[] permissionArr) {
        ActivityCompat.requestPermissions(activity, permissionArr, REQUEST_MULTIPLE_PERMISSION);
    }

    private void requestPermissionSuccess() {
        setFullScreen(true);
        findViewById(R.id.take_photo_layout).setVisibility(View.VISIBLE);
        mFragmentTakePhoto.show();
    }

    public void setFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    private ChildEventListener mChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

            mChatList.add(chatMessage);
            mChatAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };
}