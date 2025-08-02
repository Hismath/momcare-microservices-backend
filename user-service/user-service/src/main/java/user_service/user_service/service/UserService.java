package user_service.user_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import user_service.user_service.repository.UserRepository;
import user_service.user_service.entity.User;

@Service
public class UserService {
	
	@Autowired
	private UserRepository uRepo;
	
	public User register(User user)
	{
		return uRepo.save(user);
	}
	
	public Optional<User> findByEmail(String email)
	{
		return uRepo.findByEmail(email);
	}
	
	public List<User> getAllUsers()
	{
		return uRepo.findAll();
	}
	
	public Optional<User> findUserById(Long id)
	{
		return uRepo.findById(id);
	}

}
