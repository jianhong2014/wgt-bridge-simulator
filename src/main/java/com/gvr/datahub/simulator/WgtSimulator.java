package com.gvr.datahub.simulator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



@Service
public class WgtSimulator {

    private static final Logger logger = LoggerFactory.getLogger(WgtSimulator.class);

    private int port = 5000;

    private static volatile int mids = 0;

    private boolean enterOrLeave = true;

    private String wgtId = "110";

    private String visId = "TCOUPAN07";

    private long startTime;

    @PostConstruct
    public void work() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket prevSocket = null;
            Socket socket;
            boolean running = true;
            while(running){
                socket = serverSocket.accept();
                if(prevSocket != null){
                    prevSocket.close();
                }else {
                    prevSocket = socket;
                }
                new Thread(new WgtWorker(socket)).start();
            }
        } catch (IOException e) {
            logger.error("wgt simulator engine 失败，{}",port,e);
        } catch (Exception e) {
            logger.error("other exception in wgt simulator {}",e);
        }
    }

    class WgtWorker implements Runnable {
        private Socket socket;
        public WgtWorker(Socket workSocket){
            socket = workSocket;
            startTime = System.currentTimeMillis();
        }
        @Override
        public void run() {
            if(socket.isConnected()){
                try {
                    logger.info("send ready");
                    sendReady(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while(!socket.isClosed()){
                try {
                   // sendForEach5Minus(socket);
                    String cmd  = bufferRead(socket);
                    if(StringUtils.isNotBlank(cmd)){
                        if(cmd.contains("GetNozState")){
                            int sdx = cmd.indexOf("MID=\"")+5;
                            int edx = cmd.indexOf("\"",sdx);
                            String mid = cmd.substring(sdx,edx);
                            sendVisQueryResp(socket,mid);
                        }else if(cmd.contains("WGTSetup")){
                            sendWgtSetpResp(socket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * 读取server 发送过来的数据
         * @param socket
         * @return
         * @throws IOException
         */
        private String bufferRead(Socket socket) throws IOException {
            if(socket.getInputStream().available()>0){
                byte[] bytes = new byte[512];
                socket.getInputStream().read(bytes);
                String s1b = new String(bytes);
                return s1b;
            }else {
                return "";
            }

        }
    }

    private void sendForEach5Minus(Socket socket) throws IOException {
        long curTime = System.currentTimeMillis();
        long diff = curTime - startTime;
        if(diff%5000 == 0){
            String state = enterOrLeave?"1":"0";
            String cVisId = enterOrLeave?visId:"";
            sendVisReport(state,cVisId,socket);
            enterOrLeave = !enterOrLeave;
        }
    }
    private void sendReady(Socket socket) throws IOException {
         String msg = "<Ready MID=\""+mids+"\"  Ver=\"1.0.0\" />";
         mids ++;
        socket.getOutputStream().write(msg.getBytes());
        socket.getOutputStream().flush();
    }

    private void sendVisReport(String state,String cVisId,Socket socket) throws IOException {
        String msg = "<NozStateReport MID=\""+mids+"\"  WGTID= \"110\" NozNr=\"1\" State=\""+state+"\" " +
                "VIUType=\"TT\" Track1=\""+cVisId+"\" Track2=\" \" Odo=\"OOOOOOOO\" EH=\"HHHHHH\" />";
        mids ++;
        logger.info("send vis report {}",msg);
        socket.getOutputStream().write(msg.getBytes());
        socket.getOutputStream().flush();
    }

    private void sendVisQueryResp(Socket socket,String mid) throws IOException {
        String msg = "<GetNozStateRes  MID=\""+mid+"\"  WGTID= \"110\" NozNr=\"1\" State=\""+"1"+"\" " +
                "VIUType=\"TT\" Track1=\""+visId+"\" Track2=\" \" Odo=\"OOOOOOOO\" EH=\"HQuery\" />";
        logger.info("send sendVisQueryResp report {}",msg);
        mids ++;
        socket.getOutputStream().write(msg.getBytes());
        socket.getOutputStream().flush();
    }

    private void sendWgtSetpResp(Socket socket) throws IOException {
        String msg = "<WGTSetupRes MID=\""+mids+"\"  RC=\"000\" /> ";
        logger.info("send sendWgtSetpResp report {}",msg);
        mids ++;
        socket.getOutputStream().write(msg.getBytes());
        socket.getOutputStream().flush();
    }
}
