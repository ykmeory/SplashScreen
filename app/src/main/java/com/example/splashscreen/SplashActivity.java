package com.example.splashscreen;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import com.example.splashscreen.utils.NetManager;
import com.example.splashscreen.utils.SharedConfig;


/**
 * 本例只要设置图片就行了，指示器、ViewPage页都是自动添加的。这样在以后升级版本时，如果引导页数目发生变动，只需要替换图片就行了。
 *
 * Demo的流程是：闪屏->引导页->主页
 *
 * 功能简介：
 * 1、闪屏
 *    1.1、渐变的透明度；
 *    1.2、判断网络，无网络弹出对话框设置网络；
 *    1.3、判断是否第一次进入，仅第一次进入时进入引导页。
 * 2、引导页
 *    2.1、全自动的添加page页和指示器，只需要设置显示的图片即可，方便了后期维护；
 *    2.2、自动创建桌面快捷方式；到最后一页自动添加按钮。
 * 3、主页：清除配置
 *
 * 知识点：
 * 1、ViewPage适配器
 * 2、创建快捷方式的类
 * 3、主页
 * 4、闪屏
 * 5、网络管理
 * 6、配置管理
 * 7、引导页
 */

/**
 * 软件入口，闪屏界面。
 */
public class SplashActivity extends Activity {
	private boolean first;//判断是否第一次打开软件

	private View view;

	private Context context;
	private Animation animation;
	private NetManager netManager;
	private SharedPreferences shared;

	private static int TIME = 1000;//进入主程序的延迟时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		view = View.inflate(this, R.layout.activity_splash, null);
		setContentView(view);

		context = this;//得到上下文
		shared = new SharedConfig(context).GetConfig();// 得到配置文件
		netManager = new NetManager(context);// 得到网络管理器
	}

	@Override
	protected void onResume() {
		into();
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	/**
	 * 进入主程序的方法
	 */
	private void into() {
		if (netManager.isOpenNetwork()) {
			// 如果网络可用则判断是否第一次进入，如果是第一次则进入欢迎界面
			first = shared.getBoolean("First", true);

			//Splash页面启动动画的两种效果示例
			//startAnim();//透明度
			startAnimation();//旋转
		} else {
			ifHasNetwork();
		}
	}


	/**
	 * 动画一、Splash透明度变化动画
	 */
	private void startAnim() {
		//设置动画效果是alpha，在anim目录下的alpha.xml文件中定义动画效果
		animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		// 给view设置动画效果
		view.startAnimation(animation);

		animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            // 这里监听动画结束的动作，在动画结束的时候开启一个线程，这个线程中绑定一个Handler,并
            // 在这个Handler中调用goHome方法，而通过postDelayed方法使这个方法延迟1000毫秒执行，
            // 达到持续显示第一屏1000毫秒的效果
            @Override
            public void onAnimationEnd(Animation arg0) {
				toMainOrAd();
			}
        });

	}

	/**
	 * 动画二、Splash旋转动画
	 */
	private void startAnimation() {
		AnimationSet set = new AnimationSet(false); //设置动画集合；

		//旋转动画,0到360度旋转，自身围绕中心点（0.5f）旋转；
		RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(1000); //旋转事件；
		rotate.setFillAfter(true); //保持动画状态；

		//缩放动画；
		ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1,Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(1000);
		scale.setFillAfter(true);

		//淡入淡出动画；
		AlphaAnimation alpha = new AlphaAnimation(0, 1);
		alpha.setDuration(1000);
		alpha.setFillAfter(true);

		set.addAnimation(rotate);
		set.addAnimation(scale);
		set.addAnimation(alpha);

		//设置动画监听；
		set.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			//动画执行结束；
			@Override
			public void onAnimationEnd(Animation animation) {
				toMainOrAd();
			}
		});

		view.startAnimation(set); //播放动画；
	}

	/**
	 * 闪屏过后的页面跳转逻辑
	 */
	private void toMainOrAd() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent;

				//如果第一次，则进入引导页WelcomeActivity
				if (first) {
					intent = new Intent(SplashActivity.this, GuideActivity.class);
				} else {
					//intent = new Intent(SplashActivity.this,MainActivity.class);
					intent = new Intent(SplashActivity.this, AdPageActivity.class);
				}

				startActivity(intent);

				//设置Activity的切换效果
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

				SplashActivity.this.finish();
			}
		}, TIME);
	}

	/**
	 * 判断是否有网络
	 * 如果网络不可用，则弹出对话框，对网络进行设置
	 */
	private void ifHasNetwork() {
		Builder builder = new Builder(context);
		builder.setTitle("没有可用的网络");
		builder.setMessage("是否对网络进行设置?");

		builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent ;

				try {
					String sdkVersion = android.os.Build.VERSION.SDK;

					if (Integer.valueOf(sdkVersion) > 10) {
						intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
					} else {
						intent = new Intent();
						ComponentName comp = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
						intent.setComponent(comp);
						intent.setAction("android.intent.action.VIEW");
					}

					SplashActivity.this.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SplashActivity.this.finish();
			}
		});

		builder.show();
	}

	/**
	 * 屏蔽物理返回按键
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;

		return super.onKeyDown(keyCode, event);
	}

}
