package example.service;




import example.model.UserProfile;

import java.util.List;

public interface UserProfileService {

    UserProfile findById (int id);

    UserProfile findByType (String type);

    List<UserProfile> findAll();

}
