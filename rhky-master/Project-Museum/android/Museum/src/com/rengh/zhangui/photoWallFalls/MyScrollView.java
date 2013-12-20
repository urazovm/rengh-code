package com.rengh.zhangui.photoWallFalls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rengh.zhangui.R;
import com.rengh.zhangui.main.FileManager;
import com.rengh.zhangui.main.MainActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * �Զ����ScrollView�������ж�̬�ض�ͼƬ������ӡ�
 * 
 * @author guolin
 */
public class MyScrollView extends ScrollView implements OnTouchListener {

	/**
	 * ÿҳҪ���ص�ͼƬ����
	 */
	public static final int PAGE_SIZE = 15;

	/**
	 * ��¼��ǰ�Ѽ��ص��ڼ�ҳ
	 */
	private int page;

	/**
	 * ÿһ�еĿ��
	 */
	private int columnWidth;

	/**
	 * ��ǰ��һ�еĸ߶�
	 */
	private int firstColumnHeight;

	/**
	 * ��ǰ�ڶ��еĸ߶�
	 */
	private int secondColumnHeight;

	/**
	 * �Ƿ��Ѽ��ع�һ��layout������onLayout�еĳ�ʼ��ֻ�����һ��
	 */
	private boolean loadOnce;

	/**
	 * ��ͼƬ���й���Ĺ�����
	 */
	private ImageLoader imageLoader;

	/**
	 * ��һ�еĲ���
	 */
	private LinearLayout firstColumn;

	/**
	 * �ڶ��еĲ���
	 */
	private LinearLayout secondColumn;

	/**
	 * �����еĲ���
	 */
	private LinearLayout thirdColumn;

	/**
	 * ��¼�����������ػ�ȴ����ص�����
	 */
	private static Set<LoadImageTask> taskCollection;

	/**
	 * MyScrollView�µ�ֱ���Ӳ��֡�
	 */
	private static View scrollLayout;

	/**
	 * MyScrollView���ֵĸ߶ȡ�
	 */
	private static int scrollViewHeight;

	/**
	 * ��¼�ϴ�ֱ����Ĺ������롣
	 */
	private static int lastScrollY = -1;

	/**
	 * ��¼���н����ϵ�ͼƬ�����Կ�����ʱ���ƶ�ͼƬ���ͷš�
	 */
	private List<ImageView> imageViewList = new ArrayList<ImageView>();

	/**
	 * ��Handler�н���ͼƬ�ɼ��Լ����жϣ��Լ����ظ���ͼƬ�Ĳ�����
	 */
	private static Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			MyScrollView myScrollView = (MyScrollView) msg.obj;
			int scrollY = myScrollView.getScrollY();
			// �����ǰ�Ĺ���λ�ú��ϴ���ͬ����ʾ��ֹͣ����
			if (scrollY == lastScrollY) {
				// ����������ײ������ҵ�ǰû���������ص�����ʱ����ʼ������һҳ��ͼƬ
				if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
						&& taskCollection.isEmpty()) {
					myScrollView.loadMoreImages();
				}
				myScrollView.checkVisibility();
			} else {
				lastScrollY = scrollY;
				Message message = new Message();
				message.obj = myScrollView;
				// 5������ٴζԹ���λ�ý����ж�
				handler.sendMessageDelayed(message, 5);
			}
		};
	};

	/**
	 * MyScrollView�Ĺ��캯����
	 * 
	 * @param context
	 * @param attrs
	 */
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader = ImageLoader.getInstance();
		taskCollection = new HashSet<LoadImageTask>();
		setOnTouchListener(this);
	}

	/**
	 * ����һЩ�ؼ��Եĳ�ʼ����������ȡMyScrollView�ĸ߶ȣ��Լ��õ���һ�еĿ��ֵ���������￪ʼ���ص�һҳ��ͼƬ��
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			scrollViewHeight = getHeight();
			scrollLayout = getChildAt(0);
			firstColumn = (LinearLayout) findViewById(R.id.first_column);
			secondColumn = (LinearLayout) findViewById(R.id.second_column);
			columnWidth = firstColumn.getWidth();
			loadOnce = true;
			loadMoreImages();
		}
	}

	/**
	 * �����û��Ĵ����¼�������û���ָ�뿪��Ļ��ʼ���й�����⡣
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			Message message = new Message();
			message.obj = this;
			handler.sendMessageDelayed(message, 5);
		}
		return false;
	}

	/**
	 * ��ʼ������һҳ��ͼƬ��ÿ��ͼƬ���Ὺ��һ���첽�߳�ȥ���ء�
	 */
	public void loadMoreImages() {
		if (hasSDCard()) {
			int startIndex = page * PAGE_SIZE;
			int endIndex = page * PAGE_SIZE + PAGE_SIZE;
			String[] imageUrls = Images.init();
			if (startIndex < imageUrls.length) {
				if (endIndex > imageUrls.length) {
					endIndex = imageUrls.length;
				}
				for (int i = startIndex; i < endIndex; i++) {
					LoadImageTask task = new LoadImageTask();
					taskCollection.add(task);
					task.execute(imageUrls[i]);
				}
				page++;
			} else {
				Toast.makeText(getContext(), "û�и���ͼƬ", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(getContext(), "δ����SD��", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ����imageViewList�е�ÿ��ͼƬ����ͼƬ�Ŀɼ��Խ��м�飬���ͼƬ�Ѿ��뿪��Ļ�ɼ���Χ����ͼƬ�滻��һ�ſ�ͼ��
	 */
	public void checkVisibility() {
		for (int i = 0; i < imageViewList.size(); i++) {
			ImageView imageView = imageViewList.get(i);
			int borderTop = (Integer) imageView.getTag(R.string.border_top);
			int borderBottom = (Integer) imageView
					.getTag(R.string.border_bottom);
			if (borderBottom > getScrollY()
					&& borderTop < getScrollY() + scrollViewHeight) {
				String imageUrl = (String) imageView.getTag(R.string.image_url);
				Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					LoadImageTask task = new LoadImageTask(imageView);
					task.execute(imageUrl);
				}
			} else {
				imageView.setImageResource(R.drawable.empty_photo);
			}
		}
	}

	/**
	 * �ж��ֻ��Ƿ���SD����
	 * 
	 * @return ��SD������true��û�з���false��
	 */
	private boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * �첽����ͼƬ������
	 * 
	 * @author guolin
	 */
	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		/**
		 * ͼƬ��URL��ַ
		 */
		private String mImageUrl;

		/**
		 * ���ظ�ʹ�õ�ImageView
		 */
		private ImageView mImageView;

		public LoadImageTask() {
		}

		/**
		 * �����ظ�ʹ�õ�ImageView����
		 * 
		 * @param imageView
		 */
		public LoadImageTask(ImageView imageView) {
			mImageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			mImageUrl = params[0];
			Bitmap imageBitmap = imageLoader
					.getBitmapFromMemoryCache(mImageUrl);
			if (imageBitmap == null) {
				imageBitmap = loadImage(mImageUrl);
			}
			return imageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				double ratio = bitmap.getWidth() / (columnWidth * 1.0);
				int scaledHeight = (int) (bitmap.getHeight() / ratio);
				addImage(bitmap, columnWidth, scaledHeight);
			}
			taskCollection.remove(this);
		}

		/**
		 * ���ݴ����URL����ͼƬ���м��ء��������ͼƬ�Ѿ�������SD���У���ֱ�Ӵ�SD�����ȡ������ʹ����������ء�
		 * 
		 * @param imageUrl
		 *            ͼƬ��URL��ַ
		 * @return ���ص��ڴ��ͼƬ��
		 */
		private Bitmap loadImage(String imageUrl) {
			File imageFile = new File(getImagePath(imageUrl));
			if (!imageFile.exists()) {
				// downloadImage(imageUrl);
			}
			if (imageUrl != null) {
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
					return bitmap;
				}
			}
			return null;
		}

		/**
		 * ��ImageView�����һ��ͼƬ
		 * 
		 * @param bitmap
		 *            ����ӵ�ͼƬ
		 * @param imageWidth
		 *            ͼƬ�Ŀ��
		 * @param imageHeight
		 *            ͼƬ�ĸ߶�
		 */
		private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					imageWidth, imageHeight);
			if (mImageView != null) {
				mImageView.setImageBitmap(bitmap);
			} else {
				ImageView imageView = new ImageView(getContext());
				imageView.setLayoutParams(params);
				if (MainActivity.delStatus == true) {
					bitmap = setAlpha(bitmap, 80);
					imageView.setBackgroundColor(0x88333333);
				}
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setTag(R.string.image_url, mImageUrl);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// �õ������ͼƬ������Ŀ¼��
						String[] path = getImagePath(mImageUrl).split("/");
						if (MainActivity.delStatus == true) {
							MainActivity.delName = path[4];
							FileManager.resetFile(MainActivity.myFileName);
							MainActivity.del = "OK";
						} else {
							MainActivity.deviceName = path[4];
							MainActivity.collectClickStatus = true;
						}
					}
				});
				LinearLayout nowLayout = findColumnToAdd(imageView, imageHeight);
				nowLayout.addView(imageView);

				String name = getDeviceName(mImageUrl);

				TextView title = new TextView(getContext());
				title.setText(name);
				title.setTextSize(16);
				title.setGravity(Gravity.LEFT);
				title.setTextColor(0xff000000);
				nowLayout.addView(title);

				TextView date = new TextView(getContext());
				date.setText(getZipCreateDate());
				date.setTextSize(12);
				date.setGravity(Gravity.RIGHT);
				date.setTextColor(0x88000000);
				nowLayout.addView(date);

				imageViewList.add(imageView);
			}
		}

		/**
		 * �ҵ���ʱӦ�����ͼƬ��һ�С�ԭ����Ƕ����еĸ߶Ƚ����жϣ���ǰ�߶���С��һ�о���Ӧ����ӵ�һ�С�
		 * 
		 * @param imageView
		 * @param imageHeight
		 * @return Ӧ�����ͼƬ��һ��
		 */
		private LinearLayout findColumnToAdd(ImageView imageView,
				int imageHeight) {
			if (firstColumnHeight <= secondColumnHeight) {
				imageView.setTag(R.string.border_top, firstColumnHeight);
				firstColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, firstColumnHeight);
				return firstColumn;
			} else {
				imageView.setTag(R.string.border_top, secondColumnHeight);
				secondColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, secondColumnHeight);
				return secondColumn;
			}
		}

		/**
		 * ��ͼƬ���ص�SD������������
		 * 
		 * @param imageUrl
		 *            ͼƬ��URL��ַ��
		 */
		private void downloadImage(String imageUrl) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Log.d("TAG", "monted sdcard");
			} else {
				Log.d("TAG", "has no sdcard");
			}
			HttpURLConnection con = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			BufferedInputStream bis = null;
			File imageFile = null;
			try {
				URL url = new URL(imageUrl);
				con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(5 * 1000);
				con.setReadTimeout(15 * 1000);
				con.setDoInput(true);
				con.setDoOutput(true);
				bis = new BufferedInputStream(con.getInputStream());
				imageFile = new File(getImagePath(imageUrl));
				fos = new FileOutputStream(imageFile);
				bos = new BufferedOutputStream(fos);
				byte[] b = new byte[1024];
				int length;
				while ((length = bis.read(b)) != -1) {
					bos.write(b, 0, length);
					bos.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (bis != null) {
						bis.close();
					}
					if (bos != null) {
						bos.close();
					}
					if (con != null) {
						con.disconnect();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (imageFile != null) {
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
				}
			}
		}

		/**
		 * ��ȡͼƬ�ı��ش洢·����
		 * 
		 * @param imageUrl
		 *            ͼƬ��URL��ַ��
		 * @return ͼƬ�ı��ش洢·����
		 */
		private String getImagePath(String imageUrl) {
			String imagePath = MainActivity.appDir + imageUrl;
			return imagePath;
		}

		/**
		 * ��ȡͼƬ����Ŀ¼��zip���ϰ�������ʱ�䣻
		 * 
		 * @return ת��Ϊ�ַ������͵�ʱ��
		 */
		private String getZipCreateDate() {
			String zipName = getPicPathName(mImageUrl);
			String filePath = MainActivity.appDir + zipName + "/" + zipName
					+ ".zip";
			File file = new File(filePath);
			Date date = new Date(file.lastModified());
			String fileDate = new java.text.SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss").format(date);
			return fileDate;
		}

		/**
		 * �õ���ǰͼƬ����Ŀ¼
		 * 
		 * @param ��ǰͼƬ·��
		 * @return ͼƬ����Ŀ¼����
		 */
		private String getPicPathName(String imageUrl) {
			int slashIndex = imageUrl.indexOf("/");
			String picPathName = imageUrl.substring(0, slashIndex);
			return picPathName;
		}

		/**
		 * ��ȡͼƬ����Ŀ¼�µ�name.txt���ݣ�����չƷ���ƣ�
		 * 
		 * @param ��ǰʹ�õ�ͼƬ
		 * @return չƷ����
		 */
		private String getDeviceName(String mImageUrl) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String zipName = getPicPathName(mImageUrl);
				String filePath = MainActivity.appDir + zipName + "/name.txt";
				File file = new File(filePath);// ���·��
				if (file.exists()) {
					int num = 0;
					Long filelength = file.length();
					byte[] filecontent = new byte[filelength.intValue()];
					try {
						FileInputStream in = new FileInputStream(file);
						in.read(filecontent);
						in.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
					try {
						return new String(filecontent, "GBK");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
			return null;
		}
	}

	/**
	 * @param sourceImg
	 *            Ҫ�����ͼƬ
	 * @param number
	 *            ���õ�͸��ֵ 1~100֮���
	 * @return ����������ɵ�ͼƬ
	 */
	public static Bitmap setAlpha(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,
				sourceImg.getWidth(), sourceImg.getHeight());// ���ͼƬ��ARGBֵ
		number = number * 255 / 100;
		for (int i = 0; i < argb.length; i++) {
			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
		}
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(),
				sourceImg.getHeight(), Config.ARGB_8888);

		return sourceImg;
	}
}