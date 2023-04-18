package common.controller;

public abstract class AbstractController implements InterCommand {
	
	/*
	 	
	 	▶️ view단 페이지(.jsp)로 이동시 
	 	forward 방법(dispatcher)으로 하고자 한다면
	 	자식 클래스에서는 부모클래스에서 생성해둔 메소드 호출시 아래와 같이 하면 된다.
	 	
	 	super.setRedirect(false);
	 	super.setViewPage("/WEB-INF/index.jsp");
	 	
	 	
	 	▶️ url 주소를 변경하여 페이지 이동시
	 	sendRedirect 방식
	 	
	 	super.setRedirect(true);
	 	super.setViewPage("registerMember.up");
	 
	 */
	
	private boolean isRedirect = false;
	// 변수의 값이 false라면 view단 페이지로 forward하기 위한 변수
	// true 라면 sendRedirect 
	
	private String viewPage;
	// viewPage는 isRedirect 값이 false라면 view단 페이지의 경로명
	// isRedirect값이 true라면 sendRedirec의 url값

	public boolean isRedirect() {
		return isRedirect;
	}

	public void setRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}

	public String getViewPage() {
		return viewPage;
	}

	public void setViewPage(String viewPage) {
		this.viewPage = viewPage;
	}
	
	
	
}
