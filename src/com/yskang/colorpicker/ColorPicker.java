package com.yskang.colorpicker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.BitSet;

import static android.widget.SeekBar.OnSeekBarChangeListener;


public class ColorPicker implements OnUpdateColorPicker, OnSeekBarChangeListener {

	private Context context;
	private Dialog dialog;
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
	private Bitmap hueBitmapCopy;
	private Paint paintBlack;
	private Paint paintWhite;
	private int hueWidth;
	private int hueHeight;
	private int svWidth;
	private int svHeight;
    private final static float HEIGHT_RATIO = 0.3f;
    private final static float HUE_WIDTH_RATIO = 0.2f;
    private final static float SV_WIDTH_RATIO = 0.6f;
	private Paint svPaint;
	private Shader shaderValue;
	private int selectedColor;
	private View view;
	private Button presetColorButton1;
	private Button presetColorButton2;
	private Button presetColorButton3;
	private Button presetColorButton4;
	private ArrayList<Integer> presetColors;
	private OnColorSelectedListener onColorPickerSelectedListener;
    private Paint paintBlackFill;
    private Paint paintWhiteFill;
    private Paint paintSelectedHueColorForHueMarker;
    private Paint paintSelectedColorForMarker;
    private float selectionMarkerR;
    private SeekBar alphaSeekBar;
    private int alpha = 255;

    public ColorPicker(Context context, int initialColor, OnColorSelectedListener onColorPickerSelectedListener, ArrayList<Integer> presetColors){
		this.context = context;
		this.onColorPickerSelectedListener = onColorPickerSelectedListener;
		this.presetColors = presetColors;

		displaySize = new DisplaySize(context);
		initViewSize();
		makeView(context);
		initPaints();
		setViews();
		updatePresetColor(initialColor);
		initPresetColors(presetColors);
        initOldColorBox(initialColor);
		makeDialog();
		initPresetColorButtons();
	}

    private void initOldColorBox(int initialColor) {
        colorPickerView.findViewById(R.id.oldColorBox).setBackgroundColor(initialColor);
    }

    private void initPresetColors(ArrayList<Integer> presetColors) {
		presetColorButton1 = (Button) colorPickerView.findViewById(R.id.presetButton_1);
		presetColorButton2 = (Button) colorPickerView.findViewById(R.id.presetButton_2);
		presetColorButton3 = (Button) colorPickerView.findViewById(R.id.presetButton_3);
		presetColorButton4 = (Button) colorPickerView.findViewById(R.id.presetButton_4);
		
		presetColorButton1.setBackgroundColor(presetColors.get(0));
		presetColorButton2.setBackgroundColor(presetColors.get(1));
		presetColorButton3.setBackgroundColor(presetColors.get(2));
		presetColorButton4.setBackgroundColor(presetColors.get(3));
	}

	private void initSelectedColor(int initialColor) {
		selectedColor = initialColor;
		updatePreviewBox();
	}

	private void initPaints() {
		paintBlack = new Paint();
		paintWhite = new Paint();
		svPaint = new Paint();

		paintBlack.setColor(Color.BLACK);
		paintBlack.setAntiAlias(true);
		paintBlack.setStrokeWidth(2);
		paintBlack.setStyle(Paint.Style.STROKE);
		
		paintWhite.setColor(Color.WHITE);
		paintWhite.setAntiAlias(true);
		paintWhite.setStrokeWidth(1);
		paintWhite.setStyle(Paint.Style.STROKE);

        paintBlackFill = new Paint();
        paintBlackFill.setColor(Color.BLACK);
        paintBlackFill.setAntiAlias(true);
        paintBlackFill.setStyle(Paint.Style.FILL);

        paintWhiteFill = new Paint();
        paintWhiteFill.setColor(Color.WHITE);
        paintWhiteFill.setAntiAlias(true);
        paintWhiteFill.setStyle(Paint.Style.FILL);

        paintSelectedHueColorForHueMarker = new Paint();
        paintSelectedHueColorForHueMarker.setColor(selectedHue);
        paintSelectedHueColorForHueMarker.setAntiAlias(true);
        paintSelectedHueColorForHueMarker.setStyle(Paint.Style.FILL);

        paintSelectedColorForMarker = new Paint();
        paintSelectedColorForMarker.setColor(selectedColor);
        paintSelectedColorForMarker.setAntiAlias(true);
        paintSelectedColorForMarker.setStyle(Paint.Style.FILL);

        svPaint.setAntiAlias(true);
		
        shaderValue = new LinearGradient(0, selectionMarkerR, 0, svHeight - selectionMarkerR, Color.TRANSPARENT,
                Color.BLACK, TileMode.CLAMP);
	}

	private void getInitialColorPosition(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		selectedHue = color;

        hue_x = (int)(0.5 * (hueWidth-1));
        hue_y = (int)(hsv[0] * (hueHeight-1)/360);

		sv_x = (int)(hsv[1] * (svWidth-1));
		sv_y = (int)((svHeight-1) - hsv[2] * (svHeight-1));
	}

	private void initViewSize() {
		hueWidth = (int)(displaySize.getDisplayWidthInPixel() * HUE_WIDTH_RATIO);
		hueHeight = (int)(displaySize.getDisplayHeightInPixel() * HEIGHT_RATIO);
		svWidth = (int)(displaySize.getDisplayWidthInPixel() * SV_WIDTH_RATIO);
		svHeight = hueHeight;

        selectionMarkerR = displaySize.getPixel(15);
	}

	private void setViews() {
		hueBitmap = makeHueBitmap();
		
		hueBar = (ImageView) colorPickerView.findViewById(R.id.HueBar);
		hueBar.setImageBitmap(hueBitmap);
		
		hueBar.getLayoutParams().height = hueBitmap.getHeight();
		hueBar.getLayoutParams().width = hueBitmap.getWidth();

		hueBar.setOnTouchListener(new OnColorTouch(new OnHuePicker(this)));

		svBox = (ImageView) colorPickerView.findViewById(R.id.SVBox);
		makeSVBitmap(selectedHue);
		svBox.setImageBitmap(svBitmap);
		
		svBox.getLayoutParams().height = svBitmap.getHeight();
		svBox.getLayoutParams().width = svBitmap.getWidth();
		
		svBox.setOnTouchListener(new OnColorTouch(new OnSVPicker(this)));
		
		previewBox = (TextView) colorPickerView.findViewById(R.id.previewBox);

        alphaSeekBar = (SeekBar) colorPickerView.findViewById(R.id.alphaSeekBar);
        alphaSeekBar.setMax(255);
        alphaSeekBar.setProgress(255);
        alphaSeekBar.setOnSeekBarChangeListener(this);
    }

	private void initPresetColorButtons() {
		presetColorButton1.setOnClickListener(new OnPresetColorButtonClickListener(this, presetColors.get(0)));
		presetColorButton2.setOnClickListener(new OnPresetColorButtonClickListener(this, presetColors.get(1)));
		presetColorButton3.setOnClickListener(new OnPresetColorButtonClickListener(this, presetColors.get(2)));
		presetColorButton4.setOnClickListener(new OnPresetColorButtonClickListener(this, presetColors.get(3)));
	}

	private void makeView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorPickerView = (RelativeLayout) inflater.inflate(
				R.layout.color_picker, null);
	}

	private void makeDialog() {
		Builder builder = new AlertDialog.Builder(context);

		builder.setPositiveButton(R.string.positive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						onClickOk();
					}
				});

		builder.setNegativeButton(R.string.negative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						onClickCancel();
					}
				});

		builder.setView(colorPickerView);

		dialog = builder.create();
	}

	public Dialog getDialog() {
		return dialog;
	}

	private Bitmap makeHueBitmap() {
		Canvas canvas = new Canvas();

		hueBitmap = Bitmap.createBitmap(hueWidth, hueHeight,
				Bitmap.Config.ARGB_8888);
		canvas.setBitmap(hueBitmap);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		int[] colors = { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
				Color.BLUE, Color.MAGENTA, Color.RED };
		float[] colors_position = { 0.0f, 0.17f, 0.34f, 0.51f, 0.68f, 0.85f,
				1.0f };

		LinearGradient shader = new LinearGradient(hueWidth * 0.5f, selectionMarkerR,
                hueWidth * 0.5f, hueHeight- selectionMarkerR, colors, colors_position, Shader.TileMode.CLAMP);
		paint.setShader(shader);

		canvas.drawRect(hueWidth*0.5f + hueWidth*0.1f, selectionMarkerR, hueWidth - hueWidth*0.1f, hueHeight- selectionMarkerR, paint);

		return hueBitmap;
	}

	private void makeSVBitmap(int selectedColor) {
        Canvas svCanvas = new Canvas();

        svBitmap = Bitmap.createBitmap(svWidth, svHeight,
                Bitmap.Config.ARGB_8888);
        svCanvas.setBitmap(svBitmap);

		Shader shaderSaturation = new LinearGradient(selectionMarkerR, 0, svWidth-selectionMarkerR, 0,
				Color.WHITE, selectedColor, TileMode.CLAMP);

        ComposeShader shader = new ComposeShader(shaderSaturation, shaderValue,
				PorterDuff.Mode.DARKEN);

		svPaint.setShader(shader);

		svCanvas.drawRect(selectionMarkerR, selectionMarkerR, svWidth-selectionMarkerR, svHeight-selectionMarkerR, svPaint);
	}

	private void updatePreviewBox() {
        selectedColor = (selectedColor & 0x00FFFFFF) | (alpha<<24 );
		previewBox.setBackgroundColor(selectedColor);
	}

	private int getSelectedColor() {
		return svBitmap.getPixel(sv_x, sv_y);
	}

	private void setSVSelectedPosition(int x, int y) {
		sv_x = x;
		sv_y = y;
	}

	public int getHueColor(){
		return hueBitmap.getPixel((int)(hueWidth*0.75f), hue_y);
	}

	@Override
	public void updateHueBar(int x, int y) {
        if(x < 0) x = 0;
        if(x > hueWidth) x = (hueWidth - 1);
        if(y < selectionMarkerR) y = (int) selectionMarkerR;
        if(y > hueHeight - selectionMarkerR) y = (int) (hueHeight -selectionMarkerR -1);

        setHueSelectedPosition(x, y);
        selectedHue = getHueColor();
        hueBar.setImageBitmap(drawSelectionMarkOnHueBitmap(hueBitmap));
        updateSVBox(sv_x, sv_y);
	}

    @Override
	public void updateSVBox(int x, int y) {
        if(x < selectionMarkerR) x = (int) selectionMarkerR;
        if(x > svWidth-selectionMarkerR) x = (int) (svWidth - selectionMarkerR -1);
        if(y < selectionMarkerR) y = (int) selectionMarkerR;
        if(y > svHeight-selectionMarkerR ) y = (int) (svHeight - selectionMarkerR -1);

        setSVSelectedPosition(x, y);
        makeSVBitmap(selectedHue);
        selectedColor = getSelectedColor();
        svBox.setImageBitmap(drawSelectionMarkerOnSVBitmap(svBitmap));
        updatePreviewBox();
	}
	
	private Bitmap drawSelectionMarkOnHueBitmap(Bitmap hueBitmap) {
		hueBitmapCopy = Bitmap.createBitmap(hueBitmap);
		Canvas hueCanvas = new Canvas(hueBitmapCopy);

        paintSelectedHueColorForHueMarker.setColor(selectedHue);

        RectF rectFillBlackOuter = new RectF(selectionMarkerR +(0.5f* selectionMarkerR), hue_y-5, hueBitmap.getWidth(), hue_y+5);
        RectF rectFillWhite = new RectF(selectionMarkerR +(0.5f* selectionMarkerR), hue_y-4, hueBitmap.getWidth(), hue_y+4);
        RectF rectFillBlackInner = new RectF(selectionMarkerR +(0.5f* selectionMarkerR), hue_y-3, hueBitmap.getWidth(), hue_y+3);
        RectF rectFillSelectedHue = new RectF(selectionMarkerR +(0.5f* selectionMarkerR), hue_y-1, hueBitmap.getWidth(), hue_y+1);

        hueCanvas.drawCircle(selectionMarkerR, hue_y, selectionMarkerR, paintBlackFill);
        hueCanvas.drawRoundRect(rectFillBlackOuter, 10, 10, paintBlackFill);
        hueCanvas.drawCircle(selectionMarkerR, hue_y, selectionMarkerR -2, paintWhiteFill);
        hueCanvas.drawRoundRect(rectFillWhite, 10, 10, paintWhiteFill);
        hueCanvas.drawCircle(selectionMarkerR, hue_y, selectionMarkerR -4, paintBlackFill);
        hueCanvas.drawRoundRect(rectFillBlackInner, 10, 10, paintBlackFill);
        hueCanvas.drawRoundRect(rectFillSelectedHue, 10, 10, paintSelectedHueColorForHueMarker);
        hueCanvas.drawCircle(selectionMarkerR, hue_y, selectionMarkerR -6, paintSelectedHueColorForHueMarker);

		return hueBitmapCopy;
	}

	private Bitmap drawSelectionMarkerOnSVBitmap(Bitmap svBitmap) {
		Canvas canvas = new Canvas(svBitmap);

        paintSelectedColorForMarker.setColor(selectedColor);

		canvas.drawCircle(sv_x, sv_y, selectionMarkerR, paintBlackFill);
		canvas.drawCircle(sv_x, sv_y, selectionMarkerR -2, paintWhiteFill);
		canvas.drawCircle(sv_x, sv_y, selectionMarkerR -4, paintBlackFill);
        canvas.drawCircle(sv_x, sv_y, selectionMarkerR -6, paintSelectedColorForMarker);

		return svBitmap;
	}
	
	private void setHueSelectedPosition(int x, int y) {
		hue_x = x;
		hue_y = y;
	}

	public void onClickOk() {
		onColorPickerSelectedListener.onSelected(selectedColor);
	}

    private void onClickCancel() {
        //TODO : add something to do when close color picker popup
    }

    @Override
	public void updatePresetColor(int color) {
		getInitialColorPosition(color);
        getInitialTPColor(color);
		updateHueBar(hue_x, hue_y);
		initSelectedColor(color);
	}

    private void getInitialTPColor(int color) {
        alpha = (color & 0xFF000000) >>> 24;
        alphaSeekBar.setProgress(alpha);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean isFromUser) {
        alpha = i;
        updatePreviewBox();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
