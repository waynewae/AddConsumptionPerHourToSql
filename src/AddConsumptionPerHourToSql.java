import java.sql.*;
import java.io.*;

class SqlHandler {
	String Username;
	String Password;
	String Database;
	String ConnectionString;
	static Connection connection;
	static Statement command;
	
	public SqlHandler(String User, String Pw, String Db, String CS) {
		Username = User;
		Password = Pw;
		Database = Db;
		ConnectionString = CS;
	}
	
	void Connect() {
		try {
			connection = DriverManager.getConnection(ConnectionString, Username, Password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connect to database : " + Database);
	}
	
	void Disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Disonnect from database : " + Database);
	}
	
	static void insertRecords(Records Data, String Table) {
		try {
			command = connection.createStatement();
			command.execute("INSERT INTO ConsumptionPerHour (DATE) VALUES(" + Data.Date + ")");
			for(int i = 0 ; i < Data.BatteryLevel.length ; i++) {
				//System.out.println("UPDATE ConsumptionPerHour SET STAMP" + (i + 1) + "=" + Data.BatteryLevel[i] + " WHERE DATE=" + Data.Date);
				command.execute("UPDATE ConsumptionPerHour SET STAMP" + (i + 1) + "=" + Data.BatteryLevel[i] + " WHERE DATE=" + Data.Date);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

class DirHandler {
	String HomeDir;
	int DirCount;
	String[] DirList;
	
	public DirHandler(String RootPath) {
		HomeDir = RootPath;
		File Home = new File(HomeDir);
		DirList = Home.list();
		DirCount = Home.list().length;
		System.out.println("Find " + DirCount + " Directories at " + HomeDir);
	}		
}

class Records {
	String Date;
	String[] BatteryLevel;
	String[] Tokens;
	int HrCurrent;
	int HrPre = 0;
	int BatteryLevelCount = 0;
	
	public Records() {
		Date = "";
		BatteryLevel = new String[23];
	}
	public void getDate(String DirName) {
		Date = DirName.substring(0, 8);
		System.out.println("Date of " + DirName + " is " + Date);
	}
	public void getConsumption(String RootPath, String DirName) {
		String AbsolutePath = RootPath + "\\" + DirName + "\\" + Date + "_PowerLog" + "\\" + "PowerCalculator@4316001.log";	
		try {
			BufferedReader Br = new BufferedReader(new FileReader(AbsolutePath));
			String Line;
			String Tmp = null;
			while((Line = Br.readLine()) != null) {
				Tmp = Line;
				if(Tmp.length() >= 8) {
					HrCurrent = Integer.parseInt(Tmp.substring(8, 10));
					if(HrCurrent != HrPre) {
						 HrPre = HrCurrent;
						 Tmp = Tmp.substring(Tmp.indexOf(')'));
						 Tokens = Tmp.split(",");
						 BatteryLevel[BatteryLevelCount] = Tokens[4];
						 //System.out.println("Time " + HrCurrent + " BatteryLevel[" + BatteryLevelCount + "] is " + BatteryLevel[BatteryLevelCount]);
						 BatteryLevelCount++;
					}
				}
			}
			Tmp = Tmp.substring(Tmp.indexOf(')'));
			Tokens = Tmp.split(",");
			BatteryLevel[BatteryLevelCount] = Tokens[4];
			//System.out.println("Time " + HrCurrent + " BatteryLevel[" + BatteryLevelCount + "] is " + BatteryLevel[BatteryLevelCount]);
			Br.close();
		} catch (IOException e) {System.out.println(e);}
	}
}

public class AddConsumptionPerHourToSql {
	
	private static final String Username = "";
	private static final String Password = "";
	private static final String Database = "VNA_DoU";
	private static final String Table = "ConsumptionPerHour";
	private static final String ConnectionString = "jdbc:mysql://localhost:3306/" + Database;
	private static final String RootPath = "C:\\Users\\ARGO\\Dropbox\\FIH\\[VNA]DoU\\Serial test\\Analysis";
	//private static ResultSet data;
		
	public static void main(String[] args) {
		SqlHandler SqlServer;
		SqlServer = new SqlHandler(Username, Password, Database, ConnectionString);
		SqlServer.Connect();
		
		DirHandler SerialTest;
		SerialTest = new DirHandler(RootPath);

		for(int i = 0; i < SerialTest.DirCount; i++) {
			Records DoUCases = new Records();
			DoUCases.getDate(SerialTest.DirList[i]);
			//PowerLogPath = RootPath + SerialTest.DirList[i] + "\\" + DoUCases.Date + "_PowerLog";
			DoUCases.getConsumption(RootPath, SerialTest.DirList[i]);
			SqlHandler.insertRecords(DoUCases, Table);
		}
		
		SqlServer.Disconnect();
	}
}