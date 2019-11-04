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
    private RadioGroup rowGroup;
    private RadioGroup columnGroup;
    private RadioButton rowGroupButton;
    private RadioButton columnGroupButton;
    private String rowGroupButtonText;
    private String columnGroupButtonText;

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
                "<style>img{height:37mm;width:37mm;}</style>" +
                "<body>" +
                "<img style='margin-left:"+ shiftTop + ";margin-top:" + shiftBottom + ";'height='37mm' width='37mm' src='" + imagePath + "'>" +
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
        rowGroup = findViewById(R.id.rowBar);
        columnGroup = findViewById(R.id.colBar);
        if (columnGroup.getCheckedRadioButtonId() == -1)
        {
            submitPrint.setEnabled(false);
        }
        else if (rowGroup.getCheckedRadioButtonId() == -1)
        {
            submitPrint.setEnabled(false);
        }
        else
        {
            submitPrint.setEnabled(true);
        }

    }

    public PrintDialog(Context context) {

        super(context);
        setContentView(R.layout.print_dialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.print_dialog, null);
        setView(dialogLayout);
        setTitle(R.string.print_dialog_title);
        RadioGroup rowG = dialogLayout.findViewById(R.id.rowBar);
        rowG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                toggleCheck();
            }
        });
        RadioGroup colG = dialogLayout.findViewById(R.id.colBar);
        colG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                toggleCheck();
            }
        });


        Button submitPrint = dialogLayout.findViewById(R.id.submitPrint);
        submitPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rowGroup = findViewById(R.id.rowBar);
                columnGroup  = findViewById(R.id.colBar);
                /*doWebViewPrint("0","0");*/
                int rowId = rowGroup.getCheckedRadioButtonId();
                rowGroupButton = findViewById(rowId);
                rowGroupButtonText = rowGroupButton.getText().toString();
                int colId = columnGroup.getCheckedRadioButtonId();
                columnGroupButton = findViewById(colId);
                columnGroupButtonText = columnGroupButton.getText().toString();
                Integer margin = 39;
                if (columnGroupButtonText.equals("A")){
                    shiftTop = "0mm";
                }
                else if (columnGroupButtonText.equals("B")){
                    shiftTop = "39mm";
                }
                else if (columnGroupButtonText.equals("C")){
                    shiftTop = (margin*2) + "mm";
                }
                else if (columnGroupButtonText.equals("D")){
                    shiftTop = (margin*3) + "mm";
                }
                else if (columnGroupButtonText.equals("E")){
                    shiftTop = (margin*4) + "mm";
                }
                if (rowGroupButtonText.equals("1")){
                    shiftBottom = "0mm";
                }
                else if (rowGroupButtonText.equals("2")){
                    shiftBottom = (margin) + "mm";
                }
                else if (rowGroupButtonText.equals("3")){
                    shiftBottom = (margin*2) + "mm";
                }
                else if (rowGroupButtonText.equals("4")){
                    shiftBottom = (margin*3) + "mm";
                }
                else if (rowGroupButtonText.equals("5")){
                    shiftBottom = (margin*4) + "mm";
                }
                else if (rowGroupButtonText.equals("6")){
                    shiftBottom = (margin*5) + "mm";
                }
                else if (rowGroupButtonText.equals("7")){
                    shiftBottom = (margin*6) + "mm";
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
