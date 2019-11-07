package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionBusinessService {
    @Autowired
    private UserDao userDao;

    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
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

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        return userDao.createQuestion(questionEntity);
    }
    public UserAuthTokenEntity getUserAuthTokenForGetQuestions(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }

        return userAuthTokenEntity;
    }
    public List<QuestionEntity> getAllQuestions(){
        return userDao.getAllQuestions();
    }
    public UserEntity getLoggedInUserDetailsGetEditQuestion(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit the question");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());


        return userEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final String questionId,final UserEntity loggedInUser ,final String questionEditContent) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if(questionEntity == null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        if(!questionEntity.getUser().getId().equals(loggedInUser.getId())){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }
        return userDao.editQuestion(questionEntity,questionEditContent);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity getUserAuthTokenForDeleteQuestion(final String authorizationToken,final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete a question");
        }
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if(questionEntity == null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());
        if(!questionEntity.getUser().getId().equals(userEntity.getId()) && userEntity.getRole().equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }
        userDao.deleteQuestion(questionEntity);
        return questionEntity;
    }

    public UserAuthTokenEntity getUserAuthTokenForGettingAllQuestionsOfUser(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific user");
        }
        return userAuthTokenEntity;
    }

    public List<QuestionEntity> getAllQuestionsOfUser(final String userId) throws UserNotFoundException {
        UserEntity userEntity = userDao.getUser(userId);
        List<QuestionEntity> allQuestionsOfUser = userDao.getAllQuestionsOfUser(userEntity);
        if(allQuestionsOfUser.isEmpty()){
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }
        return allQuestionsOfUser;
    }
}
