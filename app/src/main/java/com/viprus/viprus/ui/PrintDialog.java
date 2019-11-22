/*
 * WiFiKeyShare. Share Wi-Fi passwords with QR codes or NFC tags.
 * Copyright (C) 2016 Bruno Parmentier <dev@brunoparmentier.be>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viprus.viprus.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.core.os.CancellationSignal;
import androidx.print.PrintHelper;

import com.viprus.viprus.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static eu.chainfire.libsuperuser.Debug.TAG;

/**
 * About dialog with HTML-formatted TextView and clickable links
 */

public class PrintDialog extends AlertDialog {
    private WebView mWebView;
    private String shiftTop;
    private String shiftBottom;
    private Integer rowId;
    private RadioGroup rowGroup1;
    private RadioGroup rowGroup2;
    private RadioGroup columnGroup;
    private RadioButton rowGroupButton;
    private RadioButton columnGroupButton;
    private String rowGroupButtonText;
    private String columnGroupButtonText;
    private RadioGroup.OnCheckedChangeListener rowG1Listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            rowGroup1 = findViewById(R.id.rowBar1);
            rowGroup2 = findViewById(R.id.rowBar2);
            if (checkedId != -1) {
                rowGroup2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                rowGroup2.clearCheck(); // clear the second RadioGroup!
                rowGroup2.setOnCheckedChangeListener(rowG2Listener); //reset the listener
            }
            toggleCheck();
        }
    };
    private RadioGroup.OnCheckedChangeListener rowG2Listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            rowGroup1 = findViewById(R.id.rowBar1);
            rowGroup2 = findViewById(R.id.rowBar2);
            if (checkedId != -1) {
                rowGroup1.setOnCheckedChangeListener(null);
                rowGroup1.clearCheck();
                rowGroup1.setOnCheckedChangeListener(rowG1Listener);
            }
            toggleCheck();
        }
    };

    LayoutInflater inflater = getLayoutInflater();
    View dialogLayout = inflater.inflate(R.layout.print_dialog, null);
    private void doWebViewPrint(String shiftTop, String shiftBottom) {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(getContext());
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        ContextWrapper cw = new ContextWrapper(getContext());
        File path1 = cw.getDir("profile", Context.MODE_PRIVATE);
        File f = new File(path1, "qrCode.png");
        String imagePath = "file://" + f.getAbsolutePath();
        String htmlDocument = "<html>" +
                "<link rel='stylesheet' href='file:///android_asset/style.css'/>" +
                "<style>@page{margin:0;} img{height:30mm;width:30mm;}</style> " +
                "<body style='margin: 0; padding: 0'>" +
                "<img style='margin-left: " + shiftTop + ";margin-top:" + shiftBottom + ";' src='" + imagePath + "''>" +
                "</body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        mWebView = webView;
    }
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getContext()
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
    }
    private void toggleCheck() {
        Button submitPrint = findViewById(R.id.submitPrint);
        rowGroup1 = findViewById(R.id.rowBar1);
        rowGroup2 = findViewById(R.id.rowBar2);
        columnGroup = findViewById(R.id.colBar);
        if (columnGroup.getCheckedRadioButtonId() != -1)
        {
            submitPrint.setEnabled(true);
        }
        else if ((rowGroup1.getCheckedRadioButtonId() != -1) && (rowGroup2.getCheckedRadioButtonId() == -1)) {
            submitPrint.setEnabled(true);
        }
        else if ((rowGroup2.getCheckedRadioButtonId() != -1) && (rowGroup1.getCheckedRadioButtonId() == -1)) {
            submitPrint.setEnabled(true);
        }
        else
        {
            submitPrint.setEnabled(false);
        }

    }

    public PrintDialog(Context context) {

        super(context);
        setContentView(R.layout.print_dialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.print_dialog, null);
        setView(dialogLayout);
        setTitle(R.string.print_dialog_title);
        RadioGroup rowG1 = dialogLayout.findViewById(R.id.rowBar1);
        RadioGroup rowG2 = dialogLayout.findViewById(R.id.rowBar2);
        rowG1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
        rowG2.clearCheck();
        rowG1.setOnCheckedChangeListener(rowG1Listener);
        rowG2.setOnCheckedChangeListener(rowG2Listener);
        Button submitPrint = dialogLayout.findViewById(R.id.submitPrint);
        submitPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rowGroup1 = findViewById(R.id.rowBar1);
                columnGroup  = findViewById(R.id.colBar);
                rowGroup2 = findViewById(R.id.rowBar2);
                if ((rowGroup1.getCheckedRadioButtonId() != -1) && (rowGroup2.getCheckedRadioButtonId() == -1)) {
                    rowId = rowGroup1.getCheckedRadioButtonId();
                }
                else if ((rowGroup2.getCheckedRadioButtonId() != -1) && (rowGroup1.getCheckedRadioButtonId() == -1)) {
                    rowId = rowGroup2.getCheckedRadioButtonId();
                }
                /*doWebViewPrint("0","0");*/
                rowGroupButton = findViewById(rowId);
                rowGroupButtonText = rowGroupButton.getText().toString();
                int colId = columnGroup.getCheckedRadioButtonId();
                columnGroupButton = findViewById(colId);
                columnGroupButtonText = columnGroupButton.getText().toString();
                Integer margin = 35;
                if (columnGroupButtonText.equals("1")){
                    shiftTop = "12mm";
                }
                else if (columnGroupButtonText.equals("2")){
                    shiftTop = 44 + "mm";
                }
                else if (columnGroupButtonText.equals("3")){
                    shiftTop = 76 + "mm";
                }
                else if (columnGroupButtonText.equals("4")){
                    shiftTop = 108 + "mm";
                }
                else if (columnGroupButtonText.equals("5")){
                    shiftTop = 140 + "mm";
                }
                else if (columnGroupButtonText.equals("6")){
                    shiftTop = 172 + "mm";
                }
                if (rowGroupButtonText.equals("A")){
                    shiftBottom = "10mm";
                }
                else if (rowGroupButtonText.equals("B")){
                    shiftBottom = 42 + "mm";
                }
                else if (rowGroupButtonText.equals("C")){
                    shiftBottom = 74 + "mm";
                }
                else if (rowGroupButtonText.equals("D")){
                    shiftBottom = 106 + "mm";
                }
                else if (rowGroupButtonText.equals("E")){
                    shiftBottom = 138 + "mm";
                }
                else if (rowGroupButtonText.equals("F")){
                    shiftBottom = 170 + "mm";
                }
                else if (rowGroupButtonText.equals("G")){
                    shiftBottom = 202 + "mm";
                }
                else if (rowGroupButtonText.equals("H")){
                    shiftBottom = 233 + "mm";
                }
                doWebViewPrint(shiftTop,shiftBottom);
                dismiss();
            }
        });

    }

    @Override
    public void show() {
        super.show();
        /* Get TextView from the original AlertDialog layout */
        /* TextView aboutContent = (TextView) findViewById(android.R.id.message);
        aboutContent.setText(Html.fromHtml(aboutMessage));
        aboutContent.setMovementMethod(LinkMovementMethod.getInstance()); // can click on links

         */
    }
}
