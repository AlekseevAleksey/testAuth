package example.dao;


import example.model.User;

import java.util.List;

public interface UserDao {

    User findById(int id);

    User FindBySSO(String sso);

    void save (User user);

    void deleteBySSO (String sso);

    List<User> findAllUsers();

}
