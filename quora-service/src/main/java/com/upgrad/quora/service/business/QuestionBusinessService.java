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

    // This method fetches the authorization token of the logged in user.
    // We need to make sure that the user is logged in and the session hasn't timed out.
    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }

        return userAuthTokenEntity;
    }

    // This method fetches the user ID and throws an exception if no user is found logged in.
    public UserEntity getUser(final String userId) throws UserNotFoundException {
        UserEntity userEntity = userDao.getUser(userId);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User does not exist");
        }
        return userEntity;
    }

    // This method creates a question in the database under the name of the logged in user.
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        return userDao.createQuestion(questionEntity);
    }

    // This method fetches the authorization token for the questions.
    // We need to make sure that the user is logged in and the session hasn't timed out.
    public UserAuthTokenEntity getUserAuthTokenForGetQuestions(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get all questions");
        }

        return userAuthTokenEntity;
    }

    // This method fetches all the questions in the database.
    public List<QuestionEntity> getAllQuestions() {
        return userDao.getAllQuestions();
    }

    // This method fetches the details of the user who is logged in and wants to edit a question.
    // We need to make sure that the user is logged in and the session hasn't timed out.
    public UserEntity getLoggedInUserDetailsGetEditQuestion(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());


        return userEntity;
    }

    // This method call is made when the user wants to edit an existing question.
    // We need to make sure that the user is logged in and the session hasn't timed out.
    //Also that the logged in user is either the admin or the one who posted the question.
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final String questionId, final UserEntity loggedInUser, final String questionEditContent) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if (!questionEntity.getUser().getId().equals(loggedInUser.getId())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
        }
        return userDao.editQuestion(questionEntity, questionEditContent);
    }

    /*
     * This method fetches the authorization token of the user who wishes to delete a question.
     * We need to make sure the following checks :
     * 1) If the user logged in is the one who posted the question in the first place.
     * 2) If the user logged in is the admin, then he/she can also delete the question.
     * 3) We need to make sure that the user is logged in and the session hasn't timed out.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity getUserAuthTokenForDeleteQuestion(final String authorizationToken, final String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete a question");
        }
        QuestionEntity questionEntity = userDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        UserEntity userEntity = userDao.getUser(userAuthTokenEntity.getUuid());
        if (!questionEntity.getUser().getId().equals(userEntity.getId()) && userEntity.getRole().equals("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
        }
        userDao.deleteQuestion(questionEntity);
        return questionEntity;
    }

    // This question fetches the authorization token for getting all questions posted by the current user.
    public UserAuthTokenEntity getUserAuthTokenForGettingAllQuestionsOfUser(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get all questions posted by a specific user");
        }
        return userAuthTokenEntity;
    }

    // This question fetches all the questions posted by the current user.
    public List<QuestionEntity> getAllQuestionsOfUser(final String userId) throws UserNotFoundException {
        UserEntity userEntity = userDao.getUser(userId);
        List<QuestionEntity> allQuestionsOfUser = userDao.getAllQuestionsOfUser(userEntity);
        if (allQuestionsOfUser.isEmpty()) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return allQuestionsOfUser;
    }
}
