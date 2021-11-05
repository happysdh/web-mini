package com.xunao.testlib.andserv;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.xunao.testlib.dns.DnsCacheManipulator;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class CoreService extends Service {

    private Server mServer;
//    private Server proxy;

    @Override
    public void onCreate() {

        try {
            mServer = AndServer.webServer(this)
                    .port(8081)
//                    .inetAddress(InetAddress.getByName("192.168.10.10"))
    //                .sslContext()
    //                .sslSocketInitializer()
                    .timeout(10, TimeUnit.SECONDS)
                    .listener(new Server.ServerListener() {
                        @Override
                        public void onStarted() {
                            InetAddress address = NetUtils.getLocalIPAddress();
                            ServerManager.onServerStart(CoreService.this, address.getHostAddress());
                        }

                        @Override
                        public void onStopped() {
                            ServerManager.onServerStop(CoreService.this);
                        }

                        @Override
                        public void onException(Exception e) {
                            e.printStackTrace();
                            ServerManager.onServerError(CoreService.this, e.getMessage());
                        }
                    })
                    .build();
            DnsCacheManipulator.setDnsCache("www.hello.com", "http://10.3.6.53:8081");

            String loaclhost = "http://" + NetUtils.getLocalIPAddress().getHostAddress();
//            proxy = AndServer.proxyServer()
//                    .addProxy("http://udshop.uniondrug.com", loaclhost).port(8081)
//                    .listener(new Server.ServerListener() {
//                        @Override
//                        public void onStarted() {
//                            InetAddress address = NetUtils.getLocalIPAddress();
////                        ServerManager.onServerStart(CoreService.this, address.getHostAddress());
//                        }
//
//                        @Override
//                        public void onStopped() {
////                        ServerManager.onServerStop(CoreService.this);
//                        }
//
//                        @Override
//                        public void onException(Exception e) {
//                            e.printStackTrace();
//                            ServerManager.onServerError(CoreService.this, e.getMessage());
//                        }
//                    }).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    /**
     * Start server.
     */
    private void startServer() {
        mServer.startup();
//        proxy.startup();
    }

    /**
     * Stop server.
     */
    private void stopServer() {
//        proxy.shutdown();
        mServer.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}