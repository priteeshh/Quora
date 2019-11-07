package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerBusinessService {
    @Autowired
    private UserDao userDao;
    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }

        return userAuthTokenEntity;
    }

    public UserEntity getUser(final String userId) throws UserNotFoundException {
        UserEntity userEntity = userDao.getUser(userId);
        if(userEntity == null){
            throw new UserNotFoundException("USR-001","User does not exist");
        }
        return userEntity;
    }

    public QuestionEntity getQuestion(final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if(questionEntity == null){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        return questionEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity){
        userDao.createAnswer(answerEntity);
        return answerEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final String answerId,final UserEntity loggedInUser ,final String answerEditContent) throws AuthorizationFailedException, InvalidQuestionException {
        AnswerEntity answerEntity = userDao.getAnswerById(answerId);
        if(answerEntity == null){
            throw new InvalidQuestionException("ANS-001","Entered answer uuid does not exist");
        }
        if(!answerEntity.getUser().getId().equals(loggedInUser.getId())){
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit the answer");
        }
        return userDao.editAnswer(answerEntity,answerEditContent);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String authorizationToken,final String answerId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete an answer");
        }
        AnswerEntity answerEntity = userDao.getAnswerById(answerId);
        if(answerEntity == null){
            throw new InvalidQuestionException("ANS-001","Entered answer uuid does not exist");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());
        if(!answerEntity.getUser().getId().equals(userEntity.getId()) && userEntity.getRole().equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
        }
        userDao.deleteAnswer(answerEntity);
        return answerEntity;
    }

}
