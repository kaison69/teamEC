package com.internousdev.maple.action;

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.internousdev.maple.dao.CartInfoDAO;
import com.internousdev.maple.dao.UserInfoDAO;
import com.internousdev.maple.dto.CartInfoDTO;
import com.internousdev.maple.dto.UserInfoDTO;
import com.internousdev.maple.util.InputChecker;
import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware{
	private String userId;
	private String password;
	private boolean savedUserIdFlag;
	private List<String>userIdErrorMessageList;
	private List<String>passwordErrorMessageList;
	private String isNotUserInfoMessage;
	private List<CartInfoDTO> cartInfoDTOList;
	private int cartPrice;
	private Map<String,Object>session;

	public String execute(){
		if(!session.containsKey("tempUserId")){
			return "sessionTimeout";
		}
		if(session.containsKey("createUserFlag")){
			userId=session.get("userIdForCreateUser").toString();
			password=session.get("password").toString();
			session.remove("userIdForCreateUser");
			session.remove("password");
			session.remove("createUserFlag");
		}else if(session.containsKey("userIdForCreateUser")){
			session.remove("userIdForCreateUser");
			session.remove("password");
			session.remove("familyName");
			session.remove("firstName");
			session.remove("familyNameKana");
			session.remove("firstNameKana");
			session.remove("sex");
			session.remove("sexList");
			session.remove("email");
		}
		String result=ERROR;

		session.remove("savedUserIdFlag");

		InputChecker inputChecker =new InputChecker();
		userIdErrorMessageList=inputChecker.doCheck("ユーザーID",userId,1,8,true,false,false,true,false,false);
		passwordErrorMessageList=inputChecker.doCheck("パスワード",password,1,16,true,false,false,true,false,false);
		if(userIdErrorMessageList.size()>0 || passwordErrorMessageList.size()>0){
			session.put("logined",0);
			return result;
		}
		UserInfoDAO userInfoDAO=new UserInfoDAO();
		if(userInfoDAO.isExistsUserInfo(userId,password)&&userInfoDAO.login(userId,password)>0){
			CartInfoDAO cartInfoDAO=new CartInfoDAO();

			String tempUserId=session.get("tempUserId").toString();
			List<CartInfoDTO>cartInfoDTOListForTempUser =cartInfoDAO.getCartInfo(tempUserId);
			if(cartInfoDTOListForTempUser!=null && cartInfoDTOListForTempUser.size()>0){
				boolean cartresult =changeCartInfo(cartInfoDTOListForTempUser,tempUserId);
				if(!cartresult){
					return "DBError";
				}
			}
			if(session.containsKey("cartFlag")){
				session.remove("cartFlag");
				cartInfoDTOList=cartInfoDAO.getCartInfo(userId);
				cartPrice=cartInfoDAO.getCartPrice(userId);
				result="cart";
			}else{
				result=SUCCESS;
			}
			UserInfoDTO userInfoDTO=userInfoDAO.getUserInfo(userId,password);
			session.put("userId",userInfoDTO.getUserId());
			session.put("logined",1);
			if(savedUserIdFlag){
				session.put("savedUserIdFlag",true);
			}
			session.remove("tempUserId");
		}else{
			isNotUserInfoMessage="ユーザーIDまたはパスワードが異なります";
		}
		return result;
	}
	private boolean changeCartInfo(List<CartInfoDTO>cartInfoDTOListForTempUser, String tempUserId){
		int count=0;
		CartInfoDAO cartInfoDAO =new CartInfoDAO();
		boolean result =false;

		for(CartInfoDTO dto : cartInfoDTOListForTempUser){
			if(cartInfoDAO.isExistsCartInfo(userId,dto.getProductId())){
				count+=cartInfoDAO.addCart(dto.getProductCount(),userId,dto.getProductId());
				cartInfoDAO.delete(tempUserId,String.valueOf(dto.getProductId()));
			}else{
				count+=cartInfoDAO.connectUserId(userId,tempUserId,dto.getProductId());
			}
		}
		if(count==cartInfoDTOListForTempUser.size()){
			result=true;
		}
		return result;
	}

	public String getUserId(){
		return userId;
	}

	public void setUserId(String userId){
		this.userId=userId;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password=password;
	}
	public boolean isSavedUserIdFlag() {
		return savedUserIdFlag;
	}

	public void setSavedUserIdFlag(boolean savedUserIdFlag) {
		this.savedUserIdFlag = savedUserIdFlag;
	}

	public List<String> getUserIdErrorMessageList() {
		return userIdErrorMessageList;
	}

	public void setUserIdErrorMessageList(List<String> userIdErrorMessageList) {
		this.userIdErrorMessageList = userIdErrorMessageList;
	}

	public List<String> getPasswordErrorMessageList() {
		return passwordErrorMessageList;
	}

	public void setPasswordErrorMessageList(List<String> passwordErrorMessageList) {
		this.passwordErrorMessageList = passwordErrorMessageList;
	}
	public String getIsNotUserInfoMessage() {
		return isNotUserInfoMessage;
	}

	public void setIsNotUserInfoMessage(String isNotUserInfoMessage) {
		this.isNotUserInfoMessage = isNotUserInfoMessage;
	}

	public List<CartInfoDTO> getCartInfoDTOList() {
		return cartInfoDTOList;
	}

	public void setCartInfoDTOList(List<CartInfoDTO> cartInfoDTOList) {
		this.cartInfoDTOList = cartInfoDTOList;
	}

	public int getCartPrice() {
		return cartPrice;
	}

	public void setTotalPrice(int cartPrice) {
		this.cartPrice = cartPrice;
	}

	public Map<String, Object> getSession() {
		return session;
	}
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
