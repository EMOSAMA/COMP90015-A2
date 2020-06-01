package GUI.Whiteboard;

import COMM.FileOperation;
import COMM.ThreadPool;
import COMM.ToolType;
import RMI.Host;
import COMM.UserInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WhiteboardGUI extends JFrame{

	private JPanel contentPane;
	private JTextField textField;
	private GUI.Whiteboard.Canvas canvas;
    private JList userList;
	private JList hostList;
    private JTextArea messageBox;
    private FileOperation fileOperation = new FileOperation();
	ThreadPool threadPool;

	/**
	 * Getter and Setter
	 **/
	public GUI.Whiteboard.Canvas getCanvas() {
		return canvas;
	}
	public JList getUserList() {
		return userList;
	}
	public JTextArea getMessageBox() {
		return messageBox;
	}
	public JList getHostList() {
		return hostList;
	}

	/**
	 * GUI
	 **/
	public WhiteboardGUI(UserInfo hostInfo,UserInfo userInfo) {
		threadPool = new ThreadPool();
		canvas = new Canvas(hostInfo,this.threadPool);
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch(Exception e) {
			System.out.println("Error setting Java LAF: " + e);
		}

		/**
		 * Frame Setting
		 **/
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1100, 1000);
		setResizable(false);
		if (userInfo.getIsHost()){
			this.setTitle("Host: "+userInfo.getUserName());
		} else {
			this.setTitle("Client: "+userInfo.getUserName());
		}
		setVisible(true);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (userInfo.getIsHost()) {
					try {
						Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
						Host host = (Host) registry.lookup( "hostServer");
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									host.hostExit ();
								}catch (Exception e1){

								}
							}
						});
						//host.hostExit ();
					}catch (Exception e1) {

					}
				} else {
					try {
						Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
						Host host = (Host) registry.lookup( "hostServer");
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									host.userQuit(userInfo);
								}catch (Exception e1){

								}
							}
						});
						//host.userQuit(userInfo);
					}catch (Exception e2) {
						System.out.print(e2);
					}
				}
				e.getWindow().dispose();
				System.exit(1);
			}
		});


		if(userInfo.getIsHost()){
			/**
			 * Menu Part - File Operation
			 **/
			JMenu mnFile = new JMenu("File");
			mnFile.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			menuBar.add(mnFile);

			JMenuItem mntmNew = new JMenuItem("New");
			mnFile.add(mntmNew);
			mntmNew.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			mntmNew.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileOperation.newFile();
					canvas.clear();
					canvas.repaint();
					try {
						Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
						Host host = (Host) registry.lookup( "hostServer");
						BufferedImage image = canvas.save();
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						ImageIO.write(image,"png", out);
						byte[] bytes = out.toByteArray();
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									host.syncImage(bytes);
								}catch (Exception e1){

								}
							}
						});
						//host.syncImage(bytes);
					}catch (Exception e1) {

					}
				}
			});

			JMenuItem mntmOpen = new JMenuItem("Open");
			mnFile.add(mntmOpen);
			mntmOpen.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			mntmOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BufferedImage image = fileOperation.openFile();
					if (image != null){
						canvas.loadImage(image);
					} else {
						JOptionPane.showMessageDialog(null,"File not found");
					}
				}
			});

			JMenuItem mntmSave = new JMenuItem("Save");
			mnFile.add(mntmSave);
			mntmSave.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			mntmSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileOperation.saveFile(canvas.getImage());
				}
			});

			JMenuItem mntmSaveAs = new JMenuItem("Save As");
			mnFile.add(mntmSaveAs);
			mntmSaveAs.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			mntmSaveAs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileOperation.saveFileAs(canvas.getImage());
				}
			});

			JSeparator separator = new JSeparator();
			mnFile.add(separator);

			JMenuItem mntmClose = new JMenuItem("Close");
			mntmClose.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			mnFile.add(mntmClose);
			mntmClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
						Host host = (Host) registry.lookup( "hostServer");
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									host.hostExit ();
								}catch (Exception e1){

								}
							}
						});
						//host.hostExit ();
					}catch (Exception e1) {

					}
					System.exit(0);
				}
			});
		}

		/**
		 * Menu Part - Whiteboard Operation
		 **/
		JMenu operationMenu = new JMenu("Operation");
		operationMenu.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		menuBar.add(operationMenu);
		
		JMenuItem clearBtn = new JMenuItem("Clear");
		clearBtn.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.clear();
				canvas.repaint();

				try {
					Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
					Host host = (Host) registry.lookup( "hostServer");
					BufferedImage image = canvas.save();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(image,"png", out);
					byte[] bytes = out.toByteArray();
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								host.syncImage(bytes);
							}catch (Exception e1){

							}
						}
					});
					//host.syncImage(bytes);
				}catch (Exception e1) {

				}
			}
		});
		operationMenu.add(clearBtn);

		JMenuItem btnUndo = new JMenuItem("undo");
		btnUndo.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
					Host host = (Host) registry.lookup("hostServer");
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								host.undo();
							}catch (Exception e){

							}
						}
					});
					//host.undo();
				} catch (Exception e1){
				}
			}
		});
		operationMenu.add(btnUndo);

		JMenuItem btnRedo = new JMenuItem("redo");
		btnRedo.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
					Host host = (Host) registry.lookup("hostServer");
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								host.redo();
							}catch (Exception e){

							}
						}
					});
					//host.redo();
				} catch (Exception e1){
				}
			}
		});
		operationMenu.add(btnRedo);

		/**
		 * Tool Part
		 **/
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBounds(0, 0, 780, 50);
		contentPane.add(toolBar);

		class Tool {
			String name;
			Image icon;
			ToolType toolType;

			public ToolType getToolType() {
				return toolType;
			}

			public void setToolType(ToolType toolType) {
				this.toolType = toolType;
			}

			Tool (String name, ToolType toolType, Image icon) {
				this.name = name;
				this.toolType = toolType;
				this.icon = icon;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public Image getIcon() {
				return icon;
			}

			public void setIcon(Image icon) {
				this.icon = icon;
			}
		}
		ArrayList<Tool> tools = new ArrayList<Tool>();

		tools.add(new Tool("Line",ToolType.LINE,new ImageIcon(this.getClass().getResource("/IMG/line.png")).getImage()));
		tools.add(new Tool("Rectangle",ToolType.RECT,new ImageIcon(this.getClass().getResource("/IMG/rect.png")).getImage()));
		tools.add(new Tool("Oval",ToolType.OVAL,new ImageIcon(this.getClass().getResource("/IMG/oval.png")).getImage()));
		tools.add(new Tool("Circle",ToolType.CIRCLE,new ImageIcon(this.getClass().getResource("/IMG/circle.png")).getImage()));
		tools.add(new Tool("Pencil",ToolType.PENCIL,new ImageIcon(this.getClass().getResource("/IMG/pencil.png")).getImage()));
		tools.add(new Tool("Text",ToolType.TEXT,new ImageIcon(this.getClass().getResource("/IMG/text.png")).getImage()));
		tools.add(new Tool("Eraser",ToolType.ERASE,new ImageIcon(this.getClass().getResource("/IMG/eraser.png")).getImage()));

		for (Tool tool : tools){
			JButton btnTool = new JButton(tool.getName(),new ImageIcon(tool.getIcon()));
			btnTool.setFont(new Font("TimesRoman", Font.PLAIN, 13));
			btnTool.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					canvas.setToolType(tool.toolType);
				}
			});
			toolBar.add(btnTool);
		}

		JComboBox cmbSize = new JComboBox();
		cmbSize.setFont(new Font("TimesRoman", Font.PLAIN, 13));
		cmbSize.setModel(new DefaultComboBoxModel(new String[] {"Size:1.0", "Size:2.0", "Size:3.0", "Size:4.0"}));
		cmbSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Stroke selectStroke = new BasicStroke(Float.parseFloat(((String)cmbSize.getSelectedItem()).substring(5)));
				canvas.setStroke(selectStroke);
			}
		});
		toolBar.add(cmbSize);

		JCheckBox checkbox = new JCheckBox("Hold");
		checkbox.setFont(new Font("TimesRoman", Font.PLAIN, 13));
		checkbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkbox.isSelected())
				{
					canvas.setHold(true);
				}else {
					canvas.setHold(false);
				}
			}
		});
		toolBar.add(checkbox);

		JComboBox cmbSpeed = new JComboBox();
		cmbSpeed.setFont(new Font("TimesRoman", Font.PLAIN, 13));
		cmbSpeed.setModel(new DefaultComboBoxModel(new String[] {"1", "10", "50", "100"}));
		cmbSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int speed = Integer.parseInt((String)cmbSpeed.getSelectedItem());
				canvas.setSpeed(speed);
			}
		});
		toolBar.add(cmbSpeed);

		/**
		 * Color Selection Part
		 **/
		// Current Color
		JLabel lblSelectedColor = new JLabel("Current:");
		lblSelectedColor.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		lblSelectedColor.setBounds(12, 60, 150, 20);
		contentPane.add(lblSelectedColor);

		JButton btnCurrentColor = new JButton();
		btnCurrentColor.setEnabled(false);
		btnCurrentColor.setBackground(Color.BLACK);
		btnCurrentColor.setBounds(100, 58, 28, 28);
		btnCurrentColor.setOpaque(true);
		btnCurrentColor.setBorderPainted(false);
		contentPane.add(btnCurrentColor);

		// Select Color
		JLabel lblPalette = new JLabel("Colors:");
		lblPalette.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		lblPalette.setBounds(220, 60, 91, 20);
		contentPane.add(lblPalette);

		Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.PINK, Color.GREEN, Color.BLACK, Color.MAGENTA};
		int startPlace = 300;
		for (Color color : colors) {
			JButton button = new JButton();
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnCurrentColor.setBackground(color);
					canvas.setColor(color);
				}
			});
			button.setOpaque(true);
			button.setBorderPainted(false);
			button.setBackground(color);
			button.setBounds(startPlace, 58, 28, 28);
			contentPane.add(button);
			startPlace += 34;
		}

		JButton btnMoreColor = new JButton("More");
		btnMoreColor.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		btnMoreColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color initialColor = null;
	            Color selectedColor = JColorChooser.showDialog(null, "Choose a color here", initialColor);
				btnCurrentColor.setBackground(selectedColor);
				canvas.setColor(selectedColor);
			}
		});
		btnMoreColor.setBounds(startPlace+15, 58, 115, 28);
		contentPane.add(btnMoreColor);
		btnMoreColor.setBackground(Color.LIGHT_GRAY);


		/**
		 * Canvas Part
		 **/
		canvas.setBounds(12, 92, 768, 495);
		canvas.setBackground(Color.white);
		contentPane.add(canvas);


		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_1.setBounds(10, 90, 771, 498);
		contentPane.add(panel_1);

		/**
		 * Users List Part
		 **/
		// Host
		JLabel lblhost = new JLabel("Host:");
		lblhost.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		lblhost.setBounds(796, 0, 82, 31);
		contentPane.add(lblhost);

		hostList = new JList();
		hostList.setBounds(796, 34, 259, 48);
		hostList.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, Color.LIGHT_GRAY));
		contentPane.add(hostList);

		// UserImpl
		JLabel lblUsers = new JLabel("Clients:");
		lblUsers.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		lblUsers.setBounds(796, 94, 82, 31);
		contentPane.add(lblUsers);

		if(userInfo.getIsHost()) {
			JButton bntKickOut = new JButton("Kick Client");
			bntKickOut.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			bntKickOut.setBounds(929, 94, 125, 28);
			contentPane.add(bntKickOut);
			bntKickOut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String input = JOptionPane.showInputDialog( "Input Client's Name" ).trim();
					if(!input.equals("") && input!=null){
						try{
							Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
							Host host = (Host) registry.lookup("hostServer");
							threadPool.execute(new Runnable() {
								@Override
								public void run() {
									try {
										host.kickUser(input);
									}catch (Exception e){

									}
								}
							});
							//host.kickUser(input);
						}catch (Exception e2){

						}
					}
				}
			});
		}

		userList = new JList();
		userList.setBounds(796, 138, 259, 450);
		userList.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, Color.LIGHT_GRAY));
		contentPane.add(userList);

		/**
		 * Message Box Part
		 **/
		// Text Filed Box
		messageBox = new JTextArea();
		messageBox.setEditable(false);
		messageBox.setLineWrap(true);
		//textArea.setBounds(611, 187, 183, 500);
		JScrollPane scroll = new JScrollPane(messageBox);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(5, 600, 1069, 259);
		contentPane.add(scroll);

		// Input Field
		textField = new JTextField();
		textField.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, Color.LIGHT_GRAY));
		textField.setBounds(5, 862, 950, 36);
		contentPane.add(textField);
		textField.setColumns(10);

		// Send Button
		JButton btnSend = new JButton("Send");
		btnSend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String text = textField.getText().trim();
				if(!text.equals("") && text != null){
					SimpleDateFormat sdf = new SimpleDateFormat();
					sdf.applyPattern("HH:mm:ss a");
					Date date = new Date();
					String time = sdf.format(date);
					String message = time+" <"+userInfo.getUserName()+">: "+textField.getText().trim()+"\n";
					try {
						Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
						Host host = (Host) registry.lookup( "hostServer");
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									host.sendMessage(message);
								}catch (Exception e){

								}
							}
						});
						//host.sendMessage(message);
					}catch (Exception e1) {

					}
				} else {
					JOptionPane.showMessageDialog(null,"Message cannot be empty!");
				}
				textField.setText(null);
			}
		});
		btnSend.setBounds(962, 862, 100, 36);
		contentPane.add(btnSend);
	}
}
