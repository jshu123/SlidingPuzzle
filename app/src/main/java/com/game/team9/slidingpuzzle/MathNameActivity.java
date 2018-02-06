package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;

public class MathNameActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {


    private final RadioButton[] m_Radios = new RadioButton[3];
    private static final Class[] m_Class = new Class[]{MathSinglePlayerActivity.class, MathOnlineDiscoveryActivity.class, MathOnlineDiscoveryActivity.class};
    private TextView m_Name;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_player_name);

        SharedPreferences pref = getSharedPreferences("MathModeName", MODE_PRIVATE);
        m_Name = findViewById(R.id.playerText);
        String name = pref.getString("name", null);
        m_Name.setText(name);
        m_Radios[0] =  findViewById(R.id.singleOpt);
        m_Radios[1] = findViewById(R.id.cutOpt);
        m_Radios[2] = findViewById(R.id.basicOpt);
        m_Radios[pref.getInt("LastMathMode", 0)].setChecked(true);
        for (RadioButton radio : m_Radios) {
            radio.setOnCheckedChangeListener(this);
        }
    }


    public void onStartClicked(View view)
    {
        String name = m_Name.getText().toString();
        if(name == null || name.length() == 0)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_LONG);
            toast.show();
            m_Name.requestFocus();
            return;
        }
        SharedPreferences.Editor pref = getSharedPreferences("MathModeName", MODE_PRIVATE).edit();
        Intent intent = null;//
        for(int i = 0; i < 3; ++i)
        {
            if(m_Radios[i].isChecked())
            {
                pref.putInt("LastMathMode", i);
                intent = new Intent(this, m_Class[i]);
                break;
            }
        }

        User u = new User();


        pref.putString("name", name);
        pref.apply();


        u.setName(name);
        u.setScore(0);
        HighScoreDatabase.updateUser(u);
    if(intent != null)
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        for (RadioButton radio : m_Radios) {
            if(radio != compoundButton)
            {
                radio.setOnCheckedChangeListener(null);
                radio.setChecked(!b);
                radio.setOnCheckedChangeListener(this);
            }
        }
    }
}
