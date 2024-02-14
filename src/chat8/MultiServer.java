package chat8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MultiServer extends MyConnection {
	
	// 멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	// 클라이언트 정보를 저장하기 위한 Map 컬렉션 생성
	Map<String, PrintWriter> clientMap;
	
	// 생성자
	public MultiServer(String user, String pass) {
		super(user, pass);
		
		clientMap = new HashMap<String, PrintWriter>();
		
		Collections.synchronizedMap(clientMap);
		
		//블랙, 금칙어 set으로  여기에
		
	} // MultiServer 끝
	
	// 채팅 서버 초기화
	public void init() {
		
		try {
			// 서버소캣 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			 // 1명의 클라이언트가 접속할때마다 허용해주고 동시에 쓰레드를 생성한다.
			while (true) {
				socket = serverSocket.accept();
				
				 // 클라이언트 1명당 하나의 쓰레드가 생성되어 메세지 전송 및 수신을 담당한다.
				Thread mst = new MultiServerT(socket);
				mst.start();
			} // while 끝
		} // try 끝
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} // finally 끝
	} // init 끝
	
	/*
	 인스턴스 생성 후 초기화 메서드를 호출한다.
	 */
	public static void main(String[] args) {
		MultiServer ms = new MultiServer("study", "1234");
		ms.init();
	} // main 끝
	
	/*
	 접속된 모든 클라이언트 측으로 서버의 메세지를 Echo 해주는 역할을 수행한다.(이전 단게에서는
	 보낸 사람에게만 Echo 되었다.)
	 */
	public void sendAllMsg(String name, String msg) {
		/*
		 Map에 저장된 클라이언트의 Key를 얻어온다. Key에는 대화명이 저장되어 있다.
		 */
		Iterator<String> it = clientMap.keySet().iterator();
		
		// 앞에서 얻어온 대화명(Key값)의 갯수만큼 반복한다.
		while (it.hasNext()) {
			try {
				// 각 클라이언트의 PrintWriter 인스턴스를 추출한다.
				PrintWriter it_out = (PrintWriter)
				clientMap.get(it.next());
				
				/*
				 클라이언트에게 메세지를 전달할 때 매개변수로 name이 있는 경우와 없는 경우를
				 구분해서 전달한다.
				 */
				if(name.equals("")) {
					/*
					 입장 혹은 퇴장에서 사용되는 부분
					 */
					it_out.println(URLEncoder.encode(msg, "UTF-8"));
				}
				else {
					/*
					 매새자룰 보낼 때 사용되는 부분
					 */
					it_out.println("[" + URLEncoder.encode(name, "UTF-8") 
						+ "]: " + URLEncoder.encode(msg, "UTF-8"));
				}
			} // try 끝
			catch (Exception e) {
				System.out.println("예외: " + e);
			}
		} // while 끝
	} // sendAllMsg 끝
	
	// 귓속말 전송 : 발신자 대화명, 메세지, 수신자 대화명
	public void sendAllMsg(String name, String msg, String receiveName) {
		Iterator<String> it = clientMap.keySet().iterator();
		
		while (it.hasNext()) {
			try {
				// HashMap에는 Key로 대화명, Value로 Printwrite인스턴스가 저장되어있다.
				String clientName = it.next();
				PrintWriter it_out = 
						(PrintWriter) clientMap.get(clientName);

				/*
				 해당 루프에서의 클라이언트 이름과 귓속말을 받을 사람의 대화명이 일치하는지
				 확인한다.
				 */
				if (clientName.equals(receiveName)) {
					// 일차하면 한 사람에게만 귓속말을 보낸다.
					it_out.println("[" +  URLEncoder.encode("귓속말", "UTF-8") +"]" 
							+ URLEncoder.encode(name, "UTF-8") 
							+ ": " + URLEncoder.encode(msg, "UTF-8"));
				}
			} // try 끝
			catch (Exception e) {
				System.out.println("예외: " + e);
			}
		} // while 끝
	} // sendAllMsg (귓속말) 끝
	
	// 참가자 리스트 출력
	public void chatLists(String lists) {
		Iterator<String> it = clientMap.keySet().iterator();
		
		while (it.hasNext()) {
			try {
				String clientName = it.next();
				PrintWriter it_out = 
						(PrintWriter) clientMap.get(clientName);
				
				it_out.println(URLEncoder.encode(lists, "UTF-8"));
				
			} // try 끝
			catch (Exception e) {
				System.out.println("예외: " + e);
			}
		} // while 끝
	} // chatLists (귓속말) 끝
	
	// 내부클래스
	class MultiServerT extends Thread {

		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;

		public MultiServerT(Socket socket) {
			this.socket = socket;
			
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(
						new InputStreamReader(this.socket.getInputStream()));
			}
			catch (Exception e) {
				System.out.println("예외: " + e);
			}
		} // MultiServerT 끝
		
		@Override
		public void run() {
			
			String name = "";
			String s = "";
			String query;
			
			try {
				// 첫번째 메세지는 대화명이므로 접속을 알린다.
				name = URLDecoder.decode(in.readLine(), "UTF-8");

				sendAllMsg("", URLEncoder.encode(name, "UTF-8") + 
						 URLEncoder.encode("님이 입장하셨습니다.", "UTF-8"));
				clientMap.put(name, out);
				
				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는 " + clientMap.size() + 
						"명 입니다.");
				
				
				// 두번째 메세지부터는 "대화내용"
				while (in != null) {
					s = URLDecoder.decode(in.readLine(), "UTF-8");
					if (s == null) break;
					
					try {
						query = "insert into chat_talking values "
								+ "(seq_serial_num.nextval, ?, ?, sysdate)";
						psmt = con.prepareStatement(query);
						psmt.setString(1, name);
						psmt.setString(2, s);
						int result = psmt.executeUpdate();
						System.out.println(result + "행이 입력되었습니다.");
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					// 서버의 콘솔에는 메세지를 그대로 출력한다.
					System.out.println(name + " >> " + s);
					
					/*
				 귓속말형식 => /to 수신자명 대화내용 블라블라
					 */
					if (s.charAt(0) == '/') {
						// 슬러쉬로 시작하면 명령어로 판단
						/*
					 split() 으로 문자열을 분리한다. 여기서 사용하는 구분자는 스페이스이다.
						 */
						String[] strArr = s.split(" ");
						
						/*
					 문자열을 스페이스로 분리하면 0번 인덱스는 명령어, 1번 인덱스는 수신자
					 대화명이 되고, 2번 인덱스부터 끝까지는 대화내용이 되므로 아래와 같이 문자열
					 처리를 해야한다.
						 */
						String msgContent = "";
						for(int i=2; i<strArr.length; i++) {
							msgContent += strArr[i]+ " ";
						}
						
						/*
					 명령어가 /to가 맞는지 확인한다. 명령어에 대한 오타가 있을수도 있고,
					 다른 명령어 일수도 있기 때문이다.
						 */
						if (strArr[0].equals("/to")) {
							// 귓속말을 보낸다.
							/*
						 기존의 메서드를 오버로딩해서 추가 정의한다. 
						 매개변수는 발신대화명, 메세지, 수신대화명 형태로 작성한다.
							 */
							sendAllMsg(name, msgContent, strArr[1]);
						} // if 끝
						
						// 참가자 목록
						if (strArr[0].equals("/list")) {
							Set<String> keys = clientMap.keySet();
							keys.remove(name);
							
							for (String lists : keys) {
								System.out.println(lists);
								chatLists(lists);
								clientMap.put(name, out);
							}
						}
					} // if 끝
					else {
						// 슬러쉬가 없다면 일반 대화내용
						sendAllMsg(name, s);
					}
				} // while 끝
				
			} // try 끝
			catch (Exception e) {
				System.out.println("예외: " + e);
			}
			finally {
				clientMap.remove(name);
				sendAllMsg("", name + "님이 퇴장하셨습니다.");
				System.out.println(name + " [" + 
				Thread.currentThread().getName() + "] 퇴장");
				System.out.println("현재 접속자 수는 " +
						clientMap.size() + "명 입니다.");
				
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} // finally 끝
		} // run 끝
	}
}
