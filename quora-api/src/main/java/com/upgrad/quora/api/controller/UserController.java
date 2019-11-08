package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserBusinessService userBusinessService;

    /*
     * Signup, as the name suggests, acts as the checkpoint where a new user can signup.
     * It asks for all the details that are relevant to keep in the database.
     * Some fields are mandatory to be filled by the user while others are optional.
     * A unique UUID is set for every new user who successfully signs up.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setSalt("123abc");
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setRole("nonadmin");
        userEntity.setContactnumber(signupUserRequest.getContactNumber());

        UserEntity userByUsername = userBusinessService.getUserByUsername(userEntity.getUsername());
        if (userByUsername == null) {
            UserEntity userByEmail = userBusinessService.getUserByEmail(userEntity.getEmail());
            if (userByEmail == null) {
                final UserEntity createdUserEntity = userBusinessService.signup(userEntity);
                SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
                return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
            }
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
        throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");

    }

    /*
     * SignIn, as the name suggests, is the checkpoint where an existing user can login using his/her credentials.
     * The entered credentials are checked against the existing users in the database and if an entry matches, login is successful.
     * Upon a successful signin, the user is given a token for that active session.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signIn(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");
        UserAuthTokenEntity userAuthToken = userBusinessService.signIn(decodedArray[0], decodedArray[1]);


        UserEntity user = userAuthToken.getUser();

        SigninResponse authorizedUserResponse = new SigninResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(authorizedUserResponse, headers, HttpStatus.OK);

    }

    /*
     * "signout" is the checkpoint where the user is signed out of the current active session.
     * The token provided at the time of signin is also terminated with this action.
     * Upon a successful signout, the user is redirected to the login page again.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOut(@RequestHeader("authorization") final String authorizationToken) throws SignOutRestrictedException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if (bearerToken.length == 1) {
            userAuthTokenEntity = userBusinessService.getUserAuthToken(bearerToken[0]);
        } else {
            userAuthTokenEntity = userBusinessService.getUserAuthToken(bearerToken[1]);
        }
        //UserAuthTokenEntity userAuthTokenEntity = userBusinessService.getUserAuthToken(bearerToken[1]);

        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthTokenEntity.getUuid()).message("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);

    }

}
