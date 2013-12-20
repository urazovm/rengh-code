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
	/* 界面按钮； */
	private ImageButton btn_menu_list, btn_delete, btn_search, btn_more_info;
	/* NFC相关； */
	private NfcAdapter mNfcAdapter;
	private IntentFilter[] intentFiltersArray;
	private PendingIntent pendingIntent;
	private String[][] techListsArray;
	/*
	 * nfcStr保存从NFC标签读取到的字符串；fenGeFu是分割nfcStr的分隔符； ipAdress, deviceName, port,
	 * webType, str[], appDir, mSSID, mPASSWORD, strWifi =
	 * "AutoConnectWifi"，分别是设备 IP地址，设备名称，端口号，网页类型，分割后字符串的保存数组，应用程序文件目录，
	 * 网络SSID，网络密码，识别是否为该应用的头字符串；
	 */
	public static String nfcStr = null, fenGeFu = "\\*", ipAdress, deviceName,
			port, str[], appDir = null, mSSID, mPASSWORD,
			strWifi = "AutoConnectWifi";
	private int isWifiEnableStatue = 0, isWifiConnectedStatue = 0, nfcState,
			mTime = 3000, mTYPE;
	/*
	 * WebView相关； savedFileName, savedFileDir, downloadAdress, loadPath;
	 * 文件保存到本地的名称，保存目录，下载地址，加载地址； bar_download;进度条； mDownStatus;下载状态；
	 */
	private String savedFileName, savedFileDir, downloadAdress, loadPath;
	private ProgressBar bar_download;
	private Handler handler;
	private int mDownStatus;
	private DownloadFile downloadFile;
	private WebView webView;
	/* api;微信SDK的api对象； APP_ID;申请到的微信APP_ID，没有这个无法调用微信； */
	private IWXAPI api;
	private static final String APP_ID = "wx37539758487b3c98";
	// 提示界面的文本组件；
	private TextView textView;
	// 侧边栏管理对象；
	private FanView fanView;
	// 定义一个开始时间，一个检查时间；判断是否要禁用菜单键；
	long startTime, checkTime;

	private Boolean cancleConnect = false, presentStatus = false,
			isExit = false;
	public static Boolean collectClickStatus = false, delStatus = false;

	public static String SDCardDir, myFileName, delName, del;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 显示启动界面；
		start_SplashLayout();
		// 获取开始时间，用来判断是否启用菜单键功能；否则在启动界面显示时按菜单键会崩溃；
		startTime = System.currentTimeMillis();
		/* 根据当前设备的SD卡目录指定程序文件存放目录appDir； */
		SDCardDir = Environment.getExternalStorageDirectory().toString() + "/";
		appDir = SDCardDir + "ZhanGuiCollection/";
		myFileName = appDir + "myCollected";
		File file = new File(appDir);
		file.mkdirs();

		/* NFC相关; */
		nfcSetting();

		/* 默认wifi使用状况；根据这个判断用户有无连接NFC标签指定wifi网络； */
		if (isWifiEnabled()) {
			isWifiEnableStatue = -1;
		}
		if (isWifiConnect()) {
			isWifiConnectedStatue = -1;
		}

		/* 默认NFC使用状况；判断是未开启程序刷的NFC还是开启后前台调度； */
		nfcState = 0;
	}

	/**
	 * 给每个界面设置基础组件；
	 * 
	 * @author RenGH
	 */
	/**
	 * 加载开始界面布局，并设置为mTime毫秒后消失；
	 */
	private void start_SplashLayout() {
		setContentView(R.layout.layout_splash);

		/* 渐显组件； */
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.gradually);
		ImageView icon_welcom = (ImageView) findViewById(R.id.icon_welcom);
		icon_welcom.startAnimation(animation);

		Handler x = new Handler();
		x.postDelayed(new layout_splash(), mTime);
	}

	/**
	 * 加载 启动 界面，经过规定时间后执行指定动作；
	 * 
	 * @author Administrator
	 */
	class layout_splash implements Runnable {
		public void run() {
			/*
			 * 当nfcState不等于0时代表有NFC标签信息传入，就不加载收藏界面，
			 * 防止在启动界面加载时间内进入展品介绍页面，时间到了后又返回到收藏界面；
			 */
			if (nfcState == 0) {
				start_collectionLayout();
			}
		}
	}

	/**
	 * 加载 收藏 界面布局;
	 */
	private void start_collectionLayout() {
		presentStatus = false;
		setContentView(R.layout.layout_fan_view);
		fanView = (FanView) findViewById(R.id.fan_view);
		fanView.setLeftMenuViews(R.layout.layout_collection,
				R.layout.layout_left_menu);
		btn_menu_list_setting();
		btn_delete_seting();

		// 创建一个Handler对象
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
	 * 加载 展馆地图 界面布局
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
	 * 加载 简介 界面布局
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
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_list));
				}
				return false;
			}
		});

		btn_more_info.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
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
	 * 加载 提示 界面布局
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
	 * 设置btn_menu_list按钮及其监听事件；
	 */
	private void btn_menu_list_setting() {
		// 设置按钮监听和按下与不安下分别显示的图片；
		btn_menu_list = (ImageButton) findViewById(R.id.btn_menu_list);
		btn_menu_list.setOnClickListener(this);
		btn_menu_list.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_list));
				}
				return false;
			}
		});
	}

	/**
	 * 设置btn_delete按钮及其监听事件；
	 */
	private void btn_delete_seting() {
		btn_delete = (ImageButton) findViewById(R.id.btn_delete);
		btn_delete.setOnClickListener(this);
		btn_delete.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_delete));
				}
				return false;
			}
		});
	}

	/**
	 * 设置btn_search按钮及其监听事件；
	 */
	private void btn_search_setting() {
		// 设置按钮监听和按下与不安下分别显示的图片；
		btn_search = (ImageButton) findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);
		btn_search.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_search));
				}
				return false;
			}
		});
	}

	/**
	 * 设置按钮及其监听事件；
	 */
	private void btn_more_info_setting() {
		// 设置按钮监听和按下与不安下分别显示的图片；
		btn_more_info = (ImageButton) findViewById(R.id.btn_more_info);
		btn_more_info.setOnClickListener(this);
		btn_more_info.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 重新设置按下时的背景图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_press));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 再修改为抬起时的正常图片
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(R.drawable.btn_more_info));
				}
				return false;
			}
		});
	}

	/**
	 * 检测资料目录， 当有对应的资料时，将对应的图标换成已下载的图标；
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
	 * 将指定ID的按钮设置为显示已下载的图片；
	 * 
	 * @param id
	 */
	public void setHistory(int id) {
		ImageView imageView = (ImageView) findViewById(id);
		imageView.setBackgroundResource(R.drawable.btn_map_history);
		imageView.setOnClickListener(this);
	}

	/**
	 * 提示信息，文本组件的设置
	 */
	private void btn_promptInfo_setting() {
		textView = (TextView) findViewById(R.id.textView_promptInfo);
		textView.setText("正在连接...");
	}

	/**
	 * NFC相关设置及函数；
	 */
	/**
	 * NFC标签读取的相关设置;
	 */
	protected void nfcSetting() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "该设备不支持NFC功能！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
			Toast.makeText(this, "请先开启设备的NFC功能！", Toast.LENGTH_SHORT).show();
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
	 * 启用NFC前台调度，设置外部含NDEF消息的Intent处理事件；在onResume方法调用；
	 */
	private void nfcOnResume() {
		/* 启用nfc前台调度，保证程序开启状态下优先处理nfc包含的消息； */
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
					intentFiltersArray, techListsArray);
		}
		/* 能够将intent从外部传递到程序； */
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())
				&& (nfcState == 0)) {
			// 调用ndef消息的处理方法；
			processIntent(getIntent());
		}
	}

	/**
	 * 当窗口不在栈顶时，禁用前台调度；在onPause()方法内调用；
	 */
	public void nfcOnPause() {
		if (mNfcAdapter != null) {
			/* 适时关闭前台调用，避免资源被占用； */
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}

	/**
	 * 定义ndef消息的处理方法， 得到读取的字符串；
	 */
	private void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		/* 提取NdefMessage消息的正文去掉字符串中所有空格后赋给字符串nfcStr； */
		nfcStr = new String(msg.getRecords()[0].getPayload()).replaceAll("\\s",
				"");
		processNfcString(); // 将读取到的字符串分割为所需各个参数；
		start_promptLayout();
		checkWifiConected();
	}

	/**
	 * Wifi连接所需信息处理及连接诶方法；
	 * 
	 * @return
	 */
	/**
	 * 处理从NFC标签读取到的字符串；
	 * 
	 * @return
	 */
	public Boolean processNfcString() {
		/*
		 * 代表含义依次为：无线网络名称, 无线网络密码, 无线加密方式，设备名称，设备IP，设备端口号；
		 * 将nfcStr字符串分解为多个字符串存到str字符串数组里；
		 */
		str = nfcStr.split(fenGeFu);
		if (!str[0].equals(strWifi)) {
			Toast.makeText(this, "该标签内容与本程序不符，请联系管理员！", Toast.LENGTH_SHORT)
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
	 * 检查网络是否已经连接到指定的网络，如果已连接则直接下载资料并显示；
	 */
	private void checkWifiConected() {
		WifiConfig myWifi = new WifiConfig(this);
		if (!isWifiConnect() || !myWifi.getSSID().equals(mSSID)) {
			newThreadAutoWifi(); // 在新线程中连接wifi网络
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
	 * 检查要下载的文件是否已经存在；
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
	 * 创建一个线程连接wifi网络，连接成功后加载展品介绍布局，然后下载文件并加载到webView；
	 */
	public void newThreadAutoWifi() {
		textView.setText("连接设备...");
		// 创建一个Handler对象
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x20) {
					textView.setText("连接成功！开始下载资料...");
					Boolean exists = checkFileIsSaved();
					if (exists) {
						start_presentLayout();
					} else {
						downloadAndOpen();
					}
				}
				if (msg.what == 0x21) {
					textView.setText("正在启用wifi...");
				}
				if (msg.what == 0x22) {
					textView.setText("正在连接网络...");
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				if (AutoWifi() == true) {// 连接wifi网络；
					handler.sendEmptyMessage(0x20);
				} else {
					return;
				}
			}
		}.start();
	}

	/**
	 * 根据传递过来的三个无线网络参数连接wifi网络；
	 */
	private Boolean AutoWifi() {
		/*
		 * 创建对象，打开wifi功能，等到wifi启动完成后将传递来的wifi网络添加进Network，
		 * 然后等待连接诶成功后，传递设备名称，设备IP，设备端口号给connectedSocketServer方法，
		 * 用来连接远程Socket服务器；Integer.parseInt(str[3])
		 * 和Integer.valueOf(str[5])都是将字符串转换为整型；
		 */
		/*
		 * 定义AutoWifiConfig对象，通过该对象对wifi进行操作； WifiConfig myWifi = new
		 * WifiConfig(this); 不能用作全局，不然会出现刷nfc连接wifi，连接到socket，再刷nfc时程序卡死的情况；
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
	 * 检查wifi是否可用；是则返回true；
	 */
	private boolean isWifiEnabled() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isAvailable();
	}

	/**
	 * 检查wifi是否连接成功；成功则返回true；
	 */
	private boolean isWifiConnect() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	/**
	 * 下载资料解压到本地目录;
	 */
	private void downloadAndOpen() {
		savedFileName = appDir + deviceName + "/" + deviceName + ".zip";
		savedFileDir = appDir + deviceName + "/";
		downloadAdress = "http://" + ipAdress + ":" + port + "/download/"
				+ deviceName + ".zip";
		loadPath = "file://" + appDir + deviceName; // 资料的查找目录；
		// 创建文件夹
		File file = new File(appDir + deviceName);
		file.mkdirs();

		bar_download = (ProgressBar) findViewById(R.id.bar_download);

		// 创建一个Handler对象
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x12) {
					textView.setText("下载中..." + String.valueOf(mDownStatus)
							+ "%");
					bar_download.setProgress(mDownStatus);
				}
				if (msg.what == 0x11) {
					textView.setText("下载失败！");
					deleteDir(new File(savedFileDir));
				}
				if (msg.what == 0x10) {
					textView.setText("正在解压，请稍等...");
					Handler x = new Handler();
					x.postDelayed(new unZipAndOpen(), 200);
				}
			}
		};

		// 初始化DownloadFile对象
		downloadFile = new DownloadFile(downloadAdress, savedFileName);
		new Thread() {
			@Override
			public void run() {
				try {
					// 开始下载
					downloadFile.download();
				} catch (Exception e) {
					handler.sendEmptyMessage(0x11);
					e.printStackTrace();
					return;
				}
				// 定义每秒调度获取一次系统的完成进度
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (cancleConnect == true) {
							handler.sendEmptyMessage(0x11);
							cancleConnect = false;
							return; // 取消；
						}
						// 获取下载任务的完成比率
						double completeRate = downloadFile.getCompleteRate();
						mDownStatus = (int) (completeRate * 100);
						// 发送消息通知界面更新进度条
						handler.sendEmptyMessage(0x12);
						// 下载完全后取消任务调度
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
	 * 解压并打开
	 * 
	 * @author Administrator
	 */
	class unZipAndOpen implements Runnable {
		public void run() {
			try {
				ZipFileUtil.unZipFile(savedFileName, savedFileDir);
			} catch (Exception e) {
				textView.setText("解压失败！");
				e.printStackTrace();
				return;
			}
			start_presentLayout();
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 分享到微信的相关方法实现；
	 */
	/**
	 * 将本应用注册给微信；
	 */
	private void regToWx() {
		api = WXAPIFactory.createWXAPI(this, APP_ID, true);
		api.registerApp(APP_ID);
	}

	/**
	 * 分享到微信聊天窗口的方法；
	 */
	private void sendToTalk() {
		WXTextObject textObj = new WXTextObject();
		textObj.text = "微信分享测试例子。 --text来自展柜应用";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = "微信分享测试例子。 --msg来自展柜应用";

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		IWXAPI iwxapi = null;
		req.scene = SendMessageToWX.Req.WXSceneSession;

		api.sendReq(req);
	}

	/**
	 * 分享到微信朋友圈的方法；
	 */
	private void sendToFriend() {
		WXTextObject textObj = new WXTextObject();
		textObj.text = "微信分享测试例子。 --text来自展柜应用";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		msg.description = "微信分享测试例子。 --msg来自展柜应用";

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		IWXAPI iwxapi = null;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;

		api.sendReq(req);
	}

	/**
	 * 被复写父类的方法，生命周期相关；
	 */
	/* 当前Activity位于栈顶，有新的intent时调用； */
	@Override
	public void onNewIntent(Intent intent) {
		/* 调用ndef消息的处理方法； */
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
	 * 实现按钮点击动作方法；
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
	 * 左侧边栏按钮的点击事件；
	 * 
	 * @param v
	 */
	/**
	 * 判断当前页面是否是简介页面，如果是，则调用mSlidingMenu.showLeftView();
	 * 如果是其他页面，则调用fanView.showLeftMenu();
	 */
	private int showLeftMenu() {
		int hTime = 0; // 动画执行的时间
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
	 * 我的收藏按钮的实现；
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
	 * 展区地图按钮的实现；
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
	 * 账户管理按钮的实现；
	 * 
	 * @param v
	 */
	public void menuUsersManager(View v) {
		Toast.makeText(getApplicationContext(), "该功能暂未添加！", Toast.LENGTH_LONG)
				.show();
		int hTime = showLeftMenu();
		Handler x = new Handler();
		x.postDelayed(new btnUsersManager_hideLeftMenu(), hTime);
	}

	class btnUsersManager_hideLeftMenu implements Runnable {
		public void run() {
			// 在此处加载账户管理界面
		}
	}

	/**
	 * 分享到：按钮的实现；
	 * 
	 * @param v
	 */
	public void menuShareTo(View v) {
	}

	/**
	 * 收藏按钮的实现；
	 * 
	 * @param v
	 */
	public void menuCollected(View v) {;
		FileManager.appendMethod(deviceName, myFileName);
		Toast.makeText(getApplicationContext(), "已收藏！", Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * 展品位置按钮的实现；
	 * 
	 * @param v
	 */
	public void menuWhere(View v) {
		Toast.makeText(getApplicationContext(), "该功能暂未添加！", Toast.LENGTH_LONG)
				.show();
		mSlidingMenu.showRightView();
	}

	/**
	 * 分享到微信；
	 * 
	 * @param v
	 */
	public void shareToWX(View v) {
		regToWx(); // 将应用注册到微信；
		// sendToTalk(); // 分享到微信聊天窗口；
		sendToFriend(); // 分享到朋友圈；
	}

	/**
	 * 返回键的监听事件；
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		checkTime = System.currentTimeMillis() - startTime;
		if (checkTime > mTime && event.getRepeatCount() == 0) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_MENU: // 按菜单键弹出左侧菜单栏；
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
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			myExitHandler.sendEmptyMessageDelayed(0, 2000);
		} else {
			// 按下返回键退出程序，如果是通过本程序连接的Wifi网络则退出时关闭；
			WifiConfig myWifi;
			myWifi = new WifiConfig(this);
			if (isWifiEnableStatue == 1) {
				myWifi.removeWifiNetwork();
				myWifi.closeWifi();
			}
			// 如果wifi已连接，断开wifi连接并退出
			if (isWifiConnectedStatue == 1) {
				myWifi.removeWifiNetwork();
				myWifi.disconnectWifi();
			}
			finish();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// 监听触摸屏事件；
		return false;
	}

	/**
	 * 展品介绍页面的viewPaper切换；
	 */
	// 定义ViewPager对象
	private ViewPager viewPager;
	// 定义ViewPager适配器
	private ViewPagerAdapter vpAdapter;
	// 定义一个ArrayList来存放View
	private ArrayList<View> views;
	// 底部小点的图片
	private ImageView[] points;
	// 记录当前选中位置
	private int currentIndex;

	/**
	 * 初始化组件
	 */
	private void initView() {
		// 实例化ArrayList对象
		views = new ArrayList<View>();

		// 实例化ViewPager
		viewPager = (ViewPager) findViewById(R.id.present_viewpager);

		// 实例化ViewPager适配器
		vpAdapter = new ViewPagerAdapter(views);
	}

	/**
	 * 初始化数据
	 */
	int picNum;

	private void initData() {
		picNum = getPicNum();
		String[] picPath = new String[picNum];
		getPicPath(picPath);
		// 定义一个布局并设置参数
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		for (int i = 0; i < picNum; i++) {
			File file = new File(picPath[i]);
			if (file.exists()) {
				Bitmap bm;
				bm = decodeFile(file);
				// 初始化引导图片列表
				ImageView iv = new ImageView(this);
				iv.setLayoutParams(mParams);
				iv.setImageBitmap(bm);
				views.add(iv);
			}
		}

		// 设置数据
		viewPager.setAdapter(vpAdapter);
		// 设置监听
		viewPager.setOnPageChangeListener(this);

		// 初始化底部小点
		initPoint();

		initText();
	}

	/**
	 * 解码图片然后对图片进行缩放以减少内存消耗
	 * 
	 * @param f
	 * @return
	 */
	private Bitmap decodeFile(File f) {
		try {
			// 解码图片大小
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// 我们想要的新的图片大小
			final int REQUIRED_SIZE = 200;
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// 用inSampleSize解码
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * 获取下载的图片路径，保存到picPath数组；
	 * 
	 * @param picPath
	 */
	private void getPicPath(String[] picPath) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = new File(appDir + deviceName + "/");// 获得路径
			File[] files = path.listFiles();// 读取
			if (files != null && files.length > 0) {// 先判断目录是否为空，否则会报空指针
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
	 * 获取图片数量；
	 * 
	 * @return
	 */
	private int getPicNum() {
		int num = 0;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = new File(appDir + deviceName + "/");// 获得路径
			File[] files = path.listFiles();// 读取
			if (files != null && files.length > 0) {// 先判断目录是否为空，否则会报空指针
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
	 * 初始化底部小点
	 */
	private void initPoint() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.present_point);
		for (int i = 0; i < picNum; i++) {
			ImageView imagePoint = new ImageView(this);
			imagePoint.setBackgroundResource(R.drawable.point);
			linearLayout.addView(imagePoint);
		}
		points = new ImageView[picNum];
		// 循环取得小点图片
		for (int i = 0; i < picNum; i++) {
			// 得到一个LinearLayout下面的每一个子元素
			points[i] = (ImageView) linearLayout.getChildAt(i);
			// 默认都设为灰色
			points[i].setEnabled(true);
			// 给每个小点设置监听
			points[i].setOnClickListener(this);
			// 设置位置tag，方便取出与当前位置对应
			points[i].setTag(i);
		}

		// 设置当面默认的位置
		currentIndex = 0;
		// 设置为白色，即选中状态
		points[currentIndex].setEnabled(false);
	}

	/**
	 * 初始化介绍信息页面；
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
	 * 获取文本内容
	 * 
	 * @return
	 */
	private String getText(String txt) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(appDir + deviceName + "/" + txt);// 获得路径
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
	 * 当滑动状态改变时调用
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	/**
	 * 当当前页面被滑动时调用
	 */
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	/**
	 * 当新的页面被选中时调用
	 */
	@Override
	public void onPageSelected(int position) {
		// 设置底部小点选中状态
		setCurDot(position);
	}

	/**
	 * 通过点击事件来切换当前的页面
	 */
	public void onClickPaper(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		setCurDot(position);
	}

	/**
	 * 设置当前页面的位置
	 */
	private void setCurView(int position) {
		if (position < 0 || position >= picNum) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	/**
	 * 设置当前的小点的位置
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
