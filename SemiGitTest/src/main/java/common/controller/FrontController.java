package common.controller;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(
		description = "사용자가 웹에서 *.up을 했을 경우 이 서블릿이 응답을 해주도록 한다.",
		urlPatterns = { "*.up" }, 
		initParams = { 
				@WebInitParam(name = "propertyConfig", value = "C:/NCS/workspace(jsp)/MyMVC/src/main/webapp/WEB-INF/Command.properties", description = "*.up에 대한 클래스의 매핑파일")
		})
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Map<String, Object> cmdMap = new HashMap<>();
	
	
	public void init(ServletConfig config) throws ServletException {
		/*
			웹브라우저 주소창에서 *.up을 하면 FrontContoleer 서블릿이 응대를 해오는데
			맨 처음에 자동적으로 실행되는 메소드가 init(ServletConfig config)
			init(ServletConfig config) 메소드는 WAS(톰캣)가 구동된 후
			딱 1번만 init(ServletConfig config)메소드가 실행되고, 그 이후에는 실행되지 않음.
			그러므로 init(ServletConfig config) 메소드에는 FrontContoller 서블릿이 동작해야할 환경설정을 잡아주는데 사용된다.
			
			*** 확인용 ***
			System.out.println("~~~ 확인용 => 서블릿 FrontController의 init(ServletConfig config) 메소드가 실행됨");
			
			
		 */
		
		FileInputStream fis = null;
		// 특정 파일에 있는 내용을 읽어오기 위한 용도로 쓰이는 객체
		
		String props = config.getInitParameter("propertyConfig");
		//System.out.printf("확인용 props => %s\n",props);
//		확인용 props => C:/NCS/workspace(jsp)/MyMVC/src/main/webapp/WEB-INF/Command.properties
		
		
		
		try {
			fis = new FileInputStream(props);
			//fis는 path값의 파일의 내용을 읽어오기 위한 용도로 쓰이는 객체.
			
			Properties pr = new Properties();
			/*
				Properties 는 HashMap의 구버전인 Hashtable을 상속받아 구현한 것으로,
			    Hashtable 은 키와 값(Object, Object)의 형태로 저장하는데 비해서
			    Properties 는 (String 키, String 밸류값)의 형태로 저장하는 단순화된 컬렉션 클래스이다.
			    키는 고유해야 한다. 즉, Key는 중복을 허락하지 않는다. 중복된 값을 넣으면 마지막에 넣은 값으로 덮어씌운다.
			    주로 어플리케이션의 환경설정과 관련된 속성(property)을 저장하는데 사용되며, 
			    데이터를 파일로 부터 읽고 쓰는 편리한 기능을 제공한다.    
			 */ 
			pr.load(fis);
			/*
			   inputStream의 path명의 파일의 내용을 읽어 Properties 클래스 객체 pr에 로드시킨다.
			   읽어온 파일(Command.properties)의 내용에서 =을 기준으로 왼쪽은 Key 오른쪽은 Value로 인식
			 */
			
			
			Enumeration<Object> en = pr.keys();
			// 모든 Key를 가져온다 
			
			
			
			while(en.hasMoreElements()) {
				String key = (String)en.nextElement();
				String className = pr.getProperty(key);
				//System.out.println(className);		
				
				if(className != null) {
					
					className = className.trim();
					Class<?> cls = Class.forName(className);
					// <?>은 어떤 클래스 타입인진 모르지만 클래스 타입이라면 무엇이든 들어올 수 있는 Generic
					// String 타입으로 된 className을 클래스화 시켜줌
					// 반드시 className과 같은 이름의 클래스가 존재해야함.
					
					Constructor<?> constr = cls.getDeclaredConstructor();
					//생성자 만들기
					
					Object obj = constr.newInstance();
//					System.out.printf(" Obj => %s\n", obj);
//					System.out.println(obj.getClass());
//					System.out.println(obj.toString());
					
					cmdMap.put(key, obj);
					// properties의 key값의 밸류에 해당하는 Class 객체를 hashMap 에 담는다.
					// key값에 해당하는 URL(ex.register.up)을 호출하면 cmdMap에서 매핑된 객체를 가져옴
					
					
				} // end of if(className != null)
			} 
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("파일이 없습니다.");
		} catch (IOException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			
			System.out.println("문자열로 명명된 클래스가 존재하지 않습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//웹브라우저 주소 입력창에서
		//http://localhost:9090/MyMVC/member/idDupllicateCheck.up?userid=leess
//		String url = request.getRequestURL().toString();
//		System.out.println("~~~ 확인용 url => " + url);
		
		String uri = request.getRequestURI();
		//System.out.println(uri);
		String path = request.getContextPath();
		String key = uri.substring(path.length());
		
		AbstractController action = (AbstractController)cmdMap.get(key);
		
		if(action == null) {
			System.out.println(">>> key는 uri패턴에 매핑된 클래스가 없습니다.");
		} else {
			try {
				/*
                post 방식으로 넘어온 데이터중 영어는 글자가 안깨지지만,
                   한글은 글자모양이 깨져나온다.
                   그래서  post 방식에서 넘어온 한글 데이터가 글자가 안깨지게 하려면 
                   아래처럼 request.setCharacterEncoding("UTF-8"); 을 해야 한다.
                   주의할 것은 request.getParameter("변수명"); 보다 먼저 기술을 해주어야 한다는 것이다.      
             */
				request.setCharacterEncoding("UTF-8");
				action.execute(request, response);
				String viewPage = action.getViewPage();
				if (action.isRedirect()) {
					
					//
					
					response.sendRedirect(viewPage);
					
				} else {
					// forward는 웹브라우저의 URL 주소는 변경하지 않고 그대로 화면에 JSP를 띄우는 것
					// forward 방식은 forward 되어지는 페이지로 데이터를 전달할 수 있다
					if(viewPage != null) {
						RequestDispatcher dispatcher = request.getRequestDispatcher(viewPage);
						dispatcher.forward(request, response);
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
