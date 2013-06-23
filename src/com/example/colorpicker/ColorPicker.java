package com.example.colorpicker;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ColorPicker implements OnUpdateColorPicker{

	private Context context;
	private AlertDialog dialog;
	private RelativeLayout colorPickerView;
	private DisplaySize displaySize;
	private Bitmap hueBitmap;
	private Bitmap svBitmap;
	private ImageView svBox;
	private ImageView hueBar;
	private TextView previewBox;
	private int hue_x;
	private int hue_y;
	private int selectedHue;
	private int sv_x;
	private int sv_y;

	public ColorPicker(Context context) {
		this.context = context;
		displaySize = new DisplaySize(context);
		makeView(context);
		setViews();
		makeDialog();
	}
	
	private void setViews() {
		hueBar = (ImageView) colorPickerView.findViewById(R.id.HueBar);
		hueBar.setImageBitmap(makeHueBitmap());

		hueBar.getLayoutParams().height = hueBitmap.getHeight();
		hueBar.getLayoutParams().width = hueBitmap.getWidth();
		
		hueBar.setOnTouchListener(new OnColorTouch(new OnHuePicker(this)));

		svBox = (ImageView) colorPickerView.findViewById(R.id.SVBox);
		svBox.setImageBitmap(makeSVBitmap(Color.YELLOW));
		
		svBox.getLayoutParams().height = svBitmap.getHeight();
		svBox.getLayoutParams().width = svBitmap.getWidth();
		
		svBox.setOnTouchListener(new OnColorTouch(new OnSVPicker(this)));
		
		previewBox = (TextView) colorPickerView.findViewById(R.id.previewBox);
	}

	private void makeView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorPickerView = (RelativeLayout) inflater.inflate(
				R.layout.color_picker, null);
	}

	private void makeDialog() {
		Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialogTitle);

		builder.setPositiveButton(R.string.positive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		builder.setNegativeButton(R.string.negative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		builder.setView(colorPickerView);

		dialog = builder.create();
	}

	public AlertDialog getDialog() {
		return dialog;
	}

	private Bitmap makeHueBitmap() {
		int width = (int) (displaySize.getDisplayWidthInPixel() * 0.6);
		int height = (int) (displaySize.getDisplayHeightInPixel() * 0.05);

		Canvas canvas = new Canvas();

		hueBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		canvas.setBitmap(hueBitmap);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		int[] colors = { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
				Color.BLUE, Color.MAGENTA, Color.RED };
		float[] colors_position = { 0.0f, 0.17f, 0.34f, 0.51f, 0.68f, 0.85f,
				1.0f };

		LinearGradient shader = new LinearGradient(0, (float) (height * 0.5),
				width, (float) (height * 0.5), colors, colors_position,
				Shader.TileMode.CLAMP);
		paint.setShader(shader);

		canvas.drawRect(0, 0, width, height, paint);

		return hueBitmap;
	}

	private Bitmap makeSVBitmap(int selectedColor) {

		int width = (int) (displaySize.getDisplayWidthInPixel() * 0.6);
		int height = (int) (displaySize.getDisplayHeightInPixel() * 0.3);

		Canvas canvas = new Canvas();

		svBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		canvas.setBitmap(svBitmap);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		Shader shaderSaturation = new LinearGradient(1, 1, width - 1, 1,
				Color.WHITE, selectedColor, TileMode.CLAMP);
		Shader shaderValue = new LinearGradient(1, 1, 1, height - 1, 0,
				Color.BLACK, TileMode.CLAMP);

		ComposeShader shader = new ComposeShader(shaderSaturation, shaderValue,
				PorterDuff.Mode.DARKEN);

		paint.setShader(shader);
		canvas.drawRect(1, 1, width - 1, height - 1, paint);

		return svBitmap;
	}

	private void updatePreviewBox(int color) {
		previewBox.setBackgroundColor(color);
	}

	@Override
	public void updateSVBitmap(int x, int y) {
		if(checkValidate(x, y, this.svBitmap)){
			Bitmap tempSVBitmap;
			int selectedColor;
			setSVSelectedPosition(x, y);
			tempSVBitmap = makeSVBitmap(selectedHue);
			selectedColor = getSelectedColor();
			svBox.setImageBitmap(drawSelectionBoxOnSVBitmap(tempSVBitmap));
			updatePreviewBox(selectedColor);
		}
	}

	private int getSelectedColor() {
		return svBitmap.getPixel(sv_x, sv_y);
	}

	private void setSVSelectedPosition(int x, int y) {
		sv_x = x;
		sv_y = y;
	}

	public int getHueColor(){
		return hueBitmap.getPixel(hue_x, hue_y);
	}
	
	private boolean checkValidate(int x, int y, Bitmap bitmap) {
		if(x > 0 && x < bitmap.getWidth()-1 && y > 0 && y < bitmap.getHeight()-1){
			return true;
		}
		return false;
	}

	
	@Override
	public void updateHueBar(int x, int y) {
		if(checkValidate(x, y, hueBitmap)){
			Bitmap hueBitmap;
			setHueSelectedPosition(x, y);
			hueBitmap = makeHueBitmap();
			selectedHue = getHueColor();
			hueBar.setImageBitmap(drawSelectionBoxOnHueBitmap(hueBitmap));
			updateSVBitmap(sv_x, sv_y);
		}
	}

	private Bitmap drawSelectionBoxOnHueBitmap(Bitmap hueBitmap) {
		Canvas canvas = new Canvas(hueBitmap);
		Paint paintBlack = new Paint();
		Paint paintWhite = new Paint();
		
		RectF rectBlack = new RectF((int)(hue_x-5), 0.f, (int)(hue_x+5), (int)hueBar.getHeight());
		RectF rectWhite = new RectF((int)(hue_x-4), 1.f, (int)(hue_x+6), (int)hueBar.getHeight());
		
		paintBlack.setColor(Color.BLACK);
		paintBlack.setAntiAlias(true);
		paintBlack.setStrokeWidth(2);
		paintBlack.setStyle(Paint.Style.STROKE);
		paintWhite.setColor(Color.WHITE);
		paintWhite.setAntiAlias(true);
		paintWhite.setStrokeWidth(1);
		paintWhite.setStyle(Paint.Style.STROKE);
		
		canvas.drawRoundRect(rectWhite, 10, 10, paintWhite);
		canvas.drawRoundRect(rectBlack, 10, 10, paintBlack);

		return hueBitmap;
	}

	private Bitmap drawSelectionBoxOnSVBitmap(Bitmap svBitmap) {
		Canvas canvas = new Canvas(svBitmap);
		Paint paintBlack = new Paint();
		Paint paintWhite = new Paint();

		paintBlack.setColor(Color.BLACK);
		paintBlack.setAntiAlias(true);
		paintBlack.setStrokeWidth(2);
		paintBlack.setStyle(Paint.Style.STROKE);
		paintWhite.setColor(Color.WHITE);
		paintWhite.setAntiAlias(true);
		paintWhite.setStrokeWidth(1);
		paintWhite.setStyle(Paint.Style.STROKE);

		canvas.drawCircle(sv_x, sv_y, 10, paintBlack);
		canvas.drawCircle(sv_x, sv_y, 9, paintWhite);
		canvas.drawCircle(sv_x, sv_y, 12, paintWhite);
		return svBitmap;
	}
	
	private void setHueSelectedPosition(int x, int y) {
		hue_x = x;
		hue_y = y;
	}

}