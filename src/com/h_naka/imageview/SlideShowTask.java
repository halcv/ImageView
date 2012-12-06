package com.h_naka.imageview;

import java.util.TimerTask;
import android.content.Context;

public class SlideShowTask extends TimerTask {
	private Context m_Parent;

	public SlideShowTask(Context parent) {
		m_Parent = parent;
	}

	@Override
	public void run() {
		((ImageViewMainActivity)m_Parent).slideShowChangeImage();
	}
}
