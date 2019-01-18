package com.watshout.mobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PolarSensor {

    // HR example code
    public enum AD_TYPE
    {
        GAP_ADTYPE_UNKNOWN(0),
        GAP_ADTYPE_FLAGS(1)                         ,
        GAP_ADTYPE_16BIT_MORE(2)                    , //!< Service: More 16-bit UUIDs available
        GAP_ADTYPE_16BIT_COMPLETE(3)                , //!< Service: Complete list of 16-bit UUIDs
        GAP_ADTYPE_32BIT_MORE(4)                    , //!< Service: More 32-bit UUIDs available
        GAP_ADTYPE_32BIT_COMPLETE(5)                , //!< Service: Complete list of 32-bit UUIDs
        GAP_ADTYPE_128BIT_MORE(6)                   , //!< Service: More 128-bit UUIDs available
        GAP_ADTYPE_128BIT_COMPLETE(7)               , //!< Service: Complete list of 128-bit UUIDs
        GAP_ADTYPE_LOCAL_NAME_SHORT(8)              , //!< Shortened local name
        GAP_ADTYPE_LOCAL_NAME_COMPLETE(9)           , //!< Complete local name
        GAP_ADTYPE_POWER_LEVEL(10)                  , //!< TX Power Level: 0xXX: -127 to +127 dBm
        GAP_ADTYPE_OOB_CLASS_OF_DEVICE(11)          , //!< Simple Pairing OOB Tag: Class of device (3 octets)
        GAP_ADTYPE_OOB_SIMPLE_PAIRING_HASHC(12)     , //!< Simple Pairing OOB Tag: Simple Pairing Hash C (16 octets)
        GAP_ADTYPE_OOB_SIMPLE_PAIRING_RANDR(13)     , //!< Simple Pairing OOB Tag: Simple Pairing Randomizer R (16 octets)
        GAP_ADTYPE_SM_TK(14)                        , //!< Security Manager TK Value
        GAP_ADTYPE_SM_OOB_FLAG(15)                  , //!< Secutiry Manager OOB Flags
        GAP_ADTYPE_SLAVE_CONN_INTERVAL_RANGE(16)    , //!< Min and Max values of the connection interval (2 octets Min, 2 octets Max) (0xFFFF indicates no conn interval min or max)
        GAP_ADTYPE_SIGNED_DATA(17)                  , //!< Signed Data field
        GAP_ADTYPE_SERVICES_LIST_16BIT(18)          , //!< Service Solicitation: list of 16-bit Service UUIDs
        GAP_ADTYPE_SERVICES_LIST_128BIT(19)         , //!< Service Solicitation: list of 128-bit Service UUIDs
        GAP_ADTYPE_SERVICE_DATA(20)                 , //!< Service Data
        GAP_ADTYPE_MANUFACTURER_SPECIFIC(0xFF);       //!< Manufacturer Specific Data: first 2 octets contain the Company Identifier Code followed by the additional manufacturer specific data

        private int numVal;

        AD_TYPE(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }

    };

    public static AD_TYPE getCode(byte type){
        try {
            return type == -1 ? AD_TYPE.GAP_ADTYPE_MANUFACTURER_SPECIFIC : AD_TYPE.values()[type];
        }catch (ArrayIndexOutOfBoundsException ex){
            return AD_TYPE.GAP_ADTYPE_UNKNOWN;
        }
    }

    public static HashMap<AD_TYPE,byte[]> advertisementBytes2Map(byte[] record){
        int offset=0;
        HashMap<AD_TYPE,byte[]> adTypeHashMap = new HashMap<>();
        try {
            while ((offset + 2) < record.length) {
                AD_TYPE type = getCode(record[offset + 1]);
                int fieldLen = record[offset];
                if (fieldLen <= 0) {
                    // skip if incorrect adv is detected
                    break;
                }
                if (adTypeHashMap.containsKey(type) && type == AD_TYPE.GAP_ADTYPE_MANUFACTURER_SPECIFIC) {
                    byte data[] = new byte[adTypeHashMap.get(type).length + fieldLen - 1];
                    System.arraycopy(record, offset + 2, data, 0, fieldLen - 1);
                    System.arraycopy(adTypeHashMap.get(type), 0, data, fieldLen - 1, adTypeHashMap.get(type).length);
                    adTypeHashMap.put(type, data);
                } else {
                    byte data[] = new byte[fieldLen - 1];
                    System.arraycopy(record, offset + 2, data, 0, fieldLen - 1);
                    adTypeHashMap.put(type, data);
                }
                offset += fieldLen + 1;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // corrupted adv data find
        }
        return adTypeHashMap;
    }


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager btManager;
    private Context context;

    public final UUID HR_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public final UUID HR_SERVICE = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    public static final UUID DESCRIPTOR_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            processDeviceDiscovered(result.getDevice(),result.getRssi(),result.getScanRecord().getBytes());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private  BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            processDeviceDiscovered(device,rssi,scanRecord);
        }
    };

    private void processDeviceDiscovered(final BluetoothDevice device, int rssi, byte[] scanRecord){
        Map<AD_TYPE,byte[]> content = advertisementBytes2Map(scanRecord);
        if( content.containsKey(AD_TYPE.GAP_ADTYPE_LOCAL_NAME_COMPLETE) ) {
            String name = new String(content.get(AD_TYPE.GAP_ADTYPE_LOCAL_NAME_COMPLETE));
            if (name.startsWith("Polar ")) {
                String names[] = name.split(" ");
                if (names.length > 2) {
                    String deviceId = names[names.length-1];
                    if( deviceId.equals("12345678") ){ // TODO NOTE REPLACE with your device id
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                        } else {
                            bluetoothAdapter.stopLeScan(leScanCallback);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            device.connectGatt(context,false,bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                        } else {
                            device.connectGatt(context,false,bluetoothGattCallback);
                        }
                    }
                }
            }
        }
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices();
            } else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                } else {
                    bluetoothAdapter.startLeScan(leScanCallback);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            for (BluetoothGattService gattService : gatt.getServices()) {
                if( gattService.getUuid().equals(HR_SERVICE) ){
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        if( characteristic.getUuid().equals(HR_MEASUREMENT) ){
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_CCC);
                            gatt.setCharacteristicNotification(characteristic, true);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.getUuid().equals(HR_MEASUREMENT)) {
                byte[] data = characteristic.getValue();
                int hrFormat = data[0] & 0x01;
                boolean sensorContact = true;
                final boolean contactSupported = !((data[0] & 0x06) == 0);
                if( contactSupported ) {
                    sensorContact = ((data[0] & 0x06) >> 1) == 3;
                }
                int energyExpended = (data[0] & 0x08) >> 3;
                int rrPresent = (data[0] & 0x10) >> 4;
                final int hrValue = (hrFormat == 1 ? data[1] + (data[2] << 8) : data[1]) & (hrFormat == 1 ? 0x0000FFFF : 0x000000FF);
                if( !contactSupported && hrValue == 0 ){
                    // note does this apply to all sensors, also 3rd party
                    sensorContact = false;
                }
                final boolean sensorContactFinal = sensorContact;
                int offset = hrFormat + 2;
                int energy = 0;
                if (energyExpended == 1) {
                    energy = (data[offset] & 0xFF) + ((data[offset + 1] & 0xFF) << 8);
                    offset += 2;
                }
                final ArrayList<Integer> rrs = new ArrayList<>();
                if (rrPresent == 1) {
                    int len = data.length;
                    while (offset < len) {
                        int rrValue = (int) ((data[offset] & 0xFF) + ((data[offset + 1] & 0xFF) << 8));
                        offset += 2;
                        rrs.add(rrValue);
                    }
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}

