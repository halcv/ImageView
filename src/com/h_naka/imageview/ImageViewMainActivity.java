package com.h_naka.imageview;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
//import android.util.Log;
import android.content.SharedPreferences;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

public class ImageViewMainActivity
	extends Activity
	implements OnClickListener {

	private final String PICTURES_DIRECTORY = "/sdcard/Pictures";
	private final String REGEXP = ".*jpg|.*JPG";
	private ArrayList<String> m_FileList;
	private Button m_btnPre;
	private Button m_btnFore;
	private Button m_btnUpdate;
	private Button m_btnSlide;
	private ImageView m_vImage;
	private int m_iImageCnt = 0;
	private int m_iImageMax = 0;
	private boolean m_bSlideStart = false;
	private Timer m_Timer = null;
	private SlideShowTask m_SlideShowTask = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		initChild();
		m_FileList = searchFile(PICTURES_DIRECTORY,REGEXP,true);
		m_iImageMax = m_FileList.size();
		m_iImageCnt = getListPosition();
		if (m_iImageCnt == -1) {
			m_iImageCnt = 0;
		} else if (m_iImageCnt > m_iImageMax) {
			m_iImageCnt = 0;
		}
		
		changeButtonVisible();
		m_Timer = new Timer(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (m_SlideShowTask != null) {
			m_SlideShowTask.cancel();
			m_SlideShowTask = null;
		}
		m_Timer.cancel();
		m_Timer = null;
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.prebutton:
			preButtonClicked();
			break;
		case R.id.forebutton:
			foreButtonClicked();
			break;
		case R.id.updatebutton:
			updateButtonClicked();
			break;
		case R.id.slidebutton:
			slideButtonClicked();
			break;
		}
	}

	private void initChild() {
		m_btnPre = (Button)findViewById(R.id.prebutton);
		m_btnPre.setOnClickListener(this);

		m_btnFore = (Button)findViewById(R.id.forebutton);
		m_btnFore.setOnClickListener(this);

		m_btnUpdate = (Button)findViewById(R.id.updatebutton);
		m_btnUpdate.setOnClickListener(this);

		m_btnSlide = (Button)findViewById(R.id.slidebutton);
		m_btnSlide.setOnClickListener(this);
		
		m_vImage = (ImageView)findViewById(R.id.image);
	}
	
	private void updateButtonClicked() {
		m_FileList = searchFile(PICTURES_DIRECTORY,REGEXP,true);
		m_iImageMax = m_FileList.size();
		m_iImageCnt = 0;
		saveListPostion(m_iImageCnt);
		changeButtonVisible();
	}

	private void preButtonClicked() {
		m_iImageCnt--;
		if (m_iImageCnt < 0) {
			m_iImageCnt = 0;
		}

		changeButtonVisible();
	}

	private void foreButtonClicked() {
		m_iImageCnt++;
		if (m_iImageCnt >= m_iImageMax) {
			if (m_bSlideStart) {
				m_iImageCnt = 0;
			} else {
				m_iImageCnt = m_iImageMax - 1;
			}
		}

		changeButtonVisible();
	}

	private void slideButtonClicked() {
		if (m_bSlideStart) {
			m_bSlideStart = false;
			slideShowStopChangeButtonVisible();
			slideShowStop();
			changeButtonVisible();
		} else {
			m_bSlideStart = true;
			slideShowStartChangeButtonVisible();
			slideShowStart();
		}
	}

	private void slideShowStart() {
		if (m_SlideShowTask == null) {
			m_SlideShowTask = new SlideShowTask(this);
		}
		m_Timer.schedule(m_SlideShowTask,0,10000);
	}

	private void slideShowStop() {
		if (m_SlideShowTask != null) {
			m_SlideShowTask.cancel();
			m_SlideShowTask = null;
		}
	}
	
	private void slideShowStartChangeButtonVisible() {
		m_btnPre.setVisibility(View.GONE);
		m_btnFore.setVisibility(View.GONE);
		m_btnUpdate.setVisibility(View.GONE);
		m_btnSlide.setText(R.string.slideStop_text);
	}

	private void slideShowStopChangeButtonVisible() {
		m_btnPre.setVisibility(View.VISIBLE);
		m_btnFore.setVisibility(View.VISIBLE);
		m_btnUpdate.setVisibility(View.VISIBLE);
		m_btnSlide.setText(R.string.slideStart_text);
	}
	
	private void changeButtonVisible() {
		if (!m_bSlideStart) {
			if (m_iImageMax <= 1) {
				m_btnPre.setVisibility(View.GONE);
				m_btnFore.setVisibility(View.GONE);
			} else if (m_iImageCnt == (m_iImageMax - 1)) {
				m_btnFore.setVisibility(View.GONE);
				m_btnPre.setVisibility(View.VISIBLE);
			} else if (m_iImageCnt == 0) {
				m_btnPre.setVisibility(View.GONE);
				m_btnFore.setVisibility(View.VISIBLE);
			} else {
				m_btnFore.setVisibility(View.VISIBLE);
				m_btnPre.setVisibility(View.VISIBLE);
			}
		}

		drawImage();
	}
	
	private ArrayList<String> searchFile(String dir_path,String expr,boolean search_subdir) {
		final File dir = new File(dir_path);
		ArrayList<String> fileList = new ArrayList<String>();
		
		final File[] files = dir.listFiles();
		if(null != files){
			for(int i = 0; i < files.length; ++i) {
				if(!files[i].isFile()){
					if(search_subdir){
						ArrayList<String> sub_files = searchFile(files[i].getPath(), expr, search_subdir);
						fileList.addAll(sub_files);
					}
					continue;
				}
				
				final String filename = files[i].getName();
				if((null == expr) || filename.matches(expr)){
					fileList.add(dir.getPath() + "/" + filename);
				}
			}
		}

		return fileList;
	}

	private void drawImage() {
		if (m_iImageMax == 0) {
			return;
		}

		saveListPostion(m_iImageCnt);
		String filepath = m_FileList.get(m_iImageCnt);
		File f = new File(filepath);
		BitmapFactory.Options bmpOp = new BitmapFactory.Options();
		Bitmap image = null;
		image = BitmapFactory.decodeFile(f.getPath(), bmpOp);
		if (image == null) {
			String str = filepath + "のデコードに失敗しました。";
			Toast.makeText(this,str,Toast.LENGTH_LONG).show();
		} else {
			m_vImage.setImageBitmap(image);
		}
	}

	private int getListPosition() {
		SharedPreferences sp = getSharedPreferences("ImageView",MODE_PRIVATE);
		return sp.getInt("ListPosition",-1);
	}

	private void saveListPostion(int position) {
		SharedPreferences sp = getSharedPreferences("ImageView",MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("ListPosition",position);
		editor.commit();
	}

	public void slideShowChangeImage() {
		runOnUiThread(new Runnable() {
			public void run() {
				foreButtonClicked();
			}
		});
	}
}
