package com.upgrad.quora.service.dao;


import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {
    @PersistenceContext
    private EntityManager entityManager;
    public UserEntity createUser(UserEntity userEntity) throws SignUpRestrictedException {

//        UserEntity userByUsername = getUserByUsername(userEntity.getUsername());
//        if(userByUsername == null){
//            UserEntity userByEmail = getUserByEmail(userEntity.getEmail());
//            if(userByEmail == null){
//                entityManager.persist(userEntity);
//                return userEntity;
//            }
//            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
//        }
//        throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");

        entityManager.persist(userEntity);
        return userEntity;
    }
    public UserEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", UserEntity.class).setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    public UserEntity getUserByUsername(final String username) {
        try {
            return entityManager.createNamedQuery("userByUsername", UserEntity.class).setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    public UserEntity getUser(final String userId){
        try {
            return entityManager.createNamedQuery("userById", UserEntity.class).setParameter("uuid", userId)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public UserAuthTokenEntity createAuthToken(final UserAuthTokenEntity userAuthTokenEntity){
        entityManager.persist(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

    public UserAuthTokenEntity getUserAuthToken(final String accessToken){
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken",
                    UserAuthTokenEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre){
            return null;
        }
    }

    public void updateUserAuthToken(final UserAuthTokenEntity userAuthTokenEntity){
        entityManager.merge(userAuthTokenEntity);
    }
    public void deleteUser(final UserEntity userEntity){
        entityManager.remove(userEntity);
    }

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestions(){
        List<QuestionEntity> questiontList = entityManager.createNamedQuery("getAllQuestions", QuestionEntity.class).getResultList();
        return questiontList;
    }

}
