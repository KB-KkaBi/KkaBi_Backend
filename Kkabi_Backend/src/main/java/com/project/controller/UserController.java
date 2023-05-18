package com.project.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.domain.AccountList;
import com.project.domain.AccountLog;
import com.project.domain.User;
import com.project.service.UserService;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;
	
	/**
	 * 로그인하기
	 * @params : email, pw
	 * 
	 * */
	@PostMapping("/login")
	public Object signIn(@RequestBody User user, HttpSession session) {
		
		User dbUser = userService.signIn(user);
		
		
		//HttpSession에 정보를 저장한다.
		dbUser.setPw(null);
		session.setAttribute("loginUser", dbUser); //jsp에서 ${loginUser} 
		
		//보내줄 유저정보 다시 세팅
		HashMap<String, Object> users = new LinkedHashMap<String, Object>();
		users.put("userSeq", dbUser.getUserSeq());
		users.put("nickname", dbUser.getNickname());
		users.put("character", dbUser.getCharacter());
		
		
		//response할 객체 생성
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("user", users);
		
		
		return map;
	}
	
	/**
	 * 로그아웃하기
	 **/
	@PostMapping("/logout")
	public Object logout(HttpSession session) {
	
		//그냥 세션에서 값 있는지 확인
		User user = (User)session.getAttribute("loginUser");
		//User dbUser = userService.logout(user.getUserSeq());
		
		HashMap<String, Object> maps = new HashMap<String, Object>();
		//모든 세션의 정보를 삭제한다.
		if (user != null) {
			session.invalidate();
			maps.put("message", "로그아웃 성공");
		}else {
			throw new RuntimeException("로그아웃 실패");
		}
		
		
		
		return maps;
	}
}
