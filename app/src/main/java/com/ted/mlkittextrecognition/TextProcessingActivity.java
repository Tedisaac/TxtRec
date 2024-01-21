package com.ted.mlkittextrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ted.mlkittextrecognition.databinding.ActivityMainBinding;
import com.ted.mlkittextrecognition.databinding.ActivityTextProcessingBinding;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessingActivity extends AppCompatActivity {

    ActivityTextProcessingBinding activityTextProcessingBinding;
    Bitmap bitmap = null;
    String image;
    String TAG = "CameraXApp";
    int currentBlock = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTextProcessingBinding = ActivityTextProcessingBinding.inflate(getLayoutInflater());
        setContentView(activityTextProcessingBinding.getRoot());

        getImage();
    }

    private void getImage() {
        bitmap = BitmapTransfer.bitmap;
        setImage(bitmap);
        runTextRecognition(bitmap);

    }

    private void runTextRecognition(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        TextRecognizerOptions textRecognizerOptions = TextRecognizerOptions.DEFAULT_OPTIONS;
        TextRecognizer textRecognizer = TextRecognition.getClient(textRecognizerOptions);

        textRecognizer.process(inputImage)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> textBlocks = texts.getTextBlocks();
        if (textBlocks.isEmpty()) {
            showToast("No Text Found!");
            return;
        }

        for (int a = 0; a < textBlocks.size(); a++) {
            List<Text.Line> textLines = textBlocks.get(a).getLines();
            showLog("Blocks : " + textBlocks.get(a).getText());
            currentBlock = a;
            for (int b = 0; b < textLines.size(); b++) {
                List<Text.Element> textElements = textLines.get(b).getElements();
                showLog("Lines : " + textLines.get(b).getText());
                if (textLines.get(b).getText().equalsIgnoreCase("Driving Licence")) {
                    showToast("Valid Driving Licence");
                }
                if (textLines.get(b).getText().equalsIgnoreCase("Date of Expiry")) {
                    String expiryDate = textBlocks.get(currentBlock + 1).getLines().get(0).getText();
                    if (isFormatValid(expiryDate)) {
                        activityTextProcessingBinding.txtBlock.setText("Expiry Date : " + textBlocks.get(currentBlock + 1).getLines().get(0).getText());
                        return;
                    } else {
                        showToast("Please retake image");
                        finish();
                    }

                }
            }
        }
    }

    public static boolean isFormatValid(String input) {
        String regex = "^\\d{2}\\.\\d{2}\\.\\d{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
    private void setImage(Bitmap bitmap) {
        activityTextProcessingBinding.imgResult.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLog(String message) {
        Log.e(TAG, "showLog: " + message);
    }
}