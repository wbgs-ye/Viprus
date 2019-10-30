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

package be.brunoparmentier.wifikeyshare.model;

public enum WifiAuthType {
    OPEN("Open"),
    WEP("WEP"),
    WPA_PSK("WPA PSK"),
    WPA_EAP("WPA EAP"),
    WPA2_EAP("WPA2 EAP"),
    WPA2_PSK("WPA2 PSK");

    private final String printableName;

    WifiAuthType(String printableName) {
        this.printableName = printableName;
    }

    @Override
    public String toString() {
        return printableName;
    }
}
