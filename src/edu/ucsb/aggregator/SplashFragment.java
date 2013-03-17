package edu.ucsb.aggregator;




import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.widget.LoginButton;



public class SplashFragment extends Fragment {
    
	private Button twitterLogin;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash, container, false);
        
        LoginButton loginButton = (LoginButton)view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("read_stream"));
        
        twitterLogin = (Button) view.findViewById(R.id.login_twitter);
        
//		
        twitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent myIntent = new Intent(getActivity() , TwitterFragment.class);
            	startActivity(myIntent);
            	//MainActivity.this.startActivity(myIntent);
                
            }
        });
        return view;
    }
}
