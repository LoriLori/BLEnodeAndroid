package org.blenoAndroid;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.content.Context;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class BleConnection extends BluetoothGattCallback {
    private static final Logger log = LoggerFactory.getLogger(BleConnection.class);


    private final BluetoothManager mBluetoothManager;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothDevice mDevice;
    private final Context mContext;
    private final OnMessage mOnMessage;
    private int mConnectionState;
    private BluetoothGattCharacteristic mCharData;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mBLEService;

    private short mMessagePos;
    public StringBuffer mMessage;


    public BleConnection(Context context, String address, BleConnection.OnMessage onMessage) {
        mOnMessage = onMessage;
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(address);

        mBluetoothGatt = mDevice.connectGatt(context.getApplicationContext(), true, this);
    }

    @Override
    public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        byte[] val = characteristic.getValue();
        int valLength = val.length;

        if(valLength>0) {
            ByteBuffer pos = ByteBuffer.wrap(val, 0, 2);
            pos.order(ByteOrder.LITTLE_ENDIAN);

            short posShort = pos.getShort();

            log.debug("onCharacteristicChanged " + toHexString(val) + " " + posShort);

            val = Arrays.copyOfRange(val, 2, valLength);

            if(mMessagePos==posShort) {
                mMessagePos++;
                String ret = new String(val, StandardCharsets.US_ASCII);
                mMessage.append(ret);
            } else {
                log.debug("onCharacteristicChanged duplicate " + posShort);

            }
            mBluetoothGatt.readCharacteristic(mCharData);
        } else {
            log.debug("onCharacteristicChanged message "+mMessage.toString());
            //mOnMessage.onMessage(mMessage.toString());
            try {
                JSONArray jsonObj = new JSONArray(mMessage.toString());
                mOnMessage.onMessage(jsonObj.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMessagePos=0;
            mMessage = new StringBuffer();
        }

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        mBluetoothGatt.readCharacteristic(mCharData);
    }

    public void request () {

        mMessage = new StringBuffer();

        mCharData.setValue(new byte[1]);
        mBluetoothGatt.writeCharacteristic(mCharData);

    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
        log.debug("onServicesDiscovered");
        if (status != BluetoothGatt.GATT_SUCCESS) {
            log.info("onServicesDiscovered received: " + status);
            return;
        }


        List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();
        mBLEService = serviceList.get(2);
        List<BluetoothGattCharacteristic> gattCharacteristicList = mBLEService.getCharacteristics();
        mCharData = gattCharacteristicList.get(0);


        enableNotify();
    }

    public void connect() {
        mBluetoothGatt.connect();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enableNotify() {
        List<BluetoothGattDescriptor> charDataDescriptors = mCharData.getDescriptors();
        BluetoothGattDescriptor config = charDataDescriptors.get(0);
        config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

        mBluetoothGatt.writeDescriptor(config);

        mBluetoothGatt.setCharacteristicNotification(mCharData, true);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        log.info("onConnectionStateChange status: " + status + " new state: " + newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mConnectionState = BluetoothProfile.STATE_CONNECTED;
            log.info("Connected to GATT server.");
            boolean discoverServices = gatt.discoverServices();
            log.info("Attempting to start service discovery:" + discoverServices);
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {
            mConnectionState = BluetoothProfile.STATE_CONNECTING;
            log.info("Attempting to connect to GATT server...");
        } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
            log.info("Disconnecting GATT server...");
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
            log.info("Disconnected from GATT server.");
        } else {
            log.info("Unknown state " + newState);
        }

    }

    public static String toHexString(byte[] value)
    {
        StringBuffer sb = new StringBuffer();

        for (byte element : value)
        {
            sb.append(String.format("%02x", element));
            sb.append("-");
        }
        sb.setLength(sb.length()-1);

        return sb.toString();
    }

    public interface OnMessage {

        void onMessage(String message);
    }
}
