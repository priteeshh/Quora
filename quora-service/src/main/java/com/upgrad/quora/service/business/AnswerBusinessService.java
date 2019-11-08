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

import java.util.List;

@Service
public class AnswerBusinessService {
    @Autowired
    private UserDao userDao;

    //This method fetches the authorization token of the logged in user.
    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }

        return userAuthTokenEntity;
    }

    // This method fetches the user by its ID.
    // If not found, an exception is thrown.
    public UserEntity getUser(final String userId) throws UserNotFoundException {
        UserEntity userEntity = userDao.getUser(userId);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User does not exist");
        }
        return userEntity;
    }

    // This method fetches the question by its ID.
    // If not found, an exception is thrown.
    public QuestionEntity getQuestion(final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }

    // This method call will create an answer by the user that is currently logged in.
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        userDao.createAnswer(answerEntity);
        return answerEntity;
    }

    /*
     * This method is called when a user needs to edit an answer.
     * The following checks are made before the editing takes place :
     * 1) The answer which needs to be edited exists in the database or not.
     * 2) The user who is logged in is the one who posted the answer in the first place.
     * Only upon these checks getting successful, can the answer be edited.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final String answerId, final UserEntity loggedInUser, final String answerEditContent) throws AuthorizationFailedException, InvalidQuestionException {
        AnswerEntity answerEntity = userDao.getAnswerById(answerId);
        if (answerEntity == null) {
            throw new InvalidQuestionException("ANS-001", "Entered answer uuid does not exist");
        }
        if (!answerEntity.getUser().getId().equals(loggedInUser.getId())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        return userDao.editAnswer(answerEntity, answerEditContent);
    }

    /*
     * This method call will delete an answer by the user that is currently logged in.
     * The following checks will be made before deleting the answer :
     * 1) If the user has an authorization token. Or in other words, if the user is logged in or not.
     * 2) If the current session is active or has timed out. The user will have to login again in both cases.
     * 3) If the answer is posted by the user who is currently logged in or is an admin. No other user can delete an answer.
     * If all these conditions are fulfilled, an answer can be deleted by the logged in user.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String authorizationToken, final String answerId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
        }
        AnswerEntity answerEntity = userDao.getAnswerById(answerId);
        if (answerEntity == null) {
            throw new InvalidQuestionException("ANS-001", "Entered answer uuid does not exist");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());
        if (!answerEntity.getUser().getId().equals(userEntity.getId()) && userEntity.getRole().equals("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        }
        userDao.deleteAnswer(answerEntity);
        return answerEntity;
    }

    // This method fetches the question details for a particular question id.
    public QuestionEntity getQuestionToGetAllAnswers(final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return questionEntity;
    }

    // This method fetches all the answers posted for a particular question.
    public List<AnswerEntity> getAllAnswersOfQuestion(final String authorizationToken, final QuestionEntity question) throws UserNotFoundException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
        }
        List<AnswerEntity> allAnswers = userDao.getAllAnswersOfQuestion(question);
        if (allAnswers.isEmpty()) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return allAnswers;
    }
}
