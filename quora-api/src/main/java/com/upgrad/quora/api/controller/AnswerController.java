package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
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
public class AnswerController {
    @Autowired
    private AnswerBusinessService answerBusinessService;

    // This endpoint creates an answer for the concerned question by the user currently logged in.
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorizationToken, @PathVariable("questionId") final String questionId, final AnswerRequest answerRequest) throws AuthorizationFailedException, InvalidQuestionException, UserNotFoundException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
        if(bearerToken.length == 1){
            userAuthTokenEntity = answerBusinessService.getUserAuthToken(bearerToken[0]);
        }else{
            userAuthTokenEntity = answerBusinessService.getUserAuthToken(bearerToken[1]);
        }
        UserEntity userEntity = answerBusinessService.getUser(userAuthTokenEntity.getUuid());
        QuestionEntity questionEntity = answerBusinessService.getQuestion(questionId);
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());
        final ZonedDateTime now = ZonedDateTime.now();
        questionEntity.setDate(now);
        answerEntity.setDate(now);
        answerEntity.setUser(userEntity);
        answerEntity.setQuestion(questionEntity);

        AnswerEntity createdAnswer = answerBusinessService.createAnswer(answerEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");

        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    // This endpoint enables the user to edit an answer that he/she has already posted.
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswer(@RequestHeader("authorization") final String authorizationToken, @PathVariable("answerId") String answerId, final AnswerEditRequest answerRequest) throws AuthorizationFailedException, InvalidQuestionException, UserNotFoundException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();

        if(bearerToken.length == 1){
            userAuthTokenEntity = answerBusinessService.getUserAuthToken(bearerToken[0]);
        }else{
            userAuthTokenEntity = answerBusinessService.getUserAuthToken(bearerToken[1]);
        }
        UserEntity userEntity = answerBusinessService.getUser(userAuthTokenEntity.getUuid());
        AnswerEntity answerEntity = answerBusinessService.editAnswer(answerId,userEntity,answerRequest.getContent());
        AnswerEditResponse answerResponse = new AnswerEditResponse().id(answerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerResponse, HttpStatus.OK);
    }

    // This endpoint helps the user to delete an answer that was posted.
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@RequestHeader("authorization") final String authorizationToken, @PathVariable("answerId") String answerId) throws AuthorizationFailedException, InvalidQuestionException {
        String[] bearerToken = authorizationToken.split("Bearer ");

        AnswerEntity answerEntity = new AnswerEntity();

        if(bearerToken.length == 1){
            answerEntity = answerBusinessService.deleteAnswer(bearerToken[0],answerId);
        }else{
            answerEntity = answerBusinessService.deleteAnswer(bearerToken[1],answerId);
        }

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse,HttpStatus.OK);
    }

    // This endpoint fetches all the answers posted by every user for a given question.
    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersForQuestion(@RequestHeader("authorization") final String authorizationToken, @PathVariable("questionId") String questionId) throws AuthorizationFailedException, InvalidQuestionException, UserNotFoundException {
        String[] bearerToken = authorizationToken.split("Bearer ");
        QuestionEntity questionEntity = answerBusinessService.getQuestionToGetAllAnswers(questionId);
        List<AnswerEntity> answerEntity = new ArrayList<AnswerEntity>();

        if(bearerToken.length == 1){
            answerEntity = answerBusinessService.getAllAnswersOfQuestion(bearerToken[0],questionEntity);
        }else{
            answerEntity = answerBusinessService.getAllAnswersOfQuestion(bearerToken[1],questionEntity);
        }
        List<AnswerDetailsResponse> allAnswers = new ArrayList<AnswerDetailsResponse>();

        for (ListIterator<AnswerEntity> iter = answerEntity.listIterator(); iter.hasNext(); ) {
            AnswerEntity answer = iter.next();
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
            answerDetailsResponse.setId(answer.getUuid());
            answerDetailsResponse.setQuestionContent(questionEntity.getContent());
            answerDetailsResponse.setAnswerContent(answer.getAns());
            allAnswers.add(answerDetailsResponse);
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(allAnswers,HttpStatus.OK);
    }
}
