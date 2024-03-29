package com.usoroos.usorosyncprototype.TCP;

import android.util.Log;

import com.usoroos.usorosyncprototype.getLocalIP;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Mariusz on 15.10.14.
 *
 * Class for getting dynamic IP Address from Local Network. Remember to start listen server on your
 * computer! Usage is in every AsyncTask in TCP Client constructor.
 *
 * @author Mariusz
 */
class IPGetter {

    private static final String TAG = "IpGetter";
    private static String excludedNumbers = Objects.requireNonNull(getLocalIP.getIp()).substring(10);
    private static int exclude = Integer.parseInt(excludedNumbers);

    /**
     * Method which should return one not-null object with String that is an ip number.
     * @param executorService Producing 'Future' objects with 20 asynchronous tasks
     * @param ip String passed in loop, from 192.168.1.0 to 192.168.1.255 .
     * @param port Hardcoded int, in here: 1488
     * @param timeout Timeout set in [ ms ]. Also hardcoded.
     * @return Should be one not-null object with proper ip.
     */
    private static Future<String> checkIfPortIsOpen(final ExecutorService executorService,
                                                    final String ip,
                                                    final int port,
                                                    final int timeout) {
        return executorService.submit(new Callable<String>() {
            Socket socket;
            PrintWriter mOut;
            @Override public String call() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    return ip;
                } catch (Exception ex) {
                    return null;
                }finally{
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * Method used in TCPClient constructor in AsyncTask for getting String with proper IP number.
     * @return After looping from 192.168.1.0 to 192.168.1.255 should return one String with ip.
     */
    public static String getIp(){

        final ExecutorService           es = Executors.newFixedThreadPool(20);
        final int                  timeout = 30                            ;
        final int                     port = 1488                        ;
        final List<Future<String>> futures = new ArrayList<>() ;


        for (int i = 0; i <= 255;  i++) {
                String ip  = "192.168.1"+i;
            if(!Objects.equals(i,exclude))
                futures.add(checkIfPortIsOpen(es, ip, port, timeout));
        }
        es.shutdown();

        for (final Future<String> future : futures) {
            try {
                if (future.get() != null) {
                    Log.d(TAG,"Local Ip: " + future.get());
                    return future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}