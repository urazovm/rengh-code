package com.rengh.zhangui.main;

import com.deaux.fan.FanView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipFile;

import com.rengh.zhangui.R;
import com.rengh.zhangui.R.layout;
import com.rengh.zhangui.slidingmenu.SlidingMenu;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.LabeledIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		OnTouchListener, OnPageChangeListener {
	SlidingMenu mSlidingMenu;
	/* ���水ť�� */
	private ImageButton btn_menu_list, btn_delete, btn_search, btn_more_info;
	/* NFC��أ� */
	private NfcAdapter mNfcAdapter;
	private IntentFilter[] intentFiltersArray;
	private PendingIntent pendingIntent;
	private String[][] techListsArray;
	/*
	 * nfcStr�����NFC��ǩ��ȡ�����ַ�����fenGeFu�Ƿָ�nfcStr�ķָ����� ipAdress, deviceName, port,
	 * webType, str[], appDir, mSSID, mPASSWORD, strWifi =
	 * "AutoConnectWifi"���ֱ����豸 IP��ַ���豸���ƣ��˿ںţ���ҳ���ͣ��ָ���ַ����ı������飬Ӧ�ó����ļ�Ŀ¼��
	 * ����SSID���������룬ʶ���Ƿ�Ϊ��Ӧ�õ�ͷ�ַ�����
	 */
	public static String nfcStr = null, fenGeFu = "\\*", ipAdress, deviceName,
			port, str[], appDir = null, mSSID, mPASSWORD,
			strWifi = "AutoConnectWifi";
	private int isWifiEnableStatue = 0, isWifiConnectedStatue = 0, nfcState,
			mTime = 3000, mTYPE;
	/*
	 * WebView��أ� savedFileName, savedFileDir, downloadAdress, loadPath;
	 * �ļ����浽���ص����ƣ�����Ŀ¼�����ص�ַ�����ص�ַ�� bar_download;�������� mDownStatus;����״̬��
	 */
	private String savedFileName, savedFileDir, downloadAdress, loadPath;
	private ProgressBar bar_download;
	private Handler handler;
	private int mDownStatus;
	private DownloadFile downloadFile;
	private WebView webView;
	/* api;΢��SDK��api���� APP_ID;���뵽��΢��APP_ID��û������޷�����΢�ţ� */
	private IWXAPI api;
	private static final String APP_ID = "wx37539758487b3c98";
	// ��ʾ������ı������
	private TextView textView;
	// ������������
	private FanView fanView;
	// ����һ����ʼʱ�䣬һ�����ʱ�䣻�ж��Ƿ�Ҫ���ò˵�����
	long startTime, checkTime;

	private Boolean cancleConnect = false, presentStatus = false,
			isExit = false;
	public static Boolean collectClickStatus = false, delStatus = false;

	public static String SDCardDir, myFileName, delName, del;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʾ�������棻
		start_SplashLayout();
		// ��ȡ��ʼʱ�䣬�����ж��Ƿ����ò˵������ܣ�����������������ʾʱ���˵����������
		startTime = System.currentTimeMillis();
		/* ���ݵ�ǰ�豸��SD��Ŀ¼ָ�������ļ����Ŀ¼appDir�� */
		SDCardDir = Environment.getExternalStorageDirectory().toString() + "/";
		appDir = SDCardDir + "ZhanGuiCollection/";
		myFileName = appDir + "myCollected";
		File file = new File(appDir);
		file.mkdirs();

		/* NFC���; */
		nfcSetting();

		/* Ĭ��wifiʹ��״������������ж��û���������NFC��ǩָ��wifi���磻 */
		if (isWifiEnabled()) {
			isWifiEnableStatue = -1;
		}
		if (isWifiConnect()) {
			isWifiConnectedStatue = -1;
		}

		/* Ĭ��NFCʹ��״�����ж���δ��������ˢ��NFC���ǿ�����ǰ̨���ȣ� */
		nfcState = 0;
	}

	/**
	 * ��ÿ���������û��������
	 * 
	 * @author RenGH
	 */
	/**
	 * ���ؿ�ʼ���沼�֣�������ΪmTime�������ʧ��
	 */
	private void start_SplashLayout() {
		setContentView(R.layout.layout_splash);

		/* ��������� */
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.gradually);
		ImageView icon_welcom = (ImageView) findViewById(R.id.icon_welcom);
		icon_welcom.startAnimation(animation);

		Handler x = new Handler();
		x.postDelayed(new layout_splash(), mTime);
	}

	/**
	 * ���� ���� ���棬�����涨ʱ���ִ��ָ��������
	 * 
	 * @author Administrator
	 */
	class layout_splash implements Runnable {
		public void run() {
			/*
			 * ��nfcState������0ʱ������NFC��ǩ��Ϣ���룬�Ͳ������ղؽ��棬
			 * ��ֹ�������������ʱ���ڽ���չƷ����ҳ�棬ʱ�䵽�˺��ַ��ص��ղؽ��棻
			 */
			if (nfcState == 0) {
				start_collectionLayout();
			}
		}
	}

	/**
	 * ���� �ղ� ���沼��;
	 */
	private void start_collectionLayout() {
		presentStatus = false;
		setContentView(R.layout.layout_fan_view);
		fanView = (FanView) findViewById(R.id.fan_view);
		fanView.setLeftMenuViews(R.layout.layout_collection,
				R.layout.layout_left_menu);
		btn_menu_list_setting();
		btn_delete_seting();

		// ����һ��Handler����
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x01) {
					start_presentLayout();
				}
				if (msg.what == 0x02) {
					start_collectionLayout();
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				while (true) {
					if (appDir != null) {
						if (collectClickStatus) {
							collectClickStatus = false;
							handler.sendEmptyMessage(0x01);
							return;
						}
						if (del != null && del.equals("OK")) {
							del = null;
							delName = null;
							handler.sendEmptyMessage(0x02);
							return;
						}
					}
					try {
						sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * ���� չ�ݵ�ͼ ���沼��
	 */
	private void start_mapLayout() {
		presentStatus = false;
		delStatus = false;
		setContentView(R.layout.layout_fan_view);
		fanView = (FanView) findViewById(R.id.fan_view);
		fanView.setLeftMenuViews(R.layout.layout_map, R.layout.layout_left_menu);
		btn_menu_list_setting();
		btn_search_setting();
		checkDownload();
	}

	/**
	 * ���� ��� ���沼��
	 */
	public void start_presentLayout() {
		presentStatus = true;
		delStatus = false;
		/*
		 * setContentView(R.layout.layout_fan_view); fanView = (FanView)
		 * findViewById(R.id.fan_view);
		 * fanView.setLeftMenuViews(R.layout.layout_present,
		 * R.layout.layout_left_menu); btn_menu_list_setting();
		 * btn_more_info_setting();
		 */
		setContentView(R.layout.main);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int width = dm.widthPixels < dm.heightPixels ? dm.widthPixels
				: dm.heightPixels;

		mSlidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
		mSlidingMenu.setAlignScreenWidth((width / 20) * 11);

		View leftView = getLayoutInflater().inflate(R.layout.layout_left_menu,
				null);
		View rightView = getLayoutInflater().inflate(
				R.layout.layout_right_menu, null);
		View centerView = getLayoutInflater().inflate(R.layout.layout_present,
				null);

		mSlidingMenu.setLeftView(leftView);
		mSlidingMenu.setRightView(rightView);
		mSlidingMenu.setCenterView(centerView);

		ImageButton btn_menu_list = (ImageButton) centerView
				.findViewById(R.id.btn_menu_list);
		btn_menu_list.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mSlidingMenu.showLeftView();
			}
		});
		ImageButton btn_more_info = (ImageButton) centerView
				.findViewById(R.id.btn_more_info);
		btn_more_info.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mSlidingMenu.showRightView();
			}
		});

		btn_menu_list.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_list));
				}
				return false;
			}
		});

		btn_more_info.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_more_info));
				}
				return false;
			}
		});
		initView();
		initData();
	}

	/**
	 * ���� ��ʾ ���沼��
	 */
	private void start_promptLayout() {
		presentStatus = false;
		delStatus = false;
		setContentView(R.layout.layout_fan_view);
		fanView = (FanView) findViewById(R.id.fan_view);
		fanView.setLeftMenuViews(R.layout.layout_prompt,
				R.layout.layout_left_menu);
		btn_menu_list_setting();
		btn_promptInfo_setting();
		ImageButton btn_cancleConnect = (ImageButton) findViewById(R.id.btn_cancleConnect);
		btn_cancleConnect.setOnClickListener(this);
	}

	/**
	 * ����btn_menu_list��ť��������¼���
	 */
	private void btn_menu_list_setting() {
		// ���ð�ť�����Ͱ����벻���·ֱ���ʾ��ͼƬ��
		btn_menu_list = (ImageButton) findViewById(R.id.btn_menu_list);
		btn_menu_list.setOnClickListener(this);
		btn_menu_list.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_list));
				}
				return false;
			}
		});
	}

	/**
	 * ����btn_delete��ť��������¼���
	 */
	private void btn_delete_seting() {
		btn_delete = (ImageButton) findViewById(R.id.btn_delete);
		btn_delete.setOnClickListener(this);
		btn_delete.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_delete));
				}
				return false;
			}
		});
	}

	/**
	 * ����btn_search��ť��������¼���
	 */
	private void btn_search_setting() {
		// ���ð�ť�����Ͱ����벻���·ֱ���ʾ��ͼƬ��
		btn_search = (ImageButton) findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);
		btn_search.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_search));
				}
				return false;
			}
		});
	}

	/**
	 * ���ð�ť��������¼���
	 */
	private void btn_more_info_setting() {
		// ���ð�ť�����Ͱ����벻���·ֱ���ʾ��ͼƬ��
		btn_more_info = (ImageButton) findViewById(R.id.btn_more_info);
		btn_more_info.setOnClickListener(this);
		btn_more_info.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// �������ð���ʱ�ı���ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// ���޸�Ϊ̧��ʱ������ͼƬ
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_more_info));
				}
				return false;
			}
		});
	}

	/**
	 * �������Ŀ¼�� ���ж�Ӧ������ʱ������Ӧ��ͼ�껻�������ص�ͼ�ꣻ
	 */
	private void checkDownload() {
		File file = new File(appDir + "HeHeErXian/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView1);
		}
		file = new File(appDir + "002/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView2);
		}
		file = new File(appDir + "003/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView3);
		}
		file = new File(appDir + "004/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView4);
		}
		file = new File(appDir + "005/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView5);
		}
		file = new File(appDir + "006/index.jpg");
		if (file.exists()) {
			setHistory(R.id.imageView6);
		}
	}

	/**
	 * ��ָ��ID�İ�ť����Ϊ��ʾ�����ص�ͼƬ��
	 * 
	 * @param id
	 */
	public void setHistory(int id) {
		ImageView imageView = (ImageView) findViewById(id);
		imageView.setBackgroundResource(R.drawable.btn_map_history);
		imageView.setOnClickListener(this);
	}

	/**
	 * ��ʾ��Ϣ���ı����������
	 */
	private void btn_promptInfo_setting() {
		textView = (TextView) findViewById(R.id.textView_promptInfo);
		textView.setText("��������...");
	}

	/**
	 * NFC������ü�������
	 */
	/**
	 * NFC��ǩ��ȡ���������;
	 */
	protected void nfcSetting() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "���豸��֧��NFC���ܣ�", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
			Toast.makeText(this, "���ȿ����豸��NFC���ܣ�", Toast.LENGTH_SHORT).show();
			startActivity(new Intent("android.settings.NFC_SETTINGS"));
		}
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		intentFiltersArray = new IntentFilter[] { ndef, };
		techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
	}

	/**
	 * ����NFCǰ̨���ȣ������ⲿ��NDEF��Ϣ��Intent�����¼�����onResume�������ã�
	 */
	private void nfcOnResume() {
		/* ����nfcǰ̨���ȣ���֤������״̬�����ȴ���nfc��������Ϣ�� */
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
					intentFiltersArray, techListsArray);
		}
		/* �ܹ���intent���ⲿ���ݵ����� */
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())
				&& (nfcState == 0)) {
			// ����ndef��Ϣ�Ĵ�������
			processIntent(getIntent());
		}
	}

	/**
	 * �����ڲ���ջ��ʱ������ǰ̨���ȣ���onPause()�����ڵ��ã�
	 */
	public void nfcOnPause() {
		if (mNfcAdapter != null) {
			/* ��ʱ�ر�ǰ̨���ã�������Դ��ռ�ã� */
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}

	/**
	 * ����ndef��Ϣ�Ĵ������� �õ���ȡ���ַ�����
	 */
	private void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		/* ��ȡNdefMessage��Ϣ������ȥ���ַ��������пո�󸳸��ַ���nfcStr�� */
		nfcStr = new String(msg.getRecords()[0].getPayload()).replaceAll("\\s",
				"");
		processNfcString(); // ����ȡ�����ַ����ָ�Ϊ�������������
		start_promptLayout();
		checkWifiConected();
	}

	/**
	 * Wifi����������Ϣ����������������
	 * 
	 * @return
	 */
	/**
	 * �����NFC��ǩ��ȡ�����ַ�����
	 * 
	 * @return
	 */
	public Boolean processNfcString() {
		/*
		 * ����������Ϊ��������������, ������������, ���߼��ܷ�ʽ���豸���ƣ��豸IP���豸�˿ںţ�
		 * ��nfcStr�ַ����ֽ�Ϊ����ַ����浽str�ַ��������
		 */
		str = nfcStr.split(fenGeFu);
		if (!str[0].equals(strWifi)) {
			Toast.makeText(this, "�ñ�ǩ�����뱾���򲻷�������ϵ����Ա��", Toast.LENGTH_SHORT)
					.show();
			nfcStr = null;
			return false;
		}
		mSSID = str[1];
		mPASSWORD = str[2];
		mTYPE = Integer.parseInt(str[3]);
		deviceName = str[4];
		ipAdress = str[5];
		port = str[6];
		++nfcState;
		return true;
	}

	/**
	 * ��������Ƿ��Ѿ����ӵ�ָ�������磬�����������ֱ���������ϲ���ʾ��
	 */
	private void checkWifiConected() {
		WifiConfig myWifi = new WifiConfig(this);
		if (!isWifiConnect() || !myWifi.getSSID().equals(mSSID)) {
			newThreadAutoWifi(); // �����߳�������wifi����
		} else {
			Boolean exists = checkFileIsSaved();
			if (exists) {
				start_presentLayout();
			} else {
				downloadAndOpen();
			}
		}
	}

	/**
	 * ���Ҫ���ص��ļ��Ƿ��Ѿ����ڣ�
	 * 
	 * @return
	 */
	public Boolean checkFileIsSaved() {
		File file = new File(appDir + deviceName + "/" + deviceName + ".zip");
		if (file.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * ����һ���߳�����wifi���磬���ӳɹ������չƷ���ܲ��֣�Ȼ�������ļ������ص�webView��
	 */
	public void newThreadAutoWifi() {
		textView.setText("�����豸...");
		// ����һ��Handler����
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x20) {
					textView.setText("���ӳɹ�����ʼ��������...");
					Boolean exists = checkFileIsSaved();
					if (exists) {
						start_presentLayout();
					} else {
						downloadAndOpen();
					}
				}
				if (msg.what == 0x21) {
					textView.setText("��������wifi...");
				}
				if (msg.what == 0x22) {
					textView.setText("������������...");
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				if (AutoWifi() == true) {// ����wifi���磻
					handler.sendEmptyMessage(0x20);
				} else {
					return;
				}
			}
		}.start();
	}

	/**
	 * ���ݴ��ݹ������������������������wifi���磻
	 */
	private Boolean AutoWifi() {
		/*
		 * �������󣬴�wifi���ܣ��ȵ�wifi������ɺ󽫴�������wifi������ӽ�Network��
		 * Ȼ��ȴ��������ɹ��󣬴����豸���ƣ��豸IP���豸�˿ںŸ�connectedSocketServer������
		 * ��������Զ��Socket��������Integer.parseInt(str[3])
		 * ��Integer.valueOf(str[5])���ǽ��ַ���ת��Ϊ���ͣ�
		 */
		/*
		 * ����AutoWifiConfig����ͨ���ö����wifi���в����� WifiConfig myWifi = new
		 * WifiConfig(this); ��������ȫ�֣���Ȼ�����ˢnfc����wifi�����ӵ�socket����ˢnfcʱ�������������
		 */
		WifiConfig myWifi = new WifiConfig(this);
		Boolean b;
		if (!isWifiEnabled()) {
			isWifiEnableStatue = 1;
			myWifi.openWifi();
			handler.sendEmptyMessage(0x21);
			do {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (cancleConnect == true) {
					myWifi.closeWifi();
					cancleConnect = false;
					return false;
				}
				b = isWifiEnabled();
			} while (!b);
		}
		if (!isWifiConnect() || !myWifi.getSSID().equals(mSSID)) {
			myWifi.addNetwork(myWifi.CreateWifiInfo(mSSID, mPASSWORD, mTYPE));
			handler.sendEmptyMessage(0x22);
			do {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (cancleConnect == true) {
					myWifi.disconnectWifi();
					cancleConnect = false;
					return false;
				}
				b = isWifiConnect();
			} while (!b);
			isWifiConnectedStatue = 1;
		}
		return true;
	}

	/**
	 * ���wifi�Ƿ���ã����򷵻�true��
	 */
	private boolean isWifiEnabled() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isAvailable();
	}

	/**
	 * ���wifi�Ƿ����ӳɹ����ɹ��򷵻�true��
	 */
	private boolean isWifiConnect() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	/**
	 * �������Ͻ�ѹ������Ŀ¼;
	 */
	private void downloadAndOpen() {
		savedFileName = appDir + deviceName + "/" + deviceName + ".zip";
		savedFileDir = appDir + deviceName + "/";
		downloadAdress = "http://" + ipAdress + ":" + port + "/download/"
				+ deviceName + ".zip";
		loadPath = "file://" + appDir + deviceName; // ���ϵĲ���Ŀ¼��
		// �����ļ���
		File file = new File(appDir + deviceName);
		file.mkdirs();

		bar_download = (ProgressBar) findViewById(R.id.bar_download);

		// ����һ��Handler����
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x12) {
					textView.setText("������..." + String.valueOf(mDownStatus)
							+ "%");
					bar_download.setProgress(mDownStatus);
				}
				if (msg.what == 0x11) {
					textView.setText("����ʧ�ܣ�");
					deleteDir(new File(savedFileDir));
				}
				if (msg.what == 0x10) {
					textView.setText("���ڽ�ѹ�����Ե�...");
					Handler x = new Handler();
					x.postDelayed(new unZipAndOpen(), 200);
				}
			}
		};

		// ��ʼ��DownloadFile����
		downloadFile = new DownloadFile(downloadAdress, savedFileName);
		new Thread() {
			@Override
			public void run() {
				try {
					// ��ʼ����
					downloadFile.download();
				} catch (Exception e) {
					handler.sendEmptyMessage(0x11);
					e.printStackTrace();
					return;
				}
				// ����ÿ����Ȼ�ȡһ��ϵͳ����ɽ���
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (cancleConnect == true) {
							handler.sendEmptyMessage(0x11);
							cancleConnect = false;
							return; // ȡ����
						}
						// ��ȡ�����������ɱ���
						double completeRate = downloadFile.getCompleteRate();
						mDownStatus = (int) (completeRate * 100);
						// ������Ϣ֪ͨ������½�����
						handler.sendEmptyMessage(0x12);
						// ������ȫ��ȡ���������
						if (mDownStatus >= 100) {
							handler.sendEmptyMessage(0x10);
							timer.cancel();
							return;
						}
					}
				}, 0, 100);
			}
		}.start();
	}

	/**
	 * ��ѹ����
	 * 
	 * @author Administrator
	 */
	class unZipAndOpen implements Runnable {
		public void run() {
			try {
				ZipFileUtil.unZipFile(savedFileName, savedFileDir);
			} catch (Exception e) {
				textView.setText("��ѹʧ�ܣ�");
				e.printStackTrace();
				return;
			}
			start_presentLayout();
		}
	}

	/**
	 * �ݹ�ɾ��Ŀ¼�µ������ļ�����Ŀ¼�������ļ�
	 * 
	 * @param dir
	 *            ��Ҫɾ�����ļ�Ŀ¼
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// �ݹ�ɾ��Ŀ¼�е���Ŀ¼��
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// Ŀ¼��ʱΪ�գ�����ɾ��
		return dir.delete();
	}

	/**
	 * ����΢�ŵ���ط���ʵ�֣�
	 */
	/**
	 * ����Ӧ��ע���΢�ţ�
	 */
	private void regToWx() {
		api = WXAPIFactory.createWXAPI(this, APP_ID, true);
		api.registerApp(APP_ID);
	}

	/**
	 * ����΢�����촰�ڵķ�����
	 */
	private void sendToTalk() {
		WXTextObject textObj = new WXTextObject();
		textObj.text = "΢�ŷ���������ӡ� --text����չ��Ӧ��";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = "΢�ŷ���������ӡ� --msg����չ��Ӧ��";

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		IWXAPI iwxapi = null;
		req.scene = SendMessageToWX.Req.WXSceneSession;

		api.sendReq(req);
	}

	/**
	 * ����΢������Ȧ�ķ�����
	 */
	private void sendToFriend() {
		WXTextObject textObj = new WXTextObject();
		textObj.text = "΢�ŷ���������ӡ� --text����չ��Ӧ��";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = "΢�ŷ���������ӡ� --msg����չ��Ӧ��";

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		IWXAPI iwxapi = null;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;

		api.sendReq(req);
	}

	/**
	 * ����д����ķ���������������أ�
	 */
	/* ��ǰActivityλ��ջ�������µ�intentʱ���ã� */
	@Override
	public void onNewIntent(Intent intent) {
		/* ����ndef��Ϣ�Ĵ������� */
		processIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		nfcOnResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		nfcOnPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main_layout, menu);
		return true;
	}

	/**
	 * ʵ�ְ�ť�������������
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_delete:
			// FileManager.clearFile(appDir + "myCollected");
			if(delStatus)
				delStatus = false;
			else
				delStatus = true;
			start_collectionLayout();
			break;
		case R.id.btn_menu_list:
			fanView.showLeftMenu();
			break;
		case R.id.btn_search:
			Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btn_cancleConnect:
			cancleConnect = true;
			start_mapLayout();
			break;
		case R.id.imageView1:
			deviceName = "HeHeErXian";
			start_presentLayout();
			break;
		case R.id.imageView2:
			deviceName = "002";
			start_presentLayout();
			break;
		case R.id.imageView3:
			deviceName = "003";
			start_presentLayout();
			break;
		case R.id.imageView4:
			deviceName = "004";
			start_presentLayout();
			break;
		case R.id.imageView5:
			deviceName = "005";
			start_presentLayout();
			break;
		case R.id.imageView6:
			deviceName = "006";
			start_presentLayout();
			break;
		}
	}

	/**
	 * ��������ť�ĵ���¼���
	 * 
	 * @param v
	 */
	/**
	 * �жϵ�ǰҳ���Ƿ��Ǽ��ҳ�棬����ǣ������mSlidingMenu.showLeftView();
	 * ���������ҳ�棬�����fanView.showLeftMenu();
	 */
	private int showLeftMenu() {
		int hTime = 0; // ����ִ�е�ʱ��
		if (presentStatus == false) {
			fanView.showLeftMenu();
			hTime = 570;
		} else {
			hTime = 300;
			mSlidingMenu.showLeftView();
		}
		return hTime;
	}

	/**
	 * �ҵ��ղذ�ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuMyCollection(View v) {
		int hTime = showLeftMenu();
		Handler x = new Handler();
		x.postDelayed(new btnCollection_hideLeftMenu(), hTime);
	}

	class btnCollection_hideLeftMenu implements Runnable {
		public void run() {
			start_collectionLayout();
		}
	}

	/**
	 * չ����ͼ��ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuDisplayMap(View v) {
		int hTime = showLeftMenu();
		Handler x = new Handler();
		x.postDelayed(new btnMap_hideLeftMenu(), hTime);
	}

	class btnMap_hideLeftMenu implements Runnable {
		public void run() {
			start_mapLayout();
		}
	}

	/**
	 * �˻�����ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuUsersManager(View v) {
		Toast.makeText(getApplicationContext(), "�ù�����δ��ӣ�", Toast.LENGTH_LONG)
				.show();
		int hTime = showLeftMenu();
		Handler x = new Handler();
		x.postDelayed(new btnUsersManager_hideLeftMenu(), hTime);
	}

	class btnUsersManager_hideLeftMenu implements Runnable {
		public void run() {
			// �ڴ˴������˻��������
		}
	}

	/**
	 * ��������ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuShareTo(View v) {
	}

	/**
	 * �ղذ�ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuCollected(View v) {;
		FileManager.appendMethod(deviceName, myFileName);
		Toast.makeText(getApplicationContext(), "���ղأ�", Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * չƷλ�ð�ť��ʵ�֣�
	 * 
	 * @param v
	 */
	public void menuWhere(View v) {
		Toast.makeText(getApplicationContext(), "�ù�����δ��ӣ�", Toast.LENGTH_LONG)
				.show();
		mSlidingMenu.showRightView();
	}

	/**
	 * ����΢�ţ�
	 * 
	 * @param v
	 */
	public void shareToWX(View v) {
		regToWx(); // ��Ӧ��ע�ᵽ΢�ţ�
		// sendToTalk(); // ����΢�����촰�ڣ�
		sendToFriend(); // ��������Ȧ��
	}

	/**
	 * ���ؼ��ļ����¼���
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		checkTime = System.currentTimeMillis() - startTime;
		if (checkTime > mTime && event.getRepeatCount() == 0) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_MENU: // ���˵����������˵�����
				fanView.showLeftMenu();
				if (presentStatus) {
					mSlidingMenu.checkShowView("Left");
				}
				return false;
			case KeyEvent.KEYCODE_BACK:
				if (delStatus) {
					delStatus = false;
					start_collectionLayout();
					return true;
				}
				if (presentStatus) {
					mSlidingMenu.checkShowView("Right");
					return true;
				} else if (!presentStatus) {
					exit();
					return true;
				}
				return false;
			default:
				return super.onKeyDown(keyCode, event);
			}
		}
		return false;
	}

	Handler myExitHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			isExit = false;
		}

	};

	public void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����",
					Toast.LENGTH_SHORT).show();
			myExitHandler.sendEmptyMessageDelayed(0, 2000);
		} else {
			// ���·��ؼ��˳����������ͨ�����������ӵ�Wifi�������˳�ʱ�رգ�
			WifiConfig myWifi;
			myWifi = new WifiConfig(this);
			if (isWifiEnableStatue == 1) {
				myWifi.removeWifiNetwork();
				myWifi.closeWifi();
			}
			// ���wifi�����ӣ��Ͽ�wifi���Ӳ��˳�
			if (isWifiConnectedStatue == 1) {
				myWifi.removeWifiNetwork();
				myWifi.disconnectWifi();
			}
			finish();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// �����������¼���
		return false;
	}

	/**
	 * չƷ����ҳ���viewPaper�л���
	 */
	// ����ViewPager����
	private ViewPager viewPager;
	// ����ViewPager������
	private ViewPagerAdapter vpAdapter;
	// ����һ��ArrayList�����View
	private ArrayList<View> views;
	// �ײ�С���ͼƬ
	private ImageView[] points;
	// ��¼��ǰѡ��λ��
	private int currentIndex;

	/**
	 * ��ʼ�����
	 */
	private void initView() {
		// ʵ����ArrayList����
		views = new ArrayList<View>();

		// ʵ����ViewPager
		viewPager = (ViewPager) findViewById(R.id.present_viewpager);

		// ʵ����ViewPager������
		vpAdapter = new ViewPagerAdapter(views);
	}

	/**
	 * ��ʼ������
	 */
	int picNum;

	private void initData() {
		picNum = getPicNum();
		String[] picPath = new String[picNum];
		getPicPath(picPath);
		// ����һ�����ֲ����ò���
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		for (int i = 0; i < picNum; i++) {
			File file = new File(picPath[i]);
			if (file.exists()) {
				Bitmap bm;
				bm = decodeFile(file);
				// ��ʼ������ͼƬ�б�
				ImageView iv = new ImageView(this);
				iv.setLayoutParams(mParams);
				iv.setImageBitmap(bm);
				views.add(iv);
			}
		}

		// ��������
		viewPager.setAdapter(vpAdapter);
		// ���ü���
		viewPager.setOnPageChangeListener(this);

		// ��ʼ���ײ�С��
		initPoint();

		initText();
	}

	/**
	 * ����ͼƬȻ���ͼƬ���������Լ����ڴ�����
	 * 
	 * @param f
	 * @return
	 */
	private Bitmap decodeFile(File f) {
		try {
			// ����ͼƬ��С
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// ������Ҫ���µ�ͼƬ��С
			final int REQUIRED_SIZE = 200;
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// ��inSampleSize����
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * ��ȡ���ص�ͼƬ·�������浽picPath���飻
	 * 
	 * @param picPath
	 */
	private void getPicPath(String[] picPath) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = new File(appDir + deviceName + "/");// ���·��
			File[] files = path.listFiles();// ��ȡ
			if (files != null && files.length > 0) {// ���ж�Ŀ¼�Ƿ�Ϊ�գ�����ᱨ��ָ��
				int num = 0;
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (fileName.endsWith(".jpg")) {
						picPath[num++] = appDir + deviceName + "/" + fileName;
					}
				}
			}
		}
	}

	/**
	 * ��ȡͼƬ������
	 * 
	 * @return
	 */
	private int getPicNum() {
		int num = 0;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = new File(appDir + deviceName + "/");// ���·��
			File[] files = path.listFiles();// ��ȡ
			if (files != null && files.length > 0) {// ���ж�Ŀ¼�Ƿ�Ϊ�գ�����ᱨ��ָ��
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (fileName.endsWith(".jpg")) {
						num++;
					}
				}
			}
		}
		return num;
	}

	/**
	 * ��ʼ���ײ�С��
	 */
	private void initPoint() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.present_point);
		for (int i = 0; i < picNum; i++) {
			ImageView imagePoint = new ImageView(this);
			imagePoint.setBackgroundResource(R.drawable.point);
			linearLayout.addView(imagePoint);
		}
		points = new ImageView[picNum];
		// ѭ��ȡ��С��ͼƬ
		for (int i = 0; i < picNum; i++) {
			// �õ�һ��LinearLayout�����ÿһ����Ԫ��
			points[i] = (ImageView) linearLayout.getChildAt(i);
			// Ĭ�϶���Ϊ��ɫ
			points[i].setEnabled(true);
			// ��ÿ��С�����ü���
			points[i].setOnClickListener(this);
			// ����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
			points[i].setTag(i);
		}

		// ���õ���Ĭ�ϵ�λ��
		currentIndex = 0;
		// ����Ϊ��ɫ����ѡ��״̬
		points[currentIndex].setEnabled(false);
	}

	/**
	 * ��ʼ��������Ϣҳ�棻
	 */
	private void initText() {
		String presentName = getText("name.txt");
		String presentInfo = getText("text.txt");

		LinearLayout linearLayoutName = (LinearLayout) findViewById(R.id.present_name);
		TextView name = new TextView(this);
		name.setText(presentName);
		name.setTextSize(24);
		name.setTextColor(0xff000000);
		name.setGravity(Gravity.RIGHT);
		linearLayoutName.addView(name);

		LinearLayout linearLayoutText = (LinearLayout) findViewById(R.id.present_text);
		TextView text = new TextView(this);
		text.setText(presentInfo);
		text.setTextSize(16);
		text.setTextColor(0xff000000);
		text.setMaxLines(1000);
		text.setMovementMethod(new ScrollingMovementMethod());
		linearLayoutText.addView(text);
	}

	/**
	 * ��ȡ�ı�����
	 * 
	 * @return
	 */
	private String getText(String txt) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(appDir + deviceName + "/" + txt);// ���·��
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

	/**
	 * ������״̬�ı�ʱ����
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	/**
	 * ����ǰҳ�汻����ʱ����
	 */
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	/**
	 * ���µ�ҳ�汻ѡ��ʱ����
	 */
	@Override
	public void onPageSelected(int position) {
		// ���õײ�С��ѡ��״̬
		setCurDot(position);
	}

	/**
	 * ͨ������¼����л���ǰ��ҳ��
	 */
	public void onClickPaper(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		setCurDot(position);
	}

	/**
	 * ���õ�ǰҳ���λ��
	 */
	private void setCurView(int position) {
		if (position < 0 || position >= picNum) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	/**
	 * ���õ�ǰ��С���λ��
	 */
	private void setCurDot(int positon) {
		if (positon < 0 || positon > picNum - 1 || currentIndex == positon) {
			return;
		}
		points[positon].setEnabled(false);
		points[currentIndex].setEnabled(true);

		currentIndex = positon;
	}
}
