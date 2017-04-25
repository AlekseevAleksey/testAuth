package example.controller;

import example.model.User;
import example.model.UserProfile;
import example.service.UserProfileService;
import example.service.UserService;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/")
@SessionAttributes("roles")
public class AppController {

    @Autowired
    UserService userService;

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

    @Autowired
    AuthenticationTrustResolver authenticationTrustResolver;

    /**
    Метод отображающий список всех существующих пользователей
     */
    @RequestMapping(value = {"/", "/list"}, method = RequestMethod.GET)
    public String listUser (ModelMap model) {

        List<User> users = userService.findAllUser();
        model.addAttribute("users", users);
        model.addAttribute("loggedinuser", getPrincipal());
        return "userslist";
    }

    /**
     * Метод добавления пользователя
     */
    @RequestMapping(value = "/newuser",method = RequestMethod.GET)
    public String newUser (ModelMap model) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("edit", false);
        model.addAttribute("loggedinuser", getPrincipal());
        return "registration";
    }

    /**
     * Метод сохранеия пользователя в базе. Так же проверяет правильность ввода.
     */
    @RequestMapping(value = {"/newuser"}, method = RequestMethod.POST)
    public String saveUser (@Valid User user, BindingResult result, ModelMap model) {

        if (result.hasErrors()) {
            return "registration";
        }

        /**
         * проверка уникальности поля sso и вывод сообщений
         */
        if (!userService.isUserSSOUnique(user.getId(), user.getSsoId())) {
            FieldError ssoError = new FieldError("user", "ssoId",
                    messageSource.getMessage("non.unique.ssoId", new String[]{user.getSsoId()},
                            Locale.getDefault()));
            result.addError(ssoError);
            return "registration";
        }

        userService.saveUser(user);

        model.addAttribute("success", "User " + user.getFirstName() + " " + user.getLastName() + " registered successfully");
        model.addAttribute("loggedinuser", getPrincipal());
        return "registrationsuccess";
    }

    /**
     * Метод обнавления существующих пользователей
     */
    @RequestMapping(value = {"/edit-user-{ssoId}"}, method = RequestMethod.GET)
    public String editUser (@PathVariable String ssoId, ModelMap model) {
        User user = userService.findBySSO(ssoId);
        model.addAttribute("user", user);
        model.addAttribute("edit", true);
        model.addAttribute("loggedinuser", getPrincipal());
        return "registration";
    }

    /**
     * Метод отпарвки формы для обнавления списка пользователей в базе и валидация введенных данных
     */
    @RequestMapping(value = {"/edite-user-{ssoId}"}, method = RequestMethod.POST)
    public String updateUser (@Valid User user, BindingResult result, ModelMap model,
                              @PathVariable String ssoId) {
        if (result.hasErrors()){
            return "registration";
        }

        userService.updateUser(user);

        model.addAttribute("success", "User " + user.getFirstName() + " "+ user.getLastName() + " updated successfully");
        model.addAttribute("loggedinuser", getPrincipal());
        return "registrationsuccess";
    }

    /**
     * Метод удаляющий пользователя по его ssoId
     */
    @RequestMapping(value = {"/delete-user-{ssoId}"}, method = RequestMethod.GET)
    public String deleteUser (@PathVariable String ssoId) {
        userService.deleteUserBySSO(ssoId);
        return "redirect: /list";
    }

    /**
     * Метод прдоставляющий список пользователей для просмотра
     */
    @ModelAttribute("roles")
    public List<UserProfile> initializeProfiles () {
        return userProfileService.findAll();
    }

    /**
     * Метод обрабатывает переадрисацию Access-Denied
     */
    @RequestMapping(value = "/Access_Denied")
    public String accessDeniedPage (ModelMap model) {
        model.addAttribute("loggedinuser", getPrincipal());
        return "accessDenied";
    }

    /**
     * Метод обработки get-запроса для входа.
     * Если пользователь авторизирован, то получит редирект на страницу списка
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage () {
        if (isCurrentAuthenticationAnonymous()) {
            return "login";
        } else {
            return "redirect: /list";
        }
    }

    /**
     *Метод обрабатывающий выход из системы.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            persistentTokenBasedRememberMeServices.logout(request,response,authentication);
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        return "redirect: /login?logout";
    }

    /**
     *Метод возвращает userName пользователя вошедшего в систему
     */
    private String getPrincipal() {
        String userName = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            userName = ((UserDetails) principal).getUsername();
        } else {
            userName = principal.toString();
        }
        return userName;
    }

    private boolean isCurrentAuthenticationAnonymous() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authenticationTrustResolver.isAnonymous(authentication);
    }


}
