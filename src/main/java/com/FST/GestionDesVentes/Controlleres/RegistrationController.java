package com.FST.GestionDesVentes.Controlleres;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.FST.GestionDesVentes.model.User;
import com.FST.GestionDesVentes.service.UserRegistrationService;

@RestController
public class RegistrationController 
{
	@Autowired
	private UserRegistrationService userRegisterService;
	
	
	@PostMapping("/registeruser")
	@CrossOrigin(origins = "http://localhost:4200")
	public User registerUser(@RequestBody User user) throws Exception
	{
		String currEmail = user.getEmail();
		if(currEmail != null || !"".equals(currEmail))
		{
			User userObj = userRegisterService.fetchUserByEmail(currEmail);
			if(userObj != null)
			{
				throw new Exception("User with "+currEmail+" already exists !!!");
			}
		}
		System.out.println("here");
		User userObj = null;
		userObj = userRegisterService.saveUser(user);
		return userObj;
	}

	@GetMapping("/gettotalusers")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<List<Integer>> getTotalSlots() throws Exception
	{
		List<User> users = userRegisterService.getAllUsers();
		List<Integer> al = new ArrayList<>();
		al.add(users.size());
		return new ResponseEntity<List<Integer>>(al, HttpStatus.OK);
	}

}
