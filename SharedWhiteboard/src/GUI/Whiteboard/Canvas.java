package GUI.Whiteboard;
import COMM.ThreadPool;
import COMM.ToolType;
import RMI.Host;
import STRUCTURE.UserInfo;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Canvas extends JPanel{
    class Shape {
        private int x, x1, y, y1;
        private ToolType toolType;
        private Color c;
        private ArrayList<Point> freedraw;
        private Stroke stroke;
        private String input;

        public Shape(Graphics g, int x, int y, int x1, int y1, ToolType toolType, Color c, Stroke stroke)
        {
            this.x = x;
            this.y = y;
            this.x1 = x1;
            this.y1 = y1;
            this.toolType = toolType;
            this.c = c;
            this.stroke = stroke;

        }

        public Shape(Graphics g, ArrayList<Point> s, ToolType toolType, Color c, Stroke stroke)
        {
            this.freedraw = s;
            this.toolType = toolType;
            this.c = c;
            this.stroke = stroke;
        }

        public Shape(Graphics g, int x, int y, String in, ToolType toolType, Color color)
        {
            this.x =x;
            this.y = y;
            this.input = in;
            this.toolType = toolType;
            this.c =color;
        }

        public void rePaint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            if(toolType == ToolType.TEXT){
                g.setColor(c);
                g.drawString(input,x,y);
            }
            else {
                g2.setColor(c);
                g2.setStroke(stroke);
                if (toolType == ToolType.LINE) {
                    g.drawLine(x, y, x1, y1);
                } else if (toolType == ToolType.RECT) {
                    g.drawRect(Math.min(x, x1), Math.min(y, y1), Math.abs(x - x1), Math.abs(y - y1));
                } else if (toolType == ToolType.OVAL) {
                    g.drawOval(Math.min(x, x1), Math.min(y, y1), Math.abs(x - x1), Math.abs(y - y1));
                } else if (toolType == ToolType.CIRCLE) {
                    g.drawOval(Math.min(x, x1), Math.min(y, y1), Math.max(Math.abs(x - x1),Math.abs(y - y1)),Math.max(Math.abs(x - x1),Math.abs(y - y1)));
                } else if (toolType == ToolType.PENCIL) {
                    for (int i = 1; i < freedraw.size(); i++) {
                        g.drawLine(freedraw.get(i - 1).x, freedraw.get(i - 1).y, freedraw.get(i).x, freedraw.get(i).y);
                    }
                } else if (toolType == ToolType.ERASE) {
                    for (int i = 1; i < freedraw.size(); i++) {
                        g.drawLine(freedraw.get(i - 1).x, freedraw.get(i - 1).y, freedraw.get(i).x, freedraw.get(i).y);
                    }
                }
            }
        }
    }
    /**
     * Variables
     **/
    private ToolType toolType = ToolType.LINE;
	private int x,y;
	private Color selectColor = Color.BLACK;
	private Stroke selectStroke = new BasicStroke(1.0f);
	private BufferedImage image;
    private static ArrayList<Point> points = new ArrayList<Point>();
    private static ArrayList<Shape> shapelist = new ArrayList<Shape>();
    private boolean hold;
    private int speed;
    ThreadPool threadPool;


    /**
     * Getter and Setter
     **/
    public void setSpeed(int speed) {
        this.speed = speed;
    }
	public void setToolType(ToolType toolType) {
		this.toolType = toolType; }
	public void setStroke(Stroke stroke) {
		this.selectStroke = stroke;
	}
	public void setColor(Color color){
		this.selectColor = color;
	}
    public void setHold(boolean hold) {
        this.hold = hold;
    }
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Constructor
     **/
    public Canvas(UserInfo hostInfo,ThreadPool threadPool) {
        this.threadPool = threadPool;
        this.hold = false;
        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
                if(toolType == ToolType.TEXT){
                    Graphics2D g = (Graphics2D)getGraphics();
                    String input;
                    input = JOptionPane.showInputDialog("Input Text");
                    if(input != null) {
                        g.setColor(selectColor);
                        g.drawString(input,x,y);
                        shapelist.add(new Shape(g,x,y,input, toolType,selectColor));
                    }

                    try {
                        Registry hostRegistry  = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
                        Host host = (Host) hostRegistry.lookup( "hostServer");
                        BufferedImage image = save();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        ImageIO.write(image,"png", out);
                        byte[] bytes = out.toByteArray();
                        threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    host.syncImage(bytes);
                                }catch (Exception e){

                                }
                            }
                        });
                        threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    host.syncImage(bytes);
                                }catch (Exception e){

                                }
                            }
                        });
                        //host.syncImage(bytes);
                        //host.syncImage(bytes);
                    }catch (Exception e1) {
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                int x1 = e.getX();
                int y1 = e.getY();
                String s = (x + " " + y + " " + x1 + " " + y1 + " " + toolType);
                draw(x, y, x1, y1, toolType);

                try {
                    Registry hostRegistry  = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
                    Host host = (Host) hostRegistry.lookup( "hostServer");
                    BufferedImage image = save();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(image,"png", out);
                    byte[] bytes = out.toByteArray();

                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                host.syncImage(bytes);
                            }catch (Exception e){

                            }
                        }
                    });
                    //host.syncImage(bytes);
                }catch (Exception e1) {

                }

            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }

        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x2 = e.getX();
                int y2 = e.getY();
                int x3;
                int y3;
                Graphics2D g = (Graphics2D)getGraphics();
                g.setColor(selectColor);
                g.setStroke(selectStroke);
                if(toolType == ToolType.PENCIL){
                    if(points.size()!=0){
                        x3=points.get(points.size()-1).x;
                        y3=points.get(points.size()-1).y;}
                    else{
                        x3=x;
                        y3=y;
                    }
                    g.drawLine(x3,y3,x2,y2);
                    points.add(new Point(x2,y2));
                }
                else if(toolType == ToolType.ERASE){
                    if(points.size()!=0){
                        x3=points.get(points.size()-1).x;
                        y3=points.get(points.size()-1).y;}
                    else{
                        x3=x;
                        y3=y;
                    }
                    Color c = new Color(selectColor.getRGB());
                    g.setColor(Color.WHITE);
                    g.drawLine(x3,y3,x2,y2);
                    points.add(new Point(x2,y2));
                    g.setColor(c);
                }
                //Other shapes
                else {
                    if (toolType == ToolType.LINE) {
                        g.drawLine(x, y, x2, y2);
                    }
                    else {
                        int height = Math.abs(y2 - y);
                        int width = Math.abs(x2 - x);
                        if (toolType == ToolType.RECT) {
                            g.drawRect(Math.min(x, x2), Math.min(y, y2), width, height);
                        }
                        if (toolType == ToolType.OVAL) {
                            g.drawOval(Math.min(x, x2), Math.min(y, y2), width, height);
                        }
                        if (toolType == ToolType.CIRCLE) {
                            int round = Math.max(width, height);
                            g.drawOval(Math.min(x, x2),Math.min(y, y2), round,round);
                        }
                    }
                    repaint();
                }

                if (hold == true && toolType != ToolType.PENCIL &&toolType!= ToolType.ERASE){
                    try {
                        Thread.sleep(speed);
                        draw(x, y, x2, y2, toolType);
                    }catch (Exception e1){
                        System.out.print(e1);
                    }
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub
            }

        });
    }

    /**
     * Methods
     **/
    public void clear() {
    	shapelist = new ArrayList<Shape>();
    	image = null;
    }

    public BufferedImage save() {
    	Dimension imagesize = this.getSize();
		BufferedImage image = new BufferedImage(imagesize.width,imagesize.height,BufferedImage.TYPE_INT_BGR);
		Graphics2D graphics = image.createGraphics();//draw the image
        this.paint(graphics);
        graphics.dispose();
        return image;
    }

    public void loadImage(BufferedImage image) {
    	clear();
    	repaint();
    	this.image = image;
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if(image != null) {
        	g.drawImage(image, 0, 0, this);
        }
        for (int i = 0; i < shapelist.size(); i++) {
            if (shapelist.get(i) == null) {break;}
                shapelist.get(i).rePaint(g);
        }
    }

    public void draw(int x, int y, int x1, int y1, ToolType toolType) {
        Graphics2D graph = (Graphics2D)getGraphics();
        graph.setColor(selectColor);
        graph.setStroke(selectStroke);
        if(toolType == ToolType.LINE) {
            shapelist.add(new Shape(graph,x,y,x1,y1, toolType,selectColor,selectStroke));
            graph.drawLine(x,y, x1, y1);
        }
        else {
            int height = Math.abs(y1 - y);
            int width = Math.abs(x1 - x);
            if(toolType == ToolType.RECT) {
                shapelist.add(new Shape(graph,x,y,x1,y1, toolType,selectColor,selectStroke));
                graph.drawRect(Math.min(x, x1),Math.min(y, y1), width, height);
            }
            if(toolType == ToolType.OVAL) {
                shapelist.add(new Shape(graph,x,y,x1,y1, toolType,selectColor,selectStroke));
                graph.drawOval(Math.min(x, x1),Math.min(y, y1), width, height);
            }
            if(toolType == ToolType.PENCIL) {
                ArrayList<Point> newPoints = new ArrayList<Point>(1000);
                newPoints.addAll(points);
                shapelist.add(new Shape(graph,newPoints, toolType,selectColor,selectStroke));
            }
            if(toolType == ToolType.ERASE) {
                ArrayList<Point> newPoints = new ArrayList<Point>(1000);
                newPoints.addAll(points);
                shapelist.add(new Shape(graph,newPoints, toolType,Color.white,selectStroke));
            }
            if(toolType == ToolType.CIRCLE) {
                shapelist.add(new Shape(graph,x,y,x1,y1, toolType,selectColor,selectStroke));
                int round = Math.max(width, height);
                graph.drawOval(Math.min(x, x1),Math.min(y, y1), round,round);
            }
            points.clear();
        }
    }
}
