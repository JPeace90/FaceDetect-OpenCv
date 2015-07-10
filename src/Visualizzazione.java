package org.opencv.samples.facedetect;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Visualizzazione extends Activity {
	public ArrayList listx = new ArrayList();
	public ArrayList listy = new ArrayList();
	public int flag;
	public EditText outputx, outputy;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_visualizzazione);		
		Intent intent = getIntent();
		
		outputx = (EditText) findViewById(R.id.outputx);
		outputy = (EditText) findViewById(R.id.outputy);
		
		listx =(ArrayList) intent.getSerializableExtra(getPackageName()+".myListx");
		listy =(ArrayList) intent.getSerializableExtra(getPackageName()+".myListy");
		flag = (Integer) intent.getSerializableExtra(getPackageName()+".myFlag");
		
		
		String ox = "";
		String oy = "";
		
		int i=0;
		int size = listx.size();
		
		while( i < size){
			ox += listx.get(i) + "\n";
			oy += listy.get(i) + "\n";
			i++;
		}
		
		outputx.setText(ox);
		outputy.setText(oy);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.visualizzazione, menu);
		return true;
	}
	
	public void onIndietroClick(View v){
		Intent intent2 = new Intent(getApplicationContext(), FdActivity.class);
		
		startActivity(intent2);
		outputx.setText("");
		outputy.setText("");
	}
	
	public void onAvantiClick(View v){
		Intent intent3 = new Intent(getApplicationContext(), GraphActivity.class);
		
		intent3.putExtra(getPackageName()+".myListx", listx);
		intent3.putExtra(getPackageName()+".myListy", listy);
		intent3.putExtra(getPackageName()+".myFlag", flag);
		
		startActivity(intent3);
	}
	
	
}
