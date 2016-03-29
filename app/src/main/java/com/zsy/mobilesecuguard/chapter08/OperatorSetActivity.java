package com.zsy.mobilesecuguard.chapter08;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.zsy.mobilesecuguard.R;

/**
 * Created by zsy on 2016/3/27.
 */
public class OperatorSetActivity extends Activity implements OnClickListener {
    private String[] operators={"中国移动", "中国联通", "中国电信"};
    private Spinner mSelectSP;
    private ArrayAdapter mSelectadapter;
    private SharedPreferences msp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_operatorset);
        msp = getSharedPreferences("config", MODE_PRIVATE);
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
        ImageView mLeftImgv = (ImageView)findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("运营商信息设置");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mSelectSP = (Spinner) findViewById(R.id.spinner_operator_select);
        mSelectadapter = new ArrayAdapter(this, R.layout.item_spinner_operatorset, R.id.tv_provice, operators);
        mSelectSP.setAdapter(mSelectadapter);
        findViewById(R.id.btn_operator_finish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = msp.edit();
        switch (v.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_operator_finish:
                editor.putInt("operator", mSelectSP.getSelectedItemPosition() + 1);
                editor.putBoolean("isset_operator", true);
                editor.apply();
                startActivity(new Intent(this, TrafficMonitoringActivity.class));
                finish();
                break;
        }
    }
}
