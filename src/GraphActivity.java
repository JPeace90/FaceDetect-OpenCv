package org.opencv.samples.facedetect;

import java.text.MessageFormat;
import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class GraphActivity extends Activity {

	public GraphView graphView;
	public GraphViewSeries seriesx, seriesy;
	public GraphViewSeries origx, origy;
	public ArrayList listx, listy;
	public int flag;
	public Button totale;
	public Button preview;
	public Cursor c;
	public DatabaseHelper database;
	public SQLiteDatabase db;
	public GraphViewData [] datax;
	public GraphViewData [] datay;
	public GraphViewData [] originx;
	public GraphViewData [] originy;
	public boolean precedente = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		Intent intent = getIntent();
		
		totale = (Button) findViewById(R.id.totale);
		preview = (Button) findViewById(R.id.preview);
		
		listx = (ArrayList) intent.getSerializableExtra(getPackageName()+".myListx");
		listy = (ArrayList) intent.getSerializableExtra(getPackageName()+".myListy");
		flag = (Integer) intent.getSerializableExtra(getPackageName()+".myFlag");
		
		database = new DatabaseHelper(this);

		db = database.getWritableDatabase();
		
		if(flag <= 1){
			preview.setEnabled(false);
			int a = db.delete(TabellaCoordinate.TABLE_NAME, null, null);
			Log.d("ELIMINAZIONEDB", a +"");
		}
		else preview.setEnabled(true);
		
		for(int i = 0; i < listx.size(); i++){
			database.inserisciCoordinate(db, (Double)listx.get(i), (Double)listy.get(i), flag);
		}
		
		c = database.getCoordinate(flag);
		
		stampa(listx.size());
		
		graphView = new LineGraphView(
		      this // context
		      , "Grafico" // heading
		);
				
		graphView.setViewPort(2, 40);
		graphView.setScrollable(true);
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		layout.addView(graphView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.graph, menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.buttonx).setEnabled(true);
		menu.findItem(R.id.buttony).setEnabled(true);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.buttonx:
			graphView.removeAllSeries();
			graphView.addSeries(seriesx); // data
			graphView.addSeries(origx);
			graphView.setTitle("Grafico dx/sx");
			return true;
		case R.id.buttony:
			graphView.removeAllSeries();
			graphView.addSeries(seriesy); // data
			graphView.addSeries(origy);
			graphView.setTitle("Grafico up/down");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
public interface TabellaCoordinate extends BaseColumns{
		
		String TABLE_NAME = "coordinate";
		
		String ASCISSA = "ascissa";
		
		String ORDINATA = "ordinata";
		
		String ESECUZIONE = "esecuzione";
		
		String [] COLUMNS = new String[]{ _ID, ASCISSA, ORDINATA, ESECUZIONE };
		
	}
	
	public class DatabaseHelper extends SQLiteOpenHelper{

		private static final String DATABASE_NAME = "devAPP.db";
		
		private static final int SCHEMA_VERSION = 1;
		
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, SCHEMA_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {			
			String sql = "CREATE TABLE {0} ({1} INTEGER PRIMARY KEY AUTOINCREMENT," + 
							"{2} TEXT NOT NULL, {3} TEXT NOT NULL, {4} TEXT NOT NULL);";
			
			db = getWritableDatabase();
			
			db.execSQL(MessageFormat.format(sql, TabellaCoordinate.TABLE_NAME, 
						TabellaCoordinate._ID, TabellaCoordinate.ASCISSA, 
						TabellaCoordinate.ORDINATA, TabellaCoordinate.ESECUZIONE));
		}
		
		public void inserisciCoordinate(SQLiteDatabase db, Double x, Double y, int index){
			ContentValues v = new ContentValues();
			
			v.put(TabellaCoordinate.ASCISSA, x);
			v.put(TabellaCoordinate.ORDINATA, y);
			v.put(TabellaCoordinate.ESECUZIONE, index);
			
			db.insert(TabellaCoordinate.TABLE_NAME, null, v);
		}
		
		public Cursor getCoordinate(int flag){
			
			Cursor c = getWritableDatabase().query(TabellaCoordinate.TABLE_NAME, 
					TabellaCoordinate.COLUMNS, TabellaCoordinate.ESECUZIONE + "=" + flag, null, null, null, null);
			
			if(c!=null) c.moveToFirst();
			
			return c;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public void onIndietroClick(View v){
		Intent intent = new Intent(getApplicationContext(), FdActivity.class);
		flag++;
		
		intent.putExtra(getPackageName()+".myFlag", flag);
		listx.clear();
		listy.clear();
		
		startActivity(intent);
	}
	
	public void onPreviewClick(View v){
		if(!precedente){
			c = database.getCoordinate(flag - 1);
			String sql_instruction = "select count(*) from " + TabellaCoordinate.TABLE_NAME +" where "+ TabellaCoordinate.ESECUZIONE + " = " + (flag-1);
			Cursor c2 = db.rawQuery(sql_instruction, null);
			c2.moveToFirst();
			int count = c2.getInt(0);
			c2.close();
			stampa(count);
			precedente = true;
			preview.setText("Corrente");
			Log.d("COOUNT_PRECEDENTE", count+ "");
		}
		else {
			c = database.getCoordinate(flag);
			String sql_instruction = "select count(*) from " + TabellaCoordinate.TABLE_NAME +" where "+ TabellaCoordinate.ESECUZIONE + " = " + (flag);
			Cursor c2 = db.rawQuery(sql_instruction, null);
			c2.moveToFirst();
			int count = c2.getInt(0);
			c2.close();
			stampa(count);
			precedente = false;
			preview.setText("Precedente");
			Log.d("COOUNT_SUCC", count+ "");
		}
		if(!totale.isEnabled()) totale.setEnabled(true);
	}
	
	public void onTotaleClick(View v){		
		c = db.query(TabellaCoordinate.TABLE_NAME, TabellaCoordinate.COLUMNS, null, null, null, null, null);
		if(c!=null) c.moveToFirst();
		
		String sql_instruction = "select count(*) from " + TabellaCoordinate.TABLE_NAME;
		Cursor c2 = db.rawQuery(sql_instruction, null);
		c2.moveToFirst();
		int count = c2.getInt(0);
		c2.close();
		stampa(count);
		totale.setEnabled(false);
	}
	
	public void stampa(int size){
		datax = new GraphViewData[size];
		datay = new GraphViewData[size];
		originx = new GraphViewData[size];
		originy = new GraphViewData[size];
		
		int i = 0;
		try {
			while (c.moveToNext() && i<size){
				datax[i] = new GraphViewData(i, c.getDouble(1));
				originx[i] = new GraphViewData(i, 0);
				
				datay[i] = new GraphViewData(i, c.getDouble(2));
				originy[i] = new GraphViewData(i, 0);
				i++;	
			}

		}
		finally {
			c.close();
		}
		
		while(i<datax.length){
			datax[i] = new GraphViewData(i, 0);
			originx[i] = new GraphViewData(i, 0);
			
			datay[i] = new GraphViewData(i, 0);
			originy[i] = new GraphViewData(i, 0);
			i++;	
		}
		
		datax[0] = new GraphViewData(0, 0);
		originx[0] = new GraphViewData(0, 0);
		
		datay[0] = new GraphViewData(0, 0);
		originy[0] = new GraphViewData(0, 0);
		
		GraphViewSeriesStyle style = new GraphViewSeriesStyle();
		style.color = Color.rgb(255, 0, 0);
		seriesx = new GraphViewSeries("Dati dx/sx", style, datax);
		
		GraphViewSeriesStyle style2 = new GraphViewSeriesStyle();
		style2.color = Color.rgb(0, 255, 0);
		origx = new GraphViewSeries("Origine",style2,originx);
		
		seriesy = new GraphViewSeries("Dati up/down", style, datay);
		
		origy = new GraphViewSeries("Origine", style2, originy);		
	}
	
	public void onExitClick(View v){
		android.os.Process.killProcess(android.os.Process.myTid());
		
		
	}
}
