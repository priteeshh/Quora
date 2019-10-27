package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.SignUpBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private SignUpBusinessService signUpBusinessService;

    @RequestMapping(method= RequestMethod.POST, path="/signup", consumes= MediaType.APPLICATION_JSON_UTF8_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
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

        UserEntity userByUsername = signUpBusinessService.getUserByUsername(userEntity.getUsername());
        if(userByUsername == null){
            UserEntity userByEmail = signUpBusinessService.getUserByEmail(userEntity.getEmail());
            if(userByEmail == null){
                final UserEntity createdUserEntity = signUpBusinessService.signup(userEntity);
                SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
                return new ResponseEntity<SignupUserResponse>(userResponse,HttpStatus.CREATED);
            }
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }
        throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");

    }

}
