/*
 * WiFiKeyShare. Share Wi-Fi passwords with QR codes or NFC tags.
 * Copyright (C) 2016, 2018 Bruno Parmentier <dev@brunoparmentier.be>
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

import android.net.wifi.WifiConfiguration;

import java.io.Serializable;
import java.util.Objects;

public class WifiNetwork implements Serializable {
    private String ssid;
    private String key;
    private WifiAuthType authType;
    private boolean isHidden;

    public WifiNetwork(String ssid, WifiAuthType authType, String key, boolean isHidden) {
        this.ssid = ssid;
        this.key = key;
        this.authType = authType;
        this.isHidden = isHidden;
    }

    public String getSsid() {
        return ssid;
    }

    public String getKey() {
        return key;
    }

    public WifiAuthType getAuthType() {
        return authType;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public boolean isPasswordProtected() {
        return authType == WifiAuthType.WPA_PSK
                || authType == WifiAuthType.WPA2_PSK
                || authType == WifiAuthType.WEP
                || !key.isEmpty();
    }

    public boolean needsPassword() {
        return isPasswordProtected() && key.isEmpty();
    }

    public static WifiNetwork fromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        String ssid = getSsidFromWifiConfiguration(wifiConfiguration);
        WifiAuthType authType = getSecurityFromWifiConfiguration(wifiConfiguration);
        String key = "";
        boolean isHidden = wifiConfiguration.hiddenSSID;

        return new WifiNetwork(ssid, authType, key, isHidden);
    }

    private static String getSsidFromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        String ssid = wifiConfiguration.SSID;
        if (ssid != null) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                return ssid.substring(1, ssid.length() - 1);
            } else {
                return ssid; // FIXME: convert hex string to ascii string
            }
        }
        return "";
    }

    private static WifiAuthType getSecurityFromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            if (wifiConfiguration.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                return WifiAuthType.WPA2_PSK;
            } else { // WifiConfiguration.Protocol.WPA
                return WifiAuthType.WPA_PSK;
            }
        }
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            if (wifiConfiguration.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                return WifiAuthType.WPA2_EAP;
            } else { // WifiConfiguration.Protocol.WPA
                return WifiAuthType.WPA_EAP;
            }
        }
        return (wifiConfiguration.wepKeys[0] != null) ? WifiAuthType.WEP : WifiAuthType.OPEN;
    }

    @Override
    public String toString() {
        return "WifiNetwork{" +
                ", ssid='" + ssid + '\'' +
                ", key='" + key + '\'' +
                ", authType=" + authType +
                ", isHidden=" + isHidden +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WifiNetwork that = (WifiNetwork) o;
        return isHidden == that.isHidden &&
                Objects.equals(ssid, that.ssid) &&
                Objects.equals(key, that.key) &&
                authType == that.authType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ssid, key, authType, isHidden);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static boolean isValidKeyLength(WifiAuthType authType, String key)
            throws WifiException {
        int keyLength = key.length();

        if (authType == WifiAuthType.WEP) {
            if (keyLength != 5 && keyLength != 13) {
                throw new WifiException(WifiException.WEP_KEY_LENGTH_ERROR);
            }
        } else { // WPA
            if ((keyLength >= 5 && keyLength < 8) || keyLength > 63) { // TODO: support hex key (64)
                throw new WifiException(WifiException.WPA_KEY_LENGTH_ERROR);
            }
        }

        return true;
    }
}
