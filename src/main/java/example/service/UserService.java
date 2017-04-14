package example.service;

import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserService {

    User findByID (int id);

    User findBySSO (String sso);

    void saveUser (User user);

    void updateUser (User user);

    void deleteUserBySSO (String sso);

    List<User> findAllUser();

    boolean isUserSSOUnique (Integer id, String sso);

}
