package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {
    @Autowired
    private QuestionBusinessService questionBusinessService;

    /*
     * This endpoint is used to help user create a question and post.
     * The created question has a unique ID generated for it.
     * The newly created question will have a timestamp and mapped to the user who posted it.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") final String authorizationToken, final QuestionRequest questionRequest) throws UserNotFoundException, AuthorizationFailedException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if (bearerToken.length == 1) {
            userAuthTokenEntity = questionBusinessService.getUserAuthToken(bearerToken[0]);
        } else {
            userAuthTokenEntity = questionBusinessService.getUserAuthToken(bearerToken[1]);
        }

        UserEntity userEntity = questionBusinessService.getUser(userAuthTokenEntity.getUuid());
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        final ZonedDateTime now = ZonedDateTime.now();
        questionEntity.setDate(now);
        questionEntity.setUser(userEntity);

        QuestionEntity question = questionBusinessService.createQuestion(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(question.getUuid()).status("QUESTION CREATED");

        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);

    }

    // This endpoint is called to fetch all the questions posted in the database.
    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorizationToken) throws AuthorizationFailedException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if (bearerToken.length == 1) {
            userAuthTokenEntity = questionBusinessService.getUserAuthTokenForGetQuestions(bearerToken[0]);
        } else {
            userAuthTokenEntity = questionBusinessService.getUserAuthTokenForGetQuestions(bearerToken[1]);
        }

        List<QuestionEntity> questions = questionBusinessService.getAllQuestions();
        List<QuestionDetailsResponse> questionResponseList = new ArrayList<QuestionDetailsResponse>();
        for (ListIterator<QuestionEntity> iter = questions.listIterator(); iter.hasNext(); ) {
            QuestionEntity question = iter.next();
            QuestionDetailsResponse questionResponse = new QuestionDetailsResponse();
            questionResponse.setId(question.getUuid());
            questionResponse.setContent(question.getContent());
            questionResponseList.add(questionResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(questionResponseList, HttpStatus.OK);
    }

    // This endpoint is called to edit an already existing question in the database.
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(@RequestHeader("authorization") final String authorizationToken, @PathVariable("questionId") String questionId, final QuestionRequest questionRequest) throws AuthorizationFailedException, InvalidQuestionException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserEntity loggedInUserEntity = new UserEntity();

        if (bearerToken.length == 1) {
            loggedInUserEntity = questionBusinessService.getLoggedInUserDetailsGetEditQuestion(bearerToken[0]);
        } else {
            loggedInUserEntity = questionBusinessService.getLoggedInUserDetailsGetEditQuestion(bearerToken[1]);
        }
        QuestionEntity questionEntity = questionBusinessService.editQuestion(questionId, loggedInUserEntity, questionRequest.getContent());
        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    // This endpoint is called to delete the question in concern.
    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@RequestHeader("authorization") final String authorizationToken, @PathVariable("questionId") String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        QuestionEntity questionEntity = new QuestionEntity();

        if (bearerToken.length == 1) {
            questionEntity = questionBusinessService.getUserAuthTokenForDeleteQuestion(bearerToken[0], questionId);
        } else {
            questionEntity = questionBusinessService.getUserAuthTokenForDeleteQuestion(bearerToken[1], questionId);
        }
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(questionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }

    // This endpoint is called to fetch all the questions posted by the currently logged in user.
    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsOfUser(@RequestHeader("authorization") final String authorizationToken, @PathVariable("userId") String userId) throws AuthorizationFailedException, UserNotFoundException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if (bearerToken.length == 1) {
            userAuthTokenEntity = questionBusinessService.getUserAuthTokenForGettingAllQuestionsOfUser(bearerToken[0]);
        } else {
            userAuthTokenEntity = questionBusinessService.getUserAuthTokenForGettingAllQuestionsOfUser(bearerToken[1]);
        }
        List<QuestionEntity> questions = questionBusinessService.getAllQuestionsOfUser(userId);
        List<QuestionDetailsResponse> allQuestionOfUserResponseList = new ArrayList<QuestionDetailsResponse>();
        for (ListIterator<QuestionEntity> iter = questions.listIterator(); iter.hasNext(); ) {
            QuestionEntity question = iter.next();
            QuestionDetailsResponse questionResponse = new QuestionDetailsResponse();
            questionResponse.setId(question.getUuid());
            questionResponse.setContent(question.getContent());
            allQuestionOfUserResponseList.add(questionResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(allQuestionOfUserResponseList, HttpStatus.OK);
    }

}