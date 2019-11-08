package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /*
     * The functionality of Signup where the Salt and Password entered by the user
     * are stored in encrypted form. These two fields are stored in the database (in
     * encrypted form) after this function is called.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);
    }

    // Method to fetch user's details by searching by the username as this is a unique attribute.
    public UserEntity getUserByUsername(String userName) {
        return userDao.getUserByUsername(userName);
    }

    // Method to fetch user's details by searching by the email as this is a unique attribute as well.
    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    /*
     * This method call is made on the signin checkpoint.
     * The concerned user is provided with an authorization token if the signin is successful.
     * The user is signed in only if the password entered matches the password from the database after encryption.
     * The session timeout for the generated token also starts as soon as the login is successful.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity signIn(final String username, final String password) throws AuthenticationFailedException {

        UserEntity userEntity = userDao.getUserByUsername(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }
        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthTokenEntity userAuthToken = new UserAuthTokenEntity();
            userAuthToken.setUser(userEntity);
            userAuthToken.setUuid(userEntity.getUuid());
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);

            userDao.createAuthToken(userAuthToken);
            return userAuthToken;

        } else {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }

    /*
     * Method to fetch the Authorization Token of the user that is logged in at that
     * current time. The session timeout of 8 hours starts as soon as a token is
     * given to the user. This also checks if there is no token assigned then the
     * user must not be signed in.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws SignOutRestrictedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        final ZonedDateTime now = ZonedDateTime.now();
        userAuthTokenEntity.setLogoutAt(now);
        userDao.updateUserAuthToken(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

}
