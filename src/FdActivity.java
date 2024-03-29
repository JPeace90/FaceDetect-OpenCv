package org.opencv.samples.facedetect;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.math.*;
import java.lang.Math;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.utils.Converters;
import org.opencv.video.Video;

import com.jjoe64.graphview.GraphView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FdActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";
	//TODO: rettangolo per la faccia
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	private static final int TM_SQDIFF = 0;
	private static final int TM_SQDIFF_NORMED = 1;
	private static final int TM_CCOEFF = 2;
	private static final int TM_CCOEFF_NORMED = 3;
	private static final int TM_CCORR = 4;
	private static final int TM_CCORR_NORMED = 5;
	

	private int learn_frames = 0;
	private Mat teplateR;
	private Mat teplateL;
	int method = 5;

	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemType;

	private Mat mRgba;
	private Mat mGray;
	private Mat mGray2;
	// matrix for zooming
	private Mat mZoomWindow;
	private Mat mZoomWindow2;

	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private CascadeClassifier mJavaDetectorEye;
	
	
	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.5f;
	private int mAbsoluteFaceSize = 0;

	private CameraBridgeViewBase mOpenCvCameraView;

	private SeekBar mMethodSeekbar;
	private TextView mValue;
	
	//_________________________//
	private TextView coordx;
	private TextView coordy;
	private double coordx_def;
	private double coordy_def;
	public Point iris = new Point();
	public ArrayList arrayY = new ArrayList();
	public ArrayList arrayX = new ArrayList();
	private Point center1, center2;
	private boolean first = true;
	public boolean trovato = false;
	public int flag;
	//_________________________//

	double xCenter = -1;
	double yCenter = -1;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");


				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,
							"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					// --------------------------------- load left eye
					// classificator -----------------------------------
					InputStream iser = getResources().openRawResource(
							R.raw.haarcascade_lefteye_2splits);
					File cascadeDirER = getDir("cascadeER",
							Context.MODE_PRIVATE);
					File cascadeFileER = new File(cascadeDirER,
							"haarcascade_eye_right.xml");
					FileOutputStream oser = new FileOutputStream(cascadeFileER);

					byte[] bufferER = new byte[4096];
					int bytesReadER;
					while ((bytesReadER = iser.read(bufferER)) != -1) {
						oser.write(bufferER, 0, bytesReadER);
					}
					iser.close();
					oser.close();

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

					mJavaDetectorEye = new CascadeClassifier(
							cascadeFileER.getAbsolutePath());
					if (mJavaDetectorEye.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorEye = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

				

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}
				mOpenCvCameraView.setCameraIndex(1);
				//mOpenCvCameraView.enableFpsMeter();
				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public FdActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";

		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.face_detect_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		if (getIntent().getSerializableExtra(getPackageName()+".myFlag") != null){
			flag = (Integer) getIntent().getSerializableExtra(getPackageName()+".myFlag");
			trovato = true;
		}
		//mMethodSeekbar = (SeekBar) findViewById(R.id.methodSeekBar);
		//mValue = (TextView) findViewById(R.id.method);
		
		/*mMethodSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				method = progress;
				switch (method) {
				case 0:
					mValue.setText("TM_SQDIFF");
					break;
				case 1:
					mValue.setText("TM_SQDIFF_NORMED");
					break;
				case 2:
					mValue.setText("TM_CCOEFF");
					break;
				case 3:
					mValue.setText("TM_CCOEFF_NORMED");
					break;
				case 4:
					mValue.setText("TM_CCORR");
					break;
				case 5:
					mValue.setText("TM_CCORR_NORMED");
					break;
				}

				
			}
		});*/
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		mGray2 = new Mat();
	}

	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
		mGray.release();
		//mZoomWindow.release();
		//mZoomWindow2.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {		
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		mGray2 = inputFrame.gray();
		
		/*
		Core.MinMaxLocResult result = Core.minMaxLoc(mGray2);
		Imgproc.threshold(mGray2, mGray2, 50, result.maxVal, Imgproc.THRESH_BINARY);
		Core.multiply(mGray, mGray2, mGray2);
		*/
		
		//TODO: applicazione del filtro sobel a tutta la matrice
		//Sobel(mGray);
		//mGray = Sobel(mGray);
		
/* --------------------------------------------------------------------------- */

		
/* --------------------------------------------------------------------------- */
		

		
		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}
		
		
		//if (mZoomWindow == null || mZoomWindow2 == null)
	        //CreateAuxiliaryMats();

		MatOfRect faces = new MatOfRect();

			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2,
						2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
						new Size());
		
			
		//TODO: br() prende il lato in basso(bottom) e quello a destra(right)
		//TODO: tl() prende il lato in alto(top) e quello a sinistra(left)
		Rect[] facesArray = faces.toArray();
		
		//TODO: rettangolo per il rilevamento della faccia
		for (int i = 0; i < facesArray.length; i++) {
			/*
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
					FACE_RECT_COLOR, 3);
			*/
			String xstring = facesArray[i].x+"";
			String ystring = facesArray[i].y+"";
			
			xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
			yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
			
			//Point center = new Point(xCenter, yCenter);
			
			//TODO: centro del rettangolo contenente la faccia
			/*
			Core.circle(mRgba, center, 10, new Scalar(255, 255, 255, 255), 3);
			
			//TODO: testo con coordinate relative al cerchio
			Core.putText(mRgba, "[" + center.x + "," + center.y + "]",
					new Point(center.x + 20, center.y + 20),
					Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
							255));
			*/
			
			Rect r = facesArray[i];
			// compute the eye area
			Rect eyearea = new Rect(r.x + r.width / 8,
					(int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
					(int) (r.height / 3.0));
			// split it
			
			Core.rectangle(mRgba, eyearea.tl(), eyearea.br(), new Scalar(255, 0, 0, 255));
			
			Rect eyearea_right = new Rect((r.x + r.width / 16),
					(int) (r.y + (r.height / 4.5))+25,
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 4.5));
			Rect eyearea_left = new Rect((r.x + r.width / 16
					+ (r.width - 2 * r.width / 16) / 2),
					(int) (r.y + (r.height / 4.5))+25,
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 4.5));
			
			//posizione degli occhi in base al quadrato della faccia
			center1 = new Point(r.x+(r.width*0.3), r.y+(r.height*0.4));
			Point l1 = new Point(center1.x-60, center1.y+30);
			Point l2 = new Point(center1.x+60, center1.y-30);
			Rect left = new Rect((int)l1.x,(int) l2.y, 120, 60);
			
			Core.circle(mRgba, center1, 10, new Scalar(255, 0, 0, 255));
			
			center2 = new Point(r.x+(r.width*0.7), r.y+(r.height*0.4));
			Point l3 = new Point(center2.x-60, center2.y+30);
			Point l4 = new Point(center2.x+60, center2.y-30);
			Rect right = new Rect((int)l3.x,(int) l4.y, 120, 60);
			Core.circle(mRgba, center2, 10, new Scalar(255, 0, 0, 255));
			
			if(!first) gestioneDati(center1.x, center1.y);
			// draw the area - mGray is working grayscale mat, if you want to
			// see area in rgb preview, change mGray to mRgba
			
			//TODO: rettangoli occhio destro e occhio sinistro
			
			/*
			Core.rectangle(mRgba, left.tl(), left.br(),
					new Scalar(255, 255, 0, 255), 2);
			Core.rectangle(mRgba, right.tl(), right.br(),
					new Scalar(255, 0, 255, 255), 2);
			*/
			
			/*
			Core.rectangle(mGray, eyearea_left.tl(), eyearea_left.br(),
					new Scalar(255, 255, 0, 255), 2);
			Core.rectangle(mGray, eyearea_right.tl(), eyearea_right.br(),
					new Scalar(255, 0, 255, 255), 2);
			*/
			//Core.line(mGray, center1, center2, new Scalar(255, 0, 0, 255));
			
			
			if (learn_frames < 5) {
				teplateR = get_template(mJavaDetectorEye, eyearea_right, 20); //il 25 gestisce la dimensione
				teplateL = get_template(mJavaDetectorEye, eyearea_left, 20);  // del rettangolo giallo
				learn_frames++;
			} else {
				// Learning finished, use the new templates for template
				// matching
				match_eye(eyearea_right, teplateR, method); 
				match_eye(eyearea_left, teplateL, method); 
				
			}
			
			
			// cut eye areas and put them to zoom windows
			/*Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2,
					mZoomWindow2.size());
			Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow,
					mZoomWindow.size());*/
			
			
		}
		//deve ritornare mRgba
		//return mGray2;
		//return peaks;
		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		/*mItemFace50 = menu.add("Face size 50%");
		mItemFace40 = menu.add("Face size 40%");
		mItemFace30 = menu.add("Face size 30%");
		mItemFace20 = menu.add("Face size 20%");
		mItemType = menu.add(mDetectorName[mDetectorType]);*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		/*if (item == mItemFace50)
			setMinFaceSize(0.5f);
		else if (item == mItemFace40)
			setMinFaceSize(0.4f);
		else if (item == mItemFace30)
			setMinFaceSize(0.3f);
		else if (item == mItemFace20)
			setMinFaceSize(0.2f);
		else if (item == mItemType) {
			int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
			item.setTitle(mDetectorName[tmpDetectorType]);
		}*/
		return true;
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	//TODO: crea le matrici per lo zoom degli occhi
	/*private void CreateAuxiliaryMats() {
		if (mGray.empty())
			return;

		int rows = mGray.rows();
		int cols = mGray.cols();

		if (mZoomWindow == null) {
			mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
					+ cols / 10, cols);
			mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
					+ cols / 10, cols);
		}

	}*/
	//TODO: metodo per rilevare gli occhi
	private void match_eye(Rect area, Mat mTemplate, int type) {
		Point matchLoc;
		Mat mROI = mGray.submat(area);
		Mat mROI2 = mGray.submat(area);
		
		Core.MinMaxLocResult result = Core.minMaxLoc(mROI2);
		Imgproc.threshold(mROI2, mROI2, 50, result.maxVal, Imgproc.THRESH_BINARY);
		Core.multiply(mROI2, mROI, mROI);

		
		int result_cols = mROI.cols() - mTemplate.cols() + 1;
		int result_rows = mROI.rows() - mTemplate.rows() + 1;
		// Check for bad template size
		if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
			return ;
		}
		Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

		/*switch (type) {
		case TM_SQDIFF:
			Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
			break;
		case TM_SQDIFF_NORMED:
			Imgproc.matchTemplate(mROI, mTemplate, mResult,
					Imgproc.TM_SQDIFF_NORMED);
			break;
		case TM_CCOEFF:
			Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
			break;
		case TM_CCOEFF_NORMED:
			Imgproc.matchTemplate(mROI, mTemplate, mResult,
					Imgproc.TM_CCOEFF_NORMED);
			break;
		case TM_CCORR:
			Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
			break;
		case TM_CCORR_NORMED:
			Imgproc.matchTemplate(mROI, mTemplate, mResult,
					Imgproc.TM_CCORR_NORMED);
			break;
		}*/
		
		Mat m = mTemplate;
		Core.MinMaxLocResult result2 = Core.minMaxLoc(m);
		Imgproc.threshold(m, m, 50, result.maxVal, Imgproc.THRESH_BINARY);
		Core.multiply(m, mTemplate, mTemplate);

		Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR_NORMED);
		
		Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
		// there is difference in matching methods - best match is max/min value
		if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
			matchLoc = mmres.minLoc;
		} else {
			matchLoc = mmres.maxLoc;
		}

		Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
		Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x,
				matchLoc.y + mTemplate.rows() + area.y);
		
		Point a = new Point(matchLoc.x-area.x, matchLoc.y-area.y);
		Point b = new Point(matchLoc.x+mTemplate.cols() - area.x, 
				matchLoc.y + mTemplate.rows() - area.y);
		/*
		double sumx = (mmres.minLoc.x + mmres.maxLoc.x)/2;
		double sumy = (mmres.minLoc.y + mmres.maxLoc.y)/2;
		
		Point center = new Point(sumx, sumy);
		
		Core.circle(mGray, center, 10, new Scalar(255, 255, 255, 255));
		*/
		//TODO: rettangolo che viene visualizzato intorno alla pupilla
		//qui ci deve essere mrgba
		
		
		//Core.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0,255));
		
		
		
		
		Log.d("MATCHLOOOOOOOOC", matchLoc_tx.x + ""+ matchLoc_tx.y +"" );
		Log.d("MATCHLOOOOOOOOC__Y", matchLoc_ty.x + ""+ matchLoc_ty.y +"" );
		//Core.rectangle(mRgba, a, b, new Scalar(255, 255, 255, 255));
		
		 //Rect rec = new Rect(matchLoc_tx,matchLoc_ty);

	}

	
	//TODO: metodo per la creazione del template per gli occhi (RECREATE)
	private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
		Mat template = new Mat();
		Mat mROI = mGray.submat(area);
		
		
		MatOfRect eyes = new MatOfRect();
		//Point iris = new Point();
		Rect eye_template = new Rect();
		clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT
						| Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30),
				new Size());

		Rect[] eyesArray = eyes.toArray();
		for (int i = 0; i < eyesArray.length;) {
			Rect e = eyesArray[i];
			e.x = area.x + e.x;
			e.y = area.y + e.y;
			Rect eye_only_rectangle = new Rect((int) e.tl().x,
					(int) (e.tl().y + e.height * 0.4), (int) e.width,
					(int) (e.height * 0.6));
			mROI = mGray.submat(eye_only_rectangle);
			
			//qui c'� mRgba ________________________________
			Mat vyrez = mRgba.submat(eye_only_rectangle);
			
			//Sobel(mROI);
			//Sobel(vyrez);
			
			Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);
			//TODO: cerchio che rileva la pupilla con il recreate
			Core.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
			iris.x = mmG.minLoc.x + eye_only_rectangle.x;
			iris.y = mmG.minLoc.y + eye_only_rectangle.y;
			eye_template = new Rect((int) iris.x - size / 2, (int) iris.y
					- size / 2, size, size);
			
			
			Point center = new Point(iris.x, iris.y);
			//Point center_text = new Point(iris.x, iris.y);
			/*Core.putText(mRgba, "[X: "+iris.x+" Y: "+iris.y+" ]", center_text ,Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
					255));*/
			//gestioneDati(iris.x, iris.y);
			//TODO: rettangolo centrato sulla pupilla per il recreate
			Core.circle(mRgba, center, 10, new Scalar(255, 255, 255, 255));
			//qui ci deve essere mrgba
			/*
			Core.rectangle(mGray, eye_template.tl(), eye_template.br(),
					new Scalar(255, 0, 0, 255), 2);
			*/
			template = (mGray.submat(eye_template)).clone();
			return template;
		}
		return template;
	}
	
	public void gestioneDati(double x, double y){
		double a = (x - coordx_def);
		double b = (y - coordy_def);
		
		if ((a > 25 || a < -25) && (b > 10 || b < -10)){
			arrayX.add(a);
			arrayY.add(b);
		}
		
		
		
	}
	
	public void onRecreateClick(View v)
    {
    	learn_frames = 0;
    	coordx = (TextView) findViewById(R.id.coordx);
		coordx.setText(""+center1.x);
		coordy = (TextView) findViewById(R.id.coordy);
		coordy.setText(""+center1.y);
		
		coordx_def = center1.x;
		coordy_def = center1.y;
		arrayX.add(coordx_def);
		arrayY.add(coordy_def);
		first = false;
		Log.d("ORIGINE", coordx_def + "    " + coordy_def);
    }
	
	public void OnVisualizzaClick(View v){
		Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
		intent.putExtra(getPackageName()+".myListy", arrayY);
		intent.putExtra(getPackageName()+".myListx", arrayX);
		if(trovato) intent.putExtra(getPackageName()+".myFlag", flag);
		else intent.putExtra(getPackageName()+".myFlag", 1);
		startActivity(intent);
		
	}
	
	public void Sobel(Mat immagine){
		Mat grad_x, grad_y;
		Mat abs_x, abs_y;
		
		
		grad_x = new Mat();
		grad_y = new Mat();
		abs_x = new Mat();
		abs_y = new Mat();
		
		//int ddepth = grad_x.depth();
		int ddepth = CvType.CV_16U;
		
		Imgproc.GaussianBlur(mGray2, mGray2, new Size(3,3), 0.05, 0.05, Imgproc.BORDER_DEFAULT);
		Imgproc.cvtColor(mGray2, immagine, Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.Sobel(immagine, grad_x, ddepth, 1, 0, 3, 1, 0, Imgproc.BORDER_DEFAULT);
		Core.convertScaleAbs(grad_x, abs_x);
		
		Imgproc.Sobel(immagine, grad_y, ddepth, 0, 1, 3, 1, 0, Imgproc.BORDER_DEFAULT);
		Core.convertScaleAbs(grad_y, abs_y);
		
		Core.addWeighted(abs_x, 0.5, abs_y, 0.5, 0, immagine);
		
		//return immagine;
		
	}

}
