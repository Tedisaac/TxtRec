package com.ted.mlkittextrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ted.mlkittextrecognition.databinding.ActivityTextProcessingBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessingActivity extends AppCompatActivity {

    ActivityTextProcessingBinding activityTextProcessingBinding;
    Bitmap bitmap = null;
    String image;
    String TAG = "CameraXApp";
    String textTag = "";
    int currentBlock = 0;

    String testID1 = "35298349";
    String testID2 = "10176916";
    String testDL1 = "IDL-CO1314";
    String testDL2 = "UGU102";
    String testID3 = "31143317";
    /*
    Driving Licence Regex
    */
    String newLicenceNoRegex = ".*(ENCE No|LICENCE No|licence no|LICENCE NO|Licence No|LICENGE NO|ICENCE No|LUCENGCE No|LUCENSE No|LICENSE No|LLCENSE No|LICENSÈ No|UICENSE N|LICENSĖ No|LICENSE NO|UCENSE No|UCENCE No|LIGENCE NA|LCENCE No|LRCENCE No|LGENSE No|LICENCE Nơ|CENCE Na).*";
    String oldLicenceNoRegex = ".*(ENCE No|Driv|Driving icence No|driving icence no|Driving ICENCE No|Driving IcEnCe No|Driving Licence No|driving licence no|Driving LICENCE No|Driving LicEnCe No|Driving icence No:|Diriving Licence No:|Driving Licence No:|Driving Licence o:|Driving icence Noc|Driving Licence Noc|LICENCE Na|Driving cence No:|Driving Licence NG|CENCE Na).*";
    String dlRegex = ".*(Driving Licence|DRIVING LICENCE).*";
    String licenceIDRegex = ".*(Natio|Nationa|National|NATIONAL|nationa|national|NATIONAL|National ID|National ID No|National ID No.|National ID No.*|National ID No.|NATIONAL IÔ No|NATIONAL IĎ No|National D Ne:|NATIONAL ID NO|NATIOAL IO N|National D No|National ID No:|NATIONA ID g|NATIONAL ID No|National iD No:|National iD Na:|NATIONÀL ID NO|NATIONAL Na).*";
    String dateOfExpiryRegex = ".*(EXP|Expiry|Date of Expiry|date of expiry|DATE OF EXPIRY|DATE OF EPIRY|DATE OF EXPIRY COUNT oe|DATE OE EXPIRY|DATE GF EXPIRY|DATE OF EXPRY|DATE OFEXPRY|DATE OF EXPIY|DATE OE EPIY|DATE OF EXPSRY|DATE UF EXPIRY|bATE UF EXPIR?|UATE OF EXPIR?|Date of Exniry|DATE OF EXXPIRY|DATE OF EXPIAY|Date of Epry|OATE OF EXPIRY|DATE OF EXPIAY|Date of Éxpiry).*";

    /*
    Date Format Regex
     */
    String dateFormatRegex = ".*(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\s(January|February|March|April|May|June|July|August|September|October|November|December)\\s(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\s(0[1-9]|1[0-2])\\.(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\s(0[1-9]|1[0-2])\\s(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\.\\s(0[1-9]|1[0-2])\\.(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\.\\s(0[1-9]|1[0-2])\\s(19[0-9][0-9]|20[0-9][0-9]).*|.*(0[1-9]|[12][0-9]|3[01])\\/(0[1-9]|1[0-2])\\/(19[0-9][0-9]|20[0-9][0-9]).*";

    /*
    PSV Badge Regex
     */
    String psvRegex = ".*(Taxis|TAXIS DRIVER PSV BADGE).*";
    String issueDateRegex = ".*(Issue Date:|issue date:|ISSUE DATE:|Issue Date).*";
    String expiryDateRegex = ".*(Expiry Date:|expiry date:|EXPIRY DATE:|Expiry Date).*";
    String idNoRegex = ".*(ID No:|id no:|ID NO:|Id No:).*";
    String psvIDRegex = ".*(ID Number|id number|ID NUMBER|Id Number|ID No|id no|ID NO|Id No).*";
    String psvDLRegex = ".*(DL Ref:|dl ref:|DL REF:|dl ref:).*|.*(DL Number:|dl number:|DL NUMBER:|Dl Number:).*";

    /*
    Police Clearance Certificate Regex
     */
    String clearanceRegex = ".*(POLICE CLEARANCE CERTIFICATE|police clearance certificate|POLICE CLEARANCE CERTIFICATE|Police Clearance Certificate|POLICE CLEARANCE CERTIFICATE).*";

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
                .addOnSuccessListener(texts -> {
                    processTextRecognitionResult(texts, "Licence");
                })
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
    }

    private void processTextRecognitionResult(Text texts, String tag) {
        textTag = tag;
        List<Text.TextBlock> textBlocks = texts.getTextBlocks();
        if (textBlocks.isEmpty()) {
            showToast("No Text Found!");
            return;
        }

        if (tag.equalsIgnoreCase("Licence")) {
            showLog("Licence Text Recognition");
            processDrivingLicenceText(texts);
        } else if (tag.equalsIgnoreCase("Clearance")) {
            showLog("Log Book Text Recognition");
            processClearanceCertificateText(texts);
        } else if (tag.equalsIgnoreCase("PSV")) {
            showLog("PSV Badge Text Recognition");
            processPSVBadgeText(texts);
        }


    }

    boolean isValidPSV = false;
    boolean isPsvIDNumberValid = false;
    boolean isPsvDLNumberValid = false;
    String validPSVText = "";
    String psvErrorReason = "";
    String psvDLNumberText = "";
    String psvIDNumberText = "";
    String psvExpiryDateText = "";
    String oldPsvExpiryDate = "Expiry Date:";

    private void processPSVBadgeText(Text texts) {
        List<Text.TextBlock> textBlocks = texts.getTextBlocks();
        for (int a = 0; a < textBlocks.size(); a++) {
            if (isFormatValid(psvRegex, textBlocks.get(a).getText())) {
                isValidPSV = true;
                break;
            }
        }

        if (isValidPSV) {
            validPSVText = "PSV is valid";
            for (int b = 0; b < textBlocks.size(); b++) {
                showLog("Block : " + textBlocks.get(b).getText());
                if (textBlocks.get(b).getText().contains(testID1)) {
                    isPsvIDNumberValid = true;
                    psvIDNumberText = "ID number matches";
                }
                if (textBlocks.get(b).getText().contains(testDL1)) {
                    isPsvDLNumberValid = true;
                    psvDLNumberText = "DL number matches";
                }
                if (isFormatValid(expiryDateRegex, textBlocks.get(b).getText())) {
                    if (textBlocks.get(b).getLines().size() > 1) {
                        psvExpiryDateText = textBlocks.get(b).getLines().get(1).getText().replace(oldPsvExpiryDate, "");
                    } else {
                        psvExpiryDateText = textBlocks.get(b).getText().replace(oldPsvExpiryDate, "");
                    }
                } else if (isFormatValid(dateOfExpiryRegex, textBlocks.get(b).getText())) {
                    if (textBlocks.get(b).getLines().size() > 1) {
                        psvExpiryDateText = textBlocks.get(b).getLines().get(1).getText();
                    } else {
                        if (containsYear(textBlocks.get(b + 1).getText())) {
                            psvExpiryDateText = textBlocks.get(b + 1).getText();
                        } else if (textBlocks.size() > b + 2 && containsYear(textBlocks.get(b + 2).getText())) {
                            psvExpiryDateText = textBlocks.get(b + 2).getText();
                        } else if (textBlocks.size() > b + 3 && containsYear(textBlocks.get(b + 3).getText())) {
                            psvExpiryDateText = textBlocks.get(b + 3).getText();
                        }
                    }
                } else if (isFormatValid(issueDateRegex, textBlocks.get(b).getLines().get(0).getText())) {
                    if (textBlocks.get(b).getLines().size() > 1) {
                        psvExpiryDateText = textBlocks.get(b).getLines().get(1).getText().replace(oldPsvExpiryDate, "");
                    }
                } else if (isFormatValid(idNoRegex, textBlocks.get(b).getLines().get(0).getText())) {
                    if (textBlocks.get(b).getLines().size() > 1) {
                        psvExpiryDateText = textBlocks.get(b).getLines().get(2).getText().replace(oldPsvExpiryDate, "");
                    }
                } else {
                    psvErrorReason = "PSV Expiry Date not Found";
                }

            }
        } else {
            validPSVText = "Please use a valid PSV";
        }

        if (!isPsvDLNumberValid || !isPsvIDNumberValid || psvExpiryDateText.isEmpty()) {
            showLog("PSV status : " + validPSVText);
            showLog("DL Number Match : " + psvDLNumberText);
            showLog("ID Number Match: " + psvIDNumberText);
            showLog("PSV Expiry : " + psvExpiryDateText);
            showLog("Error reason : " + psvErrorReason);
            showToast("Error processing data. Kindly retake photo");
            switchToCameraScreen();
        } else {
            showLog("DL Number Match : " + psvDLNumberText);
            showLog("ID Number Match: " + psvIDNumberText);
            showLog("PSV Expiry : " + psvExpiryDateText);
            activityTextProcessingBinding.txtBlockExpiry.setText("PSV Expiry Date : " + psvExpiryDateText);
            activityTextProcessingBinding.txtBlockLicenceNo.setText("DL Number Match : " + psvDLNumberText);
            activityTextProcessingBinding.txtBlockID.setText("ID Number Match : " + psvIDNumberText);
            activityTextProcessingBinding.txtBlockValidLicence.setText("PSV Status : " + validPSVText);
            showToast("Successfully read the data");
        }


    }

    boolean isValidClearanceCertificate = false;
    String validClearanceText = "";
    String clearanceDateOfIssueText = "";
    String dateValidationText = "";
    String expiryValidationText = "";
    String clearanceErrorReason = "";
    boolean isClearanceIDNumberValid = false;
    boolean isClearanceDateOfIssueValid = false;
    boolean isClearanceExpired = false;
    private void processClearanceCertificateText(Text texts) {
        List<Text.TextBlock> textBlocks = texts.getTextBlocks();
        for (int a = 0; a < textBlocks.size(); a++) {
            if (isFormatValid(clearanceRegex, textBlocks.get(a).getText())) {
                isValidClearanceCertificate = true;
                break;
            }
        }

        if (isValidClearanceCertificate) {
            validClearanceText = "Valid Police Clearance Certificate";
            for (int b = 0; b < textBlocks.size(); b++) {
                showLog("Blocks : " + textBlocks.get(b).getText());
                if (containsYear(textBlocks.get(b).getText())) {
                    clearanceDateOfIssueText = textBlocks.get(b).getText().replace("Date.", "").replace("Date", "").replace(".", "");
                }
                if (textBlocks.get(b).getText().contains(testID3)) {
                    isClearanceIDNumberValid = true;
                }
            }
        } else {
            validClearanceText = "Please use a valid Police Clearance Certificate";
        }
        if (!clearanceDateOfIssueText.isEmpty()) {
            if (!isDateGreaterThanToday(clearanceDateOfIssueText)) {
                isClearanceDateOfIssueValid = true;
                dateValidationText = "Date of issue Valid";
                if (!isDateGreaterThanTodayAfterAddingOneYear(clearanceDateOfIssueText)) {
                    isClearanceExpired = true;
                    expiryValidationText = "Certificate is expired";
                } else {
                    expiryValidationText = "Certificate is not expired";
                }
            } else {
                dateValidationText = "Date of Issue not valid";
            }

        } else {
            clearanceErrorReason = "Date of Issue not found";
        }

        if (isClearanceExpired || !isClearanceDateOfIssueValid || !isClearanceIDNumberValid) {
            showLog("ID Match : " + isClearanceDateOfIssueValid);
            showLog("Certificate Expiry Status : " + expiryValidationText);
            showLog("Date of Issue : " + clearanceDateOfIssueText);
            showLog("Date of Issue Validity : " + dateValidationText);
            showLog("Certificate Status : " + validClearanceText);
            showToast("Error processing data. Kindly retake photo");
            switchToCameraScreen();
        } else {
            showLog("Certificate Expiry Status : " + expiryValidationText);
            showLog("Date of Issue : " + clearanceDateOfIssueText);
            showLog("Date of Issue Validity : " + dateValidationText);
            showLog("Certificate Status : " + validClearanceText);
            activityTextProcessingBinding.txtBlockLicenceNo.setText("Certificate Expiry Status : " + expiryValidationText);
            activityTextProcessingBinding.txtBlockID.setText("Date of Issue : " + clearanceDateOfIssueText);
            activityTextProcessingBinding.txtBlockExpiry.setText("Date of Issue Validity : " + dateValidationText);
            activityTextProcessingBinding.txtBlockValidLicence.setText("Certificate Status : " + validClearanceText);
            showToast("Successfully read the data");
        }

    }


    String idNumber = "";
    String dlExpiryDate = "";
    String licenceErrorReason = "";
    String validLicenceText = "";
    String dlExpiryText = "";
    String dlNumberText = "";
    boolean isValidLicence = false;
    boolean isNewLicenceNo = false;

    // Validity of Title texts read
    boolean isNationalIDTextFormatValid = false;
    boolean isExpiryDateTextFormatValid = false;
    boolean isOldLicenceNumberTextFormatValid = false;
    boolean isNewLicenceNumberTextFormatValid = false;
    String reason = "";

    private void processDrivingLicenceText(Text texts) {
        List<Text.TextBlock> textBlocks = texts.getTextBlocks();
        for (int a = 0; a < textBlocks.size(); a++) {
            if (isFormatValid(dlRegex, textBlocks.get(a).getText())) {
                isValidLicence = true;
                break;
            }
        }

        if (isValidLicence) {
            validLicenceText = "Licence is Valid";
            for (int b = 0; b < textBlocks.size(); b++) {
                showLog("Block : " + textBlocks.get(b).getText());
                currentBlock = b;
                if (isFormatValid(licenceIDRegex, textBlocks.get(currentBlock).getText())) {
                    isNationalIDTextFormatValid = true;
                    if (textBlocks.get(currentBlock).getLines().size() > 1) {
                        idNumber = textBlocks.get(currentBlock).getLines().get(1).getText();
                    } else {
                        idNumber = textBlocks.get(currentBlock + 1).getText();
                    }
                }
                if (isFormatValid(dateOfExpiryRegex, textBlocks.get(currentBlock).getText())) {
                    isExpiryDateTextFormatValid = true;
                    if (textBlocks.get(currentBlock).getLines().size() > 1) {
                        dlExpiryDate = textBlocks.get(currentBlock).getLines().get(1).getText();
                    } else {
                        List<Text.Line> textLines = textBlocks.get(currentBlock + 1).getLines();
                        if (textLines.size() > 1) {
                            dlExpiryDate = "";
                            for (int c = 0; c < textLines.size(); c++) {
                                dlExpiryDate = dlExpiryDate.concat(textLines.get(c).getText());
                                dlExpiryDate = dlExpiryDate.concat(" ");
                                showLog("Line " + c + " : " + textLines.get(c).getText());
                            }
                        } else {
                            dlExpiryDate = "";
                            dlExpiryDate = textLines.get(0).getText();
                        }
                    }
                }
                if (isFormatValid(oldLicenceNoRegex, textBlocks.get(currentBlock).getText())) {
                    isOldLicenceNumberTextFormatValid = true;
                    if (textBlocks.get(currentBlock).getLines().size() > 1) {
                        dlNumberText = textBlocks.get(currentBlock).getLines().get(1).getText();
                    } else {
                        dlNumberText = textBlocks.get(currentBlock + 1).getText();
                    }
                } else if (isFormatValid(newLicenceNoRegex, textBlocks.get(currentBlock).getText())) {
                    isNewLicenceNumberTextFormatValid = true;
                    isNewLicenceNo = true;
                    if (textBlocks.get(currentBlock).getLines().size() > 1) {
                        dlNumberText = textBlocks.get(currentBlock).getLines().get(1).getText();
                    } else {
                        dlNumberText = textBlocks.get(currentBlock + 1).getText();
                    }
                }

            }
        } else {
            validLicenceText = "Please use a valid Driving Licence";
        }

        if (!dlExpiryDate.isEmpty()) {
            if (isFormatValid(dateFormatRegex, dlExpiryDate)) {
                dlExpiryText = dlExpiryDate;
            } else {
                for (int d = 0; d < textBlocks.size(); d++) {
                    if (isFormatValid(dateOfExpiryRegex, textBlocks.get(d).getText())) {
                        dlExpiryDate = "";
                        dlExpiryDate = dlExpiryDate.concat(textBlocks.get(d + 1).getText());
                        dlExpiryDate = dlExpiryDate.concat(" ");
                        if (containsYear(textBlocks.get(d + 2).getText())) {
                            dlExpiryDate = dlExpiryDate.concat(textBlocks.get(d + 2).getText());
                        }
                    }
                }
                if (isFormatValid(dateFormatRegex, dlExpiryDate)) {
                    dlExpiryText = dlExpiryDate;
                } else {
                    licenceErrorReason = "Date Format not valid";
                }
            }

        }

        if (idNumber.isEmpty() || dlExpiryDate.isEmpty() || dlNumberText.isEmpty() || !containsYear(dlExpiryDate)) {
            showLog("-------------------------------------");
            showLog("is Valid Licence Text Format Valid? : " + isValidLicence);
            showLog("is ID Text Format Valid? : " + isNationalIDTextFormatValid);
            showLog("is Old Licence Number Text Format Valid? : " + isOldLicenceNumberTextFormatValid);
            showLog("is New Licence Number Text Format Valid? : " + isNewLicenceNumberTextFormatValid);
            showLog("is Expiry Date Text Format Valid? : " + isExpiryDateTextFormatValid);
            reason = determineLicenceFailureReason(isValidLicence, isNationalIDTextFormatValid, isOldLicenceNumberTextFormatValid, isNewLicenceNumberTextFormatValid, isExpiryDateTextFormatValid);
            showLog("-------------------------------------");
            showLog("ID : " + idNumber);
            showLog("DL Number : " + dlNumberText);
            showLog("New DL Number? : " + isNewLicenceNo);
            showLog("DL Expiry : " + dlExpiryDate);
            showLog("Error reason : " + licenceErrorReason);
            showToast("Error processing data, " + reason);
            showLog("Error processing data, " + reason);

            activityTextProcessingBinding.txtBlockExpiry.setText("Licence expiry date : " + dlExpiryText);
            activityTextProcessingBinding.txtBlockID.setText("ID number : " + idNumber);
            activityTextProcessingBinding.txtBlockLicenceNo.setText("DL number : " + dlNumberText);
            activityTextProcessingBinding.txtBlockValidLicence.setText("Licence status : " + validLicenceText);
            //switchToCameraScreen();
        } else {
            showLog("-------------------------------------");
            showLog("is Valid Licence Text Format Valid? : " + isValidLicence);
            showLog("is ID Text Format Valid? : " + isNationalIDTextFormatValid);
            showLog("is Old Licence Number Text Format Valid? : " + isOldLicenceNumberTextFormatValid);
            showLog("is New Licence Number Text Format Valid? : " + isNewLicenceNumberTextFormatValid);
            showLog("is Expiry Date Text Format Valid? : " + isExpiryDateTextFormatValid);
            showLog("-------------------------------------");
            showLog("ID : " + idNumber);
            showLog("DL Number : " + dlNumberText);
            showLog("DL Expiry : " + dlExpiryDate);
            activityTextProcessingBinding.txtBlockExpiry.setText("Licence expiry date : " + dlExpiryText);
            activityTextProcessingBinding.txtBlockID.setText("ID number : " + idNumber);
            activityTextProcessingBinding.txtBlockLicenceNo.setText("DL number : " + dlNumberText);
            activityTextProcessingBinding.txtBlockValidLicence.setText("Licence status : " + validLicenceText);
            showToast("Successfully read the data");
        }
    }

    private String determineLicenceFailureReason(boolean isValidLicence, boolean isNationalIDTextFormatValid, boolean isOldLicenceNumberTextFormatValid, boolean isNewLicenceNumberTextFormatValid, boolean isExpiryDateTextFormatValid) {
        if (!isValidLicence) {
            return "Invalid Driver's Licence";
        } else if (!isNationalIDTextFormatValid) {
            return "Invalid ID Title Text";
        } else if (!isOldLicenceNumberTextFormatValid && !isNewLicenceNumberTextFormatValid) {
            return "Invalid Licence No Text";
        }  else if (!isExpiryDateTextFormatValid) {
            return "Invalid Expiry Text";
        } else return "";
    }

    public static boolean containsYear(String text) {
        Pattern yearRegex = Pattern.compile("20[0-9][0-9]");

        Matcher match = yearRegex.matcher(text);

        return match.find();
    }


    public static boolean isFormatValid(String regex, String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public String changeDateFormat(String date, String fromFormat, String toFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(fromFormat);
        Date parsedDate = null;
        try {
            parsedDate = formatter.parse(date);
        } catch (ParseException e) {
            showLog(e.getMessage());
        }
        SimpleDateFormat toFormatter = new SimpleDateFormat(toFormat);
        String convertedDate = toFormatter.format(parsedDate);
        return convertedDate;
    }

    public boolean isDateGreaterThanToday(String date) {
        Date parsedDate = null;

        try {
            parsedDate = new SimpleDateFormat("dd MMMM yyyy").parse(date);
        } catch (Exception e) {
            showLog(e.getMessage());
        }

        showLog("Date " + parsedDate);

        Date today = new Date();

        return parsedDate.after(today);
    }

    public boolean isDateGreaterThanTodayAfterAddingOneYear(String date) {
        Date parsedDate = null;

        try {
            parsedDate = new SimpleDateFormat("dd MMMM yyyy").parse(date);
        } catch (Exception e) {

        }

        Date newDate = new Date(parsedDate.getTime() + 365L * 24 * 60 * 60 * 1000);

        showLog("New Date: " + newDate);

        Date today = new Date();

        return newDate.after(today);
    }

    private void setImage(Bitmap bitmap) {
        activityTextProcessingBinding.imgResult.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLog(String message) {
        Log.e(TAG, message);
    }

    private void switchToCameraScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switchToCameraScreen();
    }
}