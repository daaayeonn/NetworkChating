package chat8;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyConnection {
	
	public Connection con;
	public ResultSet rs;
	public PreparedStatement psmt;
	
	public MyConnection(String user, String pass) {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			
			String url = "jdbc:oracle:thin:@localhost:1521:xe";
			con = DriverManager.getConnection(url, user, pass);
			
			if (con != null) {
				System.out.println("연결 성공");
			}
		}
		catch (Exception e) {
			System.out.println("DB 커넥션 예외발생");
		}
	}
	
	public void dbClose() {
		try {
			if (con != null) con.close();
			if (rs != null) rs.close();
			if (psmt != null) psmt.close();
		}
		catch (Exception e) {
			System.out.println("DB 자원 반납 시 예외발생");
		}
	}
}
