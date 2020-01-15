package gameClient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.node_data;
import utils.Point3D;
import utils.StdDraw;

public class MyGameGUI  implements Runnable  
{
	public static double EPS=0.001;
	private game_service game;
	private DGraph g;
	private Graph_Algo ga;
	private ArrayList<Robot> robots;
	private ArrayList<myFruits> fruits;	
    private double maxX=Double.NEGATIVE_INFINITY; //check if to put them inside the func
	private double maxY=Double.NEGATIVE_INFINITY;
	private double minX=Double.POSITIVE_INFINITY;
	private double minY=Double.POSITIVE_INFINITY;
	private int scenario; //0-23
	private String type;
	private MyManageGame m1;

	public MyGameGUI()
		{
			StdDraw.enableDoubleBuffering();
             this.setRobots(new ArrayList<Robot>());
             this.setFruits(new ArrayList<myFruits>());
         	 Object s[]=new Object[24]; //list for choose scenario [0,23]
    		 for(int i=0;i<s.length;i++)
    			s[i]=i;
             this.scenario=(Integer)JOptionPane.showInputDialog(null,"Choose a scenario","scenario", JOptionPane.QUESTION_MESSAGE,null,s,null);
             this.setGame(Game_Server.getServer(scenario)); //update the scenario which chosen
             String graph=getGame().getGraph();
             this.setG(new DGraph());
             this.getG().init(graph);
             this.setGa(new Graph_Algo());
             getGa().init(this.getG());
             limit(); //size of the GUI screen
             drawS(); //build the graph
             initGUI(); // place the fruits and place the robots
         	 String t[]=new String[2]; //play by mouse or automatic
         	 t[0]="automatic";
         	 t[1]="mouse";
    		 this.type=(String)JOptionPane.showInputDialog(null,"play automatic/by mouse","choose type", JOptionPane.QUESTION_MESSAGE,null,t,null);
    		 if (type=="mouse")
    			 Mouse();
    		 else
    		 {
    			 m1=new MyManageGame(this);
    			 m1.Auto();
    		 }
    		
    		 drawRobot();    		
		}
	
    public void initGUI()
	{	
    	StdDraw.clear();
    	drawS();
		getFruits().clear();
		addFruit(getGame());
		drawFruits();
		getRobots().clear();
		addRobots(getGame());
		drawRobot();
		StdDraw.show();
	}
    
    public void limit()
    {

		for(Iterator<node_data> verticles=getG().getV().iterator(); verticles.hasNext();)
		{
			int p=verticles.next().getKey();
			if(getG().getNode(p).getLocation().x()>maxX)
				maxX=getG().getNode(p).getLocation().x();
			if(getG().getNode(p).getLocation().y()>maxY)
				maxY=getG().getNode(p).getLocation().y();
			if(getG().getNode(p).getLocation().x()<minX)
				minX=getG().getNode(p).getLocation().x();
			if(getG().getNode(p).getLocation().y()<minY)
				minY=getG().getNode(p).getLocation().y();	
		} 
		StdDraw.setCanvasSize(800,600);
		minX=minX-EPS;
		maxX=maxX+EPS;
		minY=minY-EPS/4;
		maxY=EPS/4+maxY;
		StdDraw.setXscale(minX,maxX);
		StdDraw.setYscale(minY,maxY);
    }
	
	public void drawS() 
	{	
		StdDraw.picture((minX+maxX)/2,(minY+maxY)/2,"background.png");
	Iterator<node_data> verticles=getG().getV().iterator();
		while(verticles.hasNext()) 
		{
			int point=verticles.next().getKey();
			StdDraw.setPenColor(Color.BLUE);
			StdDraw.setPenRadius(0.02);
			StdDraw.point(getG().getNode(point).getLocation().x(),getG().getNode(point).getLocation().y());
			StdDraw.text(getG().getNode(point).getLocation().x(),getG().getNode(point).getLocation().y()+0.00021, (""+point));
			
			try {
				Iterator<edge_data> edges=getG().getE(point).iterator();
				while(edges.hasNext()) 
				{
					edge_data line=edges.next();
					int dest=getG().getNode(line.getDest()).getKey();
					int src=point;
					double weight=line.getWeight();
					double t=weight*100;
					int f=(int) t;
					weight=(f/100);
					StdDraw.setPenColor(Color.DARK_GRAY);
					StdDraw.setPenRadius(0.005);
					double xSRC=getG().getNode(src).getLocation().x();
					double ySRC=getG().getNode(src).getLocation().y();
					double xDEST= getG().getNode(dest).getLocation().x();
					double yDEST=getG().getNode(dest).getLocation().y();
					StdDraw.line(xSRC,ySRC,xDEST,yDEST);
					StdDraw.text(xSRC+(xDEST-xSRC)/4,ySRC+(yDEST-ySRC)/4,(""+weight));
					StdDraw.setPenColor(Color.YELLOW);
					StdDraw.setPenRadius(0.014);
					StdDraw.point(xDEST+(xSRC-xDEST)/10,yDEST+(ySRC-yDEST)/10);
				}
				
				long time=getGame().timeToEnd();
				StdDraw.setPenColor(Color.BLACK);
				StdDraw.setPenRadius(0.020); 
				StdDraw.text(minX+(maxX-minX)*0.8,minY+(maxY-minY)*0.9,"time to end: " +time/1000);
			}
			catch (Exception e) {}
		}
	}
	
	public void addRobots(game_service g) {
		
		List<String> list = g.getRobots();
		if(list!=null) {
			String _json = list.toString();

			try {
				JSONArray line= new JSONArray(_json);
				
				for(int i=0; i< line.length();i++) {
					
					JSONObject t= line.getJSONObject(i);
					JSONObject jrobots = t.getJSONObject("Robot");
					String loc = jrobots.getString("pos");
					String[] point = loc.split(",");
					double x = Double.parseDouble(point[0]);
					double y = Double.parseDouble(point[1]);	
					double z = Double.parseDouble(point[2]);
					Point3D p = new Point3D(x,y,z);
					int id = jrobots.getInt("id");
					int src = jrobots.getInt("src");
					int dest = jrobots.getInt("dest");
					Robot r = new Robot(id,src,dest,p);
					this.getRobots().add(r);				
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}
	}
	
	public void addFruit(game_service g) 
	{	
		List<String> list = g.getFruits();
		if(list!=null) {
			String json = list.toString();

			try {
				JSONArray line= new JSONArray(json);
				
				
				for(int i =0; i<line.length();i++) {
					JSONObject t = line.getJSONObject(i);
					JSONObject fru = t.getJSONObject("Fruit");
					String loc = fru.getString("pos");
					String[] point = loc.split(",");
					double x = Double.parseDouble(point[0]);
					double y = Double.parseDouble(point[1]);
					double z = Double.parseDouble(point[2]);
					Point3D p = new Point3D(x,y,z);
					double value = fru.getDouble("value");
					int type = fru.getInt("type");
					myFruits f = new myFruits(value,type,p);
					this.getFruits().add(f);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
}
	
    public void drawFruits()
	{
	for (int i=0;i<getFruits().size();i++)
	{
			myFruits fruit=getFruits().get(i);
			boolean type=fruit.whichfruit;
			Point3D pos=fruit.pos;
			if(type==false)
				StdDraw.picture(pos.x(), pos.y(),"apple.png", 0.0004, 0.0004);
			else
				StdDraw.picture(pos.x(), pos.y(),"banana.png", 0.0004, 0.0004);
	}
	}
	
    public void drawRobot()
	{
		for (int i=0;i<this.getRobots().size();i++)
		{
				Robot robot=this.getRobots().get(i);
				Point3D pos=robot.pos;
				StdDraw.picture(pos.x(), pos.y(),"robot.png", 0.0009, 0.0007);
		}
	}
		
    public void Mouse()
	{
		String info = getGame().toString();
		JSONObject line;
		int rs=0;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			rs = ttt.getInt("robots");
		}
		catch (JSONException e) {
			e.printStackTrace();
			}
		int nodessize=getG().nodeSize();
		Object nodes[]=new Object[nodessize];
		int j=0;
		for(Iterator<node_data> v=getG().getV().iterator();v.hasNext();) 
		{
			int point=v.next().getKey();
			nodes[j]=point;
			j++;
		}
		int i=0;
		while(i<rs)
		{
			int v=(Integer)JOptionPane.showInputDialog(null,"start place to"+ i +" robot number","add robot",JOptionPane.QUESTION_MESSAGE,null,nodes,null);
			getGame().addRobot(v);
			i++;
		}
		addRobots(getGame());
	}
	
	public void moveRobotsManual()
	{
		List<String> log = getGame().move();
		if(log!=null) {
			long t = getGame().timeToEnd();
			for(int i=0;i<log.size();i++)
			{
					int rid=getRobots().get(i).id;
					int src=getRobots().get(i).src;
					int dest=getRobots().get(i).dest;
					if(dest==-1) 
					{	
						Collection<edge_data> e=this.getG().getE(src);
						Iterator<edge_data> it=e.iterator();
						Object dests[]=new Object[e.size()];
						int j=0;
						while (it.hasNext())
						{
							dests[j]=it.next().getDest();
							j++;
						}
			            dest=(Integer)JOptionPane.showInputDialog(null,"choose next node","move robot manual", JOptionPane.QUESTION_MESSAGE,null,dests,null);
						getGame().chooseNextEdge(rid, dest);
						System.out.println("Turn to node: "+dest+"  time to end:"+(t/1000));
					}
				
			}
		}
		for(Iterator<node_data> verIter=getG().getV().iterator(); verIter.hasNext();)
		{
			int src=verIter.next().getKey();
			try 
			{
				for(Iterator<edge_data> edgeIter=getG().getE(src).iterator();edgeIter.hasNext();)
				{
					edgeIter.next().setTag(0);
				}	
			}
			catch(NullPointerException e)
			{}
		}
		this.getG().init(getGame().getGraph());
		this.getGa().init(getG());
	}
	
	@Override
	public void run() 
	{
		drawS();
		getGame().startGame();
		int index=0;
		while(getGame().isRunning())
		{
				synchronized(this) 
				{
					if (type.equals("automatic"))
					{
					   this.m1.moveRobotsAuto();
						if(index%4==0)
						{
							initGUI();
						}
						  
					}
					else
					{
						moveRobotsManual();
						initGUI();
					}
			   
					index++;
				}
		}		
		String results = getGame().toString();
		System.out.println("Game Over: "+results);
	}

	public static void main(String[] args) 
	{
		MyGameGUI my=new MyGameGUI();
	    my.run();
	}

	public game_service getGame() {
		return game;
	}

	public void setGame(game_service game) {
		this.game = game;
	}

	public DGraph getG() {
		return g;
	}

	public void setG(DGraph g) {
		this.g = g;
	}

	public void setFruits(ArrayList<myFruits> fruits) {
		this.fruits = fruits;
	}

	public Graph_Algo getGa() {
		return ga;
	}

	public void setGa(Graph_Algo ga) {
		this.ga = ga;
	}

	public ArrayList<myFruits> getFruits() {
		
		return fruits;
	}

	public ArrayList<Robot> getRobots() {
		return robots;
	}

	public void setRobots(ArrayList<Robot> robots) {
		this.robots = robots;
	}


}