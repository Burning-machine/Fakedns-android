package web.my;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketException;



public class MainActivity extends Activity
{
    private Thread thread = null;
    private TextView v1 =null;
    public int Dns_port;
    public Button start = null;
    public EditText input;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //DNS port used to start the server
        //even as root we can't start on port 53
        Dns_port = 3233;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.input);
        v1 = findViewById(R.id.log);
        rootexec();
        start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            startfdns("google.com",input.getText().toString().trim());
            v1.setText("Started");
            }
        });
    }

    public void startfdns(final String target, final String iph2h){
        thread = new Thread(new Runnable(){
            @Override
            public void run(){
                Looper.prepare();
                ServerKernelProgram serve = new ServerKernelProgram();
                String IP_Server = target;
                String ip_h2h = iph2h;
                try {
                    serve.receiveANDanswer(Dns_port, IP_Server, iph2h);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void rootexec(){
        try {
            Runtime.getRuntime().exec("su");
            //Bypassing Android's < 1024 port restrictions
            Runtime.getRuntime().exec("su -c iptables -t nat -A PREROUTING -p udp --dport "+Dns_port+" -j REDIRECT --to-port 53");
            Runtime.getRuntime().exec("su -c iptables -t nat -A PREROUTING -p tcp --dport "+Dns_port+" -j REDIRECT --to-port 53");
            Runtime.getRuntime().exec("su -c iptables -t nat -A PREROUTING -p udp --dport 53 -j REDIRECT --to-port "+Dns_port);
            Runtime.getRuntime().exec("su -c iptables -t nat -A PREROUTING -p tcp --dport 53 -j REDIRECT --to-port "+Dns_port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("App","Failed to get root");
        }
    }
}