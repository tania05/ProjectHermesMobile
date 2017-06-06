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
        assertTrue(networkDevice.getIsGroupOwner());
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

    @Test
    public void CanConnectSuccessfully() {
        final boolean[] wasCalled = new boolean[] { false };

        WifiP2pManager manager = mock(WifiP2pManager.class);
        ArgumentCaptor<WifiP2pConfig> configArgument = ArgumentCaptor.forClass(WifiP2pConfig.class);
        ArgumentCaptor<WifiP2pManager.ActionListener> callbackArgument = ArgumentCaptor.forClass(WifiP2pManager.ActionListener.class);
        doNothing().when(manager).connect(any(WifiP2pManager.Channel.class), configArgument.capture(), callbackArgument.capture());

        ArgumentCaptor<WifiP2pManager.ConnectionInfoListener> connectionInfoArgument = ArgumentCaptor.forClass(WifiP2pManager.ConnectionInfoListener.class);
        doNothing().when(manager).requestConnectionInfo(any(WifiP2pManager.Channel.class), connectionInfoArgument.capture());


        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "1.2.3.4";

        NetworkDevice networkDevice = new NetworkDevice(
                device,
                manager,
                mock(WifiP2pManager.Channel.class),
                new DefaultFactory<>(WifiP2pConfig.class)
        );

        networkDevice.connect().subscribe(new IObservableListener<WifiP2pInfo>() {
            @Override
            public void update(WifiP2pInfo arg) {
                wasCalled[0] = true;
            }

            @Override
            public void error(Exception e) {
                throw new ShouldNotBeCalledException();
            }
        });
        assertEquals("1.2.3.4", configArgument.getValue().deviceAddress);

        callbackArgument.getValue().onSuccess();
        connectionInfoArgument.getValue().onConnectionInfoAvailable(null);
        assertTrue(wasCalled[0]);
    }

    @Test
    public void CanFailToConnect() {
        final boolean[] wasCalled = new boolean[] { false };

        WifiP2pManager manager = mock(WifiP2pManager.class);
        ArgumentCaptor<WifiP2pConfig> configArgument = ArgumentCaptor.forClass(WifiP2pConfig.class);
        ArgumentCaptor<WifiP2pManager.ActionListener> callbackArgument = ArgumentCaptor.forClass(WifiP2pManager.ActionListener.class);
        doNothing().when(manager).connect(any(WifiP2pManager.Channel.class), configArgument.capture(), callbackArgument.capture());

        WifiP2pDevice device = mock(WifiP2pDevice.class);
        device.deviceAddress = "1.2.3.4";

        NetworkDevice networkDevice = new NetworkDevice(
                device,
                manager,
                mock(WifiP2pManager.Channel.class),
                new DefaultFactory<>(WifiP2pConfig.class)
        );

        networkDevice.connect().subscribe(new IObservableListener<WifiP2pInfo>() {
            @Override
            public void update(WifiP2pInfo arg) {
                throw new ShouldNotBeCalledException();
            }

            @Override
            public void error(Exception e) {
                assertTrue(e instanceof IntValueException);
                assertEquals(4, ((IntValueException) e).value);
                wasCalled[0] = true;
            }
        });
        assertEquals("1.2.3.4", configArgument.getValue().deviceAddress);

        callbackArgument.getValue().onFailure(4);
        assertTrue(wasCalled[0]);
    }
}
