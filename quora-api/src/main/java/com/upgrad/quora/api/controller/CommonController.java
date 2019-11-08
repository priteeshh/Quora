package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController {
    @Autowired
    private CommonBusinessService commonBusinessService;

    // This endpoint is called to fetch the details of the user who is logged in at the given time.
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUser(@PathVariable("userId")final String userId, @RequestHeader("authorization")  final String authorizationToken) throws UserNotFoundException, AuthorizationFailedException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if(bearerToken.length == 1){
            userAuthTokenEntity = commonBusinessService.getUserAuthToken(bearerToken[0]);
        }else{
            userAuthTokenEntity = commonBusinessService.getUserAuthToken(bearerToken[1]);
        }

        final UserEntity userEntity = commonBusinessService.getUser(userId);
       UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName()).lastName(userEntity.getLastName())
                .userName(userEntity.getUsername()).emailAddress(userEntity.getEmail()).country(userEntity.getCountry())
                .aboutMe(userEntity.getAboutme()).dob(userEntity.getDob()).contactNumber(userEntity.getContactnumber());
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
