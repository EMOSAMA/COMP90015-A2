package GUI.Home;

import RMI.Client;
import RMI.Host;
import RMI.Impl.ClientImpl;
import RMI.Impl.HostImpl;
import STRUCTURE.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class HomeGUI extends JFrame {
    private JPanel panel;
    private JButton bntCreateWhiteboard;
    private JButton bntJoinWhiteboard;

    public HomeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(1000, 400, 400, 400);
        setVisible(true);
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        panel.setLayout(null);

        bntCreateWhiteboard = new JButton("Create Whiteboard");
        bntCreateWhiteboard.setBounds(100, 150, 200, 30);
        bntCreateWhiteboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                String info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                String[] infos = info.split(",");

                if(infos.length != 3) {
                    JOptionPane.showMessageDialog(null,"Wrong input, please input ip address, port and username!");
                    info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                    infos = info.split(",");
                }

                int port;
                while (true) {
                    try {
                        port = Integer.parseInt(infos[1]);
                        break;
                    }catch (Exception e){
                        JOptionPane.showMessageDialog(null,"The inputted port is in wrong format!");
                        info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                        infos = info.split(",");
                    }
                }

                UserInfo userInfo = new UserInfo(infos[0],port, infos[2], Boolean.TRUE);

                //HostImpl host;
                try {
                    Registry registry = LocateRegistry.createRegistry(port);
                    Host host = new HostImpl(userInfo);
                    registry.bind("hostServer", host);
                    System.out.println("Bind Successful");
                    host.syncUserList();
                    setVisible(false);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,"This Port is bind by others!");
                    e.printStackTrace();
                }
            }
        });
        panel.add(bntCreateWhiteboard);


        bntJoinWhiteboard = new JButton("Join Whiteboard");
        bntJoinWhiteboard.setBounds(100, 200, 200, 30);
        bntJoinWhiteboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                String info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                String[] infos = info.split(",");

                if(infos.length != 3) {
                    JOptionPane.showMessageDialog(null,"Wrong input, please input ip address, port and username!");
                    info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                    infos = info.split(",");
                }

                int port;

                while (true) {
                    try {
                        port = Integer.parseInt(infos[1]);
                        break;
                    }catch (Exception e){
                        JOptionPane.showMessageDialog(null,"The inputted port is in wrong format!");
                        info = JOptionPane.showInputDialog( "IP Address Input Port Username (eg. 127.0.0.1,5555,emo)" );
                        infos = info.split(",");
                    }
                }

                //UserInfo hostInfo = new UserInfo(infos[0],port, infos[2], Boolean.TRUE);
                String hostIpAddress = infos[0];

                ClientImpl user;
                try {
                    String ipAddress = InetAddress.getLocalHost().getHostAddress();
                    UserInfo userInfo = new UserInfo(ipAddress,port, infos[2], Boolean.FALSE);

                    Registry registry = LocateRegistry.getRegistry(hostIpAddress,port);
                    Host host = (Host) registry.lookup( "hostServer");


                    UserInfo hostInfo = host.userJoin(userInfo);

                    if(hostInfo == null) {
                        JOptionPane.showMessageDialog(null, "Join Refused", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else if(hostInfo.getUserName().equals(userInfo.getUserName())){
                        JOptionPane.showMessageDialog(null, "Your username is used, please change a username!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }else {
                        if(userInfo.getUserIpAddress().equals(hostInfo.getUserIpAddress())|| hostInfo.getUserIpAddress().equals("127.0.0.1")|| hostInfo.getUserIpAddress().equals("localhost")){
                            Client client = new ClientImpl(hostInfo, userInfo);
                            Naming.rebind("rmi://"+userInfo.getUserIpAddress()+":"+userInfo.getPort()+"/"+userInfo.getUserId().toString(), client);
                        }else{
                            registry = LocateRegistry.createRegistry(port);
                            Client client = new ClientImpl(hostInfo, userInfo);
                            registry.bind(userInfo.getUserId().toString(), client);
                        }
                    }
                    host.syncImage(null);
                    host.syncUserList();
                    host.syncMessage();
                    setVisible(false);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The Whiteboard room is not exist", "error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(bntJoinWhiteboard);
    }
}
