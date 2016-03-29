package com.zsy.mobilesecuguard.chapter09;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter09.db.NumBelongtoDao;
import com.zsy.mobilesecuguard.utils.DBUtils;

import java.io.File;

/**
 * 归属地查询
 */
public class NumBelongtoActivity extends Activity implements OnClickListener {

    private EditText mNumET;
    private TextView mResultTV;
    private String dbName = "address.db";
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_numbelongto);
        initView();
        DBUtils.copyDB(this, dbName);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.bright_red));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("号码归属地查询");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        findViewById(R.id.btn_searchnumbelongto).setOnClickListener(this);
        mNumET = (EditText) findViewById(R.id.et_num_numbelongto);
        mResultTV = (TextView) findViewById(R.id.tv_searchresult);

        mNumET.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //文本变化之后
                String string = s.toString().trim();
                if (string.length() == 0) {
                    mResultTV.setText("");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;

            case R.id.btn_searchnumbelongto:
                //判断edittext中的号码是否为空
                //判断数据库是否存在
                final String phonenumber = mNumET.getText().toString().trim();
                if (!TextUtils.isEmpty(phonenumber)) {
                    File file = new File(getFilesDir(), dbName);
                    if (!file.exists() || file.length() <= 0) {
                        //数据库不存在,复制数据库
                        DBUtils.copyDB(this, dbName);
                    }
                    //查询数据库
                    new Thread() {
                        @Override
                        public void run() {
                            final String location = NumBelongtoDao.getLocation(getDatabasePath(dbName).getPath(), phonenumber);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mResultTV.setText("归属地： " + location);
                                }
                            });
                        }
                    }.start();
                } else {
                    Toast.makeText(this, "请输入需要查询的号码", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
