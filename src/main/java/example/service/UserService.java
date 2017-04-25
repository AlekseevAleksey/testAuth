package example.service;

import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserService {

    User findByID (int id);

    example.model.User findBySSO (String sso);

    void saveUser (User user);

    void updateUser (User user);

    void deleteUserBySSO (String sso);

    List<example.model.User> findAllUser();

    boolean isUserSSOUnique (Integer id, String sso);

}
