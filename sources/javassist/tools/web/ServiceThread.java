package javassist.tools.web;

import java.io.IOException;
import java.net.Socket;

/* JADX INFO: compiled from: Webserver.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/tools/web/ServiceThread.class */
class ServiceThread extends Thread {
    Webserver web;
    Socket sock;

    public ServiceThread(Webserver w, Socket s) {
        this.web = w;
        this.sock = s;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        try {
            this.web.process(this.sock);
        } catch (IOException e) {
        }
    }
}
