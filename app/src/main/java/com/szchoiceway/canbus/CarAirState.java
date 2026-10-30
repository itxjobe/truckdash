package com.szchoiceway.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Receiver for the canbus app's climate broadcast ({@code com.choiceway.canbus.carairstruct}).
 *
 * <p>Android unparcels the extra by class name, so this class must be called
 * {@code com.szchoiceway.canbus.CarAirState}. Only the fields the dash shows are kept; the rest of
 * the record is read and dropped so the parcel stays in step. {@link #LAYOUT} is the wire order,
 * worked out from the broadcast; see docs/notes/climate-keys.md.
 */
public class CarAirState implements Parcelable {
    /** Each entry: B = byte used as a boolean, I = int, S = string; then the field name. */
    public static final String[] LAYOUT = {
        "B bAirOn", "B bAcOn", "B bOutCircleOn", "B bBigAutoOn", "B bSmallAutoOn", "B bDualOn",
        "B bMaxFrontOn", "B bRearOn", "B bFunDirectHead", "B bFunDirectLevel", "B bFunDirectFoot",
        "B bAcMax", "I byFunStrength", "S m_byLeftTemp", "S m_byRighTemp", "I m_byTempUnit",
        "B bRearLock", "B bHuaFenOn", "I byLeftColdLevel", "I byRightColdLevel", "B bAQSInCircle",
        "I bLeftSeatHotLevel", "I bRightSeatHotLevel", "I byMaxFunStrengthStall", "B bFunStrengthAuto",
        "B bSmallAutoHide", "B bDualHide", "B bFrontHotOn", "B bNanoeOn", "B bFanAuto",
        "B bFanDirection", "I m_OutSideTemp", "B bShowLeftAQS", "B bShowRightAQS", "I byLeftAQSLevel",
        "I byRightAQSLevel", "B bECOOn", "I byAirMode", "I byRearFunStrength", "S m_byRearLeftTemp",
        "S m_byRearRighTemp", "B bRearAutoOn", "B bRearAirOn", "B bRearFunDirectLevel",
        "B bRearFunDirectFoot", "B bFunDirectHeadFoot", "B bFunDirectLevelFoot", "B bAjustAirSoft",
        "B bAjustAirFast", "B bAjustAirAutoInOut", "B bMaxFrontOnHide", "B bTempLevel",
        "I bMaxFanLevel", "B bRearFunDirectHead", "B bAutoBaiDong", "B bRequestShowAirInfo",
        "B bWheelHeat", "B bLeftAuto", "B bRightAuto", "B bLeftFULL", "B bRightFULL", "B bMONO",
        "B bFunDirectHead_Right", "B bFunDirectLevel_Right", "B bFunDirectFoot_Right", "B bESP",
        "B bDoorLock", "I bRearMaxFanLevel",
    };

    /** What the reader needs from a Parcel; lets tests feed values without Android. */
    public interface Source {
        byte readByte();
        int readInt();
        String readString();
    }

    public boolean bAirOn, bAcOn, bOutCircleOn, bSmallAutoOn, bMaxFrontOn, bRearOn, bFunStrengthAuto, bFanAuto;
    public int byFunStrength, byMaxFunStrengthStall, bLeftSeatHotLevel, bRightSeatHotLevel;
    public String m_byLeftTemp = "", m_byRighTemp = "";

    public CarAirState() {}

    public static CarAirState read(Source in) {
        CarAirState s = new CarAirState();
        for (String entry : LAYOUT) {
            String name = entry.substring(2);
            switch (entry.charAt(0)) {
                case 'B': s.setFlag(name, in.readByte() != 0); break;
                case 'I': s.setInt(name, in.readInt()); break;
                default: s.setText(name, in.readString()); break;
            }
        }
        return s;
    }

    private void setFlag(String name, boolean v) {
        switch (name) {
            case "bAirOn": bAirOn = v; break;
            case "bAcOn": bAcOn = v; break;
            case "bOutCircleOn": bOutCircleOn = v; break;
            case "bSmallAutoOn": bSmallAutoOn = v; break;
            case "bMaxFrontOn": bMaxFrontOn = v; break;
            case "bRearOn": bRearOn = v; break;
            case "bFunStrengthAuto": bFunStrengthAuto = v; break;
            case "bFanAuto": bFanAuto = v; break;
            default: break;
        }
    }

    private void setInt(String name, int v) {
        switch (name) {
            case "byFunStrength": byFunStrength = v; break;
            case "byMaxFunStrengthStall": byMaxFunStrengthStall = v; break;
            case "bLeftSeatHotLevel": bLeftSeatHotLevel = v; break;
            case "bRightSeatHotLevel": bRightSeatHotLevel = v; break;
            default: break;
        }
    }

    private void setText(String name, String v) {
        if (name.equals("m_byLeftTemp")) m_byLeftTemp = v;
        else if (name.equals("m_byRighTemp")) m_byRighTemp = v;
    }

    public static final Parcelable.Creator<CarAirState> CREATOR = new Parcelable.Creator<CarAirState>() {
        @Override public CarAirState createFromParcel(final Parcel p) {
            return read(new Source() {
                public byte readByte() { return p.readByte(); }
                public int readInt() { return p.readInt(); }
                public String readString() { return p.readString(); }
            });
        }
        @Override public CarAirState[] newArray(int size) { return new CarAirState[size]; }
    };

    @Override public int describeContents() { return 0; }

    /** Receive-only: the dash never sends this record. */
    @Override public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException("receive-only");
    }
}
