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
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.viprus.viprus.R;

import java.io.File;

/**
 * About dialog with HTML-formatted TextView and clickable links
 */

public class addDialog extends AlertDialog {
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

    LayoutInflater inflater = getLayoutInflater();
    View dialogLayout = inflater.inflate(R.layout.dialog_add_man, null);

    public addDialog(Context context) {

        super(context);
        setContentView(R.layout.dialog_add_man);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_add_man, null);
        setView(dialogLayout);
        setTitle("Add WiFi Network");
                dismiss();
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
