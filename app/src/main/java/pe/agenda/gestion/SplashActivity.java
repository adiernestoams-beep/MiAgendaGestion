package pe.agenda.gestion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(40,40,40,40);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(229,166,255),Color.rgb(79,56,245),Color.rgb(43,28,196)});root.setBackground(bg);
        ImageView iv=new ImageView(this);iv.setImageResource(R.mipmap.ic_launcher);iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);root.addView(iv,new LinearLayout.LayoutParams(dp(250),dp(250)));
        TextView t=new TextView(this);t.setText("365 Agenda");t.setTextColor(Color.WHITE);t.setTextSize(38);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);tp.topMargin=dp(18);root.addView(t,tp);
        setContentView(root);new Handler().postDelayed(()->{startActivity(new Intent(this,MainActivity.class));finish();},900);
    }
    int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
}
