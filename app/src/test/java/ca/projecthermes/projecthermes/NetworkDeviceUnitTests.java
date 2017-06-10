package ca.projecthermes.projecthermes;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ca.projecthermes.projecthermes.exception.ShouldNotBeCalledException;
import ca.projecthermes.projecthermes.exceptions.IntValueException;
import ca.projecthermes.projecthermes.networking.INetworkDevice;
import ca.projecthermes.projecthermes.networking.NetworkDevice;
import ca.projecthermes.projecthermes.util.DefaultFactory;
import ca.projecthermes.projecthermes.util.IObservableListener;
import ca.projecthermes.projecthermes.util.Null;
import ca.projecthermes.projecthermes.util.Source;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class NetworkDeviceUnitTests {

    @Test
    public void CanGetDeviceProperties() {
        WifiP2pDevice p2pDevice = mock(WifiP2pDevice.class);

        when(p2pDevice.isGroupOwner()).thenReturn(true);
        p2pDevice.status = WifiP2pDevice.INVITED;
        p2pDevice.deviceName = "TestDeviceName";
        p2pDevice.deviceAddress = "1.1.1.1";

        NetworkDevice networkDevice = new NetworkDevice(
                p2pDevice,
                mock(WifiP2pManager.class),
                mock(WifiP2pManager.Channel.class),
                new DefaultFactory<>(WifiP2pConfig.class)
        );

        assertEquals(WifiP2pDevice.INVITED, networkDevice.getStatus());
        assertEquals("TestDeviceName", networkDevice.getName());
        assertEquals("1.1.1.1", networkDevice.getDeviceAddress());
//        assertTrue(networkDevice.getIsGroupOwner());
    }

    @Test
    public void CanDetectDeviceUpdates() {
        final boolean[] wasUpdated = new boolean[] { false };

        WifiP2pDevice firstDevice = mock(WifiP2pDevice.class);
        firstDevice.status = 0;
        WifiP2pDevice secondDevice = mock(WifiP2pDevice.class);
        secondDevice.status = 1;


        NetworkDevice networkDevice = new NetworkDevice(
                firstDevice,
                mock(WifiP2pManager.class),
                mock(WifiP2pManager.Channel.class),
                new DefaultFactory<>(WifiP2pConfig.class)
        );

        networkDevice.getStatusChangeObservable().subscribe(new IObservableListener<INetworkDevice>() {
            @Override
            public void update(INetworkDevice arg) {
                wasUpdated[0] = true;
            }

            @Override
            public void error(Exception e) {
                throw new ShouldNotBeCalledException();
            }
        });

        networkDevice.deviceStatusUpdate(firstDevice);
        assertFalse(wasUpdated[0]);
        networkDevice.deviceStatusUpdate(secondDevice);
        assertTrue(wasUpdated[0]);
    }

    //TODO need to test on connect.
    //TODO XXX CANTSHIP

}
