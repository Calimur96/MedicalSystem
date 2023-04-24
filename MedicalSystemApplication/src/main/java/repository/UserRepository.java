package repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import model.User;
import model.User.UserRole;

public interface UserRepository extends JpaRepository<User,Long>{

	User findByEmail(String email);
	User getByEmail(String email);

	User findByPhone(String phone);

	User findByEmailAndDeleted(String email, boolean deleted);

	List<User> findAllByRole(UserRole role);


}
