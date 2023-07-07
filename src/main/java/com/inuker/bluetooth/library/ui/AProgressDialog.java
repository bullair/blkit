// Copyright (C) 2022 即时通讯网(52im.net) & Jack Jiang.
// The RainbowChat & RainbowChat-Web Project. All rights reserved.
// 
// 【本产品为著作权产品，合法授权后请放心使用，禁止外传！】
// 【本次授权给：<深圳市信受奉行信息技术有限公司>，授权编号：<NT220214114726、NT220214114727>，代码指纹：<A.644810446.202>，技术对接人微信：<ID: gituu_cn>】
// 【授权寄送：<收件：彭维、地址：深圳南山区文苑路35号聚创金谷创意园A栋五楼508号、电话：15602991312、邮箱：pengwei@ibodhi.cn>】
// 
// 【本系列产品在国家版权局的著作权登记信息如下】：
// 1）国家版权局登记名(简称)和权证号：RainbowChat    （证书号：软著登字第1220494号、登记号：2016SR041877）
// 2）国家版权局登记名(简称)和权证号：RainbowChat-Web（证书号：软著登字第3743440号、登记号：2019SR0322683）
// 3）国家版权局登记名(简称)和权证号：RainbowAV      （证书号：软著登字第2262004号、登记号：2017SR676720）
// 4）国家版权局登记名(简称)和权证号：MobileIMSDK-Web（证书号：软著登字第2262073号、登记号：2017SR676789）
// 5）国家版权局登记名(简称)和权证号：MobileIMSDK    （证书号：软著登字第1220581号、登记号：2016SR041964）
// * 著作权所有人：江顺/苏州网际时代信息科技有限公司
// 
// 【违法或违规使用投诉和举报方式】：
// 联系邮件：jack.jiang@52im.net
// 联系微信：hellojackjiang
// 联系QQ号：413980957
// 授权说明：http://www.52im.net/thread-1115-1-1.html
// 官方社区：http://www.52im.net
package com.inuker.bluetooth.library.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inuker.bluetooth.library.R;

/**
 * 一个仿微信的菊花进度提示框架。
 *
 * @author JackJiang
 * @since 7.3
 */
public class AProgressDialog extends Dialog
{
	private String message = null;
	private TextView tipTextView = null;
	private ImageView spaceshipImage = null;
	private Animation hyperspaceJumpAnimation = null;
	
	private AnimStyle animStyle = AnimStyle.colorFullStyle;
	
	public AProgressDialog(Context context, String message)
	{
//		this(context, message, AnimStyle.colorFullStyle);
		this(context, message, AnimStyle.normalStyle1);
	}
	public AProgressDialog(Context context, String message, AnimStyle animStyle)
	{
		super(context, R.style.widget_loading_dialog);
		this.message = message;
		this.animStyle = animStyle;
		
		initViews();
	}

	protected void initViews()
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());  
		View v = inflater.inflate(R.layout.widget_progress_dialog, null);// 得到加载view
		LinearLayout layout = v.findViewById(R.id.widget_progress_dialog_view);// 加载布局
		
		// main.xml中的ImageView  
		spaceshipImage = v.findViewById(R.id.widget_progress_dialog_img);
		tipTextView = v.findViewById(R.id.widget_progress_dialog_tipTextView);// 提示文字
		
		// 加载动画  
		if(animStyle == AnimStyle.normalStyle1) {
			hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.widget_loading_big1);
		}
//		// 使用ImageView显示动画  
//		spaceshipImage.startAnimation(hyperspaceJumpAnimation);  
		tipTextView.setText(message);// 设置加载信息  

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(  
				LinearLayout.LayoutParams.FILL_PARENT,  
				LinearLayout.LayoutParams.FILL_PARENT);
//		lp.gravity = Gravity.CENTER;
//		this.setCancelable(false);// 不可以用“返回键”取消  
		this.setContentView(layout, lp);// 设置布局  
//		this.getWindow().setGravity(Gravity.CENTER);
		this.getWindow().getAttributes().gravity = Gravity.CENTER;
	}
	
	@Override
	public void show()
	{
		super.show();
//		if(animStyle == AnimStyle.normalStyle1)
		{
			// 使用ImageView显示动画  
			spaceshipImage.startAnimation(hyperspaceJumpAnimation); 
		}
//		else
//		{
//			int animId = R.drawable.widget_loading_infinite_colorfull;
//			spaceshipImage.setImageResource(animId);
//			((AnimationDrawable) spaceshipImage.getDrawable()).start();
//		}
	}

	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
		tipTextView.setText(message);// 设置加载信息  
	}
	
	public static enum AnimStyle
	{
		normalStyle1,
		colorFullStyle,
	}
}
